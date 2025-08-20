package org.tutvsisvoyi.testkmpapp.data.network

import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import java.io.File

actual object Platform {
    actual val isAndroid: Boolean = true
    actual val isIOS: Boolean = false
}

//@Composable
//actual fun SavePdfFile(pdfBytes: ByteArray, fileName: String, onSaved: (String) -> Unit, onError: (String) -> Unit) {
//    val context = LocalContext.current
//
//    LaunchedEffect(pdfBytes) {
//        try {
//            val result = withContext(Dispatchers.IO) {
//                val reportsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reports")
//                if (!reportsDir.exists()) {
//                    reportsDir.mkdirs()
//                }
//
//                val file = File(reportsDir, fileName)
//                file.writeBytes(pdfBytes)
//                file.absolutePath
//            }
//            onSaved(result)
//        } catch (e: Exception) {
//            onError(e.message ?: "Failed to save file")
//        }
//    }
//}

actual suspend fun savePdfToDownloads(pdfBytes: ByteArray, fileName: String): Result<String> {
    return try {
        // Save to a hardcoded path that works on most Android devices
        val downloadsDir = File("/storage/emulated/0/Download")
        val file = File(downloadsDir, fileName)
        file.writeBytes(pdfBytes)
        Result.success(file.absolutePath)
    } catch (e: Exception) {
        Result.failure(e)
    }
}