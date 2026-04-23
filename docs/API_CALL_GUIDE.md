# LatentYield (灵源) - 开发者调用指南

LatentYield 底座内置了与 **OpenAI API 完全兼容** 的服务标准。任何支持自定义 API 端点的第三方软件（如 Chatbox, NextChat 等）都可以零成本接入。

## 🌐 1. 服务端点 (Endpoints)

默认情况下，LatentYield 服务会在 Android 设备的本地回环地址上监听：
- **Base URL**: `http://127.0.0.1:8080`
- **模型列表**: `GET /v1/models`
- **对话生成**: `POST /v1/chat/completions`
- **健康检查**: `GET /health`

---

## 💻 2. 对话 API 调用示例 (cURL)

```bash
curl http://127.0.0.1:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {
        "role": "user",
        "content": "你好，请介绍一下你自己。"
      }
    ],
    "temperature": 0.7,
    "max_tokens": 512,
    "stream": true
  }'
```

*建议将 `stream` 设置为 `true`，以获得 Server-Sent Events (SSE) 的流式打字机体验，这在移动端推理较慢时能极大缓解用户的等待焦虑。*

---

## ⚠️ 3. 开发者必看的“避坑指南”

如果你准备自己写一个 APP 或前端来调用 LatentYield，请**务必注意以下两点**：

### 🚨 坑一：Android 明文 HTTP 流量限制
**现象**：向 `127.0.0.1:8080` 发送请求时，直接抛出 `Network Error` 或 `Cleartext HTTP traffic not permitted`。
**原因**：Android 9 (API 28) 以上，系统默认禁止发送非 HTTPS 的明文请求。
**解法**：在你的调用端 APP 的 `AndroidManifest.xml` 中，为 `<application>` 标签添加如下属性：
```xml
<application
    ...
    android:usesCleartextTraffic="true">
```

### 🚨 坑二：读取超时 (Read Timeout) 断连
**现象**：大模型开始处理后，几十秒没有返回首字，客户端直接抛出 `SocketTimeoutException` 断开连接。
**原因**：手机端加载模型（特别是第一次拉取到内存时）需要时间，部分老旧机型的 Prompt 评估时间可能长达 10~30 秒。而常用的网络库（如 Axios, OkHttp）默认的读取超时通常为 10 秒。
**解法**：在初始化网络请求客户端时，**强制拉长超时时间**。
- **OkHttp (Android)**: `.readTimeout(60, TimeUnit.SECONDS)`
- **Axios (Web/React)**: `timeout: 60000`

---

## 🔗 4. 模型管理建议
调用 API 时，不需要在请求体中硬编码模型名称（如 `"model": "gemma-3-1b"`）。LatentYield 在启动时会自动挂载唯一的模型文件，无论是传入什么名字，引擎都会默认使用当前装载的模型。
