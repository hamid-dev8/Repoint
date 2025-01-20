package com.repoint.basics.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repoint.dependencies.theme.Purple40
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.repointOrange


@Composable
@Preview
fun PreviewPng() {

    Column(modifier = Modifier.fillMaxWidth()) {

        PngWithText(
            com.repoint.dependencies.R.drawable.orglogo,
            "desc",
            "re-Point",
            repointOrange,
            Modifier
        )

    }
}

@Composable
fun PngWithText(png: Int, desc: String, text: String, textColor: Color, modifier: Modifier) {

    Row() {

        Image(
            painter = painterResource(png),
            contentDescription = desc,
            modifier = modifier
                .size(52.dp)
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically),
            contentScale = ContentScale.Fit
        )

        Text(
            text = text,
            color = textColor,
            fontSize = 18.sp,
            style = RepointTypography.displayLarge,
            textAlign = TextAlign.Center,
            modifier = modifier
                .align(Alignment.CenterVertically)
                .padding(start = 8.dp)
        )
    }
}

@Composable
fun VectorWithText(
    vector: ImageVector,
    desc: String,
    text: String,
    textColor: Color,
    modifier: Modifier,
    onClick : () -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.wrapContentWidth().padding(top = 32.dp).clickable {
            onClick()
        }
    ) {

        Icon(
            imageVector = vector,
            desc,
            Modifier.size(18.dp),
            tint = Purple40
        )

        Text(
            text = text,
            color = textColor,
            style = RepointTypography.titleSmall,
            textAlign = TextAlign.Center,
            modifier = modifier
                .align(Alignment.CenterVertically)
                .padding(start = 8.dp)
        )

    }

}

@Composable
@Preview
fun PreviewBigPng() {
    BigPng(com.repoint.dependencies.R.drawable.wallet)

}

@Composable
fun BigPng(
    png: Int,
    modifier: Modifier = Modifier,
    aspectRatioWidth: Float = 16f,
    aspectRatioHeight: Float = 12f
) {

    Box(
        modifier = modifier
            .wrapContentHeight()
            .aspectRatio(aspectRatioWidth / aspectRatioHeight)
    )
    {
        Image(
            painter = painterResource(png),
            "Logo",
            modifier
                .fillMaxSize()
                .padding(8.dp),
            contentScale = ContentScale.Inside
        )
    }
}