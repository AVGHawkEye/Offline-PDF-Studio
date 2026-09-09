package com.example.model

import android.graphics.Bitmap

enum class PageNumberPosition(val label: String) {
    BOTTOM_CENTER("Bottom Center"),
    BOTTOM_RIGHT("Bottom Right"),
    BOTTOM_LEFT("Bottom Left"),
    TOP_CENTER("Top Center"),
    TOP_RIGHT("Top Right"),
    TOP_LEFT("Top Left")
}

enum class PageNumberFormat(val formatPattern: String, val label: String) {
    PAGE_X_OF_Y("Page {X} of {Y}", "Page 1 of 5"),
    X_OF_Y("{X} / {Y}", "1 / 5"),
    DASH_X_DASH("- {X} -", "- 1 -"),
    ONLY_X("{X}", "1")
}

data class WatermarkConfig(
    val text: String = "CONFIDENTIAL",
    val angle: Float = 45f,
    val opacity: Float = 0.25f,
    val fontSize: Float = 48f,
    val colorHex: Long = 0xFF888888
)

enum class RotateAngle(val degrees: Int, val label: String) {
    DEG_90(90, "+90° (Clockwise)"),
    DEG_180(180, "180° (Flip)"),
    DEG_270(270, "270° (-90° Counter-Clockwise)")
}

data class CropBounds(
    val leftRatio: Float = 0.05f,
    val topRatio: Float = 0.05f,
    val rightRatio: Float = 0.95f,
    val bottomRatio: Float = 0.95f
)

data class AcroFormField(
    val fullyQualifiedName: String,
    val partialName: String,
    val value: String = "",
    val isCheckbox: Boolean = false,
    val isChecked: Boolean = false
)

data class RedactionBox(
    val id: String,
    val pageIndex: Int,
    val leftRatio: Float,
    val topRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float
)

data class DrawPoint(val x: Float, val y: Float)

data class FreehandStroke(
    val points: List<DrawPoint>,
    val color: Long = 0xFFFF0000,
    val strokeWidth: Float = 4f,
    val isHighlight: Boolean = false
)

data class TextAnnotation(
    val text: String,
    val xRatio: Float,
    val yRatio: Float,
    val fontSize: Float = 16f,
    val color: Long = 0xFF000000
)

data class CompareDiffResult(
    val pageIndex: Int,
    val page1Bitmap: Bitmap?,
    val page2Bitmap: Bitmap?,
    val diffBitmap: Bitmap?,
    val differencePercentage: Float
)
