package com.example.mkvsubtitle.services

import android.content.Context
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
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
            val session = FFmpegKit.execute(command)
            val returnCode = session.returnCode

            if (ReturnCode.isSuccess(returnCode) && outputFile.exists()) {
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
                    error = "FFmpeg returned non-zero exit code: $rc"
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
