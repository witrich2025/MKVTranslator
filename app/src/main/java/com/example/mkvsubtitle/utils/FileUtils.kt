package com.example.mkvsubtitle.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

/**
 * Utilidades para manejo de archivos con soporte para scoped storage de Android
 */
object FileUtils {

    /**
     * Obtiene el nombre real del archivo desde un URI
     */
    fun getFileName(context: Context, uri: Uri): String? {
        return when {
            uri.scheme == "content" -> {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    it.moveToFirst()
                    it.getString(nameIndex)
                } ?: "unknown"
            }
            uri.scheme == "file" -> File(uri.path ?: "").name
            else -> uri.lastPathSegment
        }
    }

    /**
     * Obtiene la ruta del archivo - para Android 10+ copia a caché
     */
    fun getPath(context: Context, uri: Uri?): String? {
        if (uri == null) return null

        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                try {
                    val cursor = context.contentResolver.query(
                        uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
                    )
                    var result = ""
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val displayName = it.getString(it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
                            result = copyFileToCache(context, uri, displayName)
                        }
                    }
                    result
                } catch (e: Exception) {
                    ""
                }
            }
            else -> {
                try {
                    val cursor = context.contentResolver.query(
                        uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            it.getString(it.getColumnIndex(MediaStore.MediaColumns.DATA))
                        } else {
                            ""
                        }
                    } ?: ""
                } catch (e: Exception) {
                    uri.path ?: ""
                }
            }
        }
    }

    /**
     * Copia un archivo de URI al directorio caché
     */
    private fun copyFileToCache(context: Context, uri: Uri, fileName: String): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val cacheFile = File(context.cacheDir, fileName)
            
            inputStream?.use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            cacheFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } ?: ""
    }

    /**
     * Crea una carpeta para archivos temporales
     */
    fun getOutputDirectory(context: Context): File {
        val mediaDir = File(context.cacheDir, "MKVSubtitles").apply {
            mkdirs()
        }
        return if (mediaDir.exists()) mediaDir else context.cacheDir
    }

    /**
     * Obtiene la URI para compartir un archivo
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Verifica si un archivo es un MKV válido
     */
    fun isValidMKVFile(filePath: String): Boolean {
        return filePath.lowercase().endsWith(".mkv")
    }

    /**
     * Obtiene el tamaño del archivo en MB
     */
    fun getFileSizeInMB(filePath: String): Double {
        val file = File(filePath)
        return if (file.exists()) {
            file.length() / (1024.0 * 1024.0)
        } else {
            0.0
        }
    }

    /**
     * Crea un archivo SRT en el directorio caché
     */
    fun createSubtitleFile(context: Context, content: String, fileName: String = "subtitles.srt"): File {
        val outputDir = getOutputDirectory(context)
        val outputFile = File(outputDir, fileName)
        outputFile.writeText(content, Charsets.UTF_8)
        return outputFile
    }

    /**
     * Lee el contenido de un archivo SRT
     */
    fun readSubtitleFile(filePath: String): String {
        return try {
            File(filePath).readText(Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Limpia archivos temporales
     */
    fun clearCacheDirectory(context: Context) {
        try {
            val cacheDir = getOutputDirectory(context)
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
