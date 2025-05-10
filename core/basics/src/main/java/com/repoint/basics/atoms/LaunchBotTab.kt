package com.repoint.basics.atoms

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun launchBotTab(context: Context, url: String) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(false)
        .setCloseButtonIcon(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)) // Hides icon visually
        .setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder()
                .setToolbarColor(Color.Transparent.toArgb()) // ✅ Use Compose color
                .build()
        )
        .build()
    customTabsIntent.launchUrl(context, Uri.parse(url))
}
