package com.repoint.dashboard.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.repoint.basics.atoms.shimmerEffect
import com.repoint.dashboard.ThemeViewModel
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.richBlack
import com.repoint.models.sharedmodels.local.TokenEntity
import com.repoint.models.sharedmodels.remote.Token
import com.repoint.models.sharedmodels.remote.TokensBalance

@Composable
fun TokenWithNetworkBadge(
    token: Token,
    networkIcon : String,
    modifier: Modifier = Modifier
) {

    val themeViewModel : ThemeViewModel = hiltViewModel()
    val isDark by themeViewModel.isDarkTheme.collectAsState() // Light/dark detection
    val backgroundColor = if (isDark) ghostWhite else richBlack
    val networkBackgroundColor = if (isDark)  richBlack else ghostWhite

    Box(Modifier.size(48.dp).background(networkBackgroundColor, shape = CircleShape)) {

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(token.logoUrl.trim())
            .crossfade(true)
            .diskCacheKey(token.logoUrl) // helps prevent cache miss
            .memoryCacheKey(token.logoUrl).listener(
                onError = { request, throwable ->
                    Log.e(
                        "COIL_IMAGE",
                        "Image Load failed : ${token.logoUrl}",
                        throwable.throwable
                    )
                },
                onSuccess = { _, _ ->
                    Log.d("COIL_IMAGE", "Image Loaded Successfully ${token.logoUrl}")
                }
            )
            .build(),
        contentDescription = token.name,
        modifier = Modifier.matchParentSize(),
        contentScale = ContentScale.Fit,
        placeholder = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder),
        error = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder)
    )


    Box(modifier = modifier.size(16.dp).align(Alignment.BottomEnd).clip(CircleShape).background(backgroundColor), contentAlignment = Alignment.Center) {

    AsyncImage(
        model = networkIcon.trim(),
        contentDescription = "Network Logo",
        modifier = Modifier
            .size(14.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Inside
    )

    }
    }
}

@Composable
fun TokenWithNetworkDb(
    token: TokenEntity,
    networkIcon : String,
    modifier: Modifier = Modifier,
    onTokenImageLoaded: () -> Unit,
    onNetworkImageLoaded: () -> Unit
) {

    val themeViewModel : ThemeViewModel = hiltViewModel()
    val isDark by themeViewModel.isDarkTheme.collectAsState() // Light/dark detection
    val backgroundColor = if (isDark) ghostWhite else richBlack
    val networkBackgroundColor = if (isDark)  richBlack else ghostWhite

    var tokenLoaded by remember { mutableStateOf(false) }
    var networkLoaded by remember { mutableStateOf(false) }

    Box(Modifier.size(48.dp).background(networkBackgroundColor, shape = CircleShape)) {

        if (!tokenLoaded) {
            // Shimmer placeholder for Token
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .shimmerEffect()
            )
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(token.logoUrl.trim())
                .crossfade(true)
                .diskCacheKey(token.logoUrl) // helps prevent cache miss
                .memoryCacheKey(token.logoUrl).listener(
                    onError = { request, throwable ->
                        Log.e(
                            "COIL_IMAGE",
                            "Image Load failed : ${token.logoUrl}",
                            throwable.throwable
                        )
                        tokenLoaded = false
                    },
                    onSuccess = { _, _ ->
                        tokenLoaded = true
                        Log.d("COIL_IMAGE", "Image Loaded Successfully ${token.logoUrl}")
                    }
                )
                .build(),
            contentDescription = token.name,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit,
            placeholder = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder),
            error = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder)
        )


        Box(modifier = modifier.size(16.dp).align(Alignment.BottomEnd).clip(CircleShape).background(backgroundColor), contentAlignment = Alignment.Center) {
            if (!networkLoaded) {
                // Shimmer placeholder for Token
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .shimmerEffect()
                )
            }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(networkIcon.trim()).crossfade(true).diskCacheKey(networkIcon).memoryCacheKey(token.logoUrl).listener(
                    onError = { request, throwable ->
                        Log.e(
                            "COIL_IMAGE",
                            "Image Load failed : ${token.logoUrl}",
                            throwable.throwable
                        )
                        networkLoaded = false
                    },
                    onSuccess = { _, _ ->
                        networkLoaded = true
                        Log.d("COIL_IMAGE", "Image Loaded Successfully ${token.logoUrl}")
                    }

                ).build(),
                contentDescription = "Network Logo",
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Inside,
                placeholder = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder),
                error = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder)
            )

        }
    }
}

