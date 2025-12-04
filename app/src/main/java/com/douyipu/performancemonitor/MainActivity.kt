package com.douyipu.performancemonitor

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.douyipu.monitorsdk.core.MonitorCallback
import com.douyipu.monitorsdk.core.MonitorManager

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvFps: TextView
    private lateinit var tvDropFrames: TextView
    private lateinit var tvDropLevel: TextView
    private lateinit var tvAnr: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupCallback()
        setupButtons()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        tvFps = findViewById(R.id.tvFps)
        tvDropFrames = findViewById(R.id.tvDropFrames)
        tvDropLevel = findViewById(R.id.tvDropLevel)
        tvAnr = findViewById(R.id.tvAnr)
    }

    private fun setupCallback() {
        MonitorManager.setCallback(object : MonitorCallback {
            override fun onFrameUpdate(fps: Double, totalDropCount: Int, dropLevelCount: Map<Int, Int>) {
                runOnUiThread {
                    tvFps.text = "FPS: %.1f".format(fps)
                    tvDropFrames.text = "总丢帧次数: $totalDropCount"
                    tvDropLevel.text = "丢帧分布: ${formatDropLevel(dropLevelCount)}"
                }
            }

            override fun onAnrDetected(threadInfo: String, stackTrace: String) {
                runOnUiThread {
                    tvAnr.text = "ANR检测到!\n$threadInfo\n${stackTrace.take(200)}..."
                    Toast.makeText(this@MainActivity, "检测到ANR!", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            MonitorManager.start()
            tvStatus.text = "状态: 监控中..."
            Toast.makeText(this, "开始监控", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            MonitorManager.stop()
            tvStatus.text = "状态: 已停止"
            Toast.makeText(this, "停止监控", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnSimulateJank).setOnClickListener {
            Toast.makeText(this, "模拟卡顿中...", Toast.LENGTH_SHORT).show()
            Thread.sleep(200)
        }

        findViewById<Button>(R.id.btnSimulateAnr).setOnClickListener {
            Toast.makeText(this, "即将模拟ANR，请等待...", Toast.LENGTH_SHORT).show()
            Thread.sleep(6000)
        }
    }

    private fun formatDropLevel(dropLevelCount: Map<Int, Int>): String {
        if (dropLevelCount.isEmpty()) return "无"
        return dropLevelCount.entries
            .sortedBy { it.key }
            .joinToString(", ") { "≥${it.key}帧:${it.value}次" }
    }

    override fun onDestroy() {
        super.onDestroy()
        MonitorManager.destroy()
    }
}
