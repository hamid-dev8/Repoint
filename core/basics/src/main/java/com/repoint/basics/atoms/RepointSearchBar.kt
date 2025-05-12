package com.repoint.basics.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.ghostWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepointSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    content: @Composable () -> Unit // 🔥 ADD THIS
) {

    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch, // 🔥 Handle search actions here
        active = active,
        onActiveChange = onActiveChange,
        placeholder = { Text("Search crypto") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                }
            }
        },
        colors = SearchBarDefaults.colors(
            containerColor = ghostWhite.copy(0.9f)
        ),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 🔥 Required content inside SearchBar (e.g., recent search history, suggestions)
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp).background(ghostWhite)) {
            Text(text = "No recent searches", color = Color.Red)
        }
        content()
    }
}
