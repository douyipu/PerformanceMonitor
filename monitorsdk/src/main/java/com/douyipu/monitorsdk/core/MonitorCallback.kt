package com.douyipu.monitorsdk.core

interface MonitorCallback {
    fun onFrameUpdate(fps: Double, totalDropCount: Int, dropLevelCount: Map<Int, Int>)
    fun onAnrDetected(threadInfo: String, stackTrace: String)
}

