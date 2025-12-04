package com.douyipu.monitorsdk.core

import android.content.Context
import com.douyipu.monitorsdk.anr.AnrMonitor
import com.douyipu.monitorsdk.frame.FrameMonitor

object MonitorManager {
    private var isInitialized = false
    private var config: MonitorConfig = MonitorConfig()
    private var callback: MonitorCallback? = null
    private val monitors = mutableListOf<IMonitor>()

    fun init(context: Context, config: MonitorConfig = MonitorConfig()) {
        if (isInitialized) return
        this.config = config
        
        if (config.enableFrame) {
            monitors.add(FrameMonitor())
        }
        
        if (config.enableAnr) {
            monitors.add(AnrMonitor())
        }
        
        monitors.forEach { it.init(context.applicationContext) }
        isInitialized = true
    }

    fun setCallback(callback: MonitorCallback) {
        this.callback = callback
    }

    fun getCallback(): MonitorCallback? = callback

    fun getConfig(): MonitorConfig = config

    fun start() {
        checkInitialized()
        monitors.forEach { it.start() }
    }

    fun stop() {
        checkInitialized()
        monitors.forEach { it.stop() }
    }

    fun destroy() {
        monitors.forEach { it.destroy() }
        monitors.clear()
        callback = null
        isInitialized = false
    }

    fun isRunning(): Boolean {
        return monitors.isNotEmpty() && monitors.all { it.isRunning() }
    }

    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("MonitorManager not initialized. Call init() first.")
        }
    }
}

