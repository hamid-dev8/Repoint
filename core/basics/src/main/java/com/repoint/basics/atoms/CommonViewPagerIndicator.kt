
package com.repoint.basics.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.PurpleGrey80
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
    val gradients = listOf(
        listOf(Color(0xFFff9a9e), Color(0xFFfad0c4)), // Soft pink
        listOf(Color(0xFFa18cd1), Color(0xFFfbc2eb)), // Lavender purple
        listOf(Color(0xFF96e6a1), Color(0xFFd4fc79)), // Mint green
        listOf(Color(0xFFff758c), Color(0xFFff7eb3)), // Rose red
        listOf(Color(0xFFa6c0fe), Color(0xFFf68084)), // Blue coral
        listOf(Color(0xFFfbc2eb), Color(0xFFa6c1ee)), // Purple-pink
        listOf(Color(0xFFfddb92), Color(0xFFd1fdff)), // Pastel yellow to teal
        listOf(Color(0xFFc2e9fb), Color(0xFFa1c4fd)), // Light sky blue
        listOf(Color(0xFFd299c2), Color(0xFFfef9d7)), // Purple cream
        listOf(Color(0xFFfddb92), Color(0xFFd1fdff))  // Golden peach
    )


    val pages = listOf(
        com.repoint.dependencies.R.drawable.crytoimgone,
        com.repoint.dependencies.R.drawable.crytoimgtwo,
        com.repoint.dependencies.R.drawable.crytoimgthree,
        com.repoint.dependencies.R.drawable.crytoimgfour,
        com.repoint.dependencies.R.drawable.crytoimgfive
    )

    HeroCarousel(pages)
    //CircularImageCarousel(images = pages,gradients)
  /*  HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = 300.dp,
        itemSpacing = 4.dp,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) { page ->
        Box(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ghostWhite)
        ) {
            PagerContent(imageRes = pages[page])
        }
    }*/


}
@Composable
fun PagerContent(imageRes: Int) {

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Carousel Image",
            contentScale = ContentScale.Crop, // 🔥 fills the box without distortion
            modifier = Modifier
                .fillMaxSize()                // 🔥 forces full area usage
                .clip(RoundedCornerShape(16.dp))
        )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroCarousel(pages: List<Int>) {
    val carouselState = rememberCarouselState(initialItem = 1) { pages.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = 300.dp,
        itemSpacing = 4.dp,
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) { page ->
        Box(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .maskClip(RoundedCornerShape(16.dp))
                .background(Color.LightGray)
        ) {
            Image(
                painter = painterResource(id = pages[page]),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun CircularImageCarousel(
    images: List<Int>,
    gradients: List<List<Color>> = emptyList()
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        itemsIndexed(images) { index, imageRes ->
            Box(
                modifier = Modifier
                    .aspectRatio(16f / 9f)
                    .width(280.dp) // or use `.fillParentMaxWidth(0.8f)` for responsiveness
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            gradients.getOrNull(index % gradients.size)
                                ?: listOf(Color.LightGray, Color.DarkGray)
                        )
                    )
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Carousel Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
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