package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 100% Client-side Office OpenXML (.docx, .xlsx, .pptx) parser and generator.
 * Operates purely offline using standard ZIP and XML streams without heavy external dependencies.
 */
class OfficeDocumentService(
    private val context: Context,
    private val storageService: StorageService
) {

    // ==========================================
    // 1. WORD (.docx) <-> PDF
    // ==========================================

    /**
     * Parse .docx file (OpenXML) and render into formatted A4 PDF.
     */
    suspend fun docxToPdf(
        docxUri: Uri,
        onProgress: (Float, String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Opening Word document stream...")
        val paragraphs = mutableListOf<String>()

        context.contentResolver.openInputStream(docxUri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val xmlContent = zis.bufferedReader().readText()
                        paragraphs.addAll(parseDocxXml(xmlContent))
                        break
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } ?: throw IllegalArgumentException("Could not read Word document")

        if (paragraphs.isEmpty()) {
            paragraphs.add("[Empty Word Document or no text content found]")
        }

        onProgress(0.5f, "Rendering ${paragraphs.size} paragraphs to PDF canvas...")

        // Render paragraphs to PDF using android.graphics.pdf.PdfDocument
        val pdfDoc = PdfDocument()
        val pageWidth = 595 // A4 points at 72dpi
        val pageHeight = 842
        val margin = 50f
        val printableWidth = pageWidth - (margin * 2)

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val headingPaint = Paint().apply {
            color = Color.rgb(20, 20, 80)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas
        var y = margin + 20f

        for ((idx, para) in paragraphs.withIndex()) {
            val isHeading = idx == 0 || para.length < 40 && !para.endsWith(".")
            val currentPaint = if (isHeading) headingPaint else textPaint
            val lineHeight = if (isHeading) 24f else 18f

            val words = para.split(" ")
            var line = ""

            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                val measure = currentPaint.measureText(testLine)

                if (measure > printableWidth) {
                    if (y + lineHeight > pageHeight - margin) {
                        pdfDoc.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDoc.startPage(pageInfo)
                        canvas = page.canvas
                        y = margin + 20f
                    }
                    canvas.drawText(line, margin, y, currentPaint)
                    y += lineHeight
                    line = word
                } else {
                    line = testLine
                }
            }

            if (line.isNotEmpty()) {
                if (y + lineHeight > pageHeight - margin) {
                    pdfDoc.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin + 20f
                }
                canvas.drawText(line, margin, y, currentPaint)
                y += lineHeight + 6f
            }
        }

        pdfDoc.finishPage(page)

        val outFile = storageService.createTempFile("converted_word", "pdf")
        FileOutputStream(outFile).use { fos ->
            pdfDoc.writeTo(fos)
        }
        pdfDoc.close()

        onProgress(1.0f, "Word to PDF conversion complete!")
        outFile
    }

    private fun parseDocxXml(xml: String): List<String> {
        val paragraphs = mutableListOf<String>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            val currentPara = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (name.equals("t", ignoreCase = true) || name.endsWith(":t")) {
                            parser.next()
                            if (parser.text != null) {
                                currentPara.append(parser.text)
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (name.equals("p", ignoreCase = true) || name.endsWith(":p")) {
                            val text = currentPara.toString().trim()
                            if (text.isNotEmpty()) {
                                paragraphs.add(text)
                            }
                            currentPara.setLength(0)
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // Fallback: extract text using regex if XML parser encountered namespace/tag variances
            val pRegex = "<w:p[^>]*>(.*?)</w:p>".toRegex(RegexOption.DOT_MATCHES_ALL)
            val tRegex = "<w:t[^>]*>(.*?)</w:t>".toRegex(RegexOption.DOT_MATCHES_ALL)
            for (match in pRegex.findAll(xml)) {
                val pText = tRegex.findAll(match.value).joinToString("") { it.groupValues[1] }.trim()
                if (pText.isNotEmpty()) paragraphs.add(pText)
            }
        }
        return paragraphs
    }

    /**
     * Convert extracted PDF text into an editable .docx OpenXML file.
     */
    suspend fun pdfToDocx(
        textLines: List<String>,
        onProgress: (Float, String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        onProgress(0.2f, "Constructing OpenXML Word document structure...")
        val outFile = storageService.createTempFile("converted_doc", "docx")

        val docXml = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">""")
            append("<w:body>")
            for (line in textLines) {
                val cleanLine = escapeXml(line.trim())
                if (cleanLine.isNotEmpty()) {
                    append("<w:p><w:r><w:t>$cleanLine</w:t></w:r></w:p>")
                } else {
                    append("<w:p/>")
                }
            }
            append("</w:body></w:document>")
        }

        val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

        val relsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            writeZipEntry(zos, "[Content_Types].xml", contentTypesXml)
            writeZipEntry(zos, "_rels/.rels", relsXml)
            writeZipEntry(zos, "word/document.xml", docXml)
        }

        onProgress(1.0f, "Saved editable .docx file!")
        outFile
    }

    // ==========================================
    // 2. EXCEL (.xlsx) <-> PDF
    // ==========================================

    /**
     * Parse .xlsx file (OpenXML) and render into printable grid PDF pages.
     */
    suspend fun xlsxToPdf(
        xlsxUri: Uri,
        onProgress: (Float, String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Opening Excel spreadsheet...")
        val sharedStrings = mutableListOf<String>()
        var sheetXml: String? = null

        context.contentResolver.openInputStream(xlsxUri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "xl/sharedStrings.xml" -> {
                            sharedStrings.addAll(parseSharedStrings(zis.bufferedReader().readText()))
                        }
                        "xl/worksheets/sheet1.xml" -> {
                            sheetXml = zis.bufferedReader().readText()
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } ?: throw IllegalArgumentException("Could not read Excel file")

        val rows = parseSheetRows(sheetXml ?: "", sharedStrings)

        onProgress(0.5f, "Formatting ${rows.size} rows into PDF table...")

        // Render table to Landscape A4 PDF
        val pdfDoc = PdfDocument()
        val pageWidth = 842 // Landscape A4
        val pageHeight = 595
        val margin = 40f

        val cellPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(40, 100, 60)
            style = Paint.Style.FILL
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas
        var y = margin + 20f

        val colWidth = 120f
        val rowHeight = 24f

        for ((rIdx, row) in rows.withIndex()) {
            if (y + rowHeight > pageHeight - margin) {
                pdfDoc.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                y = margin + 20f
            }

            for ((cIdx, cellText) in row.withIndex()) {
                val x = margin + (cIdx * colWidth)
                if (x + colWidth <= pageWidth - margin) {
                    if (rIdx == 0) {
                        canvas.drawRect(x, y, x + colWidth, y + rowHeight, headerBgPaint)
                        canvas.drawRect(x, y, x + colWidth, y + rowHeight, borderPaint)
                        val text = cellText.take(18)
                        canvas.drawText(text, x + 6f, y + 16f, headerPaint)
                    } else {
                        canvas.drawRect(x, y, x + colWidth, y + rowHeight, borderPaint)
                        val text = cellText.take(18)
                        canvas.drawText(text, x + 6f, y + 16f, cellPaint)
                    }
                }
            }
            y += rowHeight
        }

        pdfDoc.finishPage(page)

        val outFile = storageService.createTempFile("converted_excel", "pdf")
        FileOutputStream(outFile).use { fos ->
            pdfDoc.writeTo(fos)
        }
        pdfDoc.close()

        onProgress(1.0f, "Excel to PDF complete!")
        outFile
    }

    private fun parseSharedStrings(xml: String): List<String> {
        val strings = mutableListOf<String>()
        val regex = "<t[^>]*>(.*?)</t>".toRegex(RegexOption.DOT_MATCHES_ALL)
        for (m in regex.findAll(xml)) {
            strings.add(m.groupValues[1])
        }
        return strings
    }

    private fun parseSheetRows(xml: String, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val rowRegex = "<row[^>]*>(.*?)</row>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val cellRegex = "<c[^>]*?(?:t=\"([^\"]*)\")?[^>]*>(?:<v>(.*?)</v>|<is><t>(.*?)</t></is>)</c>".toRegex(RegexOption.DOT_MATCHES_ALL)

        for (rMatch in rowRegex.findAll(xml)) {
            val cells = mutableListOf<String>()
            for (cMatch in cellRegex.findAll(rMatch.value)) {
                val type = cMatch.groupValues[1]
                val vVal = cMatch.groupValues[2]
                val isVal = cMatch.groupValues[3]

                val value = when {
                    isVal.isNotEmpty() -> isVal
                    type == "s" -> {
                        val idx = vVal.toIntOrNull() ?: -1
                        if (idx in sharedStrings.indices) sharedStrings[idx] else vVal
                    }
                    else -> vVal
                }
                cells.add(value)
            }
            if (cells.isNotEmpty()) {
                rows.add(cells)
            }
        }

        if (rows.isEmpty()) {
            rows.add(listOf("Sheet1", "No table data detected in sheet"))
        }
        return rows
    }

    /**
     * Convert PDF extracted text into an .xlsx OpenXML spreadsheet.
     */
    suspend fun pdfToXlsx(
        textLines: List<String>,
        onProgress: (Float, String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        onProgress(0.2f, "Structuring table data for Excel...")
        val outFile = storageService.createTempFile("converted_sheet", "xlsx")

        val sheetDataXml = buildString {
            append("<sheetData>")
            var rowIdx = 1
            for (line in textLines) {
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    // Split on tabs, commas, or multiple spaces as delimiters
                    val cells = trimmed.split(Regex("[\\t,]|\\s{2,}"))
                    append("""<row r="$rowIdx">""")
                    for ((cIdx, cellText) in cells.withIndex()) {
                        val colLetter = ('A'.code + (cIdx % 26)).toChar()
                        val cellRef = "$colLetter$rowIdx"
                        val clean = escapeXml(cellText.trim())
                        append("""<c r="$cellRef" t="inlineStr"><is><t>$clean</t></is></c>""")
                    }
                    append("</row>")
                    rowIdx++
                }
            }
            append("</sheetData>")
        }

        val worksheetXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
$sheetDataXml
</worksheet>"""

        val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Sheet1" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""

        val workbookRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""

        val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

        val relsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            writeZipEntry(zos, "[Content_Types].xml", contentTypesXml)
            writeZipEntry(zos, "_rels/.rels", relsXml)
            writeZipEntry(zos, "xl/workbook.xml", workbookXml)
            writeZipEntry(zos, "xl/_rels/workbook.xml.rels", workbookRelsXml)
            writeZipEntry(zos, "xl/worksheets/sheet1.xml", worksheetXml)
        }

        onProgress(1.0f, "Saved .xlsx spreadsheet!")
        outFile
    }

    // ==========================================
    // 3. POWERPOINT (.pptx) <-> PDF
    // ==========================================

    /**
     * Parse .pptx slides and render as landscape PDF pages.
     */
    suspend fun pptxToPdf(
        pptxUri: Uri,
        onProgress: (Float, String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Opening PowerPoint presentation...")
        val slideTexts = mutableMapOf<String, MutableList<String>>()

        context.contentResolver.openInputStream(pptxUri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (entry.name.startsWith("ppt/slides/slide") && entry.name.endsWith(".xml")) {
                        val slideName = entry.name
                        val xml = zis.bufferedReader().readText()
                        val textList = parseSlideText(xml)
                        slideTexts[slideName] = textList.toMutableList()
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } ?: throw IllegalArgumentException("Could not read PowerPoint presentation")

        onProgress(0.5f, "Rendering ${slideTexts.size} slides to PDF...")

        val pdfDoc = PdfDocument()
        val pageWidth = 960 // 16:9 Landscape
        val pageHeight = 540

        val titlePaint = Paint().apply {
            color = Color.rgb(20, 20, 90)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(200, 210, 230)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        var slideNumber = 1
        val sortedSlides = slideTexts.entries.sortedBy { it.key }

        if (sortedSlides.isEmpty()) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            page.canvas.drawText("Presentation (No text slides detected)", 80f, 200f, titlePaint)
            pdfDoc.finishPage(page)
        } else {
            for (slide in sortedSlides) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, slideNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas = page.canvas

                // Draw slide border & slide number
                canvas.drawRoundRect(20f, 20f, pageWidth - 20f, pageHeight - 20f, 16f, 16f, borderPaint)
                canvas.drawText("Slide $slideNumber", pageWidth - 100f, pageHeight - 35f, bodyPaint)

                var y = 80f
                for ((tIdx, text) in slide.value.withIndex()) {
                    if (tIdx == 0) {
                        canvas.drawText(text.take(60), 60f, y, titlePaint)
                        y += 45f
                    } else {
                        canvas.drawText("• ${text.take(80)}", 70f, y, bodyPaint)
                        y += 26f
                    }
                    if (y > pageHeight - 70f) break
                }

                pdfDoc.finishPage(page)
                slideNumber++
            }
        }

        val outFile = storageService.createTempFile("converted_presentation", "pdf")
        FileOutputStream(outFile).use { fos ->
            pdfDoc.writeTo(fos)
        }
        pdfDoc.close()

        onProgress(1.0f, "PowerPoint to PDF complete!")
        outFile
    }

    private fun parseSlideText(xml: String): List<String> {
        val texts = mutableListOf<String>()
        val regex = "<a:t[^>]*>(.*?)</a:t>".toRegex(RegexOption.DOT_MATCHES_ALL)
        for (m in regex.findAll(xml)) {
            val t = m.groupValues[1].trim()
            if (t.isNotEmpty()) {
                texts.add(t)
            }
        }
        return texts
    }

    /**
     * Convert PDF page images into an assembled .pptx presentation.
     */
    suspend fun pdfToPptx(
        pageImages: List<File>,
        onProgress: (Float, String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        onProgress(0.2f, "Assembling slides for PowerPoint...")
        val outFile = storageService.createTempFile("converted_presentation", "pptx")

        val slideCount = pageImages.size.coerceAtLeast(1)

        val presentationXml = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<p:presentation xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""")
            append("<p:sldIdLst>")
            for (i in 1..slideCount) {
                append("""<p:sldId id="${255 + i}" r:id="rId$i"/>""")
            }
            append("</p:sldIdLst>")
            append("<p:sldSz cx=\"9144000\" cy=\"5143500\"/>") // 16:9 widescreen
            append("</p:presentation>")
        }

        val presentationRelsXml = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
            for (i in 1..slideCount) {
                append("""<Relationship Id="rId$i" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide$i.xml"/>""")
            }
            append("</Relationships>")
        }

        val contentTypesXml = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
            append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
            append("""<Default Extension="xml" ContentType="application/xml"/>""")
            append("""<Default Extension="png" ContentType="image/png"/>""")
            append("""<Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>""")
            for (i in 1..slideCount) {
                append("""<Override PartName="/ppt/slides/slide$i.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>""")
            }
            append("</Types>")
        }

        val rootRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
</Relationships>"""

        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            writeZipEntry(zos, "[Content_Types].xml", contentTypesXml)
            writeZipEntry(zos, "_rels/.rels", rootRelsXml)
            writeZipEntry(zos, "ppt/presentation.xml", presentationXml)
            writeZipEntry(zos, "ppt/_rels/presentation.xml.rels", presentationRelsXml)

            // Write slides and media images
            for ((idx, imageFile) in pageImages.withIndex()) {
                val slideNum = idx + 1
                val imageBytes = imageFile.readBytes()
                writeZipBytesEntry(zos, "ppt/media/image$slideNum.png", imageBytes)

                val slideXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
<p:cSld>
<p:spTree>
<p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
<p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
<p:pic>
<p:nvPicPr><p:cNvPr id="2" name="Page $slideNum"/><p:cNvPicPr/><p:nvPr/></p:nvPicPr>
<p:blipFill><a:blip r:embed="rId1"/><a:stretch><a:fillRect/></a:stretch></p:blipFill>
<p:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="9144000" cy="5143500"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>
</p:pic>
</p:spTree>
</p:cSld>
</p:sld>"""

                val slideRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="../media/image$slideNum.png"/>
</Relationships>"""

                writeZipEntry(zos, "ppt/slides/slide$slideNum.xml", slideXml)
                writeZipEntry(zos, "ppt/slides/_rels/slide$slideNum.xml.rels", slideRelsXml)
            }
        }

        onProgress(1.0f, "Saved .pptx presentation!")
        outFile
    }

    // ==========================================
    // 4. HTML <-> PDF
    // ==========================================

    /**
     * Render formatted HTML content or string to offline PDF canvas.
     */
    suspend fun htmlToPdf(
        htmlContent: String,
        onProgress: (Float, String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        onProgress(0.2f, "Parsing offline HTML markup...")
        val cleanText = htmlContent
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<h[1-6][^>]*>", RegexOption.IGNORE_CASE), "\n### ")
            .replace(Regex("</h[1-6]>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<li[^>]*>", RegexOption.IGNORE_CASE), "\n• ")
            .replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")

        val lines = cleanText.lines().filter { it.isNotBlank() }

        onProgress(0.6f, "Rendering HTML text to PDF document...")

        val pdfDoc = PdfDocument()
        val pageWidth = 595 // A4
        val pageHeight = 842
        val margin = 45f
        val printableWidth = pageWidth - (margin * 2)

        val textPaint = Paint().apply {
            color = Color.rgb(30, 30, 30)
            textSize = 11f
            isAntiAlias = true
        }

        val headingPaint = Paint().apply {
            color = Color.rgb(10, 40, 90)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas
        var y = margin + 20f

        for (rawLine in lines) {
            val isHeading = rawLine.startsWith("### ")
            val lineContent = if (isHeading) rawLine.removePrefix("### ") else rawLine
            val currentPaint = if (isHeading) headingPaint else textPaint
            val lineHeight = if (isHeading) 22f else 16f

            val words = lineContent.split(" ")
            var curLine = ""

            for (w in words) {
                val test = if (curLine.isEmpty()) w else "$curLine $w"
                if (currentPaint.measureText(test) > printableWidth) {
                    if (y + lineHeight > pageHeight - margin) {
                        pdfDoc.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDoc.startPage(pageInfo)
                        canvas = page.canvas
                        y = margin + 20f
                    }
                    canvas.drawText(curLine, margin, y, currentPaint)
                    y += lineHeight
                    curLine = w
                } else {
                    curLine = test
                }
            }

            if (curLine.isNotEmpty()) {
                if (y + lineHeight > pageHeight - margin) {
                    pdfDoc.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin + 20f
                }
                canvas.drawText(curLine, margin, y, currentPaint)
                y += lineHeight + 4f
            }
        }

        pdfDoc.finishPage(page)

        val outFile = storageService.createTempFile("converted_html", "pdf")
        FileOutputStream(outFile).use { fos ->
            pdfDoc.writeTo(fos)
        }
        pdfDoc.close()

        onProgress(1.0f, "HTML to PDF complete!")
        outFile
    }

    // ==========================================
    // Helpers
    // ==========================================

    private fun writeZipEntry(zos: ZipOutputStream, entryName: String, content: String) {
        writeZipBytesEntry(zos, entryName, content.toByteArray(Charsets.UTF_8))
    }

    private fun writeZipBytesEntry(zos: ZipOutputStream, entryName: String, bytes: ByteArray) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        zos.write(bytes)
        zos.closeEntry()
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
