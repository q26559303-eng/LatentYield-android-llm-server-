package com.localllm.server.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.localllm.server.R
import com.localllm.server.data.ModelInfo
import com.localllm.server.data.ModelManager
import com.localllm.server.service.LlamaServerService
import com.localllm.server.service.ServerProcessManager

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerModels: Spinner
    private lateinit var rowModelSelect: View
    private lateinit var tvSelectedModel: TextView
    private lateinit var btnImportModel: TextView
    private lateinit var btnRefreshModels: TextView
    private lateinit var btnStart: TextView
    private lateinit var btnStop: TextView
    private lateinit var btnClearLog: TextView
    private lateinit var btnCopyLog: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvLog: TextView
    private lateinit var btnToggleTheme: TextView
    private lateinit var btnToggleLang: TextView
    private lateinit var tvCpu: TextView
    private lateinit var tvRam: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var switchAutoWake: androidx.appcompat.widget.SwitchCompat
    private lateinit var btnOpenChat: View
    private lateinit var chatOverlay: View
    private lateinit var btnChatClose: View
    private lateinit var etChatMessage: android.widget.EditText
    private lateinit var btnChatSendFull: View
    private lateinit var tvChatLog: TextView // For simple chat history inside overlay

    private var models: List<ModelInfo> = emptyList()

    private val statusPoller = Handler(Looper.getMainLooper())
    private val pollRunnable = object : Runnable {
        override fun run() {
            updateUIStatus()
            updateHardwareStats()
            statusPoller.postDelayed(this, 2000)
        }
    }

    private val pickModelLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val fileName = getFileNameFromUri(uri, false)
            if (fileName == null || !fileName.lowercase().endsWith(".gguf")) {
                Toast.makeText(this, "Error: Must be a .gguf file", Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                ModelManager.addUriModel(this, uri)
                refreshModels()
                Toast.makeText(this, "Model Imported Successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to import model: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        handleIntent(intent)

        spinnerModels  = findViewById(R.id.spinnerModels)
        rowModelSelect = findViewById(R.id.rowModelSelect)
        tvSelectedModel = findViewById(R.id.tvSelectedModel)
        btnImportModel = findViewById(R.id.btnImportModel)
        btnRefreshModels = findViewById(R.id.btnRefreshModels)
        btnStart       = findViewById(R.id.btnStart)
        btnStop        = findViewById(R.id.btnStop)
        btnClearLog    = findViewById(R.id.btnClearLog)
        btnCopyLog     = findViewById(R.id.btnCopyLog)
        tvStatus       = findViewById(R.id.tvStatus)
        tvLog          = findViewById(R.id.tvLog)
        btnToggleTheme = findViewById(R.id.btnToggleTheme)
        btnToggleLang  = findViewById(R.id.btnToggleLang)
        tvCpu          = findViewById(R.id.tvCpu)
        tvRam          = findViewById(R.id.tvRam)
        tvSpeed        = findViewById(R.id.tvSpeed)
        switchAutoWake = findViewById(R.id.switchAutoWake)
        btnOpenChat    = findViewById(R.id.btnOpenChat)
        chatOverlay    = findViewById(R.id.chatOverlayContainer)
        btnChatClose   = findViewById(R.id.btnChatClose)
        etChatMessage  = findViewById(R.id.etChatMessage)
        btnChatSendFull = findViewById(R.id.btnChatSendFull)
        
        tvChatLog      = findViewById(R.id.tvChatLog)

        btnOpenChat.setOnClickListener {
            chatOverlay.visibility = android.view.View.VISIBLE
            // Use an animation for premium feel
            chatOverlay.alpha = 0f
            chatOverlay.animate().alpha(1f).setDuration(300).start()
        }

        btnChatClose.setOnClickListener {
            // Hide keyboard if visible
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(etChatMessage.windowToken, 0)
            
            chatOverlay.animate().alpha(0f).setDuration(200).withEndAction {
                chatOverlay.visibility = android.view.View.GONE
            }.start()
        }

        btnChatSendFull.setOnClickListener {
            val prompt = etChatMessage.text.toString().trim()
            if (prompt.isNotEmpty()) {
                appendChatMessage("User", prompt)
                sendChatMessage(prompt)
                etChatMessage.setText("")
                
                // Keep focus but maybe hide keyboard if user wants, 
                // but usually for chat we keep it open. 
                // Let's at least ensure it doesn't get in the way.
            }
        }

        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        if (isNightMode) {
            btnToggleTheme.text = getString(R.string.theme_to_light)
        } else {
            btnToggleTheme.text = getString(R.string.theme_to_dark)
        }

        btnToggleTheme.setOnClickListener {
            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        btnToggleLang.setOnClickListener {
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val newLang = if (currentLocales.toLanguageTags().contains("zh")) "en" else "zh"
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLang))
        }

        rowModelSelect.setOnClickListener {
            spinnerModels.performClick()
        }

        spinnerModels.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (models.isNotEmpty()) {
                    tvSelectedModel.text = models[position].name
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        btnImportModel.setOnClickListener {
            // Must use */* because Android file managers don't assign octet-stream to .gguf reliably.
            pickModelLauncher.launch(arrayOf("*/*"))
        }

        btnRefreshModels.setOnClickListener {
            refreshModels()
            Toast.makeText(this, getString(R.string.refresh), Toast.LENGTH_SHORT).show()
        }

        btnStart.setOnClickListener {
            if (models.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_download_first), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val pos = spinnerModels.selectedItemPosition
            if (pos in models.indices) {
                val isAutoWake = switchAutoWake.isChecked
                startLlamaService(models[pos].path, if (isAutoWake) 8081 else 8080)
            }
        }

        btnStop.setOnClickListener {
            stopLlamaService()
        }

        btnCopyLog.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("LocalLLM Log", tvLog.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
        }

        btnClearLog.setOnClickListener {
            tvLog.text = getString(R.string.log_cleared)
        }

        ServerProcessManager.logCallback = { line ->
            runOnUiThread {
                appendLog(line)
                updateUIStatus()
                if (line.contains("tokens per second")) {
                    val match = "([0-9.]+)\\s*tokens per second".toRegex().find(line)
                    if (match != null) {
                        tvSpeed.text = "${match.groupValues[1]} T/s"
                    }
                }
            }
        }

        ServerProcessManager.onExitCallback = {
            runOnUiThread { updateUIStatus() }
        }

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        switchAutoWake.isChecked = prefs.getBoolean("auto_wake", false)

        switchAutoWake.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_wake", isChecked).apply()
            val pos = spinnerModels.selectedItemPosition
            val modelPath = if (pos in models.indices) models[pos].path else null

            if (isChecked && modelPath != null) {
                val intent = Intent(this, com.localllm.server.service.DaemonProxyService::class.java).apply {
                    putExtra("MODEL_PATH", modelPath)
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            } else {
                val intent = Intent(this, com.localllm.server.service.DaemonProxyService::class.java).apply { 
                    action = "STOP" 
                }
                startService(intent)
            }
        }

        refreshModels()
        updateUIStatus()
    }

    override fun onResume() {
        super.onResume()
        statusPoller.post(pollRunnable)
    }

    override fun onPause() {
        super.onPause()
        statusPoller.removeCallbacks(pollRunnable)
    }

    private fun refreshModels() {
        models = ModelManager.listModels(this)
        val names = if (models.isEmpty()) {
            listOf(getString(R.string.no_engine_found))
        } else {
            models.map { "${it.name} (${it.sizeMb} MB)" }
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModels.adapter = adapter

        if (models.isEmpty()) {
            tvSelectedModel.text = getString(R.string.no_engine_found)
            appendLog(getString(R.string.no_engine_found))
            switchAutoWake.isEnabled = false
        } else {
            tvSelectedModel.text = models[spinnerModels.selectedItemPosition].name
            appendLog("Detected ${models.size} models")
            switchAutoWake.isEnabled = true
        }
    }

    private fun updateUIStatus() {
        if (ServerProcessManager.isRunning) {
            tvStatus.text = getString(R.string.status_online)
            tvStatus.setTextColor(android.graphics.Color.parseColor("#32D74B"))
            btnStart.isEnabled = false
            btnStop.isEnabled  = true
            btnStart.alpha = 0.5f
            btnStop.alpha = 1.0f
        } else {
            tvStatus.text = getString(R.string.status_idle)
            tvStatus.setTextColor(android.graphics.Color.parseColor("#8E8E93"))
            btnStart.isEnabled = true
            btnStop.isEnabled  = false
            btnStart.alpha = 1.0f
            btnStop.alpha = 0.5f
        }
    }

    private var statsThread: Thread? = null
    private fun updateHardwareStats() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val mi = android.app.ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val usedRam = (mi.totalMem - mi.availMem) / (1024 * 1024)
        tvRam.text = "$usedRam MB"

        if (ServerProcessManager.isRunning) {
            if (statsThread?.isAlive == true) return
            statsThread = Thread {
                try {
                    val p = Runtime.getRuntime().exec("top -n 1")
                    val reader = java.io.BufferedReader(java.io.InputStreamReader(p.inputStream))
                    var cpuFound = false
                    reader.forEachLine { line ->
                        if (line.contains("llama")) {
                            val parts = line.trim().split("\\s+".toRegex())
                            if (parts.size >= 9) {
                                // Find State column to reliably offset to %CPU
                                var cpuIndex = 8
                                if (parts.size > 9 && parts[8].length == 1 && parts[8].matches(Regex("[RSDZTW]"))) {
                                    cpuIndex = 9
                                }
                                val cpu = parts[cpuIndex]
                                runOnUiThread { tvCpu.text = "$cpu%" }
                                cpuFound = true
                            }
                        }
                    }

                    if (!cpuFound) runOnUiThread { tvCpu.text = "Idle" }
                } catch (e: Exception) {}
            }.apply { start() }
        } else {
            tvCpu.text = "-"
            tvSpeed.text = "-"
        }
    }

    private fun appendChatMessage(role: String, message: String) {
        val color = if (role == "User") "#007AFF" else "#32D74B"
        val html = "<br/><br/><font color='$color'><b>$role:</b></font> $message"
        
        val spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            android.text.Html.fromHtml(html)
        }
        
        tvChatLog.append(spanned)
        val scroll = findViewById<android.widget.ScrollView>(R.id.scrollChat)
        scroll.post { scroll.fullScroll(android.view.View.FOCUS_DOWN) }
    }

    private fun sendChatMessage(prompt: String) {
        Thread {
            try {
                val url = java.net.URL("http://127.0.0.1:8080/v1/chat/completions")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 10000
                conn.readTimeout = 60000

                val jsonInputString = """
                    {
                        "messages": [{"role": "user", "content": "$prompt"}],
                        "temperature": 0.7,
                        "max_tokens": 512
                    }
                """.trimIndent()

                conn.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val response = conn.inputStream.bufferedReader().use { it.readText() }
                
                val contentRegex = Regex("\"content\"\\s*:\\s*\"([^\"]*)\"")
                val match = contentRegex.find(response)
                val aiMessage = match?.groupValues?.get(1)?.replace("\\n", "\n") ?: "No response."

                runOnUiThread {
                    appendChatMessage("AI", aiMessage)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    appendChatMessage("System", "Error: ${e.message}. Is the engine running?")
                }
            }
        }.start()
    }

    private fun appendLog(line: String) {
        val current = tvLog.text.toString()
        val lines = current.lines()
        val trimmed = if (lines.size > 300) lines.takeLast(250).joinToString("\n") else current
        tvLog.text = "$trimmed\n$line"
        val scroll = tvLog.parent as? android.widget.ScrollView
        scroll?.post { scroll.fullScroll(android.view.View.FOCUS_DOWN) }
    }

    private fun startLlamaService(modelPath: String, port: Int = 8080) {
        tvLog.text = String.format(getString(R.string.boot_sequence), modelPath)
        val intent = Intent(this, LlamaServerService::class.java).apply {
            putExtra("MODEL_PATH", modelPath)
            putExtra("PORT", port)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        updateUIStatus()
    }

    private fun stopLlamaService() {
        val intent = Intent(this, LlamaServerService::class.java).apply {
            action = "STOP"
        }
        startService(intent)
        updateUIStatus()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data ?: return
            importModelFromUri(uri, true)
        } else if (intent?.action == Intent.ACTION_SEND) {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, android.net.Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            } ?: return
            importModelFromUri(uri, true)
        }
    }

    private fun importModelFromUri(uri: android.net.Uri, isExternalShare: Boolean) {
        var canPersist = false
        if (!isExternalShare) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                canPersist = true
            } catch (e: Exception) {
                canPersist = false
            }
        }

        if (canPersist) {
            ModelManager.addUriModel(this, uri)
            refreshModels()
            Toast.makeText(this, getString(R.string.toast_imported_success), Toast.LENGTH_SHORT).show()
        } else {
            // Must copy temporary URI to private storage (e.g. from WeChat)
            copyUriToLocalModels(uri)
        }
    }

    private fun copyUriToLocalModels(uri: android.net.Uri) {
        val fileName = getFileNameFromUri(uri, true) ?: "imported_model_${System.currentTimeMillis()}.gguf"
        val modelsDir = ModelManager.getModelsDir(this)
        val destFile = java.io.File(modelsDir, fileName)

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.importing_title))
        builder.setMessage(String.format(getString(R.string.importing_msg), fileName))
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()

        Thread {
            try {
                contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output, 8192)
                    }
                }
                runOnUiThread {
                    dialog.dismiss()
                    Toast.makeText(this, getString(R.string.toast_imported_success), Toast.LENGTH_SHORT).show()
                    refreshModels()
                    val pos = models.indexOfFirst { it.name == fileName }
                    if (pos >= 0) spinnerModels.setSelection(pos)
                }
            } catch (e: Exception) {
                destFile.delete()
                runOnUiThread {
                    dialog.dismiss()
                    Toast.makeText(this, "Copy failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun getFileNameFromUri(uri: android.net.Uri, forceGguf: Boolean): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
        if (forceGguf && name != null) {
            if (!name!!.lowercase().endsWith(".gguf")) {
                name = "$name.gguf"
            }
        }
        return name
    }
}
