package com.repoint.dashboard.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.twotone.ContentCopy
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.basics.atoms.BigBitmap
import com.repoint.basics.atoms.IconWithText
import com.repoint.basics.atoms.LoaderAnimation
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.WarningBanner
import com.repoint.basics.logic.saveBitmapToFile
import com.repoint.basics.logic.shareImage
import com.repoint.dashboard.NetworkViewModel
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.ghostWhite

@Composable
fun WalletQrCodeScreen(
    navController: NavController,
    walletAddress: String,
    masterWalletId: String,
    tokenId: Int?,
    networkViewModel: NetworkViewModel = hiltViewModel()
) {

    RepointAppBar("Receive", exp = {_,_,_ ->

        Log.d("ReceiveScreen", "wallet address is : $walletAddress")
        val context = LocalContext.current
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val activeTokens by networkViewModel.activeTokens.collectAsState()
        val activeNetworks by networkViewModel.activeNetworks.collectAsState()
        val selectedToken = activeTokens.firstOrNull { it.tokenId == tokenId }

        var tokenImageLoaded by remember { mutableStateOf(false) }
        var networkImageLoaded by remember { mutableStateOf(false) }
        var isLoading = !tokenImageLoaded && !networkImageLoaded

        LaunchedEffect(masterWalletId) {

            networkViewModel.fetchActiveTokens(masterWalletId = masterWalletId)

            tokenImageLoaded = true
            networkImageLoaded = true
            //isLoading = false
        }


        if (isLoading) {
            // 🔥 Show Loading Animation Centered
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoaderAnimation()
            }
        } else {


            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter,
            ) {

                val viewRef = remember { mutableStateOf<View?>(null) }

                var bitmap by remember { mutableStateOf<Bitmap?>(null) }

                CaptureScreen(
                    content = {
                        WarningBanner(
                            "only Send Polygon(POL) assets to this address , other assets will be lost forever",
                            modifier = Modifier.padding(2.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {


                            if (selectedToken != null) {


                                if (masterWalletId.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Wallet ID missing!",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    return@Column
                                }
                                Log.d(
                                    "receiveScreen",
                                    "image Request : token : ${selectedToken.logoUrl.trim()} "
                                )
                                Log.d(
                                    "receiveScreen",
                                    "image Request : symbol : ${selectedToken.symbol.trim()} "
                                )
                                TokenWithNetworkDb(
                                    token = selectedToken,
                                    activeTokens[0].logoUrl,
                                    onTokenImageLoaded = { tokenImageLoaded = true },
                                    onNetworkImageLoaded = { networkImageLoaded = true })

                                Text(
                                    selectedToken.name,
                                    style = RepointTypography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                            }



                            BigBitmap(
                                aspectRatioWidth = 9.0f,
                                aspectRatioHeight = 9.0f,
                                walletAddress = walletAddress,
                                modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
                            )

                            Text(
                                text = walletAddress,
                                style = RepointTypography.labelMedium,
                                modifier = Modifier.padding(top = 24.dp)
                            )

                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {

                                IconWithText(
                                    modifier = Modifier
                                        .padding(32.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            val clip =
                                                ClipData.newPlainText("phrases", walletAddress)
                                            clipboardManager.setPrimaryClip(clip)
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Copied To Clipboard!",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        },
                                    iconSize = 18.dp,
                                    icon = Icons.TwoTone.ContentCopy,
                                    text = "Copy"
                                )

                                IconWithText(
                                    modifier = Modifier
                                        .padding(32.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            val uri = bitmap?.let { saveBitmapToFile(context, it) }
                                            uri?.let {
                                                shareImage(context, it, walletAddress)
                                            }
                                        },
                                    iconSize = 18.dp,
                                    icon = Icons.Default.Share,
                                    text = "Share"
                                )

                            }
                        }
                    }, onBitmapCaptured = { capturedBitmap ->
                        bitmap = capturedBitmap

                    }
                )


            }
        }
    }, navController = navController)


}

@Composable
fun CaptureScreen(
    content: @Composable () -> Unit,
    onBitmapCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val composeView = remember { ComposeView(context) }
    val activity = context as? Activity

    AndroidView(
        factory = { composeView },
        modifier = Modifier.padding(8.dp),
        update = { view ->
            view.setContent {
                content()
            }
            view.post {
                if (activity != null) {
                    val bitmap =
                        Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                    val location = IntArray(2)
                    view.getLocationInWindow(location)

                    val rect = android.graphics.Rect(
                        location[0],
                        location[1],
                        location[0] + view.width,
                        location[1] + view.height
                    )

                    PixelCopy.request(activity.window, rect, bitmap, { result ->
                        if (result == PixelCopy.SUCCESS) {
                            onBitmapCaptured(bitmap)
                        } else {
                            Log.e("CaptureScreen", "PixelCopy failed with result: $result")
                        }
                    }, Handler(Looper.getMainLooper()))
                } else {
                    Log.e("CaptureScreen", "Context is not an Activity. Cannot use PixelCopy.")
                }
            }
        }
    )
}

