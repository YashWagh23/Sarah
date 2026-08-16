package com.sarah.app.engine

import com.sarah.app.domain.engine.DocumentTextExtractor
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.PDFKit.PDFDocument
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosDocumentTextExtractor : DocumentTextExtractor {

    override suspend fun extractFromPdf(pdfBytes: ByteArray): String {
        if (pdfBytes.isEmpty()) return "Unable to open PDF stream: empty file."
        val nsData = pdfBytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), pdfBytes.size.toULong())
        }
        val pdfDoc = PDFDocument(data = nsData) ?: return "Unable to parse PDF document."
        val pageCount = pdfDoc.pageCount.toInt()
        if (pageCount == 0) return "PDF document has no pages."

        val combinedText = StringBuilder()
        // Extract text from all pages
        for (pageIndex in 0 until pageCount) {
            val page = pdfDoc.pageAtIndex(pageIndex.toLong())
            val pageText = page?.string?.trim()
            if (!pageText.isNullOrBlank()) {
                combinedText.appendLine("--- Page ${pageIndex + 1} ---")
                combinedText.appendLine(pageText)
            }
        }

        return if (combinedText.isBlank()) {
            "PDF scanned successfully, but no text could be extracted."
        } else {
            combinedText.toString().trim()
        }
    }

    override suspend fun extractFromImage(imageBytes: ByteArray): String {
        if (imageBytes.isEmpty()) return "No readable image data."
        val nsData = imageBytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), imageBytes.size.toULong())
        }
        return suspendCoroutine { continuation ->
            val requestHandler = VNImageRequestHandler(data = nsData, options = emptyMap<Any?, Any>())
            val textRequest = VNRecognizeTextRequest { request, error ->
                if (error != null) {
                    continuation.resume("Image OCR error: ${error.localizedDescription}")
                    return@VNRecognizeTextRequest
                }
                val results = request?.results ?: emptyList<Any>()
                val sb = StringBuilder()
                for (observation in results) {
                    val obs = observation as? VNRecognizedTextObservation ?: continue
                    val topCandidates = obs.topCandidates(1u)
                    val text = (topCandidates.firstOrNull() as? VNRecognizedText)?.string
                    if (!text.isNullOrBlank()) {
                        sb.appendLine(text)
                    }
                }
                val resultText = sb.toString().trim()
                if (resultText.isBlank()) {
                    continuation.resume("No readable text detected in the selected image.")
                } else {
                    continuation.resume(resultText)
                }
            }
            textRequest.recognitionLevel = VNRequestTextRecognitionLevelAccurate
            textRequest.usesLanguageCorrection = true

            try {
                requestHandler.performRequests(listOf(textRequest), null)
            } catch (e: Exception) {
                continuation.resume("Image processing failed: ${e.message ?: "Unknown error"}")
            }
        }
    }
}
