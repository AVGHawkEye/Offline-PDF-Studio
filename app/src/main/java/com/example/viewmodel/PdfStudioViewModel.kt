package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AcroFormField
import com.example.model.CompareDiffResult
import com.example.model.CompressionLevel
import com.example.model.CropBounds
import com.example.model.PageItem
import com.example.model.PageNumberFormat
import com.example.model.PageNumberPosition
import com.example.model.PdfCategory
import com.example.model.PdfTool
import com.example.model.ProcessResult
import com.example.model.ProtectMode
import com.example.model.RedactionBox
import com.example.model.RotateAngle
import com.example.model.SelectedFileItem
import com.example.model.WatermarkConfig
import com.example.service.OfficeDocumentService
import com.example.service.PdfProcessingService
import com.example.service.StorageService
import com.example.service.StorageStatsInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

sealed interface CurrentScreen {
    data object Dashboard : CurrentScreen
    data class Workspace(val tool: PdfTool) : CurrentScreen
}

data class UiState(
    val currentScreen: CurrentScreen = CurrentScreen.Dashboard,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val storageStats: StorageStatsInfo = StorageStatsInfo(0L, 0L, 0L),
    val selectedCategory: PdfCategory? = null,
    val searchQuery: String = "",

    // Selected files
    val selectedFiles: List<SelectedFileItem> = emptyList(),
    val pageItems: List<PageItem> = emptyList(),

    // Tool configurations
    val splitRangeInput: String = "all",
    val compressionLevel: CompressionLevel = CompressionLevel.MEDIUM,
    val protectMode: ProtectMode = ProtectMode.ENCRYPT,
    val passwordInput: String = "",
    val passwordConfirmInput: String = "",
    val useOcrScan: Boolean = false,
    val extractedOcrText: String = "",

    // Extended tool configs
    val selectedPagesToRemove: Set<Int> = emptySet(),
    val selectedPagesToExtract: List<Int> = emptyList(),
    val rotateAngle: RotateAngle = RotateAngle.DEG_90,
    val rotateAllPages: Boolean = true,
    val selectedPagesToRotate: Set<Int> = emptySet(),

    val pageNumberPosition: PageNumberPosition = PageNumberPosition.BOTTOM_CENTER,
    val pageNumberFormat: PageNumberFormat = PageNumberFormat.PAGE_X_OF_Y,
    val pageNumberFontSize: Float = 10f,

    val watermarkText: String = "CONFIDENTIAL",
    val watermarkAngle: Float = 45f,
    val watermarkOpacity: Float = 0.25f,
    val watermarkFontSize: Float = 44f,

    val cropBounds: CropBounds = CropBounds(),
    val htmlInputText: String = "<h1>Invoice Summary</h1><p>Client: ACME Corp<br>Total: <strong>$1,250.00</strong></p>",

    val acroFormFields: List<AcroFormField> = emptyList(),
    val acroFormFieldValues: Map<String, String> = emptyMap(),
    val flattenForm: Boolean = true,

    val redactionBoxes: List<RedactionBox> = emptyList(),
    val compareResults: List<CompareDiffResult> = emptyList(),

    val signatureBitmap: Bitmap? = null,
    val signaturePageIndex: Int = 0,
    val signatureXRatio: Float = 0.6f,
    val signatureYRatio: Float = 0.8f,
    val signatureWidthRatio: Float = 0.3f,
    val signatureHeightRatio: Float = 0.12f,

    // Canvas preview state
    val activePreviewPageIndex: Int = 0,
    val totalPagesInPreview: Int = 1,
    val activePreviewBitmap: Bitmap? = null,
    val showCanvasPreview: Boolean = false,

    // Execution & progress
    val isProcessing: Boolean = false,
    val progressValue: Float = 0f,
    val progressMessage: String = "",
    val lastResult: ProcessResult? = null,
    val showResultDialog: Boolean = false,
    val errorMessage: String? = null
)

class PdfStudioViewModel(application: Application) : AndroidViewModel(application) {

