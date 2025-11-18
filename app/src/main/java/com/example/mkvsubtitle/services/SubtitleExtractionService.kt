package com.example.mkvsubtitle.services

import android.content.Context
// FFmpegKit imports are optional - if the dependency is disabled we use a local stub
// real imports are commented out when dependency is removed
// import com.arthenica.ffmpegkit.FFmpegKit
// import com.arthenica.ffmpegkit.FFmpegKitConfig
// import com.arthenica.ffmpegkit.ReturnCode
import com.example.mkvsubtitle.utils.FFmpegKitStub
import com.example.mkvsubtitle.models.ExtractionResponse
import com.example.mkvsubtitle.models.SubtitleTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Servicio para extracción de subtítulos
 */
class SubtitleExtractionService(private val context: Context) {
    init {
        // Inicializar FFmpegKit (no es necesario verificar compatibilidad explícitamente)
        // La verificación de compatibilidad se manejará internamente
    }

    /**
     * Extrae subtítulos de un archivo MKV de forma asíncrona
     */
suspend fun extractSubtitles(mkvFilePath: String, outputDir: String): ExtractionResponse = withContext(Dispatchers.IO) {
    return@withContext try {
        val inputFile = File(mkvFilePath)
        if (!inputFile.exists()) {
            ExtractionResponse(
                success = false,
                message = "MKV file not found",
                error = "File does not exist: $mkvFilePath"
            )
        } else {
            val outputDirectory = File(outputDir).apply { mkdirs() }
            val outputFile = File(outputDirectory, "subtitle_0.srt")

            val command = "-y -i \"${inputFile.absolutePath}\" -map 0:s:0 -c:s srt \"${outputFile.absolutePath}\""
            val session: Any? = try {
                // Try real FFmpegKit via reflection (if available)
                val ffmpegClass = Class.forName("com.arthenica.ffmpegkit.FFmpegKit")
                val method = ffmpegClass.getMethod("execute", String::class.java)
                method.invoke(null, command)
            } catch (e: Throwable) {
                // Fall back to stub
                FFmpegKitStub.execute(command)
            }

            val returnCode: Int? = try {
                val getReturnCodeMethod = session?.javaClass?.getMethod("getReturnCode")
                val rcObj = getReturnCodeMethod?.invoke(session)
                when (rcObj) {
                    is Int -> rcObj
                    else -> {
                        rcObj?.let {
                            try {
                                val getValue = it.javaClass.getMethod("getValue")
                                getValue.invoke(it) as? Int
                            } catch (e: Throwable) {
                                null
                            }
                        } ?: (session as? FFmpegKitStub.Session)?.returnCode
                    }
                }
            } catch (e: Throwable) {
                (session as? FFmpegKitStub.Session)?.returnCode
            }

            val success: Boolean = try {
                val rcClass = Class.forName("com.arthenica.ffmpegkit.ReturnCode")
                val isSuccess = rcClass.getMethod("isSuccess", java.lang.Integer::class.java)
                val res = isSuccess.invoke(null, returnCode ?: -1)
                (res as? Boolean) ?: false
            } catch (e: Throwable) {
                FFmpegKitStub.ReturnCode.isSuccess(returnCode)
            }

            if (success && outputFile.exists()) {
                val track = SubtitleTrack(
                    index = 0,
                    language = "unknown",
                    codec = "srt",
                    title = "Subtitle 0",
                    format = "srt",
                    filePath = outputFile.absolutePath,
                    size = outputFile.length()
                )

                ExtractionResponse(
                    success = true,
                    message = "Successfully extracted 1 subtitle track",
                    subtitles = listOf(track)
                )
            } else {
                    ExtractionResponse(
                        success = false,
                        message = "Failed to extract subtitles",
                        error = "FFmpeg returned non-zero exit code: $returnCode"
                    )
            }
        }
    } catch (e: Exception) {
        ExtractionResponse(
            success = false,
            message = "Error extracting subtitles",
            error = e.message ?: "Unknown error"
        )
    }
}

    /**
     * Obtiene información de subtítulos disponibles en un MKV
     */
    suspend fun getSubtitleInfo(mkvFilePath: String): Map<String, Any> = withContext(Dispatchers.IO) {
        val file = File(mkvFilePath)
        return@withContext mapOf(
            "file" to mkvFilePath,
            "exists" to file.exists()
        )
    }
}
