package com.repoint.splash.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.repoint.splash.R
import com.repoint.splash.ui.theme.RepointTheme

class StartScreen
{

    @Preview(showBackground = true)
    @Composable
    private fun PreviewStartScreen(){
        ImageInCenter()
    }

    @Composable
    fun ImageInCenter(){
        RepointTheme {
            Surface(modifier = Modifier.fillMaxSize(), color = colorResource(R.color.purple_200)) {

                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "central image",
                    modifier = Modifier.size(AssistChipDefaults.IconSize).clip(CircleShape),
                    tint = colorResource(R.color.black)
                )

            }
        }
    }

}