package com.sarah.app.domain.engine

interface DocumentTextExtractor {
    suspend fun extractFromImage(imageBytes: ByteArray): String
    suspend fun extractFromPdf(pdfBytes: ByteArray): String
}
