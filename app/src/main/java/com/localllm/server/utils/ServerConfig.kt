package com.localllm.server.utils

object ServerConfig {
    const val PORT = 8080
    const val HOST = "127.0.0.1"
    const val THREADS = 2 // Optimized for Dimensity 810 (2 big cores)
    const val CONTEXT_SIZE = 2048
}
