package com.localllm.server.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.localllm.server.R
import com.localllm.server.utils.NotificationHelper

class DaemonProxyService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            DaemonProxy.stop()
            stopSelf()
            return START_NOT_STICKY
        }
        val modelPath = intent?.getStringExtra("MODEL_PATH") ?: return START_NOT_STICKY
        
        // 挂载一个极其低调的常驻通知
        startForeground(
            2, 
            NotificationHelper.createNotification(
                this, 
                "${getString(R.string.auto_wake)}: Port 8080"
            )
        )
        
        DaemonProxy.start(this, modelPath)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
