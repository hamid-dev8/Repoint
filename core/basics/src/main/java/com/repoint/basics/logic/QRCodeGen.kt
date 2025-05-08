package com.repoint.basics.logic

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.io.File
import java.io.FileOutputStream


fun generateQrCodeBitmap(content: String, size: Int = 1024): Bitmap? {


    return try {
        val bitMatrix: BitMatrix =
            MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }


}

fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
    val file = File(context.cacheDir, "shared_address_pol_repoint.jpg")
    return try {
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        if (file.exists()) {
            Log.d("ShareDebug", "File saved at: ${file.absolutePath}") // ✅ Debug log
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } else {
            Log.e("ShareDebug", "File does not exist!")
            null
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("ShareDebug","EROR is : ${e.message}")
        null
    }
}


fun shareImage(context: Context, file: Uri,walletAddress : String) {

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, file)
        putExtra(Intent.EXTRA_TEXT, "My wallet address: $walletAddress")
        type = "image/jpeg"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}
