package com.example.model

import android.net.Uri
import java.io.File

data class ProcessResult(
    val success: Boolean,
    val outputFile: File? = null,
    val outputFiles: List<File>? = null,
    val outputUri: Uri? = null,
    val mimeType: String = "application/pdf",
    val originalSizeBytes: Long = 0,
    val outputSizeBytes: Long = 0,
    val pageCount: Int = 0,
    val extractedText: String? = null,
    val message: String = "",
    val error: String? = null
)
