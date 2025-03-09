package com.repoint.dashboard.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.repoint.basics.atoms.QrScannerScreen
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrScannerActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            QrScannerScreen(
                onQrCodeScanned = { scannedAddress ->
                    val resultIntent = Intent().apply {
                        putExtra("SCANNED_ADDRESS", scannedAddress)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish() // ✅ Close the scanner after scanning
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
