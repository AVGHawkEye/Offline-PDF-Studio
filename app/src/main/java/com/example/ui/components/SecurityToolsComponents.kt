package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompareDiffResult
import com.example.model.DrawPoint
import com.example.model.RedactionBox
import com.example.ui.theme.BlueInfo
import java.util.UUID

@Composable
fun SignaturePadControl(
    onSignatureCaptured: (Bitmap) -> Unit,
    onClearSignature: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pathPoints = remember { mutableStateListOf<Offset>() }

    Card(
        modifier = modifier.fillMaxWidth().testTag("signature_pad_control"),
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
                    Icon(Icons.Default.Edit, contentDescription = null, tint = BlueInfo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Draw Digital Signature", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        pathPoints.clear()
                        onClearSignature()
                    },
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Sign in the box below using your finger or stylus:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(2.dp, BlueInfo.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                pathPoints.add(offset)
                            },
                            onDrag = { change, _ ->
                                pathPoints.add(change.position)
                            },
                            onDragEnd = {
                                // Generate Bitmap from paths
                                val bmp = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
                                val canvas = AndroidCanvas(bmp)
                                val paint = AndroidPaint().apply {
                                    color = AndroidColor.BLUE
                                    strokeWidth = 6f
                                    style = AndroidPaint.Style.STROKE
                                    strokeCap = AndroidPaint.Cap.ROUND
                                    strokeJoin = AndroidPaint.Join.ROUND
                                    isAntiAlias = true
                                }
                                for (i in 0 until pathPoints.size - 1) {
                                    val p1 = pathPoints[i]
                                    val p2 = pathPoints[i + 1]
                                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
                                }
                                onSignatureCaptured(bmp)
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (i in 0 until pathPoints.size - 1) {
                        drawLine(
                            color = Color.Blue,
                            start = pathPoints[i],
                            end = pathPoints[i + 1],
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                if (pathPoints.isEmpty()) {
                    Text(
                        text = "Sign Here ✍️",
                        color = Color.LightGray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun RedactionToolControl(
    redactionBoxes: List<RedactionBox>,
    onAddBox: (RedactionBox) -> Unit,
    onClearBoxes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("redaction_tool_control"),
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
                    Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = BlueInfo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Permanent Blackout Redaction", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                if (redactionBoxes.isNotEmpty()) {
                    OutlinedButton(onClick = onClearBoxes, modifier = Modifier.height(30.dp)) {
                        Text("Clear Masks", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Redactions irrevocably remove and black-out underlying text, shapes, and images from the PDF stream.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Add Redaction Blackout Preset:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onAddBox(RedactionBox(UUID.randomUUID().toString(), 0, 0.1f, 0.05f, 0.8f, 0.08f))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Header Mask", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = {
                        onAddBox(RedactionBox(UUID.randomUUID().toString(), 0, 0.15f, 0.35f, 0.7f, 0.25f))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Center Text Mask", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = {
                        onAddBox(RedactionBox(UUID.randomUUID().toString(), 0, 0.1f, 0.85f, 0.8f, 0.08f))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Footer Mask", fontSize = 11.sp, color = Color.White)
                }
            }

            if (redactionBoxes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Active Redaction Masks (${redactionBoxes.size}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        redactionBoxes.forEachIndexed { i, b ->
                            Text(
                                "• Mask #${i + 1} on Page ${b.pageIndex + 1}: ${((b.widthRatio * 100).toInt())}% width x ${((b.heightRatio * 100).toInt())}% height",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompareViewerControl(
    results: List<CompareDiffResult>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("compare_viewer_control"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Compare, contentDescription = null, tint = BlueInfo)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Side-by-Side Visual Comparison", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Comparing pages side-by-side with pixel divergence highlighting in magenta:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            results.forEach { res ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Page ${res.pageIndex + 1}", fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (res.differencePercentage > 1f) Color.Magenta.copy(alpha = 0.2f) else Color.Green.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Diff: ${String.format("%.1f", res.differencePercentage)}%",
                                fontWeight = FontWeight.Bold,
                                color = if (res.differencePercentage > 1f) Color.Magenta else Color(0xFF007700),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (res.page1Bitmap != null) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Doc 1", fontSize = 10.sp, color = Color.Gray)
                                Image(
                                    bitmap = res.page1Bitmap.asImageBitmap(),
                                    contentDescription = "Doc 1 Page ${res.pageIndex + 1}",
                                    modifier = Modifier.aspectRatio(0.72f).clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        if (res.diffBitmap != null) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Difference Map", fontSize = 10.sp, color = Color.Magenta, fontWeight = FontWeight.Bold)
                                Image(
                                    bitmap = res.diffBitmap.asImageBitmap(),
                                    contentDescription = "Difference map",
                                    modifier = Modifier.aspectRatio(0.72f).clip(RoundedCornerShape(6.dp)).border(1.dp, Color.Magenta, RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        if (res.page2Bitmap != null) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Doc 2", fontSize = 10.sp, color = Color.Gray)
                                Image(
                                    bitmap = res.page2Bitmap.asImageBitmap(),
                                    contentDescription = "Doc 2 Page ${res.pageIndex + 1}",
                                    modifier = Modifier.aspectRatio(0.72f).clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
