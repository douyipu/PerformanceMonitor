package com.douyipu.monitorsdk.core

data class MonitorConfig(
    val enableFrame: Boolean = true,
    val enableAnr: Boolean = true,
    val frameReportIntervalMs: Long = 1000L,
    val dropFrameLevels: List<Int> = listOf(3, 5, 7)
)

