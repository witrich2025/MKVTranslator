package com.example.mkvsubtitle.utils

import java.io.File

// Light-weight stub used when FFmpegKit dependency is disabled (CI temporary)
object FFmpegKitStub {
    data class Session(val returnCode: Int)
    object ReturnCode {
        fun isSuccess(code: Int?) = false
    }

    fun execute(command: String): Session {
        // Create a dummy session; caller should handle missing output files
        return Session(returnCode = -1)
    }
}