@Composable
fun TokenWithNetworkIcon(
    token: TokenEntity,
    networkIcon : String,
    modifier: Modifier = Modifier,
    onTokenImageLoaded: () -> Unit,
    onNetworkImageLoaded: () -> Unit
) {

    val themeViewModel : ThemeViewModel = hiltViewModel()
    val isDark by themeViewModel.isDarkTheme.collectAsState() // Light/dark detection
    val backgroundColor = if (isDark) ghostWhite else richBlack
    val networkBackgroundColor = if (isDark)  richBlack else ghostWhite

    var tokenLoaded by remember { mutableStateOf(false) }
    var networkLoaded by remember { mutableStateOf(false) }

    Box(Modifier.size(48.dp).background(networkBackgroundColor, shape = CircleShape)) {

        if (!tokenLoaded) {
            // Shimmer placeholder for Token
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .shimmerEffect()
            )
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(token.logoUrl.trim())
                .crossfade(true)
                .diskCacheKey(token.logoUrl) // helps prevent cache miss
                .memoryCacheKey(token.logoUrl).listener(
                    onError = { request, throwable ->
                        Log.e(
                            "COIL_IMAGE",
                            "Image Load failed : ${token.logoUrl}",
                            throwable.throwable
                        )
                        tokenLoaded = false
                    },
                    onSuccess = { _, _ ->
                        tokenLoaded = true
                        Log.d("COIL_IMAGE", "Image Loaded Successfully ${token.logoUrl}")
                    }
                )
                .build(),
            contentDescription = token.name,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit,
            placeholder = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder),
            error = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder)
        )


        Box(modifier = modifier.size(16.dp).align(Alignment.BottomEnd).clip(CircleShape).background(backgroundColor), contentAlignment = Alignment.Center) {
            if (!networkLoaded) {
                // Shimmer placeholder for Token
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .shimmerEffect()
                )
            }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(networkIcon.trim()).crossfade(true).diskCacheKey(networkIcon).memoryCacheKey(token.logoUrl).listener(
                    onError = { request, throwable ->
                        Log.e(
                            "COIL_IMAGE",
                            "Image Load failed : ${token.logoUrl}",
                            throwable.throwable
                        )
                        networkLoaded = false
                    },
                    onSuccess = { _, _ ->
                        networkLoaded = true
                        Log.d("COIL_IMAGE", "Image Loaded Successfully ${token.logoUrl}")
                    }

                ).build(),
                contentDescription = "Network Logo",
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Inside,
                placeholder = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder),
                error = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder)
            )

        }
    }
}

@Composable
fun TokenOfMoralisBadge(token : TokensBalance){
    val themeViewModel : ThemeViewModel = hiltViewModel()
    val isDark by themeViewModel.isDarkTheme.collectAsState() // Light/dark detection
    val backgroundColor = if (isDark) ghostWhite else richBlack
    val networkBackgroundColor = if (isDark)  richBlack else ghostWhite

    var tokenLoaded by remember { mutableStateOf(false) }
    var networkLoaded by remember { mutableStateOf(false) }

    val tokenLogo = token.logo ?: ""
    val networkLogo = if (token.nativeToken) token.logo else  ""
    Box(Modifier.size(48.dp).background(networkBackgroundColor, shape = CircleShape)) {

        if (!tokenLoaded) {
            // Shimmer placeholder for Token
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .shimmerEffect()
            )
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(tokenLogo)
                .crossfade(true)
                .diskCacheKey(tokenLogo) // helps prevent cache miss
                .memoryCacheKey(tokenLogo).listener(
                    onError = { request, throwable ->
                        Log.e(
                            "COIL_IMAGE",
                            "Image Load failed : ${tokenLogo}",
                            throwable.throwable
                        )
                        tokenLoaded = false
                    },
                    onSuccess = { _, _ ->
                        tokenLoaded = true
                        Log.d("COIL_IMAGE", "Image Loaded Successfully ${tokenLogo}")
                    }
                )
                .build(),
            contentDescription = token.name,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit,
            placeholder = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder),
            error = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder)
        )


        Box(modifier = Modifier.size(16.dp).align(Alignment.BottomEnd).clip(CircleShape).background(backgroundColor), contentAlignment = Alignment.Center) {
            if (!networkLoaded) {
                // Shimmer placeholder for Token
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .shimmerEffect()
                )
            }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(networkLogo.trim()).crossfade(true).diskCacheKey(networkLogo).memoryCacheKey(networkLogo).listener(
                    onError = { request, throwable ->
                        Log.e(
                            "COIL_IMAGE",
                            "Image Load failed : ${networkLogo}",
                            throwable.throwable
                        )
                        networkLoaded = false
                    },
                    onSuccess = { _, _ ->
                        networkLoaded = true
                        Log.d("COIL_IMAGE", "Image Loaded Successfully ${networkLogo}")
                    }

                ).build(),
                contentDescription = "Network Logo",
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Inside,
                placeholder = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder),
                error = painterResource(com.repoint.dependencies.R.drawable.ic_placeholder)
            )

        }
    }

}