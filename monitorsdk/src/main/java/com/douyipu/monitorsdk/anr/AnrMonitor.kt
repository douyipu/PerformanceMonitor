package com.douyipu.monitorsdk.anr

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.douyipu.monitorsdk.core.IMonitor
import com.douyipu.monitorsdk.core.MonitorManager

class AnrMonitor : IMonitor {

    private var isRunning = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val nativeCallback = object : NativeCallback {
        override fun onAnrDetected() {
            mainHandler.post {
                val threadInfo = getMainThreadInfo()
                val stackTrace = getMainThreadStackTrace()
                MonitorManager.getCallback()?.onAnrDetected(threadInfo, stackTrace)
            }
        }
    }

    override fun init(context: Context) {
        try {
            System.loadLibrary("monitorsdk")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    override fun start() {
        if (isRunning) return
        isRunning = nativeInit(nativeCallback)
    }

    override fun stop() {
        isRunning = false
    }

    override fun destroy() {
        stop()
        nativeDestroy()
    }

    override fun isRunning(): Boolean = isRunning

    private fun getMainThreadInfo(): String {
        val mainThread = Looper.getMainLooper().thread
        return "Thread: ${mainThread.name}, State: ${mainThread.state}"
    }

    private fun getMainThreadStackTrace(): String {
        val mainThread = Looper.getMainLooper().thread
        return mainThread.stackTrace.joinToString("\n") { 
            "    at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" 
        }
    }

    private external fun nativeInit(callback: NativeCallback): Boolean
    private external fun nativeDestroy()

    interface NativeCallback {
        fun onAnrDetected()
    }
}

