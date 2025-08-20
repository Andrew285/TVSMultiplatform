package org.tutvsisvoyi.testkmpapp.data.network

//actual suspend fun SavePdfFile(pdfBytes: ByteArray, fileName: String, onSaved: (String) -> Unit, onError: (String) -> Unit): String {
//    TODO("Not yet implemented")
//}


actual suspend fun savePdfToDownloads(pdfBytes: ByteArray, fileName: String): Result<String> {
//    return try {
//        val documentsPath = NSSearchPathForDirectoriesInDomains(
//            NSDocumentDirectory, NSUserDomainMask, true
//        ).first() as String
//
//        val filePath = "$documentsPath/$fileName"
//        val nsData = pdfBytes.toNSData()
//
//        val success = nsData.writeToFile(filePath, atomically = true)
//        if (success) {
//            Result.success(filePath)
//        } else {
//            Result.failure(Exception("Failed to write file"))
//        }
//    } catch (e: Exception) {
//        Result.failure(e)
//    }

    throw Exception()
}