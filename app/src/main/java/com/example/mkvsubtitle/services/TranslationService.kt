package com.example.mkvsubtitle.services

import android.content.Context
import com.google.gson.Gson
import com.example.mkvsubtitle.models.TranslationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Servicio para traducción de subtítulos usando LibreTranslate
 */
class TranslationService(private val context: Context) {

    private val gson = Gson()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val LIBRETRANSLATE_URL = "https://libretranslate.com/translate"
        private const val RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    /**
     * Traduce un bloque de texto a un idioma objetivo
     */
    suspend fun translateText(text: String, targetLanguage: String): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext text

        val langCode = mapLanguageToCode(targetLanguage)

        repeat(RETRY_ATTEMPTS) { attempt ->
            try {
                val payload = mapOf(
                    "q" to text,
                    "source" to "auto",
                    "target" to langCode,
                    "format" to "text"
                )

                val requestBody = gson.toJson(payload)
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(LIBRETRANSLATE_URL)
                    .post(requestBody)
                    .build()

                val response = httpClient.newCall(request).execute()

                when {
                    response.isSuccessful -> {
                        val responseBody = response.body?.string() ?: return@withContext text
                        val result = gson.fromJson(responseBody, Map::class.java)
                        if (result.containsKey("translatedText")) {
                            return@withContext result["translatedText"].toString()
                        }
                    }
                    response.code == 429 -> {
                        // Rate limit - retry con delay
                        if (attempt < RETRY_ATTEMPTS - 1) {
                            Thread.sleep(RETRY_DELAY_MS * (attempt + 1))
                        }
                    }
                    else -> {
                        if (attempt < RETRY_ATTEMPTS - 1) {
                            Thread.sleep(RETRY_DELAY_MS)
                        }
                    }
                }
            } catch (e: Exception) {
                if (attempt < RETRY_ATTEMPTS - 1) {
                    Thread.sleep(RETRY_DELAY_MS)
                }
            }
        }

        return@withContext text
    }

    /**
     * Traduce contenido SRT completo
     */
    suspend fun translateSubtitleContent(srtContent: String, targetLanguage: String): TranslationResponse = withContext(Dispatchers.IO) {
        return@withContext try {
            val lines = srtContent.split("\n").toMutableList()
            val translatedLines = mutableListOf<String>()

            for (line in lines) {
                val stripped = line.trim()

                // Saltar líneas vacías, números y timestamps
                when {
                    stripped.isEmpty() -> translatedLines.add(line)
                    stripped[0].isDigit() -> translatedLines.add(line)
                    "-->" in line -> translatedLines.add(line)
                    else -> {
                        // Traducir línea de texto
                        val translated = try {
                            translateText(stripped, targetLanguage)
                        } catch (e: Exception) {
                            line
                        }
                        translatedLines.add(translated)
                        Thread.sleep(50) // Delay para evitar rate limiting
                    }
                }
            }

            TranslationResponse(
                success = true,
                message = "Subtítulos traducidos exitosamente",
                translated_content = translatedLines.joinToString("\n")
            )
        } catch (e: Exception) {
            TranslationResponse(
                success = false,
                message = "Error en la traducción: ${e.message}",
                error = e.message
            )
        }
    }

    /**
     * Mapea nombre de idioma a código ISO 639-1
     */
    private fun mapLanguageToCode(language: String): String {
        val languageMap = mapOf(
            "Spanish" to "es", "Español" to "es",
            "French" to "fr", "Francés" to "fr",
            "German" to "de", "Alemán" to "de",
            "Chinese (Simplified)" to "zh", "Chino (Simplificado)" to "zh",
            "Chinese (Traditional)" to "zh-TW", "Chino (Tradicional)" to "zh-TW",
            "Arabic" to "ar", "Árabe" to "ar",
            "Japanese" to "ja", "Japonés" to "ja",
            "Portuguese" to "pt", "Portugués" to "pt",
            "Russian" to "ru", "Ruso" to "ru",
            "Korean" to "ko", "Coreano" to "ko",
            "Italian" to "it", "Italiano" to "it",
            "Dutch" to "nl", "Holandés" to "nl",
            "Polish" to "pl", "Polaco" to "pl",
            "Turkish" to "tr", "Turco" to "tr",
            "Greek" to "el", "Griego" to "el",
            "Hungarian" to "hu", "Húngaro" to "hu",
            "Swedish" to "sv", "Sueco" to "sv",
            "Finnish" to "fi", "Finlandés" to "fi",
            "Norwegian" to "no", "Noruego" to "no",
            "Danish" to "da", "Danés" to "da",
            "Czech" to "cs", "Checo" to "cs",
            "Romanian" to "ro", "Rumano" to "ro",
            "Vietnamese" to "vi", "Vietnamita" to "vi",
            "Thai" to "th", "Tailandés" to "th",
            "Indonesian" to "id", "Indonesio" to "id"
        )
        return languageMap[language] ?: language.lowercase()
    }

    /**
     * Obtiene lista de idiomas soportados
     */
    fun getSupportedLanguages(): List<String> {
        return listOf(
            "Spanish", "French", "German", "Chinese (Simplified)", "Chinese (Traditional)",
            "Arabic", "Japanese", "Portuguese", "Russian", "Korean", "Italian",
            "Dutch", "Polish", "Turkish", "Greek", "Hungarian", "Swedish",
            "Finnish", "Norwegian", "Danish", "Czech", "Romanian", "Vietnamese",
            "Thai", "Indonesian"
        )
    }
}
