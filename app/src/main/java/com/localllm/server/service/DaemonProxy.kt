package com.localllm.server.service

import android.content.Context
import android.content.Intent
import android.os.Build
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

object DaemonProxy {
    private var serverSocket: ServerSocket? = null
    var isEnabled = false
    var lastActiveTime = AtomicLong(System.currentTimeMillis())

    fun start(context: Context, modelPath: String) {
        if (isEnabled) return
        isEnabled = true
        
        thread {
            try {
                serverSocket = ServerSocket(8080)
                while (isEnabled) {
                    val client = serverSocket!!.accept()
                    lastActiveTime.set(System.currentTimeMillis())

                    if (!ServerProcessManager.isRunning) {
                        val intent = Intent(context, LlamaServerService::class.java).apply {
                            putExtra("MODEL_PATH", modelPath)
                            putExtra("PORT", 8081)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }

                        // Wait for engine to bind 8081 (timeout 15s)
                        var backendReady = false
                        for (i in 0..75) {
                            try {
                                Socket("127.0.0.1", 8081).close()
                                backendReady = true
                                break
                            } catch (e: Exception) {
                                Thread.sleep(200)
                            }
                        }
                        if (!backendReady) {
                            client.close()
                            continue
                        }
                    }

                    // Forward TCP stream
                    thread {
                        try {
                            val backend = Socket("127.0.0.1", 8081)
                            thread {
                                try {
                                    client.inputStream.copyTo(backend.outputStream)
                                    backend.outputStream.flush()
                                } catch (e: Exception) {}
                                try { client.close() } catch (e: Exception) {}
                            }
                            thread {
                                try {
                                    backend.inputStream.copyTo(client.outputStream)
                                    client.outputStream.flush()
                                } catch (e: Exception) {}
                                try { backend.close() } catch (e: Exception) {}
                            }
                        } catch (e: Exception) {
                            client.close()
                        }
                    }
                }
            } catch (e: Exception) {}
        }

        // Auto-shutdown watchdog (5 minutes idle)
        thread {
            while (isEnabled) {
                Thread.sleep(10000)
                if (System.currentTimeMillis() - lastActiveTime.get() > 5 * 60 * 1000) {
                    if (ServerProcessManager.isRunning) {
                        val intent = Intent(context, LlamaServerService::class.java).apply { action = "STOP" }
                        context.startService(intent)
                    }
                }
            }
        }
    }

    fun stop() {
        isEnabled = false
        try { serverSocket?.close() } catch (e: Exception) {}
        serverSocket = null
    }
}
