package com.repoint.basics.atoms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.repoint.basics.logic.ScreenActions


@Preview(showBackground = true)
@Composable
fun TabLayoutPreview() {
    val actions : ScreenActions = object : ScreenActions {
        override fun onButtonClick() {
            TODO("Not yet implemented")
        }

        override fun onItemSelected(itemId: Int) {
            TODO("Not yet implemented")
        }

        override fun onTabSelected(index: Int, title: String) {
            TODO("Not yet implemented")
        }
    }
    BasicTabLayout(actions)
}


@Composable
fun BasicTabLayout(actions : ScreenActions): Int {

    var tabIndex by remember { mutableStateOf(0) }

    val tabs = listOf("Home", "About", "Settings")

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = {
                        tabIndex = index
                        actions.onTabSelected(index,title)
                    },
                    icon = {
                        when (index) {
                            0 -> Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "salam"
                            )

                            1 -> Icon(imageVector = Icons.Default.Info, contentDescription = null)
                            2 -> Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

        }
        return tabIndex
        /* when (tabIndex) {
             0 -> HomeScreen()
             1 -> AboutScreen()
             2 -> SettingsScreen()
         }*/
    }
    return tabIndex
}