package com.localllm.server.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

data class ModelInfo(val name: String, val path: String, val sizeMb: Long, val isUri: Boolean = false)

object ModelManager {
    private const val PREFS_NAME = "latent_yield_models"
    private const val KEY_URI_MODELS = "uri_models"

    fun getModelsDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "models")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun listModels(context: Context): List<ModelInfo> {
        val models = mutableListOf<ModelInfo>()

        // 1. 本地目录模型 (Backward compatibility)
        val dir = getModelsDir(context)
        dir.listFiles { file -> file.isFile && file.name.endsWith(".gguf") }?.forEach { file ->
            models.add(ModelInfo(file.name, file.absolutePath, file.length() / (1024 * 1024), false))
        }

        // 2. SAF 授权模型 (URI)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uris = prefs.getStringSet(KEY_URI_MODELS, emptySet()) ?: emptySet()
        val validUris = mutableSetOf<String>()
        var urisChanged = false

        uris.forEach { uriString ->
            val uri = Uri.parse(uriString)
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                var name = "Unknown GGUF"
                var size: Long = 0
                var isValid = false
                cursor?.use {
                    if (it.moveToFirst()) {
                        isValid = true
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) name = it.getString(nameIndex)
                        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIndex != -1) size = it.getLong(sizeIndex)
                    }
                }
                if (isValid) {
                    models.add(ModelInfo(name, uriString, size / (1024 * 1024), true))
                    validUris.add(uriString)
                } else {
                    urisChanged = true
                }
            } catch (e: Exception) {
                urisChanged = true
            }
        }

        if (urisChanged) {
            prefs.edit().putStringSet(KEY_URI_MODELS, validUris).apply()
        }

        return models.sortedBy { it.name }
    }

    fun addUriModel(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uris = prefs.getStringSet(KEY_URI_MODELS, emptySet())?.toMutableSet() ?: mutableSetOf()
        uris.add(uri.toString())
        prefs.edit().putStringSet(KEY_URI_MODELS, uris).apply()
    }
}
