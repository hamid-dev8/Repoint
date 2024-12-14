package com.repoint.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.repoint.app.R
import kotlinx.coroutines.delay


@Preview(showBackground = true)
@Composable
private fun PreviewSplashScreen() {

}

@Composable
fun SplashScreen(onTimeout: @Composable () -> Unit) {
    val splashTimeout = 2L

    val isSplashVisible: MutableState<Boolean> = remember { mutableStateOf(true) }


    LaunchedEffect(key1 = true) {
        delay(splashTimeout)
        isSplashVisible.value = false
    }


    if (isSplashVisible.value) {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "central Image",
                modifier = Modifier
                    .size(AssistChipDefaults.IconSize)
                    .clip(CircleShape)
                    .padding(8.dp),
                tint = colorResource(R.color.black)
            )
        }
    }
    else onTimeout()

   /* RepointTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = colorResource(R.color.purple_200)) {

            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "central image",
                modifier = Modifier
                    .size(AssistChipDefaults.IconSize)
                    .clip(CircleShape)
                    .padding(padding),
                tint = colorResource(R.color.black)
            )

        }*/
    }