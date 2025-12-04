package com.douyipu.performancemonitor

import android.app.Application
import com.douyipu.monitorsdk.core.MonitorConfig
import com.douyipu.monitorsdk.core.MonitorManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        MonitorManager.init(this, MonitorConfig(
            enableFrame = true,
            enableAnr = true,
            frameReportIntervalMs = 1000L,
            dropFrameLevels = listOf(3, 5, 7)
        ))
    }
}

