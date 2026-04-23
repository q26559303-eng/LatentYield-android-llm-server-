
[🇨🇳 中文](README_zh-CN.md) | [🇬🇧 English](README.md)

LatentYield
LatentYield is a high-performance, privacy-centric local Large Language Model (LLM) server for Android. It transforms your mobile device into a self-hosted AI inference node, supporting industry-standard GGUF models with an OpenAI-compatible API.

Run GGUF models locally on Android with an OpenAI-compatible API. No cloud, no fees, no tracking.

✨ Key Features
🚀 High-Performance Inference
Native Core: Powered by a custom-built llama.cpp C++ backend via JNI for near-native execution speed.

Hardware Acceleration: Optimized for ARM Neon and supports various quantization levels (K-Quants) to balance performance and RAM usage.

GGUF Ready: Seamlessly run Llama 3, Mistral, Gemma, Qwen, and any other model in the GGUF ecosystem.

🌐 Universal Connectivity
OpenAI Compatible API: Drop-in replacement for OpenAI endpoints. Connect with apps like NextChat, Chatbox, or LobeChat by pointing the Base URL to your phone.

Built-in WebUI: A modern, responsive web interface for direct chatting and model parameter tuning (temperature, top-p, context length).

📱 Android Integration
Foreground Service: Ensures inference continues in the background even when you switch apps or lock the screen.

File Association & Intent Routing: Directly click a .gguf file in WeChat, Telegram, or File Manager to import it into LatentYield instantly.

Zero-Copy Content Provider: Efficiently handles model file URIs from third-party apps without unnecessary storage bloat.

🛠️ System Architecture
LatentYield bridges the gap between raw C++ performance and modern Android UI:

UI Layer: Built with Jetpack Compose for a fluid, reactive Android native experience.

Web Layer: A built-in server (Ktor/Express-like) hosting the React/TypeScript WebUI.

Bridge Layer: JNI (Java Native Interface) orchestrating data flow between the Android JVM and the C++ engine.

Inference Layer: A specialized build of llama.cpp optimized for Android NDK.

🚀 Quick Start
Installation
Navigate to the Releases page.

Download and install app-release.apk.

Grant "Manage External Storage" permission to allow the app to read large model files.

Importing Models
Option A (Manual): Place your .gguf files in Android/data/com.latent.yield/files/models.

Option B (Shortcut): In any app (like WeChat), click a .gguf file -> "Open with other app" -> Select LatentYield.

Starting the Server
Open LatentYield and select your model.

Adjust Context Size (e.g., 2048 or 4096) based on your device's RAM.

Tap Start Server.

Access the WebUI via http://localhost:8080 on your phone, or via the device's IP from your PC.

🔗 API Documentation
LatentYield mimics the OpenAI API structure. You can use any standard SDK (Python, JS, Go).

Chat Completions
POST /v1/chat/completions

Request Body:

JSON
{
  "model": "local-gguf",
  "messages": [
    {"role": "system", "content": "You are a helpful assistant."},
    {"role": "user", "content": "Explain quantum entanglement in one sentence."}
  ],
  "stream": true,
  "temperature": 0.7
}
👨‍💻 For Developers
Prerequisites
Android Studio Ladybug or newer.

Android NDK (Side-by-side) 25.x or 26.x.

CMake 3.22.1+.

Build from Source
Bash
# Clone the repository
git clone https://github.com/q26559303-eng/LatentYield-android-llm-server-.git
cd LatentYield-android-llm-server-

# Build the APK
./gradlew assembleRelease
Project Structure
/app: Android Native UI and Service logic.

/WebUI: Frontend source code (React/TS).

/app/src/main/cpp: C++ JNI wrappers and llama.cpp integration.

🗺️ Roadmap
[ ] GPU Acceleration (Vulkan/OpenCL) support.

[ ] Multi-modal support (Vision-LLM).

[ ] Integration with Tasker for automated AI workflows.

[ ] Local RDB (Vector Database) for long-term memory.

📄 License
This project is licensed under the MIT License. See LICENSE for details.

Developed with ❤️ by Zephyr Yang
Found a bug? Open an Issue or submit a PR.



其实我搞不明白英语 都是Gemini写的 非常感谢哈
