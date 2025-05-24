package com.repoint.basics.atoms

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.ghostWhite
import com.repoint.dependencies.theme.repointBlue
import kotlinx.coroutines.launch

@Composable
fun AnimatedTabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    indicatorColor: Color = repointBlue
) {
    val coroutineScope = rememberCoroutineScope()
    val tabPositions = remember { mutableStateListOf<TabPosition>() }
    val tabPositionsState = remember { mutableStateListOf<TabPosition>() }

    val indicatorOffset by animateDpAsState(
        targetValue = tabPositionsState.getOrNull(selectedTabIndex)?.left ?: 0.dp
    )

    val indicatorWidth by animateDpAsState(
        targetValue = tabPositionsState.getOrNull(selectedTabIndex)?.width ?: 0.dp
    )

    Column(modifier = modifier) {

        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = {
                // 'this' is TabIndicatorScope, use tabPositions from scope
                tabPositionsState.clear()
                tabPositionsState.addAll(tabPositions)

                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.BottomStart)
                        .offset(x = indicatorOffset)
                        .width(indicatorWidth)
                        .height(2.dp)
                        .background(indicatorColor, RoundedCornerShape(1.dp))
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = index == selectedTabIndex,
                    onClick = {
                        coroutineScope.launch { onTabSelected(index) }
                    },
                    text = {
                        Text(
                            title,
                            style = RepointTypography.titleSmall.copy(
                                color = if (index == selectedTabIndex) repointBlue else Color.Gray,
                                fontWeight = if (index == selectedTabIndex) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                )
            }
        }
    }
}
