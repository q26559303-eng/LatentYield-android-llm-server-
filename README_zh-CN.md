
🇨🇳 中文 | 🇬🇧 English

LatentYield
LatentYield 是一款专为 Android 平台设计的高性能、注重隐私的本地大语言模型（LLM）服务器。它将你的移动设备转变为一个私有化的 AI 推理节点，支持行业标准的 GGUF 模型，并提供完全兼容 OpenAI 规范的 API 接口。

在安卓手机上本地运行 GGUF 模型，并提供 OpenAI 兼容接口。零云端，零费用，零隐私追踪。

✨ 核心特性
🚀 高性能推理
原生内核: 底层通过 JNI 深度集成定制版的 llama.cpp C++ 引擎，提供接近原生程序的执行速度。

硬件加速: 针对 ARM Neon 指令集进行深度优化，全面支持各类 K-Quants 量化格式，在推理性能与内存占用之间取得完美平衡。

GGUF 生态: 开箱即用，完美支持 Llama 3, Mistral, Gemma, Qwen 等目前主流的开源大模型。

🌐 极简通用接入
OpenAI 兼容 API: 接口行为与 OpenAI 完全一致。只需将 Base URL 指向你的手机 IP，即可无缝接入 NextChat, Chatbox, LobeChat 等成熟的第三方 AI 客户端。

内置 WebUI: 附带一个现代化的响应式前端页面，支持直接在浏览器中进行对话以及模型参数调优（Temperature, Top-P, 上下文长度等）。

📱 深度 Android 集成
前台保活服务: 推理引擎作为系统级前台服务运行。即使你在刷微信、锁屏或切换应用，AI 思考也不会中断。

文件关联与路由: 支持在微信、Telegram 或系统文件管理器中直接点击 .gguf 文件，唤醒 LatentYield 并自动完成模型导入，极其顺滑。

零拷贝 Content Provider: 高效解析来自第三方应用的模型文件 URI，避免不必要的存储空间浪费。

🛠️ 系统架构
LatentYield 完美连接了硬核的 C++ 性能底层与现代化的 Android 交互界面：

UI 表现层: 使用 Jetpack Compose 编写，提供流畅且响应迅速的安卓原生体验。

Web 服务层: 内置轻量级 Web 服务器，用于托管 React/TypeScript 编写的 WebUI 界面。

JNI 桥接层: 通过 Java Native Interface 高效编排 Android JVM 与 C++ 引擎之间的数据流和内存调度。

底层推理层: 专为 Android NDK 环境定制优化的 llama.cpp 核心。

🚀 快速开始
安装指南
前往 Releases 页面。

下载最新的 app-release.apk 并安装到你的 Android 设备。

启动应用，并允许“管理所有文件”权限（用于读取动辄数 GB 的大模型文件）。

导入模型
方式 A（极客手动）: 将你的 .gguf 模型文件拷贝到 Android/data/com.latent.yield/files/models 目录下。

方式 B（小白快捷）: 在微信或其他应用中收到 .gguf 文件后，点击“用其他应用打开”，选择 LatentYield 即可。

启动服务
打开 LatentYield，在列表中选中你刚导入的模型。

根据手机内存大小调整 Context Size（上下文长度）（例如 2048 或 4096）。

点击 Start Server 按钮。

在手机浏览器中访问 http://localhost:8080 即可进入 WebUI；或者在同一局域网下的电脑浏览器中输入你手机的 IP 地址进行访问。

🔗 API 接口文档
LatentYield 模拟了标准的 OpenAI API 结构，你可以使用任何官方或第三方的 SDK（如 Python, JS, Go 等）进行调用。

对话补全 (Chat Completions)
POST /v1/chat/completions

Request Body 示例:

JSON
{
  "model": "local-gguf",
  "messages": [
    {"role": "system", "content": "你是一个乐于助人的 AI 助手。"},
    {"role": "user", "content": "请用一句话解释什么是量子纠缠。"}
  ],
  "stream": true,
  "temperature": 0.7
}
👨‍💻 开发者指南
环境要求
Android Studio Ladybug 或更新版本。

Android NDK (Side-by-side) 25.x 或 26.x。

CMake 3.22.1+。

本地编译
Bash
# 克隆项目仓库
git clone https://github.com/q26559303-eng/LatentYield-android-llm-server-.git
cd LatentYield-android-llm-server-

# 编译生成 Release APK
./gradlew assembleRelease
目录结构简述
/app: 包含 Android 原生 UI 与后台服务逻辑。

/WebUI: 包含前端 React/TS 源码。

/app/src/main/cpp: 包含 C++ JNI 封装层与 llama.cpp 引擎代码。

---

## 📚 项目文档

如果你希望深入了解项目的技术细节或将其集成到自己的应用中，请参阅以下详细文档：

* **[开发者调用指南 (Developer Guide)](docs/API_CALL_GUIDE.md)**: 详细说明了如何从其他安卓应用或外部客户端调用本地 LLM 服务。
* **[技术实现白皮书 (Whitepaper)](docs/ARCHITECTURE_AND_PLANNING.md)**: 深度剖析了项目的技术架构设计、实现细节以及未来的规划路线图。

---


🗺️ 未来路线图 (Roadmap)
[ ] GPU 硬件加速支持 (Vulkan/OpenCL)。

[ ] 多模态能力支持 (Vision-LLM 视觉大模型)。

[ ] 接入 Tasker，实现安卓自动化的 AI 代理流。

[ ] 内置本地向量数据库 (RDB)，实现长期记忆 (Long-term memory)。

📄 开源协议
本项目基于 MIT License 开源。详情请参阅 LICENSE 文件。

Developed with ❤️ by Zephyr Yang
发现 Bug 或是想提建议？欢迎提交 Issue 或 PR。
