package com.douyipu.monitorsdk.frame

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.Choreographer
import com.douyipu.monitorsdk.core.IMonitor
import com.douyipu.monitorsdk.core.MonitorManager

class FrameMonitor : IMonitor {

    private var isRunning = false
    private var lastFrameTimeNanos = 0L
    private var frameCount = 0
    private var totalDropCount = 0
    private val dropLevelCount = mutableMapOf<Int, Int>()
    
    private var reportHandler: Handler? = null
    private var reportThread: HandlerThread? = null
    
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return
            
            if (lastFrameTimeNanos != 0L) {
                val intervalMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000
                val droppedFrames = (intervalMs / FRAME_INTERVAL_MS).toInt() - 1
                
                if (droppedFrames > 0) {
                    totalDropCount += droppedFrames
                    countDropLevel(droppedFrames)
                }
                
                frameCount++
            }
            
            lastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }
    
    private val reportRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            
            val config = MonitorManager.getConfig()
            val fps = if (frameCount > 0) {
                frameCount.toDouble() / (config.frameReportIntervalMs / 1000.0)
            } else {
                0.0
            }
            
            MonitorManager.getCallback()?.onFrameUpdate(
                fps,
                totalDropCount,
                dropLevelCount.toMap()
            )
            
            resetCounters()
            reportHandler?.postDelayed(this, config.frameReportIntervalMs)
        }
    }

    override fun init(context: Context) {
        val config = MonitorManager.getConfig()
        config.dropFrameLevels.forEach { level ->
            dropLevelCount[level] = 0
        }
    }

    override fun start() {
        if (isRunning) return
        isRunning = true
        
        resetCounters()
        
        reportThread = HandlerThread("FrameMonitor-Report").apply { start() }
        reportHandler = Handler(reportThread!!.looper)
        
        Handler(Looper.getMainLooper()).post {
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
        
        val intervalMs = MonitorManager.getConfig().frameReportIntervalMs
        reportHandler?.postDelayed(reportRunnable, intervalMs)
    }

    override fun stop() {
        isRunning = false
        reportHandler?.removeCallbacks(reportRunnable)
    }

    override fun destroy() {
        stop()
        reportThread?.quitSafely()
        reportThread = null
        reportHandler = null
    }

    override fun isRunning(): Boolean = isRunning

    private fun countDropLevel(droppedFrames: Int) {
        val levels = MonitorManager.getConfig().dropFrameLevels.sortedDescending()
        for (level in levels) {
            if (droppedFrames >= level) {
                dropLevelCount[level] = (dropLevelCount[level] ?: 0) + 1
                break
            }
        }
    }

    private fun resetCounters() {
        frameCount = 0
        totalDropCount = 0
        dropLevelCount.keys.forEach { dropLevelCount[it] = 0 }
        lastFrameTimeNanos = 0L
    }

    companion object {
        private const val FRAME_INTERVAL_MS = 16.67
    }
}

