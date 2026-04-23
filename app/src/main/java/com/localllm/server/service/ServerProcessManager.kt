package com.localllm.server.service

import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import com.localllm.server.utils.ServerConfig

object ServerProcessManager {
    private var process: Process? = null
    var currentPfd: android.os.ParcelFileDescriptor? = null
    var currentPort: Int = ServerConfig.PORT

    var isRunning = false
        private set

    /**
     * 日志回调 —— 只在 MainActivity 里设置一次，Service 不应覆盖它。
     * 在子线程中触发，调用方需自行 runOnUiThread。
     */
    var logCallback: ((String) -> Unit)? = null

    /**
     * 进程退出回调（isRunning 已变为 false 后触发），用于通知 UI 刷新状态。
     */
    var onExitCallback: (() -> Unit)? = null

    private fun log(msg: String) {
        logCallback?.invoke(msg)
    }

    // ── 启动 ──────────────────────────────────────────────────

    /** 启动 llama-server 子进程。若已运行则忽略。 */
    fun startServer(context: Context, modelPath: String, port: Int = ServerConfig.PORT) {
        if (isRunning) {
            log("服务已在运行中，忽略重复启动。")
            return
        }

        val nativeLibDir = context.applicationInfo.nativeLibraryDir
        val llamaServerPath = "$nativeLibDir/libllama_server.so"

        if (!File(llamaServerPath).exists()) {
            log("❌ 错误：找不到引擎二进制文件\n路径: $llamaServerPath")
            return
        }
        // FD Handling for SAF URIs
        var finalModelPath = modelPath
        if (modelPath.startsWith("content://")) {
            try {
                currentPfd?.close()
                val uri = android.net.Uri.parse(modelPath)
                currentPfd = context.contentResolver.openFileDescriptor(uri, "r")
                val fd = currentPfd?.fd
                if (fd != null) {
                    finalModelPath = "/proc/${android.os.Process.myPid()}/fd/$fd"
                    log("ℹ️ 使用文件描述符读取模型 (FD: $fd)")
                } else {
                    log("❌ 错误：无法获取模型的读权限")
                    return
                }
            } catch (e: Exception) {
                log("❌ 错误：无法打开模型 URI: ${e.message}")
                return
            }
        } else {
            if (!File(modelPath).exists()) {
                log("❌ 错误：找不到模型文件\n路径: $modelPath")
                return
            }
        }

        // 检查二进制是否可执行
        val binFile = File(llamaServerPath)
        if (!binFile.canExecute()) {
            log("⚠️ 引擎文件不可执行，尝试 chmod 755...")
            runCatching { 
                Runtime.getRuntime().exec(arrayOf("chmod", "755", llamaServerPath)).waitFor() 
            }.onFailure { log("chmod 失败: ${it.message}") }
        }

        val command = mutableListOf<String>()
        if (File("/system/bin/nice").exists()) {
            command.add("/system/bin/nice")
            command.add("-n")
            command.add("10") // 降低子进程调度优先级，防止启动瞬间霸占全部 CPU 导致 Android 卡死
        }

        command.addAll(listOf(
            llamaServerPath,
            "-m", finalModelPath,
            "--host", ServerConfig.HOST,
            "--port", port.toString(),
            "-t", ServerConfig.THREADS.toString(),
            "-c", ServerConfig.CONTEXT_SIZE.toString()
        ))

        // 如果是外部 SAF 虚拟路径，必须禁用 mmap，否则会在 FUSE 边界产生数以万计的缺页异常(Page Fault)，瞬间卡死 MediaProvider
        if (modelPath.startsWith("content://")) {
            command.add("--no-mmap")
            log("⚠️ 检测到外部 SAF 存储器，已自动禁用 mmap 以防止系统级卡顿。")
        }

        log("▶ 启动命令:\n${command.joinToString(" ")}\n")

        try {
            currentPort = port
            val pb = ProcessBuilder(command)
            pb.directory(context.filesDir)
            pb.redirectErrorStream(true) // 合并 stderr，单线程读取即可
            process = pb.start()
            isRunning = true
            log("✅ 子进程已启动，等待引擎初始化...\n")

            // ── 线程 1：持续消费 stdout/stderr，防止管道缓冲区撑满导致卡死 ──
            thread(isDaemon = true, name = "llama-log-reader") {
                runCatching {
                    process!!.inputStream.bufferedReader().useLines { lines ->
                        lines.filter { it.isNotBlank() }.forEach { log(it) }
                    }
                }.onFailure { log("日志读取线程异常: ${it.message}") }
                
                isRunning = false
                log("\n⏹ 服务进程已退出。")
                onExitCallback?.invoke()
            }

            // ── 线程 2：轮询 /health，就绪后自动调用模型自我介绍 ──
            thread(isDaemon = true, name = "llama-health-watcher") {
                if (waitForReady()) {
                    autoIntroduce()
                }
            }

        } catch (e: Exception) {
            log("❌ 启动失败: ${e.message}")
            isRunning = false
            onExitCallback?.invoke()
        }
    }