    val storageService = StorageService(application.applicationContext)
    val pdfProcessingService = PdfProcessingService(application.applicationContext, storageService)
    val officeDocumentService = OfficeDocumentService(application.applicationContext, storageService)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        refreshStorageInfo()
    }

    fun refreshStorageInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = storageService.getStorageStats()
            _uiState.update { it.copy(storageStats = stats) }
        }
    }

    fun toggleTheme() {
        _uiState.update { current ->
            val nextMode = when (current.themeMode) {
                ThemeMode.SYSTEM -> ThemeMode.DARK
                ThemeMode.DARK -> ThemeMode.LIGHT
                ThemeMode.LIGHT -> ThemeMode.SYSTEM
            }
            current.copy(themeMode = nextMode)
        }
    }

    fun selectCategory(category: PdfCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openTool(tool: PdfTool) {
        resetToolState()
        _uiState.update {
            it.copy(
                currentScreen = CurrentScreen.Workspace(tool),
                errorMessage = null
            )
        }
    }

    fun navigateBack() {
        resetToolState()
        _uiState.update {
            it.copy(
                currentScreen = CurrentScreen.Dashboard,
                errorMessage = null
            )
        }
        refreshStorageInfo()
    }

    fun dismissResultDialog() {
        _uiState.update { it.copy(showResultDialog = false) }
        refreshStorageInfo()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun resetToolState() {
        _uiState.update {
            it.copy(
                selectedFiles = emptyList(),
                pageItems = emptyList(),
                splitRangeInput = "all",
                compressionLevel = CompressionLevel.MEDIUM,
                protectMode = ProtectMode.ENCRYPT,
                passwordInput = "",
                passwordConfirmInput = "",
                useOcrScan = false,
                extractedOcrText = "",
                selectedPagesToRemove = emptySet(),
                selectedPagesToExtract = emptyList(),
                rotateAngle = RotateAngle.DEG_90,
                rotateAllPages = true,
                selectedPagesToRotate = emptySet(),
                pageNumberPosition = PageNumberPosition.BOTTOM_CENTER,
                pageNumberFormat = PageNumberFormat.PAGE_X_OF_Y,
                watermarkText = "CONFIDENTIAL",
                watermarkAngle = 45f,
                watermarkOpacity = 0.25f,
                cropBounds = CropBounds(),
                acroFormFields = emptyList(),
                acroFormFieldValues = emptyMap(),
                redactionBoxes = emptyList(),
                compareResults = emptyList(),
                signatureBitmap = null,
                activePreviewPageIndex = 0,
                totalPagesInPreview = 1,
                activePreviewBitmap = null,
                showCanvasPreview = false,
                isProcessing = false,
                progressValue = 0f,
                progressMessage = "",
                lastResult = null,
                showResultDialog = false,
                errorMessage = null
            )
        }
    }

    fun onFilesSelected(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val currentTool = (_uiState.value.currentScreen as? CurrentScreen.Workspace)?.tool

        viewModelScope.launch(Dispatchers.IO) {
            val newItems = uris.map { uri ->
                val meta = storageService.getFileMetadata(uri)
                val isImg = meta.mimeType.startsWith("image/")
                val pageCount = if (!isImg && (meta.mimeType.contains("pdf") || meta.name.endsWith(".pdf", true))) {
                    pdfProcessingService.getPageCount(uri)
                } else 1

                SelectedFileItem(
                    id = UUID.randomUUID().toString(),
                    uri = uri,
                    name = meta.name,
                    sizeBytes = meta.sizeBytes,
                    pageCount = pageCount,
                    isImage = isImg
                )
            }

            val finalFiles = if (currentTool?.allowsMultipleFiles == true) {
                _uiState.value.selectedFiles + newItems
            } else {
                listOf(newItems.first())
            }

            val primary = finalFiles.firstOrNull()
            val pages = if (primary != null && !primary.isImage && primary.pageCount > 0) {
                (0 until primary.pageCount).map { idx ->
                    PageItem(
                        id = UUID.randomUUID().toString(),
                        originalPageIndex = idx,
                        displayPageNumber = idx + 1,
                        thumbnail = pdfProcessingService.renderPageBitmap(primary.uri, idx, 220),
                        rotationDegrees = 0
                    )
                }
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    selectedFiles = finalFiles,
                    pageItems = pages,
                    totalPagesInPreview = primary?.pageCount ?: 1,
                    activePreviewPageIndex = 0,
                    activePreviewBitmap = null,
                    showCanvasPreview = false,
                    errorMessage = null
                )
            }

            // Auto-load Form Fields if in PDF Forms tool
            if (currentTool == PdfTool.PDF_FORMS && primary != null) {
                loadFormFieldsForCurrentFile(primary.uri)
            }
        }
    }

    fun removeSelectedFile(id: String) {
        _uiState.update { state ->
            val updated = state.selectedFiles.filter { it.id != id }
            state.copy(selectedFiles = updated)
        }
    }

    fun removeSelectedFile(index: Int) {
        _uiState.update { state ->
            val list = state.selectedFiles.toMutableList()
            if (index in list.indices) list.removeAt(index)
            state.copy(selectedFiles = list)
        }
    }

    fun moveFileUp(index: Int) {
        if (index > 0) reorderSelectedFiles(index, index - 1)
    }

    fun moveFileDown(index: Int) {
        if (index < _uiState.value.selectedFiles.size - 1) reorderSelectedFiles(index, index + 1)
    }

    fun reorderSelectedFiles(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val list = state.selectedFiles.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
            }
            state.copy(selectedFiles = list)
        }
    }

    // Page Grid & Organize actions
    fun rotatePage(pageId: String) {
        _uiState.update { state ->
            val pages = state.pageItems.map { page ->
                if (page.id == pageId) {
                    val nextRot = (page.rotationDegrees + 90) % 360
                    page.copy(rotationDegrees = nextRot)
                } else page
            }
            state.copy(pageItems = pages)
        }
    }

    fun deletePageFromOrganize(pageId: String) {
        _uiState.update { state ->
            val pages = state.pageItems.filter { it.id != pageId }
            state.copy(pageItems = pages)
        }
    }

    fun movePageInOrganize(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val pages = state.pageItems.toMutableList()
            if (fromIndex in pages.indices && toIndex in pages.indices) {
                val p = pages.removeAt(fromIndex)
                pages.add(toIndex, p)
            }
            state.copy(pageItems = pages)
        }
    }

    // Tool specific option updates
    fun setSplitRange(range: String) {
        _uiState.update { it.copy(splitRangeInput = range) }
    }

    fun setCompressionLevel(level: CompressionLevel) {
        _uiState.update { it.copy(compressionLevel = level) }
    }

    fun setProtectMode(mode: ProtectMode) {
        _uiState.update { it.copy(protectMode = mode) }
    }

    fun setPassword(pwd: String) {
        _uiState.update { it.copy(passwordInput = pwd) }
    }

    fun setPasswordConfirm(confirm: String) {
        _uiState.update { it.copy(passwordConfirmInput = confirm) }
    }

    fun setUseOcrScan(useOcr: Boolean) {
        _uiState.update { it.copy(useOcrScan = useOcr) }
    }

    fun setExtractedOcrText(text: String) {
        _uiState.update { it.copy(extractedOcrText = text) }
    }

    // Extended tool actions
    fun togglePageToRemove(pageIndex: Int) {
        _uiState.update { current ->
            val set = current.selectedPagesToRemove.toMutableSet()
            if (set.contains(pageIndex)) set.remove(pageIndex) else set.add(pageIndex)
            current.copy(selectedPagesToRemove = set)
        }
    }

    fun togglePageToExtract(pageIndex: Int) {
        _uiState.update { current ->
            val list = current.selectedPagesToExtract.toMutableList()
            if (list.contains(pageIndex)) list.remove(pageIndex) else list.add(pageIndex)
            current.copy(selectedPagesToExtract = list)
        }
    }

    fun setRotateAngle(angle: RotateAngle) {
        _uiState.update { it.copy(rotateAngle = angle) }
    }

    fun setRotateAllPages(all: Boolean) {
        _uiState.update { it.copy(rotateAllPages = all) }
    }

    fun togglePageToRotate(pageIndex: Int) {
        _uiState.update { current ->
            val set = current.selectedPagesToRotate.toMutableSet()
            if (set.contains(pageIndex)) set.remove(pageIndex) else set.add(pageIndex)
            current.copy(selectedPagesToRotate = set)
        }
    }

    fun setPageNumberPosition(pos: PageNumberPosition) {
        _uiState.update { it.copy(pageNumberPosition = pos) }
    }

    fun setPageNumberFormat(fmt: PageNumberFormat) {
        _uiState.update { it.copy(pageNumberFormat = fmt) }
    }

    fun setPageNumberFontSize(size: Float) {
        _uiState.update { it.copy(pageNumberFontSize = size) }
    }

    fun setWatermarkText(text: String) {
        _uiState.update { it.copy(watermarkText = text) }
    }

    fun setWatermarkAngle(angle: Float) {
        _uiState.update { it.copy(watermarkAngle = angle) }
    }

    fun setWatermarkOpacity(opacity: Float) {
        _uiState.update { it.copy(watermarkOpacity = opacity) }
    }

    fun setWatermarkFontSize(size: Float) {
        _uiState.update { it.copy(watermarkFontSize = size) }
    }

    fun setCropBounds(bounds: CropBounds) {
        _uiState.update { it.copy(cropBounds = bounds) }
    }

    fun setHtmlInputText(text: String) {
        _uiState.update { it.copy(htmlInputText = text) }
    }

    fun loadFormFieldsForCurrentFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val fields = pdfProcessingService.loadAcroFormFields(uri)
            val initialValues = fields.associate { it.fullyQualifiedName to (if (it.isCheckbox) it.isChecked.toString() else it.value) }
            _uiState.update {
                it.copy(
                    acroFormFields = fields,
                    acroFormFieldValues = initialValues
                )
            }
        }
    }

    fun updateFormFieldValue(fieldName: String, value: String) {
        _uiState.update { current ->
            val updated = current.acroFormFieldValues.toMutableMap()
            updated[fieldName] = value
            current.copy(acroFormFieldValues = updated)
        }
    }

    fun setFlattenForm(flatten: Boolean) {
        _uiState.update { it.copy(flattenForm = flatten) }
    }

    fun addRedactionBox(box: RedactionBox) {
        _uiState.update { it.copy(redactionBoxes = it.redactionBoxes + box) }
    }

    fun removeRedactionBox(id: String) {
        _uiState.update { it.copy(redactionBoxes = it.redactionBoxes.filter { b -> b.id != id }) }
    }

    fun clearRedactions() {
        _uiState.update { it.copy(redactionBoxes = emptyList()) }
    }

    fun setSignatureBitmap(bitmap: Bitmap?) {
        _uiState.update { it.copy(signatureBitmap = bitmap) }
    }

    fun setSignaturePosition(page: Int, x: Float, y: Float, w: Float, h: Float) {
        _uiState.update {
            it.copy(
                signaturePageIndex = page,
                signatureXRatio = x,
                signatureYRatio = y,
                signatureWidthRatio = w,
                signatureHeightRatio = h
            )
        }
    }

    fun toggleCanvasPreview() {
        val next = !_uiState.value.showCanvasPreview
        _uiState.update { it.copy(showCanvasPreview = next) }
        if (next && _uiState.value.activePreviewBitmap == null) {
            loadCanvasPreview(_uiState.value.activePreviewPageIndex)
        }
    }

    fun loadCanvasPreview(pageIndex: Int) {
        val file = _uiState.value.selectedFiles.firstOrNull() ?: return
        if (file.isImage) return
        viewModelScope.launch(Dispatchers.IO) {
            val validIndex = pageIndex.coerceIn(0, (file.pageCount - 1).coerceAtLeast(0))
            val bmp = pdfProcessingService.renderPageBitmap(file.uri, validIndex, 1000)
            _uiState.update {
                it.copy(
                    activePreviewPageIndex = validIndex,
                    activePreviewBitmap = bmp
                )
            }
        }
    }

    // ==========================================
    // EXECUTION ENGINE FOR ALL 30 TOOLS
    // ==========================================

    fun executeProcessing() {
        val currentTool = (_uiState.value.currentScreen as? CurrentScreen.Workspace)?.tool ?: return
        val files = _uiState.value.selectedFiles

        // Verification of file requirements
        if (currentTool != PdfTool.HTML_TO_PDF && files.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please select at least one file to proceed.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    progressValue = 0.05f,
                    progressMessage = "Starting local operation...",
                    errorMessage = null
                )
            }

            try {
                when (currentTool) {
                    // --- 1. ORGANIZE ---
                    PdfTool.MERGE -> {
                        if (files.size < 2) throw IllegalArgumentException("Please add at least 2 PDF files to merge.")
                        val uris = files.map { it.uri }
                        val out = pdfProcessingService.mergePdfs(uris) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val totalPages = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, files.sumOf { it.sizeBytes }, totalPages, "Merged ${files.size} documents into ${out.name} ($totalPages pages).")
                    }

                    PdfTool.SPLIT -> {
                        val first = files.first()
                        val out = pdfProcessingService.splitPdf(first.uri, _uiState.value.splitRangeInput) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Extracted $pageCount pages successfully.")
                    }

                    PdfTool.REMOVE_PAGES -> {
                        val first = files.first()
                        val toRemove = _uiState.value.selectedPagesToRemove
                        if (toRemove.isEmpty()) throw IllegalArgumentException("Please tap pages from the grid to select them for removal.")
                        val out = pdfProcessingService.removePages(first.uri, toRemove) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Removed ${toRemove.size} pages. $pageCount pages remaining.")
                    }

                    PdfTool.EXTRACT_PAGES -> {
                        val first = files.first()
                        val toExtract = _uiState.value.selectedPagesToExtract
                        if (toExtract.isEmpty()) throw IllegalArgumentException("Please tap pages from the grid to select pages for extraction.")
                        val out = pdfProcessingService.extractPages(first.uri, toExtract) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Extracted ${toExtract.size} pages into new document.")
                    }

                    PdfTool.ORGANIZE -> {
                        val first = files.first()
                        val pages = _uiState.value.pageItems
                        val out = pdfProcessingService.organizePdf(first.uri, pages) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Reorganized $pageCount pages with custom order and rotation.")
                    }

                    PdfTool.SCAN_TO_PDF -> {
                        val uris = files.map { it.uri }
                        val out = pdfProcessingService.imagesToPdf(uris) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        showSuccessResult(out, files.sumOf { it.sizeBytes }, uris.size, "Packaged ${uris.size} scanned documents into PDF.")
                    }

                    // --- 2. OPTIMIZE ---
                    PdfTool.COMPRESS -> {
                        val first = files.first()
                        val (out, originalSize) = pdfProcessingService.compressPdf(first.uri, _uiState.value.compressionLevel) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val newSize = out.length()
                        val reduction = if (originalSize > 0) ((originalSize - newSize).toFloat() / originalSize * 100f).coerceAtLeast(0f) else 0f
                        showSuccessResult(out, originalSize, pdfProcessingService.getPageCount(Uri.fromFile(out)), "Compressed by ${String.format("%.1f", reduction)}%! ${StorageService.formatBytes(originalSize)} -> ${StorageService.formatBytes(newSize)}")
                    }

                    PdfTool.REPAIR -> {
                        val first = files.first()
                        val out = pdfProcessingService.repairPdf(first.uri) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Damaged streams sanitized and reconstructed successfully.")
                    }

                    PdfTool.OCR_PDF -> {
                        val first = files.first()
                        val text = pdfProcessingService.extractTextOrOcr(first.uri, _uiState.value.useOcrScan) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val txtFile = storageService.createTempFile("ocr_extracted", "txt")
                        txtFile.writeText(text)
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                extractedOcrText = text,
                                lastResult = ProcessResult(
                                    success = true,
                                    outputFile = txtFile,
                                    mimeType = "text/plain",
                                    originalSizeBytes = first.sizeBytes,
                                    outputSizeBytes = txtFile.length(),
                                    pageCount = first.pageCount,
                                    message = "Extracted ${text.length} characters using on-device ML text recognition."
                                ),
                                showResultDialog = true
                            )
                        }
                    }

                    // --- 3. CONVERT TO PDF ---
                    PdfTool.JPG_TO_PDF -> {
                        val uris = files.map { it.uri }
                        val out = pdfProcessingService.imagesToPdf(uris) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        showSuccessResult(out, files.sumOf { it.sizeBytes }, uris.size, "Converted ${uris.size} images to PDF.")
                    }

                    PdfTool.WORD_TO_PDF -> {
                        val first = files.first()
                        val out = officeDocumentService.docxToPdf(first.uri) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Rendered Word document (.docx) to PDF canvas.")
                    }

                    PdfTool.PPT_TO_PDF -> {
                        val first = files.first()
                        val out = officeDocumentService.pptxToPdf(first.uri) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Rendered PowerPoint (.pptx) slides to PDF.")
                    }

                    PdfTool.EXCEL_TO_PDF -> {
                        val first = files.first()
                        val out = officeDocumentService.xlsxToPdf(first.uri) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Rendered Excel (.xlsx) sheet to printable PDF.")
                    }

                    PdfTool.HTML_TO_PDF -> {
                        val htmlContent = if (files.isNotEmpty()) {
                            context.contentResolver.openInputStream(files.first().uri)?.bufferedReader()?.readText() ?: _uiState.value.htmlInputText
                        } else {
                            _uiState.value.htmlInputText
                        }
                        val out = officeDocumentService.htmlToPdf(htmlContent) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, htmlContent.length.toLong(), pageCount, "Compiled HTML content to formatted PDF.")
                    }

                    // --- 4. CONVERT FROM PDF ---
                    PdfTool.PDF_TO_JPG -> {
                        val first = files.first()
                        val outList = pdfProcessingService.pdfToImages(first.uri, "PNG") { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                lastResult = ProcessResult(
                                    success = true,
                                    outputFile = outList.firstOrNull(),
                                    outputFiles = outList,
                                    mimeType = "image/png",
                                    originalSizeBytes = first.sizeBytes,
                                    outputSizeBytes = outList.sumOf { f -> f.length() },
                                    pageCount = outList.size,
                                    message = "Exported ${outList.size} high-resolution PNG images."
                                ),
                                showResultDialog = true
                            )
                        }
                    }

                    PdfTool.PDF_TO_WORD -> {
                        val first = files.first()
                        val text = pdfProcessingService.extractTextOrOcr(first.uri, false) { p, msg ->
                            _uiState.update { it.copy(progressValue = p * 0.5f, progressMessage = msg) }
                        }
                        val lines = text.lines().filter { it.isNotBlank() }
                        val out = officeDocumentService.pdfToDocx(lines) { p, msg ->
                            _uiState.update { it.copy(progressValue = 0.5f + p * 0.5f, progressMessage = msg) }
                        }
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                lastResult = ProcessResult(
                                    success = true,
                                    outputFile = out,
                                    mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    originalSizeBytes = first.sizeBytes,
                                    outputSizeBytes = out.length(),
                                    message = "Generated editable Word document (.docx) with structured paragraphs."
                                ),
                                showResultDialog = true
                            )
                        }
                    }

                    PdfTool.PDF_TO_PPT -> {
                        val first = files.first()
                        val images = pdfProcessingService.pdfToImages(first.uri, "PNG") { p, msg ->
                            _uiState.update { it.copy(progressValue = p * 0.5f, progressMessage = "Rendering slides: $msg") }
                        }
                        val out = officeDocumentService.pdfToPptx(images) { p, msg ->
                            _uiState.update { it.copy(progressValue = 0.5f + p * 0.5f, progressMessage = msg) }
                        }
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                lastResult = ProcessResult(
                                    success = true,
                                    outputFile = out,
                                    mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                    originalSizeBytes = first.sizeBytes,
                                    outputSizeBytes = out.length(),
                                    pageCount = images.size,
                                    message = "Assembled ${images.size} slides into PowerPoint presentation (.pptx)."
                                ),
                                showResultDialog = true
                            )
                        }
                    }

                    PdfTool.PDF_TO_EXCEL -> {
                        val first = files.first()
                        val text = pdfProcessingService.extractTextOrOcr(first.uri, false) { p, msg ->
                            _uiState.update { it.copy(progressValue = p * 0.5f, progressMessage = msg) }
                        }
                        val lines = text.lines().filter { it.isNotBlank() }
                        val out = officeDocumentService.pdfToXlsx(lines) { p, msg ->
                            _uiState.update { it.copy(progressValue = 0.5f + p * 0.5f, progressMessage = msg) }
                        }
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                lastResult = ProcessResult(
                                    success = true,
                                    outputFile = out,
                                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    originalSizeBytes = first.sizeBytes,
                                    outputSizeBytes = out.length(),
                                    message = "Converted tabular text data into Excel spreadsheet (.xlsx)."
                                ),
                                showResultDialog = true
                            )
                        }
                    }

                    PdfTool.PDF_TO_PDFA -> {
                        val first = files.first()
                        val out = pdfProcessingService.convertToPdfA(first.uri) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Standard PDF converted to ISO 19005 (PDF/A) archival specification.")
                    }

                    // --- 5. EDIT PDF ---
                    PdfTool.ROTATE -> {
                        val first = files.first()
                        val angle = _uiState.value.rotateAngle.degrees
                        val pagesToRot = if (_uiState.value.rotateAllPages) null else _uiState.value.selectedPagesToRotate
                        val out = pdfProcessingService.rotatePdf(first.uri, angle, pagesToRot) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Batch rotated pages by $angle° successfully.")
                    }

                    PdfTool.PAGE_NUMBERS -> {
                        val first = files.first()
                        val out = pdfProcessingService.addPageNumbers(
                            first.uri,
                            _uiState.value.pageNumberPosition,
                            _uiState.value.pageNumberFormat,
                            _uiState.value.pageNumberFontSize
                        ) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Applied page numbers to all $pageCount pages.")
                    }

                    PdfTool.WATERMARK -> {
                        val first = files.first()
                        val config = WatermarkConfig(
                            text = _uiState.value.watermarkText,
                            angle = _uiState.value.watermarkAngle,
                            opacity = _uiState.value.watermarkOpacity,
                            fontSize = _uiState.value.watermarkFontSize
                        )
                        val out = pdfProcessingService.addWatermark(first.uri, config) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Stamped '${config.text}' watermark across all pages.")
                    }

                    PdfTool.CROP -> {
                        val first = files.first()
                        val out = pdfProcessingService.cropPdf(first.uri, _uiState.value.cropBounds) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Cropped document margins and updated media bounds.")
                    }

                    PdfTool.EDIT_PDF -> {
                        val first = files.first()
                        // Use activePreviewBitmap or create watermark overlay
                        val overlay = _uiState.value.activePreviewBitmap ?: throw IllegalArgumentException("No drawn annotations found to save.")
                        val out = pdfProcessingService.applyAnnotations(first.uri, _uiState.value.activePreviewPageIndex, overlay) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        showSuccessResult(out, first.sizeBytes, first.pageCount, "Interactive drawing and annotations burned into PDF.")
                    }

                    PdfTool.PDF_FORMS -> {
                        val first = files.first()
                        val values = _uiState.value.acroFormFieldValues
                        val flatten = _uiState.value.flattenForm
                        val out = pdfProcessingService.fillAndFlattenForm(first.uri, values, flatten) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val pageCount = pdfProcessingService.getPageCount(Uri.fromFile(out))
                        showSuccessResult(out, first.sizeBytes, pageCount, "Saved ${values.size} filled form fields with flattening=$flatten.")
                    }

                    // --- 6. SECURITY ---
                    PdfTool.UNLOCK -> {
                        val first = files.first()
                        val password = _uiState.value.passwordInput
                        if (password.isBlank()) throw IllegalArgumentException("Please enter the document password to unlock.")
                        val out = pdfProcessingService.unlockPdf(first.uri, password) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        showSuccessResult(out, first.sizeBytes, first.pageCount, "PDF successfully decrypted and unlocked!")
                    }

                    PdfTool.PROTECT -> {
                        val first = files.first()
                        val password = _uiState.value.passwordInput
                        if (password.isBlank()) throw IllegalArgumentException("Please enter a password.")
                        if (password != _uiState.value.passwordConfirmInput) throw IllegalArgumentException("Passwords do not match.")
                        val out = pdfProcessingService.protectPdf(first.uri, password) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        showSuccessResult(out, first.sizeBytes, first.pageCount, "PDF encrypted with standard AES security.")
                    }

                    PdfTool.SIGN -> {
                        val first = files.first()
                        val sigBmp = _uiState.value.signatureBitmap ?: throw IllegalArgumentException("Please draw your signature before applying.")
                        val out = pdfProcessingService.signPdf(
                            first.uri,
                            sigBmp,
                            _uiState.value.signaturePageIndex,
                            _uiState.value.signatureXRatio,
                            _uiState.value.signatureYRatio,
                            _uiState.value.signatureWidthRatio,
                            _uiState.value.signatureHeightRatio
                        ) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        showSuccessResult(out, first.sizeBytes, first.pageCount, "Digital signature stamped and embedded into document.")
                    }

                    PdfTool.REDACT -> {
                        val first = files.first()
                        val boxes = _uiState.value.redactionBoxes
                        if (boxes.isEmpty()) throw IllegalArgumentException("Please draw at least one redaction mask rectangle.")
                        val out = pdfProcessingService.redactPdf(first.uri, boxes) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        showSuccessResult(out, first.sizeBytes, first.pageCount, "Permanently redacted ${boxes.size} sensitive areas.")
                    }

                    PdfTool.COMPARE -> {
                        if (files.size < 2) throw IllegalArgumentException("Please select 2 PDF files to compare.")
                        val diffList = pdfProcessingService.comparePdfs(files[0].uri, files[1].uri) { p, msg ->
                            _uiState.update { it.copy(progressValue = p, progressMessage = msg) }
                        }
                        val avgDiff = if (diffList.isNotEmpty()) diffList.map { it.differencePercentage }.average().toFloat() else 0f
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                compareResults = diffList,
                                lastResult = ProcessResult(
                                    success = true,
                                    outputFile = null,
                                    pageCount = diffList.size,
                                    message = "Visual comparison complete across ${diffList.size} pages (Average divergence: ${String.format("%.1f", avgDiff)}%)."
                                ),
                                showResultDialog = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = e.localizedMessage ?: "An unexpected error occurred during processing."
                    )
                }
            }
        }
    }

    private fun showSuccessResult(outFile: File, originalSize: Long, pageCount: Int, message: String) {
        _uiState.update {
            it.copy(
                isProcessing = false,
                lastResult = ProcessResult(
                    success = true,
                    outputFile = outFile,
                    originalSizeBytes = originalSize,
                    outputSizeBytes = outFile.length(),
                    pageCount = pageCount,
                    message = message
                ),
                showResultDialog = true
            )
        }
    }

    fun handleSelectedUris(uris: List<Uri>) {
        onFilesSelected(uris)
    }

    fun saveResultToSaf(destinationUri: Uri): Boolean {
        val file = _uiState.value.lastResult?.outputFile ?: return false
        val success = storageService.saveFileToUri(file, destinationUri)
        refreshStorageInfo()
        return success
    }

    fun clearTemporaryCache() {
        viewModelScope.launch(Dispatchers.IO) {
            storageService.clearCache()
            refreshStorageInfo()
        }
    }

    private val context get() = getApplication<Application>().applicationContext
}
