package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BlueInfo
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.PurpleAccent

enum class PdfTool(
    val title: String,
    val description: String,
    val category: PdfCategory,
    val icon: ImageVector,
    val accentColor: Color,
    val allowsMultipleFiles: Boolean = false,
    val acceptsImages: Boolean = false,
    val acceptedMimeTypes: Array<String> = arrayOf("application/pdf"),
    val badgeText: String
) {
    // ==========================================
    // 1. ORGANIZE PDF
    // ==========================================
    MERGE(
        title = "Merge PDF",
        description = "Select and combine multiple PDF files into one local document stream",
        category = PdfCategory.ORGANIZE,
        icon = Icons.Default.MergeType,
        accentColor = CrimsonPrimary,
        allowsMultipleFiles = true,
        badgeText = "Combine"
    ),
    SPLIT(
        title = "Split PDF",
        description = "Extract specific page ranges (e.g., '1-3, 5') into a new PDF",
        category = PdfCategory.ORGANIZE,
        icon = Icons.Default.CallSplit,
        accentColor = CrimsonPrimary,
        badgeText = "Range Extractor"
    ),
    REMOVE_PAGES(
        title = "Remove Pages",
        description = "Render thumbnail grid of pages allowing user to multi-select and delete pages",
        category = PdfCategory.ORGANIZE,
        icon = Icons.Default.GridOff,
        accentColor = CrimsonPrimary,
        badgeText = "Delete Pages"
    ),
    EXTRACT_PAGES(
        title = "Extract Pages",
        description = "Select specific pages from a document and export them as a standalone PDF",
        category = PdfCategory.ORGANIZE,
        icon = Icons.Default.AutoFixHigh,
        accentColor = CrimsonPrimary,
        badgeText = "Isolate Pages"
    ),
    ORGANIZE(
        title = "Organize PDF",
        description = "Full drag-and-drop thumbnail grid to reorder, swap, or rotate any page index",
        category = PdfCategory.ORGANIZE,
        icon = Icons.Default.GridView,
        accentColor = CrimsonPrimary,
        badgeText = "Visual Grid"
    ),
    SCAN_TO_PDF(
        title = "Scan to PDF",
        description = "Access native device camera, capture photo documents, auto-crop, and package as a PDF",
        category = PdfCategory.ORGANIZE,
        icon = Icons.Default.CameraAlt,
        accentColor = CrimsonPrimary,
        acceptsImages = true,
        allowsMultipleFiles = true,
        acceptedMimeTypes = arrayOf("image/*"),
        badgeText = "Camera Scanner"
    ),

    // ==========================================
    // 2. OPTIMIZE PDF
    // ==========================================
    COMPRESS(
        title = "Compress PDF",
        description = "Downsample internal images and compress PDF object streams to reduce file size",
        category = PdfCategory.OPTIMIZE,
        icon = Icons.Default.Compress,
        accentColor = IndigoSecondary,
        badgeText = "Size Optimizer"
    ),
    REPAIR(
        title = "Repair PDF",
        description = "Parse and reconstruct corrupt or damaged PDF cross-reference tables locally",
        category = PdfCategory.OPTIMIZE,
        icon = Icons.Default.Build,
        accentColor = IndigoSecondary,
        badgeText = "Auto Rebuild"
    ),
    OCR_PDF(
        title = "OCR PDF",
        description = "Perform local text recognition on scanned PDF pages to export plain text or searchable PDF overlays",
        category = PdfCategory.OPTIMIZE,
        icon = Icons.Default.TextFields,
        accentColor = IndigoSecondary,
        badgeText = "On-Device ML"
    ),

    // ==========================================
    // 3. CONVERT TO PDF (Input File -> Output PDF)
    // ==========================================
    JPG_TO_PDF(
        title = "JPG to PDF",
        description = "Multi-select gallery images (JPG/PNG) and wrap them into a single PDF document",
        category = PdfCategory.CONVERT_TO,
        icon = Icons.Default.Image,
        accentColor = EmeraldTertiary,
        allowsMultipleFiles = true,
        acceptsImages = true,
        acceptedMimeTypes = arrayOf("image/*"),
        badgeText = "Images to PDF"
    ),
    WORD_TO_PDF(
        title = "WORD to PDF",
        description = "Parse .docx files locally using Apache POI/OpenXML and render text into a PDF Canvas",
        category = PdfCategory.CONVERT_TO,
        icon = Icons.Default.Description,
        accentColor = EmeraldTertiary,
        acceptedMimeTypes = arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword", "*/*"),
        badgeText = ".docx to PDF"
    ),
    PPT_TO_PDF(
        title = "POWERPOINT to PDF",
        description = "Render .pptx slide layouts into PDF pages",
        category = PdfCategory.CONVERT_TO,
        icon = Icons.Default.Slideshow,
        accentColor = EmeraldTertiary,
        acceptedMimeTypes = arrayOf("application/vnd.openxmlformats-officedocument.presentationml.presentation", "*/*"),
        badgeText = ".pptx to PDF"
    ),
    EXCEL_TO_PDF(
        title = "EXCEL to PDF",
        description = "Read .xlsx sheets/tables and format them into printable PDF page layouts",
        category = PdfCategory.CONVERT_TO,
        icon = Icons.Default.TableChart,
        accentColor = EmeraldTertiary,
        acceptedMimeTypes = arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "*/*"),
        badgeText = ".xlsx to PDF"
    ),
    HTML_TO_PDF(
        title = "HTML to PDF",
        description = "Load HTML string or file into an offline Android renderer and compile to formatted PDF",
        category = PdfCategory.CONVERT_TO,
        icon = Icons.Default.Html,
        accentColor = EmeraldTertiary,
        acceptedMimeTypes = arrayOf("text/html", "text/plain", "*/*"),
        badgeText = "HTML to PDF"
    ),

    // ==========================================
    // 4. CONVERT FROM PDF (Input PDF -> Output File)
    // ==========================================
    PDF_TO_JPG(
        title = "PDF to JPG",
        description = "Render every page of a PDF as high-resolution JPEG/PNG images saved to storage",
        category = PdfCategory.CONVERT_FROM,
        icon = Icons.Default.PhotoLibrary,
        accentColor = AmberWarning,
        badgeText = "Export Images"
    ),
    PDF_TO_WORD(
        title = "PDF to WORD",
        description = "Extract text content, headers, and formatting from a PDF into an editable .docx file",
        category = PdfCategory.CONVERT_FROM,
        icon = Icons.Default.Description,
        accentColor = AmberWarning,
        badgeText = "PDF to .docx"
    ),
    PDF_TO_PPT(
        title = "PDF to POWERPOINT",
        description = "Extract PDF pages as images and assemble them as slides in a .pptx file",
        category = PdfCategory.CONVERT_FROM,
        icon = Icons.Default.Slideshow,
        accentColor = AmberWarning,
        badgeText = "PDF to .pptx"
    ),
    PDF_TO_EXCEL(
        title = "PDF to EXCEL",
        description = "Detect tabular data structures inside PDF text layers and export to an .xlsx spreadsheet",
        category = PdfCategory.CONVERT_FROM,
        icon = Icons.Default.TableChart,
        accentColor = AmberWarning,
        badgeText = "PDF to .xlsx"
    ),
    PDF_TO_PDFA(
        title = "PDF to PDF/A",
        description = "Convert standard PDF streams to ISO-compliant archival PDF/A format (embedding fonts & metadata)",
        category = PdfCategory.CONVERT_FROM,
        icon = Icons.Default.Policy,
        accentColor = AmberWarning,
        badgeText = "Archival PDF/A"
    ),

    // ==========================================
    // 5. EDIT PDF
    // ==========================================
    ROTATE(
        title = "Rotate PDF",
        description = "Batch rotate selected or all PDF pages by 90, 180, or 270 degrees",
        category = PdfCategory.EDIT,
        icon = Icons.Default.RotateRight,
        accentColor = PurpleAccent,
        badgeText = "Batch Rotate"
    ),
    PAGE_NUMBERS(
        title = "Add Page Numbers",
        description = "Overlay customizable page numbers (position, font size, format like 'Page X of Y') onto PDF headers/footers",
        category = PdfCategory.EDIT,
        icon = Icons.Default.FormatListNumbered,
        accentColor = PurpleAccent,
        badgeText = "Page Numbers"
    ),
    WATERMARK(
        title = "Add Watermark",
        description = "Stamp custom text or image logos with transparency over PDF pages",
        category = PdfCategory.EDIT,
        icon = Icons.Default.WaterDrop,
        accentColor = PurpleAccent,
        badgeText = "Stamp Text"
    ),
    CROP(
        title = "Crop PDF",
        description = "Interactively drag crop bounds over a page preview to trim PDF margins and page dimensions",
        category = PdfCategory.EDIT,
        icon = Icons.Default.Crop,
        accentColor = PurpleAccent,
        badgeText = "Trim Margins"
    ),
    EDIT_PDF(
        title = "Edit PDF",
        description = "Interactive Canvas mode to draw freehand, add text overlays, highlight text, and place shapes",
        category = PdfCategory.EDIT,
        icon = Icons.Default.Draw,
        accentColor = PurpleAccent,
        badgeText = "Draw & Annotate"
    ),
    PDF_FORMS(
        title = "PDF Forms",
        description = "Fill out interactive PDF form fields (text inputs, checkboxes, radio buttons) and save flattened document",
        category = PdfCategory.EDIT,
        icon = Icons.Default.DynamicForm,
        accentColor = PurpleAccent,
        badgeText = "AcroForms"
    ),

    // ==========================================
    // 6. PDF SECURITY
    // ==========================================
    UNLOCK(
        title = "Unlock PDF",
        description = "Remove password protection from known-password protected PDFs locally",
        category = PdfCategory.SECURITY,
        icon = Icons.Default.LockOpen,
        accentColor = BlueInfo,
        badgeText = "Decrypt"
    ),
    PROTECT(
        title = "Protect PDF",
        description = "Apply AES-128 / AES-256 password encryption to lock opening or editing permissions",
        category = PdfCategory.SECURITY,
        icon = Icons.Default.Lock,
        accentColor = BlueInfo,
        badgeText = "AES-128 Lock"
    ),
    SIGN(
        title = "Sign PDF",
        description = "Draw a digital signature on a canvas layer and embed it into the PDF document stream",
        category = PdfCategory.SECURITY,
        icon = Icons.Default.Edit,
        accentColor = BlueInfo,
        badgeText = "Digital Signature"
    ),
    REDACT(
        title = "Redact PDF",
        description = "Draw black mask rectangles over sensitive text/images to permanently strip data from the PDF file",
        category = PdfCategory.SECURITY,
        icon = Icons.Default.VisibilityOff,
        accentColor = BlueInfo,
        badgeText = "Blackout Mask"
    ),
    COMPARE(
        title = "Compare PDF",
        description = "Load two PDF files side-by-side, highlight differences, and render a visual diff view",
        category = PdfCategory.SECURITY,
        icon = Icons.Default.Compare,
        accentColor = BlueInfo,
        allowsMultipleFiles = true,
        badgeText = "Visual Diff"
    )
}
