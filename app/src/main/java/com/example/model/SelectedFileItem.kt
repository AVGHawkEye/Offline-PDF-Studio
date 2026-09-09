package com.example.model

import android.graphics.Bitmap
import android.net.Uri

data class SelectedFileItem(
    val id: String,
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val pageCount: Int = 1,
    val thumbnail: Bitmap? = null,
    val isImage: Boolean = false
) {
    val displayName: String get() = name
}
