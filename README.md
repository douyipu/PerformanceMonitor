# PerformanceMonitor SDK

[中文](#中文介绍) | [English](#english)

---

## 中文介绍

### 📖 项目简介

PerformanceMonitor 是一个 Android 性能监控 SDK，提供以下核心功能：

- **流畅性监控**：基于 Choreographer 实时监控帧率(FPS)和丢帧率
- **ANR 监控**：通过 Native 层捕获 SIGQUIT 信号检测 ANR

### ✨ 特性

- 🎯 **低侵入性**：SDK 作为独立模块，不耦合业务代码
- 📊 **实时数据**：每秒上报帧率和丢帧统计
- 🔧 **灵活配置**：支持自定义上报间隔、丢帧级别等
- 📱 **兼容性好**：最低支持 Android 5.0 (API 21)

### 🏗️ 架构设计

```
monitorsdk/
├── core/                    # 核心模块
│   ├── MonitorManager      # SDK 入口（外观模式 + 单例模式）
│   ├── MonitorConfig       # 配置类
│   ├── MonitorCallback     # 回调接口
│   └── IMonitor            # Monitor 接口
├── frame/                   # 流畅性监控
│   └── FrameMonitor        # 基于 Choreographer 的帧监控
└── anr/                     # ANR 监控
    ├── AnrMonitor          # Kotlin 层
    └── signal_handler.cpp  # Native 层 SIGQUIT 捕获
```

### 🚀 快速开始

#### 1. 添加依赖

```kotlin
// settings.gradle.kts
include(":monitorsdk")

// app/build.gradle.kts
dependencies {
    implementation(project(":monitorsdk"))
}
```

#### 2. 初始化 SDK

```kotlin
class MyApp : Application() {
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
```

#### 3. 设置回调

```kotlin
MonitorManager.setCallback(object : MonitorCallback {
    override fun onFrameUpdate(fps: Double, totalDropCount: Int, dropLevelCount: Map<Int, Int>) {
        Log.d("Monitor", "FPS: $fps, 丢帧: $totalDropCount")
    }
    
    override fun onAnrDetected(threadInfo: String, stackTrace: String) {
        Log.e("Monitor", "ANR: $stackTrace")
    }
})
```

#### 4. 启动监控

```kotlin
MonitorManager.start()
// ...
MonitorManager.stop()
```

### 🔬 技术方案

#### 流畅性监控

- **原理**：通过 `Choreographer.FrameCallback` 监听每一帧的绘制时机
- **计算**：帧间隔超过 16.67ms 视为丢帧，统计各级别丢帧次数
- **指标**：
  - FPS = 帧数 / 统计时间
  - 丢帧率 = 丢N帧次数 / 统计时间

#### ANR 监控

- **原理**：当系统检测到 ANR 时，会向应用发送 SIGQUIT 信号
- **实现**：通过 Native 层 `sigaction()` 注册信号处理器捕获 SIGQUIT
- **数据**：捕获时收集主线程堆栈信息

### 📁 项目结构

```
PerformanceMonitor/
├── app/                    # 测试 App
│   └── src/main/
│       ├── java/          # 测试代码
│       └── res/           # 资源文件
├── monitorsdk/            # SDK 模块
│   └── src/main/
│       ├── java/          # Kotlin 代码
│       └── cpp/           # Native 代码
└── README.md              # 项目说明
```

---

## English

### 📖 Introduction

PerformanceMonitor is an Android performance monitoring SDK that provides:

- **Fluency Monitoring**: Real-time FPS and frame drop monitoring based on Choreographer
- **ANR Monitoring**: ANR detection by capturing SIGQUIT signal at Native layer

### ✨ Features

- 🎯 **Low Intrusion**: SDK as an independent module, decoupled from business code
- 📊 **Real-time Data**: Reports FPS and frame drop statistics every second
- 🔧 **Flexible Configuration**: Customizable report interval, drop frame levels, etc.
- 📱 **Good Compatibility**: Supports Android 5.0+ (API 21+)

### 🚀 Quick Start

#### 1. Add Dependency

```kotlin
// settings.gradle.kts
include(":monitorsdk")

// app/build.gradle.kts
dependencies {
    implementation(project(":monitorsdk"))
}
```

#### 2. Initialize SDK

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        MonitorManager.init(this, MonitorConfig(
            enableFrame = true,
            enableAnr = true
        ))
    }
}
```

#### 3. Set Callback

```kotlin
MonitorManager.setCallback(object : MonitorCallback {
    override fun onFrameUpdate(fps: Double, totalDropCount: Int, dropLevelCount: Map<Int, Int>) {
        // Handle frame data
    }
    
    override fun onAnrDetected(threadInfo: String, stackTrace: String) {
        // Handle ANR
    }
})
```

#### 4. Start Monitoring

```kotlin
MonitorManager.start()
```

### 🔬 Technical Details

#### Fluency Monitoring

- Uses `Choreographer.FrameCallback` to monitor frame timing
- Frame interval > 16.67ms is considered as dropped frame
- Reports FPS and drop count at configurable intervals

#### ANR Monitoring

- System sends SIGQUIT signal when ANR occurs
- Uses `sigaction()` in Native layer to capture SIGQUIT
- Collects main thread stack trace when ANR detected

### 📄 License

MIT License

---

## 📹 Demo

[演示视频 / Demo Video](./demo.mp4)

