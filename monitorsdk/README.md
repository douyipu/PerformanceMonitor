# PerformanceMonitor SDK

Android 性能监控 SDK，提供流畅性监控和 ANR 监控功能。

---

## 📐 架构设计

### 模块结构

```
monitorsdk/
├── core/                    # 核心基础类
│   ├── IMonitor.kt         # Monitor 接口
│   ├── MonitorManager.kt   # 总管理器，SDK 入口
│   ├── MonitorConfig.kt    # 配置类
│   └── MonitorCallback.kt  # 回调接口
├── frame/                   # 流畅性监控（帧监控）
│   └── FrameMonitor.kt     # 帧监控实现
├── anr/                     # ANR 监控
│   ├── AnrMonitor.kt       # ANR 监控实现
│   └── native/             # Native 层代码
│       └── signal_handler.cpp  # SIGQUIT 信号捕获
└── utils/                   # 工具类
    └── ThreadUtils.kt
```

### 设计模式

| 模式 | 应用 | 说明 |
|------|------|------|
| **外观模式 (Facade)** | MonitorManager | 统一入口，隐藏内部复杂性 |
| **单例模式 (Singleton)** | MonitorManager | 使用 Kotlin `object` 实现 |

---

## 🎯 技术方案

### 一、流畅性监控（FrameMonitor）

**监控指标：**
- **帧率 (FPS)**: FPS = 总帧数 / 统计时间
- **丢帧率**: 丢N帧率 = 丢N帧的次数 / 统计时长

**技术方案：**
方案：**Choreographer.FrameCallback**
API 21

**原理：**

安卓View渲染体系存在一个叫FrameCallback的接口，可以监听到每一帧绘制的时机，基于此可以统计流畅性数据

通过 `Choreographer.getInstance().postFrameCallback()` 监听每一帧的绘制时机，计算帧间隔来统计流畅性数据。

### 二、ANR 监控

**技术方案：捕获 SIGQUIT 信号**

**原理：**
1. 当应用发生 ANR 时，系统向应用发送 `SIGQUIT` 信号
2. 通过 Native 层使用 `sigaction()` 注册信号处理器
3. 捕获信号后收集堆栈信息

---

## ⚙️ 设计决策

| 问题 | 决定 | 原因 |
|------|------|------|
| MonitorManager 实现方式 | Kotlin `object` 单例 | 简单直接，天然线程安全 |
| 初始化时机 | 使用者在 `Application.onCreate()` 手动调用 | 使用者有控制权，可传配置参数 |
| 回调方式 | Callback 接口 | 不强制依赖协程/LiveData，更通用 |
| 最低 API 版本 | 21 | 项目要求 |

---

## 📖 使用方式（预期）

```kotlin
// 1. 在 Application 中初始化
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        MonitorManager.init(this, MonitorConfig(
            enableFrame = true,
            enableAnr = true
        ))
    }
}

// 2. 设置回调
MonitorManager.setCallback(object : MonitorCallback {
    override fun onFrameUpdate(fps: Double, dropCount: Int, dropLevel: Map<Int, Int>) {
        Log.d("Monitor", "FPS: $fps, 总丢帧: $dropCount, 丢帧分布: $dropLevel")
    }
    
    override fun onAnrDetected(stackTrace: String) {
        Log.e("Monitor", "ANR发生: $stackTrace")
    }
})

// 3. 开始/停止监控
MonitorManager.start()
MonitorManager.stop()
```

---

## ✅ TODO

### Phase 1: 核心框架 ✅
- [x] 创建 `IMonitor.kt` - Monitor 接口定义
- [x] 创建 `MonitorConfig.kt` - 配置类
- [x] 创建 `MonitorCallback.kt` - 回调接口
- [x] 创建 `MonitorManager.kt` - 管理器实现

### Phase 2: 流畅性监控 ✅
- [x] 创建 `FrameMonitor.kt` - 基于 Choreographer 的帧监控
- [x] 实现帧率统计逻辑
- [x] 实现丢帧率统计逻辑

### Phase 3: ANR 监控 ✅
- [x] 配置 NDK 环境
- [x] 创建 `signal_handler.cpp` - Native 信号处理
- [x] 创建 `AnrMonitor.kt` - ANR 监控 Kotlin 层
- [x] 实现堆栈信息收集

### Phase 4: 测试 App ✅
- [x] 创建测试界面展示监控数据
- [x] 添加模拟卡顿按钮
- [x] 添加模拟 ANR 按钮
- [ ] 录制演示视频

---

## 📚 参考资料

- [Choreographer 官方文档](https://developer.android.com/reference/android/view/Choreographer)
- [Android ANR 原理分析](https://developer.android.com/topic/performance/vitals/anr)
