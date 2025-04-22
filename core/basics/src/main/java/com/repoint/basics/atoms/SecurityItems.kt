package com.repoint.basics.atoms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.repoint.dependencies.theme.RepointTypography


@Composable
fun SecuritySettingItem(
    title : String,
    description : String? = null,
    checked : Boolean,
    onToggle : (Boolean) -> Unit
) {

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {

        Column(Modifier.weight(1f)) {

            Text(title, style = RepointTypography.titleSmall)
            if (!description.isNullOrEmpty()) {

                Text(description, style = RepointTypography.labelSmall, color = Color.Gray)

            }
        }
        Switch(checked,onCheckedChange = onToggle)

    }

}

@Composable
fun TextSettingItem(title : String,subtitle : String,onClick : () -> Unit){

    Column(Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp)) {
        Text(title, style = RepointTypography.titleSmall)
        Text(subtitle, style = RepointTypography.labelSmall, color = Color.Gray)
    }

}