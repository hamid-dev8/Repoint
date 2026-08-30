package com.repoint.basics.atoms

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun QrScannerScreen(onQrCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    // AtomicBoolean ensures the callback fires only once even across frames
    val scanned = remember { AtomicBoolean(false) }

    AndroidView(
        factory = { ctx ->
            Log.d("QrScanner", "PreviewView factory called")
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                post {
                    startCamera(this, context) { value ->
                        if (scanned.compareAndSet(false, true)) {
                            Log.d("QrScanner", "First QR scan accepted: $value")
                            onQrCodeScanned(value)
                        } else {
                            Log.d("QrScanner", "Duplicate QR scan ignored: $value")
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun startCamera(
    previewView: PreviewView,
    context: Context,
    onQrCodeScanned: (String) -> Unit
) {
    Log.d("QrScanner", "startCamera called")
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val cameraExecutor = Executors.newSingleThreadExecutor()

    cameraProviderFuture.addListener({
        Log.d("QrScanner", "CameraProvider ready")
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImage(imageProxy, onQrCodeScanned)
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as ComponentActivity,
                cameraSelector,
                preview,
                imageAnalyzer
            )
            Log.d("QrScanner", "Camera bound to lifecycle successfully")
        } catch (exc: Exception) {
            Log.e("QrScanner", "Use case binding failed", exc)
        }
    }, ContextCompat.getMainExecutor(context))
}

@OptIn(ExperimentalGetImage::class)
private fun processImage(imageProxy: ImageProxy, onQrCodeScanned: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        Log.w("QrScanner", "processImage: mediaImage is null, closing proxy")
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    val scanner = BarcodeScanning.getClient()

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val qr = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
            if (qr != null) {
                Log.d("QrScanner", "QR barcode found: ${qr.rawValue}")
                qr.rawValue?.let { onQrCodeScanned(it) }
            }
        }
        .addOnFailureListener { e ->
            Log.e("QrScanner", "QR scan frame failed: ${e.message}")
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
