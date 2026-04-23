[🇨🇳 中文](README_zh-CN.md) | [🇬🇧 English](README.md)

# LatentYield —— A High-Performance Local LLM Server for Android

LatentYield turns your Android device into a private, high-performance inference node for Large Language Models. It natively supports industry-standard GGUF models and offers a fully OpenAI-compatible API.

**Run GGUF models locally on your phone — zero cloud, zero cost, zero tracking.**

## ✨ Key Features

### 🚀 High-Performance Inference
- **Native Core**: Leverages a custom-built `llama.cpp` C++ backend via JNI, delivering near-native execution speeds.
- **Hardware Acceleration**: Optimized for ARM Neon and fully compatible with K-Quants quantization formats, striking an ideal balance between performance and memory usage.
- **GGUF Ecosystem**: Ready to run Llama 3, Mistral, Gemma, Qwen, and virtually any model available in the GGUF format.

### 🌐 Universal Connectivity
- **OpenAI-Compatible API**: A drop-in replacement for OpenAI endpoints. Connect your favorite AI clients—like NextChat, Chatbox, or LobeChat—simply by pointing the base URL to your phone’s IP address.
- **Built-in WebUI**: A modern, responsive chat interface accessible directly from your browser. Adjust temperature, top‑p, context length, and other parameters on the fly.

### 📱 Deep Android Integration
- **Foreground Service**: The inference engine runs as a system-level foreground service. AI generation continues seamlessly even when you switch apps or lock the screen.
- **File Association & Intent Routing**: Open `.gguf` files directly from WeChat, Telegram, or your file manager—LatentYield automatically imports the model.
- **Zero‑Copy Content Provider**: Efficiently processes model file URIs from third‑party apps without duplicating precious storage space.

## 🛠️ System Architecture

LatentYield bridges raw C++ performance with a modern Android experience:

- **UI Layer**: Built with **Jetpack Compose** for a fluid, reactive, and native Android interface.
- **Web Layer**: A lightweight embedded server hosts the **React / TypeScript** WebUI.
- **Bridge Layer**: **JNI (Java Native Interface)** efficiently orchestrates data flow and memory between the Android JVM and the C++ engine.
- **Inference Layer**: A specialized **llama.cpp** build optimized for the Android NDK environment.

## 🚀 Quick Start

### Installation
1. Go to the [Releases page](https://github.com/q26559303-eng/LatentYield-android-llm-server-/releases).
2. Download the latest `app-release.apk` and install it on your device.
3. On first launch, grant **“Manage all files”** permission (required to read multi‑gigabyte model files).

### Importing Models
- **Option A (Manual)**: Copy your `.gguf` model files into `Android/data/com.latent.yield/files/models`.
- **Option B (Shortcut)**: In any app (e.g., WeChat), tap a `.gguf` file, choose **“Open with other app”**, and select **LatentYield**.

### Starting the Server
1. Open LatentYield and select your imported model from the list.
2. Adjust the **Context Size** according to your device’s available RAM (recommended: `2048` or `4096`).
3. Tap **Start Server**.
4. Access the WebUI at `http://localhost:8080` on your phone, or use your phone’s IP address from any device on the same local network.

## 🔗 API Documentation

LatentYield mimics the standard OpenAI API structure. Use any official or third‑party SDK (Python, JavaScript, Go, etc.) to interact with it.

### Chat Completions
`POST /v1/chat/completions`

**Example Request Body:**
```json
{
  "model": "local-gguf",
  "messages": [
    {"role": "system", "content": "You are a helpful assistant."},
    {"role": "user", "content": "Explain quantum entanglement in one sentence."}
  ],
  "stream": true,
  "temperature": 0.7
}
```
👨‍💻 Developer Guide
Prerequisites
Android Studio Ladybug or newer

Android NDK (Side‑by‑side) 25.x or 26.x

CMake 3.22.1+

Build from Source
bash
# Clone the repository
git clone https://github.com/q26559303-eng/LatentYield-android-llm-server-.git
cd LatentYield-android-llm-server-

# Build the release APK
./gradlew assembleRelease
Project Structure
/app – Android native UI and background service logic.

/WebUI – Frontend source code (React / TypeScript).

/app/src/main/cpp – C++ JNI wrapper and llama.cpp engine integration.

📚 Documentation
For in‑depth technical insights and integration guidance, please refer to:

Developer Integration Guide – Detailed instructions for calling the local LLM server from other Android apps or external clients.

Technical Whitepaper – A deep dive into the architecture, implementation details, and future roadmap.

🗺️ Roadmap
GPU hardware acceleration support (Vulkan / OpenCL)

Multi‑modal capabilities (Vision‑LLM)

Tasker integration for automated AI workflows on Android

Local vector database (RAG) for long‑term memory

📄 License
This project is licensed under the MIT License. See the LICENSE file for details.

---

## 👤 Author & Contact

**Zephyr Yang**

- 💬 **WeChat**: `nonetude`
- 🐛 **Issues**: [GitHub Issues](https://github.com/q26559303-eng/LatentYield-android-llm-server-/issues)
- 💡 **Discussions**: [GitHub Discussions](https://github.com/q26559303-eng/LatentYield-android-llm-server-/discussions)

*Developed with ❤️ by Zephyr Yang*

Found a bug or have a suggestion? Feel free to open an Issue or submit a Pull Request.
