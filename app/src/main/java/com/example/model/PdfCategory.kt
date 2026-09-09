package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Transform
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueInfo
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.PurpleAccent

enum class PdfCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    ORGANIZE(
        title = "Organize PDF",
        subtitle = "Merge, split, remove, extract, reorder, and camera scan",
        icon = Icons.Default.FolderZip,
        accentColor = CrimsonPrimary
    ),
    OPTIMIZE(
        title = "Optimize PDF",
        subtitle = "Compress size, repair corrupt streams, and local OCR",
        icon = Icons.Default.Speed,
        accentColor = IndigoSecondary
    ),
    CONVERT_TO(
        title = "Convert to PDF",
        subtitle = "JPG, Word, PowerPoint, Excel, and HTML to PDF",
        icon = Icons.Default.PictureAsPdf,
        accentColor = EmeraldTertiary
    ),
    CONVERT_FROM(
        title = "Convert from PDF",
        subtitle = "PDF to JPG, Word, PowerPoint, Excel, and PDF/A",
        icon = Icons.Default.Transform,
        accentColor = AmberWarning
    ),
    EDIT(
        title = "Edit PDF",
        subtitle = "Rotate, page numbers, watermark, crop, annotate, and forms",
        icon = Icons.Default.Edit,
        accentColor = PurpleAccent
    ),
    SECURITY(
        title = "PDF Security",
        subtitle = "Protect, unlock, digital signature, redact, and compare",
        icon = Icons.Default.Security,
        accentColor = BlueInfo
    )
}
