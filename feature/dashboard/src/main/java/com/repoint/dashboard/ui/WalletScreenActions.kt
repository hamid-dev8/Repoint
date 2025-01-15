package com.repoint.dashboard.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.repoint.basics.atoms.BalanceScreen
import com.repoint.basics.atoms.CircularButtonWithText
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.SearchTextField
import com.repoint.models.sharedmodels.Tokens
import kotlinx.coroutines.launch
import kotlin.math.exp


@Composable
@Preview(showBackground = true)
fun PreviewActionsRow() {

    //HomeScreen()
}


@Composable
fun HomeScreen(navController: NavController) {


    RepointAppBar("wallet", exp = {
        val sampleList = listOf("wallet1", "wallet2", "wallet3")

        Column(Modifier.fillMaxSize()) {


            SearchTextField("sd", onValueChange = { text ->

            })

            BalanceScreen(sampleList, "12.0$")

            ActionsRow()


            AssetsTabLayout()
        }

    }, navController = navController)


}


@Composable
fun ActionsRow() {

    Row(
        Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        //arrowUp
        CircularButtonWithText(
            icon = Icons.Default.KeyboardArrowUp,
            "Send",
            onClick = { },
            Modifier.padding(12.dp),
            buttonSize = 48.dp
        )
        CircularButtonWithText(
            icon = Icons.Default.KeyboardArrowDown,
            "Receive",
            onClick = { },
            Modifier.padding(12.dp),
            buttonSize = 48.dp
        )
        CircularButtonWithText(
            icon = Icons.Default.Refresh,
            "Swap",
            onClick = { },
            Modifier.padding(12.dp),
            buttonSize = 48.dp
        )
        CircularButtonWithText(
            icon = Icons.Default.AddCircle,
            "To Bot",
            onClick = { },
            Modifier.padding(12.dp),
            buttonSize = 48.dp
        )
        CircularButtonWithText(
            icon = Icons.Default.Warning,
            "History",
            onClick = { },
            Modifier.padding(12.dp),
            buttonSize = 48.dp
        )

    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AssetsTabLayout() {
    val tabs = listOf("Token", "NFTs")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val selectedTabIndex by remember { mutableStateOf(0) }


    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (tabRowRef, pagerRef) = createRefs()

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .constrainAs(tabRowRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            tabs.forEachIndexed { index, title ->

                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = title) },
                )
            }

        }
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .constrainAs(pagerRef) {
                    top.linkTo(tabRowRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
                .padding(32.dp)
        ) { page ->
            val fakeData = generateFakeData(page)
            ListScreen(items = fakeData)
        }

    }

}

@Composable
fun ListScreen(items: List<Tokens>) {

    // val items = List(10) { "item ${it + 1} in tab ${page + 1}" }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
    ) {

        items(items) { item ->

            Text(
                text = item.content, modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        Color.LightGray, shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            )
        }

    }

}

fun generateFakeData(tabIndex: Int): List<Tokens> {
    return List(20) { index ->
        Tokens(
            id = index,
            content = "Item ${index + 1} in Tab ${tabIndex + 1}"
        )
    }
}

