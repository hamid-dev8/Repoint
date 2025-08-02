package com.repoint.basics.atoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repoint.dependencies.R
import com.repoint.dependencies.theme.CustomFontFamily
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.darkGray
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.lightGray
import com.repoint.dependencies.theme.repointOrange
import com.repoint.dependencies.theme.transparentColor
import com.repoint.models.sharedmodels.local.MasterWallet
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer


@Composable
fun ActionsRowShimmer(
    shimmer: Shimmer
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shimmer()
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .shimmer(shimmer),
    ) {
        //arrowUp
        CircularCardWithIcon(
            icon = painterResource(R.drawable.wallet_send),
            text = "Send",
            iconSize = 26.dp,
            onClick = {
            },
            modifier = Modifier.weight(1f)
        )
        CircularCardWithIcon(
            icon = painterResource(R.drawable.wallet_receive),
            text = "Receive",
            iconSize = 26.dp,
            onClick = {
            },
            modifier = Modifier.weight(1f)
        )
        CircularCardWithIcon(
            icon = painterResource(R.drawable.swap),
            text = "Swap",
            iconSize = 26.dp,
            onClick = { },
            modifier = Modifier.weight(1f)
        )
        CircularCardWithIcon(
            icon = painterResource(R.drawable.bot),
            text = "To Bot",
            iconSize = 26.dp,
            onClick = {
            },
            modifier = Modifier.weight(1f)
        )
        CircularCardWithIcon(
            icon = painterResource(R.drawable.history),
            text = "History",
            iconSize = 24.dp,
            onClick = {
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BalanceShimmer(shimmer: Shimmer) {
    var isHiddenBalance by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

            DropDownListShimmer(
                shimmer,
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shimmer(shimmer)
                    .padding(horizontal = 4.dp)
            )

        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StyledBalanceText("$0.00", isHiddenBalance)
            Box(
                modifier = Modifier
                    .size(32.dp) // ✅ fixed size prevents jumping
                    .align(Alignment.CenterVertically)
                    .shimmer(shimmer)
                    .clickable { isHiddenBalance = !isHiddenBalance },
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = isHiddenBalance, label = "eye") { show ->
                    Icon(
                        painter = painterResource(id = if (show) com.repoint.dependencies.R.drawable.eye else com.repoint.dependencies.R.drawable.eye_close),
                        contentDescription = "hide_balance",
                        Modifier
                            .padding(start = 6.dp, end = 6.dp, top = 2.dp)
                            .clickable {
                                isHiddenBalance = !isHiddenBalance
                            }
                            .fillMaxSize()
                            .align(Alignment.Center),
                        tint = Color.Gray,
                    )
                }
            }
        }
    }




// ✅ Updated DropDownList with styled circle icon per wallet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownListShimmer(
    shimmer: Shimmer,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var textFieldWidth by remember { mutableStateOf(0) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label =
        "pressScale"
    )

    //  val focusRequester = remember { FocusRequester() }  // <- Create FocusRequester

    val focusManager = LocalFocusManager.current


    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shimmer(shimmer)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
                expanded = false
            }
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = it
                /*  if  (expanded){
                      focusRequester.requestFocus()
                  }
                  else{
                      focusManager.clearFocus()
                  }*/
                //onCLick
            },
            modifier = modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldWidth = coordinates.size.width
                }
                .padding(vertical = 4.dp)
        ) {
            ///hoho
            TextField(
                value = "Loading",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(grayHound)
                    .border(0.5.dp, lightGray, shape = RoundedCornerShape(16.dp))
                    .padding(vertical = 8.dp, horizontal = 2.dp),
                label = {
                    Text(
                        "Switch Wallet",
                        textAlign = TextAlign.Center,
                        style = RepointTypography.bodySmall.copy(
                            color = if (expanded) repointOrange else darkGray
                        )
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationAngle)
                    )
                },
                leadingIcon = {
                    WalletIconGradient(
                        name = "",
                        index = 0
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedContainerColor = transparentColor,
                    unfocusedContainerColor = transparentColor
                ),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = CustomFontFamily,
                    fontWeight = FontWeight.W600
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldWidth.toDp() })
                    .background(Color.White.copy(alpha = 1f))
                    .clip(
                        RoundedCornerShape(12.dp)
                    ) // Set width here
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 4.dp,
                    color = Color.White.copy(alpha = 0.9f), // semi-transparent white background
                    modifier = Modifier.padding(2.dp)
                ) {
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(),
                        exit = shrinkVertically(
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeOut()
                    ) {

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .fillMaxWidth()
                        ) {
                            DropdownMenuItem(
                                modifier = modifier
                                    .background(grayHound)
                                    .clip(RoundedCornerShape(4.dp)),
                                text = {
                                    Text(
                                        "➕ Add New Wallet",
                                        style = RepointTypography.labelSmall
                                    )
                                },
                                onClick = {
                                }
                            )
                        }
                    }
                    //
                }
                //visi
            }
        }
    }
}

