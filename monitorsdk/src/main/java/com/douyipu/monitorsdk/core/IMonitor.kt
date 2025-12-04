package com.douyipu.monitorsdk.core

import android.content.Context

interface IMonitor {
    fun init(context: Context)
    fun start()
    fun stop()
    fun destroy()
    fun isRunning(): Boolean
}

