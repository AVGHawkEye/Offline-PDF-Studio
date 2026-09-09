package com.example.model

enum class CompressionLevel(
    val label: String,
    val description: String,
    val jpegQuality: Int,
    val dpi: Int
) {
    LOW("Light", "Minimal reduction, maximum crispness", 85, 150),
    MEDIUM("Balanced", "Recommended: good reduction and clear text", 65, 120),
    HIGH("Maximum", "Strongest reduction, ideal for sharing/email", 45, 90)
}
