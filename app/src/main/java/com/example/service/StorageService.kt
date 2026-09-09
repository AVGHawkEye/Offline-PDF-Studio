package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

data class FileMetadata(
    val name: String,
    val sizeBytes: Long,
    val mimeType: String
)

data class StorageStatsInfo(
    val availableBytes: Long,
    val totalBytes: Long,
    val cacheBytes: Long
)

class StorageService(private val context: Context) {

    private val pdfCacheDir: File by lazy {
        File(context.cacheDir, "pdfs").apply { if (!exists()) mkdirs() }
    }

    private val imageCacheDir: File by lazy {
        File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
    }

    fun createTempFile(prefix: String, suffix: String): File {
        val ext = if (suffix.startsWith(".")) suffix else ".$suffix"
        return File.createTempFile(prefix, ext, pdfCacheDir)
    }

    fun getFileMetadata(uri: Uri): FileMetadata {
        val (name, size) = getFileNameAndSize(uri)
        val mime = context.contentResolver.getType(uri) ?: when {
            name.endsWith(".pdf", true) -> "application/pdf"
            name.endsWith(".jpg", true) || name.endsWith(".jpeg", true) -> "image/jpeg"
            name.endsWith(".png", true) -> "image/png"
            name.endsWith(".docx", true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            name.endsWith(".pptx", true) -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            name.endsWith(".xlsx", true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            name.endsWith(".html", true) || name.endsWith(".htm", true) -> "text/html"
            else -> "application/octet-stream"
        }
        return FileMetadata(name, size, mime)
    }

    fun getStorageStats(): StorageStatsInfo {
        return try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val available = statFs.availableBytes
            val total = statFs.totalBytes
            val cacheSize = getDirSize(context.cacheDir)
            StorageStatsInfo(available, total, cacheSize)
        } catch (e: Exception) {
            StorageStatsInfo(0L, 0L, 0L)
        }
    }

    private fun getDirSize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getDirSize(file) else file.length()
        }
        return size
    }

    fun createTempPdfFile(prefix: String = "pdf_studio_"): File {
        return File.createTempFile(prefix, ".pdf", pdfCacheDir)
    }

    fun createTempImageFile(extension: String = "png"): File {
        return File.createTempFile("pdf_page_", ".$extension", imageCacheDir)
    }

    fun getFileNameAndSize(uri: Uri): Pair<String, Long> {
        var name = "document.pdf"
        var size = 0L
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        cursor.getString(nameIndex)?.let { name = it }
                    }
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {}

        if (size <= 0L) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    size = stream.available().toLong()
                }
            } catch (_: Exception) {}
        }
        return Pair(name, size)
    }

    fun copyUriToTempFile(uri: Uri, targetFile: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                val buffer = ByteArray(32 * 1024)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }
        } ?: throw IllegalStateException("Unable to open input stream for $uri")
    }

    fun saveFileToUri(sourceFile: File, destinationUri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                FileInputStream(sourceFile).use { input ->
                    val buffer = ByteArray(32 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getShareIntent(file: File, mimeType: String = "application/pdf"): Intent {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun getViewIntent(file: File, mimeType: String = "application/pdf"): Intent {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun clearCache() {
        try {
            pdfCacheDir.listFiles()?.forEach { it.delete() }
            imageCacheDir.listFiles()?.forEach { it.delete() }
        } catch (_: Exception) {}
    }

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
            val safeGroup = digitGroups.coerceIn(0, units.size - 1)
            return DecimalFormat("#,##0.#").format(bytes / 1024.0.pow(safeGroup.toDouble())) + " " + units[safeGroup]
        }
    }
}
