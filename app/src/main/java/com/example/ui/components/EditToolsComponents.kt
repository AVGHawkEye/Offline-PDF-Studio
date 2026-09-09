package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcroFormField
import com.example.model.CropBounds
import com.example.model.PageNumberFormat
import com.example.model.PageNumberPosition
import com.example.model.RotateAngle
import com.example.ui.theme.PurpleAccent

@Composable
fun RotatePdfControl(
    selectedAngle: RotateAngle,
    onSelectAngle: (RotateAngle) -> Unit,
    rotateAll: Boolean,
    onToggleRotateAll: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("rotate_pdf_control"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RotateRight, contentDescription = null, tint = PurpleAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rotation Angle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                RotateAngle.entries.forEachIndexed { index, angle ->
                    SegmentedButton(
                        selected = selectedAngle == angle,
                        onClick = { onSelectAngle(angle) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = RotateAngle.entries.size)
                    ) {
                        Text(angle.label, fontSize = 11.sp, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Apply to All Pages", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Rotate all pages simultaneously", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = rotateAll, onCheckedChange = onToggleRotateAll)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PageNumbersControl(
    position: PageNumberPosition,
    onPositionChange: (PageNumberPosition) -> Unit,
    format: PageNumberFormat,
    onFormatChange: (PageNumberFormat) -> Unit,
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("page_numbers_control"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = PurpleAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Page Numbering Layout", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Stamp Position", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PageNumberPosition.entries.forEach { pos ->
                    FilterChip(
                        selected = position == pos,
                        onClick = { onPositionChange(pos) },
                        label = { Text(pos.label, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Number Format", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PageNumberFormat.entries.forEach { fmt ->
                    FilterChip(
                        selected = format == fmt,
                        onClick = { onFormatChange(fmt) },
                        label = { Text(fmt.label, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Font Size: ${fontSize.toInt()} pt", style = MaterialTheme.typography.bodySmall)
            }
            Slider(
                value = fontSize,
                onValueChange = onFontSizeChange,
                valueRange = 8f..18f,
                steps = 4
            )
        }
    }
}

@Composable
fun WatermarkControl(
    text: String,
    onTextChange: (String) -> Unit,
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    angle: Float,
    onAngleChange: (Float) -> Unit,
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("watermark_control"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = PurpleAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Watermark Stamp Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                label = { Text("Watermark Text") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Opacity: ${(opacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
            }
            Slider(value = opacity, onValueChange = onOpacityChange, valueRange = 0.05f..0.7f)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rotation Angle: ${angle.toInt()}°", style = MaterialTheme.typography.bodySmall)
            }
            Slider(value = angle, onValueChange = onAngleChange, valueRange = 0f..90f)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Font Size: ${fontSize.toInt()} pt", style = MaterialTheme.typography.bodySmall)
            }
            Slider(value = fontSize, onValueChange = onFontSizeChange, valueRange = 24f..72f)
        }
    }
}

@Composable
fun CropPdfControl(
    cropBounds: CropBounds,
    onCropChange: (CropBounds) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("crop_pdf_control"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Crop, contentDescription = null, tint = PurpleAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crop Margins & Dimensions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Trim outer whitespace borders across all document pages:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Left Margin Trim: ${(cropBounds.leftRatio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }
            Slider(value = cropBounds.leftRatio, onValueChange = { onCropChange(cropBounds.copy(leftRatio = it)) }, valueRange = 0f..0.25f)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Right Margin Trim: ${((1f - cropBounds.rightRatio) * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }
            Slider(value = cropBounds.rightRatio, onValueChange = { onCropChange(cropBounds.copy(rightRatio = it)) }, valueRange = 0.75f..1f)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Top Margin Trim: ${(cropBounds.topRatio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }
            Slider(value = cropBounds.topRatio, onValueChange = { onCropChange(cropBounds.copy(topRatio = it)) }, valueRange = 0f..0.25f)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bottom Margin Trim: ${((1f - cropBounds.bottomRatio) * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }
            Slider(value = cropBounds.bottomRatio, onValueChange = { onCropChange(cropBounds.copy(bottomRatio = it)) }, valueRange = 0.75f..1f)
        }
    }
}

@Composable
fun PdfFormsControl(
    fields: List<AcroFormField>,
    fieldValues: Map<String, String>,
    onFieldValueChange: (String, String) -> Unit,
    flatten: Boolean,
    onFlattenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("pdf_forms_control"),
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
                    Icon(Icons.Default.DynamicForm, contentDescription = null, tint = PurpleAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Interactive AcroForm Fields", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                Surface(shape = RoundedCornerShape(8.dp), color = PurpleAccent.copy(alpha = 0.15f)) {
                    Text(
                        text = "${fields.size} Fields",
                        color = PurpleAccent,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (fields.isEmpty()) {
                Text(
                    text = "No interactive AcroForm fields detected in this document.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    fields.forEach { field ->
                        val currentVal = fieldValues[field.fullyQualifiedName] ?: field.value
                        if (field.isCheckbox) {
                            val isChecked = currentVal.equals("true", ignoreCase = true)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(field.partialName, style = MaterialTheme.typography.bodyMedium)
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onFieldValueChange(field.fullyQualifiedName, it.toString()) }
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = { onFieldValueChange(field.fullyQualifiedName, it) },
                                label = { Text(field.partialName) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Flatten Form Fields", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Bake interactive inputs into static, non-editable graphics", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = flatten, onCheckedChange = onFlattenChange)
            }
        }
    }
}
