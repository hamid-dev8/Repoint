
package com.repoint.basics.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.PurpleGrey80
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.repointBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPagerRobot() {

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    val carouselState = rememberCarouselState(initialItem = 0) { 5 }
// Track the current item manually
    var currentItemIndex by remember { mutableIntStateOf(0) }

/*    LaunchedEffect(carouselState) {
        snapshotFlow { carouselState.sc.itemInfo.index }
            .collect { index ->
                currentItemIndex = index
            }
    }*/


    val pages = listOf(
        com.repoint.dependencies.R.drawable.crytoimgone,
        com.repoint.dependencies.R.drawable.crytoimgtwo,
        com.repoint.dependencies.R.drawable.crytoimgthree,
        com.repoint.dependencies.R.drawable.crytoimgfour,
        com.repoint.dependencies.R.drawable.crytoimgfive
    )
    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = 300.dp,
        itemSpacing = 4.dp,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) { page ->
        PagerContent(imageRes = pages[page])
    }


}
@Composable
fun PagerContent(imageRes: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ghostWhite)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Carousel Image",
            contentScale = ContentScale.Crop, // 🔥 fills the box without distortion
            modifier = Modifier
                .fillMaxSize()                // 🔥 forces full area usage
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun CarouselIndicator(currentPage: Int, totalPages: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        repeat(totalPages) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (index == currentPage) 10.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (index == currentPage) repointBlue else PurpleGrey80)
            )
        }
    }
}



/* Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "Grow your Asset",
                style = RepointTypography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Activate your bot →",
                style = RepointTypography.titleSmall,
            )
        }*/
/*
    LaunchedEffect(pagerState) {
        while (true)
            delay(20)
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
    }*/