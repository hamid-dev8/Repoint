package com.repoint.basics.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.lightGray
import com.repoint.dependencies.theme.pureWhite

@Composable
fun ErrorScreen(
    message: String = "Something went wrong",
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    imagePainter: Painter? = null // Optional image to show
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(ghostWhite),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(0.5.dp, lightGray, shape = RoundedCornerShape(22.dp))
                .background(pureWhite)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (imagePainter != null) {
                    Image(
                        painter = imagePainter,
                        contentDescription = "Error Illustration",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Default vector or emoji if no image
                    Text(
                        text = "❌",
                        fontSize = 64.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Try Again",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}
