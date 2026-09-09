package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PdfTool
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoSecondary

@Composable
fun RepairPdfControl(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("repair_pdf_control"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IndigoSecondary.copy(alpha = 0.08f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IndigoSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = IndigoSecondary)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lenient PDF Structure Rebuilder",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Inspects cross-reference (xref) offsets, recovers corrupted object streams, and re-indexes the document catalog client-side.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun OfficeConversionPreviewControl(
    tool: PdfTool,
    fileName: String,
    modifier: Modifier = Modifier
) {
    val (icon, title, desc, badge) = when (tool) {
        PdfTool.WORD_TO_PDF -> Quad(Icons.Default.Description, "Word to PDF Engine", "Extracts OpenXML XML paragraphs and wraps with standard A4 typography", ".docx -> PDF")
        PdfTool.PPT_TO_PDF -> Quad(Icons.Default.Slideshow, "PowerPoint to PDF Engine", "Extracts OpenXML slide elements and formats into 16:9 presentation PDF", ".pptx -> PDF")
        PdfTool.EXCEL_TO_PDF -> Quad(Icons.Default.TableChart, "Excel to PDF Engine", "Parses worksheet cells and shared strings into clean table grid layouts", ".xlsx -> PDF")
        PdfTool.PDF_TO_WORD -> Quad(Icons.Default.Description, "PDF to Word OpenXML", "Transforms PDF text streams into an editable .docx document file", "PDF -> .docx")
        PdfTool.PDF_TO_PPT -> Quad(Icons.Default.Slideshow, "PDF to PowerPoint Presentation", "Renders each PDF page as high-res slide media inside a .pptx file", "PDF -> .pptx")
        PdfTool.PDF_TO_EXCEL -> Quad(Icons.Default.TableChart, "PDF to Excel Spreadsheet", "Extracts tabular data structures and generates valid .xlsx workbook", "PDF -> .xlsx")
        PdfTool.PDF_TO_PDFA -> Quad(Icons.Default.Policy, "ISO 19005-1 PDF/A Conformance", "Applies archival standards, XMP metadata headers, and sRGB color profile", "Archival PDF/A")
        else -> Quad(Icons.Default.Description, "Local Document Converter", "100% offline conversion engine", "Offline")
    }

    Card(
        modifier = modifier.fillMaxWidth().testTag("office_conversion_control"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = tool.accentColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                Surface(shape = RoundedCornerShape(8.dp), color = tool.accentColor.copy(alpha = 0.15f)) {
                    Text(
                        text = badge,
                        color = tool.accentColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (fileName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = "Target: $fileName",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HtmlEditorControl(
    htmlText: String,
    onHtmlChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("html_editor_control"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = EmeraldTertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("HTML Template / Source", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                // Preset Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = {
                            onHtmlChange("<h1>INVOICE</h1><p>Invoice #: <strong>INV-2026-001</strong><br>Date: Sept 9, 2026</p><hr><p>Items:<br>• Consulting Services: $850.00<br>• Technical Architecture: $1,400.00</p><p><strong>Total Due: $2,250.00</strong></p>")
                        },
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Invoice", fontSize = 10.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            onHtmlChange("<h1>PROJECT AUDIT REPORT</h1><h3>Executive Summary</h3><p>All data and PDF streams inspected locally without external server transmission. Verification passed.</p><h3>Status: APPROVED</h3>")
                        },
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Report", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = htmlText,
                onValueChange = onHtmlChange,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                shape = RoundedCornerShape(10.dp),
                placeholder = { Text("<p>Enter custom HTML markup to render directly to PDF...</p>") }
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
