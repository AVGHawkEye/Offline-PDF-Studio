package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompressionLevel
import com.example.model.PdfCategory
import com.example.model.PdfTool
import com.example.model.ProtectMode
import com.example.service.StorageService
import com.example.ui.components.CompareViewerControl
import com.example.ui.components.CompressionControlCard
import com.example.ui.components.CropPdfControl
import com.example.ui.components.ExtractPagesControl
import com.example.ui.components.FileSelectionList
import com.example.ui.components.HtmlEditorControl
import com.example.ui.components.InteractivePdfCanvasPreview
import com.example.ui.components.MergeInstructionsCard
import com.example.ui.components.OfficeConversionPreviewControl
import com.example.ui.components.PageNumbersControl
import com.example.ui.components.PageThumbnailCard
import com.example.ui.components.PdfFormsControl
import com.example.ui.components.QuickActionResultDialog
import com.example.ui.components.RedactionToolControl
import com.example.ui.components.RemovePagesControl
import com.example.ui.components.RepairPdfControl
import com.example.ui.components.RotatePdfControl
import com.example.ui.components.SignaturePadControl
import com.example.ui.components.SplitControlCard
import com.example.ui.components.WatermarkControl
import com.example.viewmodel.PdfStudioViewModel
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkspaceScreen(
    tool: PdfTool,
    uiState: UiState,
    viewModel: PdfStudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // SAF Document Picker Launchers
    val pdfMultiPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.handleSelectedUris(uris)
    }

    val pdfSinglePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) viewModel.handleSelectedUris(listOf(uri))
    }

    val imageMultiPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) viewModel.handleSelectedUris(uris)
    }

    val anyFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) viewModel.handleSelectedUris(listOf(uri))
    }

    // SAF Save Target Document Launcher
    val saveOutputLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            uiState.lastResult?.mimeType ?: "application/pdf"
        )
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.saveResultToSaf(uri)
            Toast.makeText(context, "Saved successfully!", Toast.LENGTH_LONG).show()
        }
    }

    var showPassword by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("workspace_screen"),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("btn_workspace_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to dashboard"
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(tool.accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = tool.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tool.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = tool.accentColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = tool.badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = tool.accentColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Category: ${tool.category.title} • 100% Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val canExecute = uiState.selectedFiles.isNotEmpty() || tool == PdfTool.HTML_TO_PDF
            if (canExecute && !uiState.isProcessing) {
                val actionLabel = when (tool) {
                    PdfTool.MERGE -> "Merge ${uiState.selectedFiles.size} PDFs Now"
                    PdfTool.SPLIT -> "Extract Split Pages"
                    PdfTool.REMOVE_PAGES -> "Remove ${uiState.selectedPagesToRemove.size} Selected Pages"
                    PdfTool.EXTRACT_PAGES -> "Extract ${uiState.selectedPagesToExtract.size} Pages"
                    PdfTool.ORGANIZE -> "Save Reorganized PDF"
                    PdfTool.SCAN_TO_PDF -> "Package ${uiState.selectedFiles.size} Scanned Pages"
                    PdfTool.COMPRESS -> "Compress PDF"
                    PdfTool.REPAIR -> "Repair Corrupt Streams"
                    PdfTool.OCR_PDF -> "Run On-Device OCR"
                    PdfTool.JPG_TO_PDF -> "Compile Images into PDF"
                    PdfTool.WORD_TO_PDF -> "Convert DOCX to PDF"
                    PdfTool.PPT_TO_PDF -> "Convert PPTX to PDF"
                    PdfTool.EXCEL_TO_PDF -> "Convert XLSX to PDF"
                    PdfTool.HTML_TO_PDF -> "Compile HTML to PDF"
                    PdfTool.PDF_TO_JPG -> "Export High-Res Images"
                    PdfTool.PDF_TO_WORD -> "Export to Word (.docx)"
                    PdfTool.PDF_TO_PPT -> "Export to PowerPoint (.pptx)"
                    PdfTool.PDF_TO_EXCEL -> "Export to Excel (.xlsx)"
                    PdfTool.PDF_TO_PDFA -> "Convert to PDF/A"
                    PdfTool.ROTATE -> "Apply ${uiState.rotateAngle.label} Rotation"
                    PdfTool.PAGE_NUMBERS -> "Stamp Page Numbers"
                    PdfTool.WATERMARK -> "Stamp Watermark"
                    PdfTool.CROP -> "Crop Document Margins"
                    PdfTool.EDIT_PDF -> "Burn Annotations into PDF"
                    PdfTool.PDF_FORMS -> "Save Filled Form Fields"
                    PdfTool.UNLOCK -> "Unlock & Decrypt PDF"
                    PdfTool.PROTECT -> "Encrypt with AES-128"
                    PdfTool.SIGN -> "Apply Digital Signature"
                    PdfTool.REDACT -> "Burn ${uiState.redactionBoxes.size} Redaction Masks"
                    PdfTool.COMPARE -> "Compare Visual Pages"
                }

                ExtendedFloatingActionButton(
                    onClick = { viewModel.executeProcessing() },
                    icon = { Icon(tool.icon, contentDescription = null) },
                    text = { Text(actionLabel, fontWeight = FontWeight.Bold) },
                    containerColor = tool.accentColor,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("btn_execute_action")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Processing Progress Bar Banner
            AnimatedVisibility(visible = uiState.isProcessing) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.progressMessage.ifBlank { "Executing offline operation..." },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${(uiState.progressValue * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uiState.progressValue },
                            modifier = Modifier.fillMaxWidth().testTag("progress_indicator_bar"),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Error Alert Banner
            if (uiState.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Check, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // Main Content Scrollable Area
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. SAF File Picker Trigger
                item {
                    val hasFiles = uiState.selectedFiles.isNotEmpty()
                    val buttonText = when {
                        !hasFiles && tool.acceptsImages -> "Select Images from Storage / Gallery"
                        !hasFiles && tool.allowsMultipleFiles -> "Select 2+ PDF Files from Storage"
                        !hasFiles && (tool == PdfTool.WORD_TO_PDF || tool == PdfTool.PPT_TO_PDF || tool == PdfTool.EXCEL_TO_PDF) -> "Select Office File (.docx, .pptx, .xlsx)"
                        !hasFiles -> "Select PDF Document from Storage"
                        tool.allowsMultipleFiles -> "Add More Files"
                        else -> "Change Selected Document"
                    }

                    OutlinedButton(
                        onClick = {
                            when {
                                tool.acceptsImages -> imageMultiPicker.launch(arrayOf("image/*"))
                                tool == PdfTool.WORD_TO_PDF || tool == PdfTool.PPT_TO_PDF || tool == PdfTool.EXCEL_TO_PDF -> anyFilePicker.launch(arrayOf("*/*"))
                                tool.allowsMultipleFiles -> pdfMultiPicker.launch(arrayOf("application/pdf"))
                                else -> pdfSinglePicker.launch(arrayOf("application/pdf"))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_pick_files"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Icon(
                            imageVector = if (hasFiles && tool.allowsMultipleFiles) Icons.Default.Add else Icons.Default.FileOpen,
                            contentDescription = null,
                            tint = tool.accentColor
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = buttonText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 2. Selected Files List Card (except when in Organize which uses thumbnails)
                if (uiState.selectedFiles.isNotEmpty() && tool != PdfTool.ORGANIZE) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Selected ${if (tool.acceptsImages) "Images" else "Documents"} (${uiState.selectedFiles.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                if (!tool.acceptsImages && tool != PdfTool.COMPARE) {
                                    OutlinedButton(
                                        onClick = { viewModel.toggleCanvasPreview() },
                                        modifier = Modifier.testTag("btn_toggle_canvas_preview")
                                    ) {
                                        Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (uiState.showCanvasPreview) "Hide Preview" else "Canvas Preview", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FileSelectionList(
                                files = uiState.selectedFiles,
                                onRemove = { idx -> viewModel.removeSelectedFile(idx) },
                                onMoveUp = { idx -> viewModel.moveFileUp(idx) },
                                onMoveDown = { idx -> viewModel.moveFileDown(idx) },
                                allowReordering = tool.allowsMultipleFiles
                            )
                        }
                    }
                }

                // 3. Interactive Canvas Preview Layer
                if (uiState.showCanvasPreview && uiState.selectedFiles.isNotEmpty()) {
                    item {
                        InteractivePdfCanvasPreview(
                            bitmap = uiState.activePreviewBitmap,
                            currentPageIndex = uiState.activePreviewPageIndex,
                            totalPages = uiState.totalPagesInPreview,
                            onPageChange = { newIdx -> viewModel.loadCanvasPreview(newIdx) },
                            onClose = { viewModel.toggleCanvasPreview() }
                        )
                    }
                }

                // 4. HTML Editor (can be used before or after selecting files)
                if (tool == PdfTool.HTML_TO_PDF) {
                    item {
                        HtmlEditorControl(
                            htmlText = uiState.htmlInputText,
                            onHtmlChange = { viewModel.setHtmlInputText(it) }
                        )
                    }
                }

                // 5. Dynamic Tool-Specific Options
                if (uiState.selectedFiles.isNotEmpty()) {
                    when (tool) {
                        PdfTool.MERGE -> {
                            item { MergeInstructionsCard(count = uiState.selectedFiles.size) }
                        }

                        PdfTool.SPLIT -> {
                            item {
                                SplitControlCard(
                                    pageCount = uiState.totalPagesInPreview,
                                    rangeInput = uiState.splitRangeInput,
                                    onRangeChange = { viewModel.setSplitRange(it) }
                                )
                            }
                        }

                        PdfTool.REMOVE_PAGES -> {
                            item {
                                RemovePagesControl(
                                    pageItems = uiState.pageItems,
                                    selectedPagesToRemove = uiState.selectedPagesToRemove,
                                    onTogglePage = { viewModel.togglePageToRemove(it) }
                                )
                            }
                        }

                        PdfTool.EXTRACT_PAGES -> {
                            item {
                                ExtractPagesControl(
                                    pageItems = uiState.pageItems,
                                    selectedPagesToExtract = uiState.selectedPagesToExtract,
                                    onTogglePage = { viewModel.togglePageToExtract(it) }
                                )
                            }
                        }

                        PdfTool.ORGANIZE -> {
                            item {
                                Text(
                                    text = "Reorder, Rotate, or Exclude Pages",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap arrows to swap pages or rotation icons to rotate 90° clockwise.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 130.dp),
                                    modifier = Modifier.fillMaxWidth().height(460.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    itemsIndexed(uiState.pageItems) { index, page ->
                                        PageThumbnailCard(
                                            page = page,
                                            currentIndex = index,
                                            totalCount = uiState.pageItems.size,
                                            onRotateLeft = { viewModel.rotatePage(page.id) },
                                            onRotateRight = { viewModel.rotatePage(page.id) },
                                            onMoveLeft = { if (index > 0) viewModel.movePageInOrganize(index, index - 1) },
                                            onMoveRight = { if (index < uiState.pageItems.size - 1) viewModel.movePageInOrganize(index, index + 1) },
                                            onToggleDelete = { viewModel.deletePageFromOrganize(page.id) }
                                        )
                                    }
                                }
                            }
                        }

                        PdfTool.COMPRESS -> {
                            item {
                                CompressionControlCard(
                                    selectedLevel = uiState.compressionLevel,
                                    onSelectLevel = { viewModel.setCompressionLevel(it) },
                                    originalSize = uiState.selectedFiles.firstOrNull()?.sizeBytes ?: 0L
                                )
                            }
                        }

                        PdfTool.REPAIR -> {
                            item { RepairPdfControl() }
                        }

                        PdfTool.OCR_PDF -> {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("ocr_control_card"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("On-Device ML Text Recognition", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text("Runs Google ML Kit Text Recognition client-side", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Switch(
                                                checked = uiState.useOcrScan,
                                                onCheckedChange = { viewModel.setUseOcrScan(it) }
                                            )
                                        }

                                        if (uiState.extractedOcrText.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Extracted Text Results", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        clipboard.setPrimaryClip(ClipData.newPlainText("PDF Text", uiState.extractedOcrText))
                                                        Toast.makeText(context, "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy text")
                                                }
                                            }
                                            OutlinedTextField(
                                                value = uiState.extractedOcrText,
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        PdfTool.WORD_TO_PDF, PdfTool.PPT_TO_PDF, PdfTool.EXCEL_TO_PDF,
                        PdfTool.PDF_TO_WORD, PdfTool.PDF_TO_PPT, PdfTool.PDF_TO_EXCEL,
                        PdfTool.PDF_TO_PDFA -> {
                            item {
                                OfficeConversionPreviewControl(
                                    tool = tool,
                                    fileName = uiState.selectedFiles.firstOrNull()?.displayName ?: ""
                                )
                            }
                        }

                        PdfTool.ROTATE -> {
                            item {
                                RotatePdfControl(
                                    selectedAngle = uiState.rotateAngle,
                                    onSelectAngle = { viewModel.setRotateAngle(it) },
                                    rotateAll = uiState.rotateAllPages,
                                    onToggleRotateAll = { viewModel.setRotateAllPages(it) }
                                )
                            }
                        }

                        PdfTool.PAGE_NUMBERS -> {
                            item {
                                PageNumbersControl(
                                    position = uiState.pageNumberPosition,
                                    onPositionChange = { viewModel.setPageNumberPosition(it) },
                                    format = uiState.pageNumberFormat,
                                    onFormatChange = { viewModel.setPageNumberFormat(it) },
                                    fontSize = uiState.pageNumberFontSize,
                                    onFontSizeChange = { viewModel.setPageNumberFontSize(it) }
                                )
                            }
                        }

                        PdfTool.WATERMARK -> {
                            item {
                                WatermarkControl(
                                    text = uiState.watermarkText,
                                    onTextChange = { viewModel.setWatermarkText(it) },
                                    opacity = uiState.watermarkOpacity,
                                    onOpacityChange = { viewModel.setWatermarkOpacity(it) },
                                    angle = uiState.watermarkAngle,
                                    onAngleChange = { viewModel.setWatermarkAngle(it) },
                                    fontSize = uiState.watermarkFontSize,
                                    onFontSizeChange = { viewModel.setWatermarkFontSize(it) }
                                )
                            }
                        }

                        PdfTool.CROP -> {
                            item {
                                CropPdfControl(
                                    cropBounds = uiState.cropBounds,
                                    onCropChange = { viewModel.setCropBounds(it) }
                                )
                            }
                        }

                        PdfTool.PDF_FORMS -> {
                            item {
                                PdfFormsControl(
                                    fields = uiState.acroFormFields,
                                    fieldValues = uiState.acroFormFieldValues,
                                    onFieldValueChange = { k, v -> viewModel.updateFormFieldValue(k, v) },
                                    flatten = uiState.flattenForm,
                                    onFlattenChange = { viewModel.setFlattenForm(it) }
                                )
                            }
                        }

                        PdfTool.UNLOCK -> {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Document Decryption Password", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedTextField(
                                            value = uiState.passwordInput,
                                            onValueChange = { viewModel.setPassword(it) },
                                            label = { Text("Password") },
                                            singleLine = true,
                                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                            trailingIcon = {
                                                IconButton(onClick = { showPassword = !showPassword }) {
                                                    Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        PdfTool.PROTECT -> {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Encrypt PDF with AES-128", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedTextField(
                                            value = uiState.passwordInput,
                                            onValueChange = { viewModel.setPassword(it) },
                                            label = { Text("Enter New Password") },
                                            singleLine = true,
                                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedTextField(
                                            value = uiState.passwordConfirmInput,
                                            onValueChange = { viewModel.setPasswordConfirm(it) },
                                            label = { Text("Confirm Password") },
                                            singleLine = true,
                                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        PdfTool.SIGN -> {
                            item {
                                SignaturePadControl(
                                    onSignatureCaptured = { viewModel.setSignatureBitmap(it) },
                                    onClearSignature = { viewModel.setSignatureBitmap(null) }
                                )
                            }
                        }

                        PdfTool.REDACT -> {
                            item {
                                RedactionToolControl(
                                    redactionBoxes = uiState.redactionBoxes,
                                    onAddBox = { viewModel.addRedactionBox(it) },
                                    onClearBoxes = { viewModel.clearRedactions() }
                                )
                            }
                        }

                        PdfTool.COMPARE -> {
                            item {
                                CompareViewerControl(results = uiState.compareResults)
                            }
                        }

                        else -> {
                            // Default view for simple tools like JPG_TO_PDF, PDF_TO_JPG
                        }
                    }
                }
            }
        }

        // Result Dialog
        if (uiState.showResultDialog && uiState.lastResult != null) {
            QuickActionResultDialog(
                result = uiState.lastResult,
                onDismiss = { viewModel.dismissResultDialog() },
                onSaveToDevice = {
                    val defaultName = uiState.lastResult.outputFile?.name ?: "processed_document.pdf"
                    saveOutputLauncher.launch(defaultName)
                },
                storageService = viewModel.storageService
            )
        }
    }
}
