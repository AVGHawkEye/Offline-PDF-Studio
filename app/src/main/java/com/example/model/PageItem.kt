package com.example.model

import android.graphics.Bitmap
import java.util.UUID

data class PageItem(
    val id: String = UUID.randomUUID().toString(),
    val originalPageIndex: Int, // 0-based
    val displayPageNumber: Int = originalPageIndex + 1, // 1-based
    val rotationDegrees: Int = 0, // 0, 90, 180, 270
    val isDeleted: Boolean = false,
    val thumbnail: Bitmap? = null,
    val isSelected: Boolean = true
) {
    val originalIndex: Int get() = originalPageIndex
    val thumbnailBitmap: Bitmap? get() = thumbnail
}
