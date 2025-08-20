package org.tutvsisvoyi.testkmpapp.data.network

expect object Platform {
    val isAndroid: Boolean
    val isIOS: Boolean
}

expect suspend fun savePdfToDownloads(pdfBytes: ByteArray, fileName: String): Result<String>