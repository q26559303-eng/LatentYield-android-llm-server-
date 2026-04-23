# LatentYield (灵源) - 项目规划与技术实现白皮书

## 一、 项目愿景与生态定位
**LatentYield (中文名：灵源)** 是一个致力于将 Android 移动设备（如 OPPO A57）转化为独立、离线、零成本的大模型推理节点的生态底座。
项目由两大部分组成：
1. **Android 原生底座 (`app/`)**：负责以极高的性能拉起 C++ 大模型推理引擎。
2. **React 监控面板 (`WebUI/`)**：提供赛博朋克风格的性能监控台，也是未来轻量级聊天应用的雏形。

---

## 二、 核心架构设计

### 1. 引擎选型与“壳”架构
- **核心引擎**：采用 `llama.cpp` 提供的 `llama-server`，并针对 Android NDK 进行静态交叉编译。
- **管理壳**：使用 Kotlin 开发原生 Android APP，通过 `ProcessBuilder` 作为守护进程管理底层二进制引擎的生命周期。
- **前端 WebUI**：采用 React + TailwindCSS 构建，通过 Custom Hooks (`useServerMetrics`, `useServerLogs`) 实现了彻底的 UI 与业务逻辑解耦。

### 2. Android 端的关键技术突破
- **SELinux 权限绕过**：将编译好的二进制重命名为 `libllama_server.so`，打包放入 `jniLibs/arm64-v8a/`。利用 Android 系统 `extractNativeLibs="true"` 的特性，在安装时自动获取可执行权限。
- **进程 I/O 死锁防御**：为了防止子进程的大量日志写满系统的管道缓冲区（约64KB）导致应用卡死，我们引入了 `runCatching` 和 `bufferedReader().useLines` 在子线程中持续无阻塞消费输出流。
- **无障碍存储**：大模型文件放置于 `/storage/emulated/0/Android/data/com.localllm.server/files/models`，免除了繁杂的 Android 动态存储权限申请，支持 `adb push` 直接推入。
- **自动验活机制**：服务启动后，原生底座会每 3 秒轮询一次 `/health` 接口。就绪后会自动构造 HTTP 请求发送给模型，让其进行“自我介绍”并输出在日志台，完成自动化闭环测试。

---

## 三、 代码质量与优化 (Refactoring)
- **去 Java 化**：废弃了臃肿的 `try-catch` 和 `while` 循环，全面拥抱 Kotlin 的标准库特性（如 `.onFailure`、`.apply`），使 Android 代码体积减小约 18%。
- **React 关注点分离**：通过剥离状态流，将原本 350 行的 `App.tsx` 压缩至 90 行的纯展示组件。

---

## 四、 未来迭代路线 (Roadmap)
1. **性能面板实装**：将 WebUI 从单纯的展示（`setInterval` 模拟数据）升级为通过 WebSocket 或 HTTP 轮询真实获取 Android 底座的 CPU 与内存负载。
2. **局域网共享 (LAN Node)**：将绑定的 Host 从 `127.0.0.1` 切换为 `0.0.0.0`，使同一局域网内的 PC 和其他设备都能调用手机的算力。
3. **生态 APP 孵化**：基于 `API_CALL_GUIDE`，开发第一款独立对话 APP，完成“服务端 + 消费端”的完整闭环。
