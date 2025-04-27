package com.repoint.dashboard.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.twotone.ContentCopy
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.BigBitmap
import com.repoint.basics.atoms.IconWithText
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.WarningBanner
import com.repoint.basics.logic.saveBitmapToFile
import com.repoint.basics.logic.shareImage
import com.repoint.dashboard.NetworkViewModel
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.ghostWhite

@Composable
fun WalletQrCodeScreen(navController: NavController, walletAddress: String,masterWalletId : String,tokenId : Int?,networkViewModel: NetworkViewModel = hiltViewModel()) {

    RepointAppBar("Receive", exp = {

        Log.d("ReceiveScreen","wallet address is : $walletAddress")
        val context = LocalContext.current
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val activeTokens by networkViewModel.activeTokens.collectAsState()
        val activeNetworks by networkViewModel.activeNetworks.collectAsState()
        val selectedToken = activeTokens.firstOrNull { it.tokenId == tokenId }

        networkViewModel.fetchActiveTokens(masterWalletId = masterWalletId)
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



                            if (selectedToken !=null){


                            if (masterWalletId.isBlank()) {
                                Toast.makeText(context, "Wallet ID missing!", Toast.LENGTH_SHORT).show()
                                return@Column
                            }
                            Log.d("receiveScreen" , "image Request : token : ${selectedToken.logoUrl.trim()} ")
                            Log.d("receiveScreen" , "image Request : symbol : ${selectedToken.symbol.trim()} ")
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(selectedToken.logoUrl.trim())
                                    .crossfade(true)
                                    .diskCacheKey(selectedToken.logoUrl) // helps prevent cache miss
                                    .memoryCacheKey(selectedToken.logoUrl).listener(
                                        onError = { request, throwable ->
                                            Log.e(
                                                "COIL_IMAGE",
                                                "Image Load failed : ${selectedToken.logoUrl}",
                                                throwable.throwable
                                            )
                                        },
                                        onSuccess = { _, _ ->
                                            Log.d("COIL_IMAGE", "Image Loaded Successfully ${selectedToken.logoUrl}")
                                        }
                                    )
                                    .build(),
                                contentDescription = selectedToken.name,
                                modifier = Modifier.size(48.dp),
                                contentScale = ContentScale.Fit,
                                placeholder = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder),
                                error = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder)
                            )

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
                                        val clip = ClipData.newPlainText("phrases", walletAddress)
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
                                            shareImage(context, it)
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
    }, navController = navController)


}


@Composable
fun CaptureScreen(
    content: @Composable () -> Unit,
    onBitmapCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val view = remember { ComposeView(context) }

    AndroidView(
        modifier = Modifier.padding(8.dp),
        factory = { view },
        update = { composeView ->
            composeView.setContent {
                content()
            }
            composeView.post {
                val bitmap = Bitmap.createBitmap(
                    composeView.width,
                    composeView.height,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                canvas.drawColor(ghostWhite.toArgb())
                composeView.draw(canvas)
                onBitmapCaptured(bitmap) // Return the captured bitmap
            }
        }
    )
}
