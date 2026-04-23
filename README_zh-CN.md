🇨🇳 中文 | 🇬🇧 English

# LatentYield —— 专为 Android 打造的高性能本地 LLM 服务器

LatentYield 将你的 Android 设备转变为一台私有的、高性能的大语言模型推理节点。它原生支持行业标准的 GGUF 模型，并提供与 OpenAI 完全兼容的 API 接口。

**在手机上本地运行 GGUF 模型，无需云端，无需费用，无需担心隐私追踪。**

## ✨ 核心特性

### 🚀 高性能推理引擎
*   **原生内核**：通过 JNI 深度集成定制版 `llama.cpp` C++ 引擎，释放接近原生的极致推理速度。
*   **硬件加速**：针对 ARM Neon 指令集深度优化，全面支持各类 K-Quants 量化格式，在模型性能与内存占用间取得理想平衡。
*   **GGUF 生态**：开箱即用，完美兼容 Llama 3、Mistral、Gemma、Qwen 等主流开源大语言模型。

### 🌐 极简通用接入
*   **OpenAI 兼容 API**：接口行为与 OpenAI 官方标准完全一致。只需将客户端 Base URL 指向手机 IP，即可无缝接入 NextChat、Chatbox、LobeChat 等成熟 AI 前端。
*   **内置 WebUI**：附带一个现代化的响应式聊天界面，支持在浏览器中直接对话，并可实时调整 Temperature、Top-P、上下文长度等参数。

### 📱 深度 Android 系统集成
*   **前台保活服务**：推理引擎以系统级前台服务运行。即使切换至微信、锁屏或使用其他应用，AI 推理进程也绝不会中断。
*   **智能文件关联**：支持在微信、Telegram 或系统文件管理器中直接点击 `.gguf` 文件唤醒 LatentYield 并自动导入，体验流畅顺滑。
*   **零拷贝 Content Provider**：高效解析来自第三方应用的模型文件 URI，避免重复占用宝贵的存储空间。

## 🛠️ 系统架构

LatentYield 在硬核的 C++ 底层性能与现代化的 Android 交互界面之间架起了完美桥梁：

*   **UI 表现层**：基于 **Jetpack Compose** 构建，提供流畅且响应迅速的安卓原生体验。
*   **Web 服务层**：内置轻量级 Web 服务器，托管由 **React / TypeScript** 编写的现代化 WebUI。
*   **JNI 桥接层**：通过 Java Native Interface 高效调度 Android JVM 与 C++ 引擎间的数据流转与内存管理。
*   **底层推理层**：专为 Android NDK 环境优化定制的 **llama.cpp** 核心。

## 🚀 快速开始

### 安装指南
1.  前往 [Releases 页面](https://github.com/q26559303-eng/LatentYield-android-llm-server-/releases)。
2.  下载最新的 `app-release.apk` 并安装至你的 Android 设备。
3.  首次启动时，请授予“管理所有文件”权限（用于读取 GB 级别的大模型文件）。

### 导入模型
*   **方式 A（手动导入）**：将 `.gguf` 模型文件拷贝至 `Android/data/com.latent.yield/files/models` 目录。
*   **方式 B（快捷导入）**：在微信或其他应用内收到 `.gguf` 文件后，选择“用其他应用打开”，点击 LatentYield 即可自动导入。

### 启动服务
1.  打开 LatentYield，在列表中选中已导入的模型。
2.  根据手机运存大小合理设置 **Context Size**（建议 2048 或 4096）。
3.  点击 **Start Server** 启动服务。
4.  在手机浏览器访问 `http://localhost:8080` 即可使用 WebUI；或在同一局域网下的电脑浏览器输入手机 IP 地址访问。

## 🔗 API 接口文档

LatentYield 模拟标准 OpenAI API 结构，支持使用任意官方或第三方 SDK（Python、JS、Go 等）进行调用。

### 对话补全 (Chat Completions)
`POST /v1/chat/completions`

**Request Body 示例:**
```json
{
  "model": "local-gguf",
  "messages": [
    {"role": "system", "content": "你是一个乐于助人的 AI 助手。"},
    {"role": "user", "content": "请用一句话解释什么是量子纠缠。"}
  ],
  "stream": true,
  "temperature": 0.7
}
```

👨‍💻 开发者指南
环境要求
Android Studio Ladybug 或更新版本。

Android NDK (Side-by-side) 25.x 或 26.x。

CMake 3.22.1+。

本地编译
bash
# 克隆项目仓库
git clone https://github.com/q26559303-eng/LatentYield-android-llm-server-.git
cd LatentYield-android-llm-server-

# 编译生成 Release APK
./gradlew assembleRelease
目录结构简述
/app：包含 Android 原生 UI 与后台服务逻辑。

/WebUI：包含前端 React / TypeScript 源码。

/app/src/main/cpp：包含 C++ JNI 封装层与 llama.cpp 引擎代码。

📚 项目文档
若需深入了解技术细节或进行二次开发集成，请参阅以下详细文档：

开发者调用指南：详细阐述如何从其他安卓应用或外部客户端调用本地 LLM 服务。

技术实现白皮书：深度剖析项目技术架构设计、实现细节及未来演进路线图。

🗺️ 未来路线图
引入 GPU 硬件加速支持 (Vulkan / OpenCL)。

支持多模态能力 (Vision-LLM 视觉大模型)。

接入 Tasker，打造安卓自动化 AI 代理流。

内置本地向量数据库 (RAG)，实现长期记忆功能。

📄 开源协议
本项目基于 MIT License 开源。详情请参阅 LICENSE 文件。

Developed with ❤️ by Zephyr Yang

发现 Bug 或想要提出建议？欢迎提交 Issue 或 PR。
