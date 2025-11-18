package com.example.mkvsubtitle

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mkvsubtitle.services.TranslationService
import com.example.mkvsubtitle.utils.FileUtils
import kotlinx.coroutines.launch

/**
 * Actividad para mostrar subtítulos
 */
class SubtitleDisplayActivity : AppCompatActivity() {

    private lateinit var subtitleTextView: TextView
    private lateinit var backButton: Button
    private lateinit var languageSpinner: Spinner
    private lateinit var translateButton: Button
    private lateinit var saveButton: Button

    private lateinit var translationService: TranslationService
    private var originalSubtitleContent: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subtitle_display)

        subtitleTextView = findViewById(R.id.subtitleTextView)
        backButton = findViewById(R.id.backButton)
        languageSpinner = findViewById(R.id.languageSpinner)
        translateButton = findViewById(R.id.translateButton)
        saveButton = findViewById(R.id.saveButton)

        translationService = TranslationService(this)

        setupLanguageSpinner()
        loadSubtitleFromIntent()

        backButton.setOnClickListener {
            finish()
        }

        translateButton.setOnClickListener {
            val targetLanguage = languageSpinner.selectedItem as? String ?: return@setOnClickListener
            translateSubtitles(targetLanguage)
        }

        saveButton.setOnClickListener {
            saveCurrentSubtitles()
        }
    }

    private fun setupLanguageSpinner() {
        val languages = translationService.getSupportedLanguages()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter
    }

    private fun loadSubtitleFromIntent() {
        val subtitlePath = intent.getStringExtra("subtitle_file_path")
        if (subtitlePath.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.no_subtitles), Toast.LENGTH_LONG).show()
            return
        }

        val content = FileUtils.readSubtitleFile(subtitlePath)
        if (content.isBlank()) {
            Toast.makeText(this, getString(R.string.no_subtitles), Toast.LENGTH_LONG).show()
        } else {
            originalSubtitleContent = content
            subtitleTextView.text = content
        }
    }

    private fun translateSubtitles(targetLanguage: String) {
        val currentContent = subtitleTextView.text.toString()
        if (currentContent.isBlank()) {
            Toast.makeText(this, getString(R.string.extract_first), Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(this, getString(R.string.translating), Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val response = translationService.translateSubtitleContent(currentContent, targetLanguage)
            if (response.success && response.translated_content.isNotBlank()) {
                subtitleTextView.text = response.translated_content
            } else {
                Toast.makeText(this@SubtitleDisplayActivity, response.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveCurrentSubtitles() {
        val content = subtitleTextView.text.toString()
        if (content.isBlank()) {
            Toast.makeText(this, getString(R.string.extract_first), Toast.LENGTH_LONG).show()
            return
        }

        val file = FileUtils.createSubtitleFile(this, content)
        Toast.makeText(this, getString(R.string.subtitles_saved) + "\n" + file.absolutePath, Toast.LENGTH_LONG).show()
    }
}
