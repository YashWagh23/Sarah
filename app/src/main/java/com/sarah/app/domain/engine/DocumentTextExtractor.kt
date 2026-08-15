package com.sarah.app.domain.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.min

class DocumentTextExtractor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractFromImage(context: Context, imageUri: Uri): String = withContext(Dispatchers.IO) {
        val inputImage = InputImage.fromFilePath(context, imageUri)
        recognizeText(inputImage)
    }

    suspend fun extractFromPdf(context: Context, pdfUri: Uri): String = withContext(Dispatchers.IO) {
        // Copy PDF stream to a temporary local file to open with ParcelFileDescriptor
        val tempFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}.pdf")
        try {
            context.contentResolver.openInputStream(pdfUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext "Unable to open PDF stream."

            val fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)
            val pageCount = min(pdfRenderer.pageCount, 5) // Extract up to first 5 pages for academic tasks

            val combinedText = StringBuilder()

            for (pageIndex in 0 until pageCount) {
                val page = pdfRenderer.openPage(pageIndex)
                val width = page.width * 2
                val height = page.height * 2
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val pageText = recognizeText(inputImage)
                if (pageText.isNotBlank()) {
                    combinedText.appendLine("--- Page ${pageIndex + 1} ---")
                    combinedText.appendLine(pageText)
                }
                bitmap.recycle()
            }

            pdfRenderer.close()
            fileDescriptor.close()

            if (combinedText.isBlank()) {
                "PDF scanned successfully, but no text could be extracted."
            } else {
                combinedText.toString().trim()
            }
        } catch (e: Exception) {
            "PDF extraction error: ${e.localizedMessage ?: "Unknown error"}"
        } finally {
            tempFile.delete()
        }
    }

    private suspend fun recognizeText(image: InputImage): String = suspendCancellableCoroutine { continuation ->
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(visionText.text)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
}