    // ── 停止 ──────────────────────────────────────────────────

    /** 停止 llama-server 子进程。 */
    fun stopServer() {
        if (process == null) {
            isRunning = false
            return
        }
        log("⏹ 正在停止服务...")
        process?.destroy()
        runCatching {
            Thread.sleep(1500)
            if (process?.isAlive == true) {
                process?.destroyForcibly()
                log("强制终止进程。")
            }
        }.onFailure { process?.destroyForcibly() }
        
        process = null
        isRunning = false
        
        try {
            currentPfd?.close()
        } catch (e: Exception) {}
        currentPfd = null
    }

    // ── 健康检查 ───────────────────────────────────────────────

    /**
     * 轮询 GET /health，最多等待 90 秒（大模型首次加载可能很慢）。
     * @return true = 服务就绪；false = 超时或服务已停止
     */
    private fun waitForReady(): Boolean {
        log("⏳ 等待引擎就绪（每 3 秒检查一次，最多等 90 秒）...")
        val maxAttempts = 30
        repeat(maxAttempts) { attempt ->
            if (!isRunning) {
                log("⚠️ 健康检查终止：服务已停止。")
                return false
            }
            runCatching {
                val conn = URL("http://${ServerConfig.HOST}:$currentPort/health")
                    .openConnection() as HttpURLConnection
                conn.apply {
                    connectTimeout = 2000
                    readTimeout = 2000
                    requestMethod = "GET"
                }
                val code = conn.responseCode
                conn.disconnect()
                
                if (code == 200) {
                    log("✅ 引擎已就绪！（第 ${attempt + 1} 次检查）")
                    return true
                }
            }
            Thread.sleep(3000)
        }
        log("⚠️ 健康检查超时（等待了 ${maxAttempts * 3} 秒），服务可能未正常启动。")
        return false
    }

    // ── 模型自我介绍 ───────────────────────────────────────────

    /**
     * 调用 /v1/chat/completions，让模型用一句话介绍自己，
     * 输出到日志区作为"服务验活"的直观确认。
     */
    private fun autoIntroduce() {
        log("\n🤖 正在向模型发送自我介绍请求...")
        runCatching {
            val conn = URL("http://${ServerConfig.HOST}:$currentPort/v1/chat/completions")
                .openConnection() as HttpURLConnection
            conn.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 60_000 // 首次推理可能稍慢，给 60 秒
            }

            val body = """
                {
                  "messages": [{"role": "user", "content": "请用一两句话介绍你自己：你是什么AI模型？"}],
                  "stream": false, "max_tokens": 150, "temperature": 0.7
                }
            """.trimIndent()

            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
            conn.disconnect()

            val contentRegex = Regex(""""content"\s*:\s*"((?:[^"\\]|\\.)*)"""")
            val content = contentRegex.find(responseText)?.groupValues?.get(1)
                ?.replace("\\n", "\n")?.replace("\\\"", "\"")?.replace("\\\\", "\\")?.trim()
                ?: "（无法解析模型回复，原始响应已记录在上方日志）"

            log("\n┌─────────────────────────────────────────")
            log("│  🤖 模型自我介绍:\n│")
            content.lines().forEach { log("│  $it") }
            log("│\n└─────────────────────────────────────────")
            log("\n✅ 服务验证通过！API 可正常调用。\n   地址: http://${ServerConfig.HOST}:$currentPort\n   POST /v1/chat/completions\n   GET  /v1/models\n   GET  /health\n")

        }.onFailure {
            log("\n⚠️ 模型自我介绍请求失败: ${it.message}\n   服务可能仍在加载中，请稍后手动用 curl 验证。")
        }
    }
}
