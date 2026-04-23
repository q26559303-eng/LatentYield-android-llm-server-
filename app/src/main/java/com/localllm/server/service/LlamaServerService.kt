package com.localllm.server.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.app.Service
import java.io.File
import com.localllm.server.utils.NotificationHelper
import com.localllm.server.utils.ServerConfig

class LlamaServerService : Service() {

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationHelper.createNotification(this, "服务已启动，准备加载模型...")
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "STOP") {
            ServerProcessManager.stopServer()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        
        val modelPath = intent?.getStringExtra("MODEL_PATH")
        val port = intent?.getIntExtra("PORT", ServerConfig.PORT) ?: ServerConfig.PORT
        if (modelPath != null) {
            // 注意：不传 logCallback — 由 MainActivity 统一设置，这里不覆盖
            ServerProcessManager.startServer(this, modelPath, port)
            
            // 更新通知状态
            val modelName = File(modelPath).name
            val notification = NotificationHelper.createNotification(this, "运行中: $modelName | 端口 $port")
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.notify(1, notification)
        }
        
        return START_STICKY
    }

    override fun onDestroy() {
        ServerProcessManager.stopServer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
