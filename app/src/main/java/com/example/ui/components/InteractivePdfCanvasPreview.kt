package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun InteractivePdfCanvasPreview(
    bitmap: Bitmap?,
    currentPageIndex: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_canvas_preview"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Page navigation & Zoom controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (currentPageIndex > 0) {
                                onPageChange(currentPageIndex - 1)
                                scale = 1f
                                offset = Offset.Zero
                            }
                        },
                        enabled = currentPageIndex > 0,
                        modifier = Modifier.size(36.dp).testTag("btn_prev_page")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Page",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Page ${currentPageIndex + 1} of ${totalPages.coerceAtLeast(1)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = {
                            if (currentPageIndex < totalPages - 1) {
                                onPageChange(currentPageIndex + 1)
                                scale = 1f
                                offset = Offset.Zero
                            }
                        },
                        enabled = currentPageIndex < totalPages - 1,
                        modifier = Modifier.size(36.dp).testTag("btn_next_page")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Page",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Zoom Controls & Close
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { scale = (scale + 0.25f).coerceAtMost(4f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = {
                            scale = (scale - 0.25f).coerceAtLeast(0.75f)
                            if (scale <= 1f) offset = Offset.Zero
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Zoom", modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp).testTag("btn_close_canvas_preview")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close preview", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Interactive Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.75f, 4.0f)
                            offset = if (scale > 1f) {
                                Offset(offset.x + pan.x, offset.y + pan.y)
                            } else {
                                Offset.Zero
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null && !bitmap.isRecycled) {
                    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("pdf_native_canvas")
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val bmpWidth = imageBitmap.width.toFloat()
                        val bmpHeight = imageBitmap.height.toFloat()

                        val fitScale = minOf(canvasWidth / bmpWidth, canvasHeight / bmpHeight) * 0.95f
                        val destWidth = bmpWidth * fitScale
                        val destHeight = bmpHeight * fitScale

                        val left = (canvasWidth - destWidth) / 2f
                        val top = (canvasHeight - destHeight) / 2f

                        translate(left + offset.x, top + offset.y) {
                            scale(scale, pivot = Offset(destWidth / 2f, destHeight / 2f)) {
                                drawImage(
                                    image = imageBitmap,
                                    dstOffset = IntOffset.Zero,
                                    dstSize = IntSize(destWidth.roundToInt(), destHeight.roundToInt())
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rendering page canvas with native PDFRenderer...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Page scrubber slider (if document has multiple pages)
            if (totalPages > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = currentPageIndex.toFloat(),
                        onValueChange = { newVal ->
                            val target = newVal.roundToInt().coerceIn(0, totalPages - 1)
                            if (target != currentPageIndex) {
                                onPageChange(target)
                            }
                        },
                        valueRange = 0f..(totalPages - 1).toFloat(),
                        steps = (totalPages - 2).coerceAtLeast(0),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .testTag("page_scrubber_slider")
                    )
                    Text(
                        text = "$totalPages",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
