package com.repoint.basics.atoms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapCard(iconUrl: String) {

    val networks = listOf("Ethereum", "Polygon", "BSC", "Arbitrum")

    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { coroutineScope.launch { showBottomSheet = true } }
            ) {

            }

        }

    }

}

@Composable
fun NetworkSelector(networkName: String, iconUrl: String) {

    var showDropdown by remember { mutableStateOf(false) }


    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { showDropdown = true}) {

        AsyncImage(
            model = iconUrl,
            contentDescription = "Network Icon",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = networkName, fontSize = 16.sp, color = Color.Black)
        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
    }
}