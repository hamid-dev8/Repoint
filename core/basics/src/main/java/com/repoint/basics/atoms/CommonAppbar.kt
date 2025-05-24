package com.repoint.basics.atoms

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.repoint.basics.R
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.pureWhite

// ✅ FINAL FIXED VERSION: AppBar back button logic and settings integration

@Composable
fun NavigationButton(
    navController: NavController,
    modifier: Modifier = Modifier,
    isSettings: Boolean,
    onSettingsClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    IconButton(
        onClick = {
            if (onBackClick != null) {
                onBackClick()
            } else if (isSettings) {
                onSettingsClick()
            } else {
                navController.popBackStack()
            }
        },
        modifier = modifier
    ) {
        Icon(
            painter = if (onBackClick != null) painterResource(com.repoint.dependencies.R.drawable.back)
            else if (isSettings) painterResource(com.repoint.dependencies.R.drawable.gear)
            else painterResource(com.repoint.dependencies.R.drawable.back),
            contentDescription = if (onBackClick != null) "Back" else if (isSettings) "Settings" else "Back"
        )
    }
}

@Composable
fun EndIconButton(showEndIcon: Boolean, onEndIconClick: () -> Unit) {
    if (showEndIcon) {
        IconButton(onClick = onEndIconClick) {
            Icon(
                painter = painterResource(com.repoint.dependencies.R.drawable.search),
                contentDescription = "Search"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithButton(
    navController: NavController,
    title: String,
    titleVector: Int? = null,
    isSettings: Boolean = false,
    showEndIcon: Boolean = false,
    isHomeScreen: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    onEndIconClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    isSearchActive: Boolean = false,
    onToggleSearch: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            AnimatedContent(targetState = isSearchActive, label = "") { active ->
                if (active) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .animateContentSize()
                    ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = { Text("Search tokens...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .align(Alignment.Center),
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                ),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = grayHound,
                                    focusedContainerColor = grayHound,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp)
                                )
                        }
                } else {
                    titleVector?.let {
                        Icon(
                            painter = painterResource(titleVector),
                            contentDescription = "repoint svg",
                            modifier = Modifier
                                .height(28.dp) // Ensures clean baseline alignment
                                .padding(end = 4.dp),
                            tint = Color.Unspecified // Prevent Compose from recoloring your icon
                        )
                    } ?: Text(text = title, style = RepointTypography.bodySmall)

                }
            }
        },
        navigationIcon = {
            NavigationButton(
                navController = navController,
                isSettings = isSettings,
                onSettingsClick = onSettingsClick,
                onBackClick = if (isSearchActive) onToggleSearch else null
            )
        },
        actions = {
            AnimatedContent(targetState = isSearchActive, label = "") { active ->
                if (!active) {
                    if (isHomeScreen) {
                        IconButton(onClick = onToggleSearch) {
                            Icon(
                                painter = painterResource(com.repoint.dependencies.R.drawable.search),
                                contentDescription = "Search"
                            )
                        }
                    } else {
                        EndIconButton(showEndIcon, onEndIconClick)
                    }
                }
            }
            actions()
        }
    )
}

@Composable
fun RepointAppBar(
    title: String? = null,
    titleVector: Int? = null, // 🔥 Optional Vector Drawable
    navController: NavController,
    isSearchActive: Boolean = false,
    setSearchActive: (Boolean) -> Unit = {},
    isSettings: Boolean = false,
    showEndIcon: Boolean = false,
    isHomeScreen: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onEndIconClick: () -> Unit = {},
    exp: @Composable (isSearchActive: Boolean, searchQuery: String, onSearchQueryChange: (String) -> Unit) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBarWithButton(
                navController = navController,
                title = title ?: "",
                titleVector = titleVector, // 🔥 forward to top bar
                isSettings = isSettings,
                showEndIcon = showEndIcon,
                isHomeScreen = isHomeScreen,
                onSettingsClick = onSettingsClick,
                onBackClick = if (isSearchActive) {
                    {
                        focusManager.clearFocus()
                        setSearchActive(false)
                    }
                } else null,
                onEndIconClick = {
                    setSearchActive(true)
                    onEndIconClick()
                },
                searchQuery = searchQuery,
                isSearchActive = isSearchActive,
                onSearchQueryChange = { searchQuery = it },
                onToggleSearch = {
                    focusManager.clearFocus()
                    setSearchActive(!isSearchActive)
                    if (!isSearchActive) searchQuery = ""
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (isSearchActive) {
                        focusManager.clearFocus()
                        setSearchActive(false)
                    }
                }
        ) {
            exp(isSearchActive, searchQuery) {
                searchQuery = it
            }
            BackHandler(enabled = isSearchActive) {
                focusManager.clearFocus()
                setSearchActive(false)
            }
        }
    }
}

