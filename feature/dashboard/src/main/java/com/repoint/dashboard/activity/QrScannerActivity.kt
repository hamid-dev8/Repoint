package com.repoint.dashboard.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.repoint.basics.atoms.QrScannerScreen
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrScannerActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private var resultDelivered = false  // guard against multiple finish() calls

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d("QrScanner", "Camera permission result: granted=$granted")
        if (granted) setScannerContent()
        else {
            Log.e("QrScanner", "Camera permission denied, finishing")
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("QrScanner", "onCreate")
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("QrScanner", "Camera permission already granted")
            setScannerContent()
        } else {
            Log.d("QrScanner", "Requesting camera permission")
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setScannerContent() {
        Log.d("QrScanner", "setScannerContent called")
        setContent {
            QrScannerScreen(
                onQrCodeScanned = { value ->
                    Log.d("QrScanner", "QR code received on callback thread: $value")
                    // Must deliver result on main thread
                    runOnUiThread {
                        if (!resultDelivered) {
                            resultDelivered = true
                            Log.d("QrScanner", "Delivering result and finishing: $value")
                            setResult(RESULT_OK, Intent().putExtra("SCANNED_ADDRESS", value))
                            finish()
                        } else {
                            Log.d("QrScanner", "Result already delivered, ignoring duplicate")
                        }
                    }
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("QrScanner", "onDestroy, shutting down cameraExecutor")
        cameraExecutor.shutdown()
    }
}
