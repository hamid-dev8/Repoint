@file:Suppress("UNREACHABLE_CODE")

package com.repoint.basics.atoms

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.repoint.basics.R
import com.repoint.dependencies.theme.PurpleGrey80
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.aliceBlue
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.repointBlue
import kotlinx.coroutines.delay


@Composable
fun ViewPagerRobot() {

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    LaunchedEffect(pagerState) {
        while (true)
            delay(3000)
        pagerState.animateScrollToPage(
            page = (pagerState.currentPage + 1) % 3
        )
    }

    val pages = listOf(
        com.repoint.dependencies.R.drawable.orange_robot,
        com.repoint.dependencies.R.drawable.solana_img,
        com.repoint.dependencies.R.drawable.hedgehog_ic
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = pages.size,
            modifier = Modifier
                .wrapContentWidth()
                .height(128.dp)
        ) { page ->
            PagerContent(imageRes = pages[page])
        }

        //dots indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            repeat(pages.size){ index ->
                Box(modifier = Modifier.size(if (pagerState.currentPage == index) 12.dp else 6.dp).background(
                    if (pagerState.currentPage == index) repointBlue else PurpleGrey80,
                    shape = RoundedCornerShape(50)
                ).padding(horizontal = 8.dp))
            }
        }
    }
}


@Composable
fun PagerContent(imageRes : Int){

    Box(modifier = Modifier.fillMaxSize().background(ghostWhite).padding(8.dp)){

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Pager Image",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                BasicText(
                    text = "Grow your Asset",
                    style = RepointTypography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicText(
                    text = "Activate your bot →",
                    style = RepointTypography.titleSmall,
                )

            }

        }


    }


}