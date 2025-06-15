package com.repoint.basics.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.RoseWood
import com.repoint.dependencies.theme.darkGray
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.lightGray
import com.repoint.dependencies.theme.pureWhite
import com.repoint.dependencies.theme.repointBlue
import com.repoint.dependencies.theme.repointLightOrange
import com.repoint.dependencies.theme.transparentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepointSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    onClear : () -> Unit = {},
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        active = active,
        onActiveChange = onActiveChange,
        placeholder = { Text("Search crypto") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    onQueryChange("")
                    onClear()
                }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = darkGray)
                }
            }
        },
        colors = SearchBarDefaults.colors(
            containerColor = transparentColor, // transparent container
            dividerColor = transparentColor
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .heightIn(min = 32.dp) // Slim height initially
            // Wrap SearchBar with border & background to create the rounded pill shape
            .border(
                width = 1.dp,
                color = if (active) lightGray else grayHound,
                shape = RoundedCornerShape(22.dp)
            )
            .background(color = grayHound, shape = RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp), // Rounded corners matching border
    ) {
        // Content shown when expanded - improved styling
        if (active) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = grayHound,
                        shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Start typing to search tokens...",
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } else {
                    content()
                }
            }
        }
    }
}