package com.example.mkvsubtitle.models

/**
 * Data class para representar un subtítulo individual
 */
data class Subtitle(
    val sequence: Int,
    val startTime: String,
    val endTime: String,
    val text: String
)

/**
 * Data class para información de pista de subtítulos extraída
 */
data class SubtitleTrack(
    val index: Int,
    val language: String,
    val codec: String,
    val title: String,
    val format: String,
    val filePath: String,
    val size: Long
)

/**
 * Data class para respuesta de traducción
 */
data class TranslationResponse(
    val success: Boolean,
    val message: String,
    val translated_content: String = "",
    val error: String? = null
)

/**
 * Data class para respuesta de extracción
 */
data class ExtractionResponse(
    val success: Boolean,
    val message: String,
    val subtitles: List<SubtitleTrack> = emptyList(),
    val error: String? = null
)

/**
 * Data class para entrada de archivo
 */
data class FileSelection(
    val filePath: String,
    val fileName: String,
    val size: Long
)
