package com.example.mkvsubtitle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import com.example.mkvsubtitle.services.SubtitleExtractionService
import com.example.mkvsubtitle.utils.FileUtils
import kotlinx.coroutines.launch

/**
 * Actividad principal para seleccionar archivos MKV
 */
class MainActivity : AppCompatActivity() {

    private lateinit var selectFileButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var progressBar: ProgressBar

    private var selectedFilePath: String? = null

    private lateinit var subtitleExtractionService: SubtitleExtractionService

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = FileUtils.getPath(this, uri)
            if (path.isNullOrEmpty()) {
                statusTextView.text = getString(R.string.extraction_failed)
                Toast.makeText(this, getString(R.string.select_mkv_file), Toast.LENGTH_LONG).show()
            } else if (!FileUtils.isValidMKVFile(path)) {
                statusTextView.text = getString(R.string.select_mkv_file)
                Toast.makeText(this, "Selected file is not a MKV", Toast.LENGTH_LONG).show()
            } else {
                selectedFilePath = path
                statusTextView.text = "File selected: ${FileUtils.getFileName(this, uri) ?: path}"
                Toast.makeText(this, "File: ${FileUtils.getFileName(this, uri) ?: uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
                startSubtitleExtraction()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        subtitleExtractionService = SubtitleExtractionService(this)

        selectFileButton = findViewById(R.id.selectFileButton)
        statusTextView = findViewById(R.id.statusTextView)
        progressBar = findViewById(R.id.progressBar)

        selectFileButton.setOnClickListener {
            requestPermissionsAndPickFile()
        }
    }

    private fun requestPermissionsAndPickFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            } else {
                pickFile()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                pickFile()
            }
        }
    }

    private fun startSubtitleExtraction() {
        val mkvPath = selectedFilePath
        if (mkvPath.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.select_file_first), Toast.LENGTH_LONG).show()
            return
        }

        val outputDir = FileUtils.getOutputDirectory(this).absolutePath
        progressBar.visibility = View.VISIBLE
        statusTextView.text = getString(R.string.extracting)

        lifecycleScope.launch {
            val result = subtitleExtractionService.extractSubtitles(mkvPath, outputDir)
            progressBar.visibility = View.GONE

            if (result.success && result.subtitles.isNotEmpty()) {
                val firstTrack = result.subtitles.first()
                statusTextView.text = getString(R.string.subtitles_extracted)

                val intent = Intent(this@MainActivity, SubtitleDisplayActivity::class.java).apply {
                    putExtra("subtitle_file_path", firstTrack.filePath)
                }
                startActivity(intent)
            } else {
                statusTextView.text = getString(R.string.extraction_failed)
                Toast.makeText(this@MainActivity, result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pickFile() {
        filePickerLauncher.launch("*/*")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickFile()
        }
    }
}
