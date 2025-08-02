package com.repoint.dashboard.ui

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.LoaderAnimation
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.logic.formatDate
import com.repoint.dashboard.HistoryViewModel
import com.repoint.dashboard.TxHistoryViewModel
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.lightGray
import com.repoint.dependencies.theme.richBlack
import com.repoint.models.sharedmodels.local.RepointWallet
import com.repoint.models.sharedmodels.remote.RepointTransactions
import com.repoint.models.sharedmodels.remote.moralisChainMap
import com.repoint.models.sharedmodels.rpc.TxHistoryItem
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.exp

@Composable
fun TransactionHistoryScreen(
    navController: NavController,
    txHistoryViewModel: TxHistoryViewModel = hiltViewModel(),
    walletAddress: String,
) {

    val txs by txHistoryViewModel.txHistory.collectAsState()
    val isLoading by txHistoryViewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(walletAddress) {
        txHistoryViewModel.loadAllChains(walletAddress)
    }

    var wallets by remember { mutableStateOf<List<RepointWallet>>(emptyList()) }
    val allChains = moralisChainMap.values.toList()

    RepointAppBar("Transactions history", exp = { _, _, _ ->

        if (isLoading) {
            LoaderAnimation()
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(txs) { tx ->
                    TxItem(tx)
                }
            }
        }
        Log.d("history", " wallet address is : $walletAddress")
        Log.d("history", "transactions list is :$txs")

    }, navController = navController)

}

@Composable
fun TxItem(tx: TxHistoryItem) {
    val isSend = !tx.isIncoming
    val label = if (isSend) "Sent" else "Received"
    val targetAddress = if (isSend) tx.to else tx.from

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { expanded = !expanded }
            .heightIn(
                min = 96.dp,
                max = if (expanded) Dp.Unspecified else 128.dp
            ) // 👈 control height
            .border(0.5.dp, lightGray, shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isSend) Color(0xFFFFEEF2) else Color(0xFFE8F5FF),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = if (isSend)
                    painterResource(id = com.repoint.dependencies.R.drawable.ic_send)
                else
                    painterResource(id = com.repoint.dependencies.R.drawable.ic_receive)

                Image(painter = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val shortDate = tx.timestamp.take(10) // refine this later with date parser

                Text(
                    text = "$shortDate - ${if (isSend) "To" else "From"} ${
                        formatAddress(
                            targetAddress
                        )
                    }",
                    fontSize = 14.sp,
                    style = RepointTypography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = "$label",
                    fontSize = 16.sp,
                    style = RepointTypography.titleSmall,
                    color = richBlack
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = tx.asset,
                    fontSize = 16.sp,
                    style = RepointTypography.labelLarge,
                    color = richBlack
                )

                // Future: Add fiat price using CMC API
                Text(
                    text = tx.value + " " + tx.asset, // Placeholder until you add price logic
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 22.dp)) {
                Text(
                    "Hash: ${tx.hash}",
                    style = RepointTypography.labelSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    "Block: ${tx.blockNum}",
                    style = RepointTypography.labelSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    "Category: ${tx.category}",
                    style = RepointTypography.labelSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    "Asset: ${tx.asset}",
                    style = RepointTypography.labelSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    "From: ${tx.from}",
                    style = RepointTypography.labelSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    "To: ${tx.to}",
                    style = RepointTypography.labelSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                tx.rawContract?.address.let {
                    Text(
                        "Contract: $it",
                        style = RepointTypography.labelSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                tx.rawContract?.value?.let {
                    Text(
                        "Raw Value: $it",
                        style = RepointTypography.labelSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                tx.rawContract?.decimal?.let {
                    Text(
                        "Decimals: $it",
                        style = RepointTypography.labelSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun TransactionItem(transaction: RepointTransactions, balance: Float) {

    val nativeTransfer = transaction.nativeTransfers.firstOrNull()
    val erc20Transfer = transaction.erc20Transfers?.firstOrNull()
    val transfer = nativeTransfer ?: erc20Transfer

    if (transfer != null) {
        // render shared info from `transfer`


        val scrollState = rememberScrollState()
        val speed = 10 // Adjust speed (Higher = Slower)

        // Auto-scroll effect (simulate marquee)
        LaunchedEffect(Unit) {
            while (true) {
                scrollState.animateScrollTo(
                    scrollState.maxValue,
                    animationSpec = tween(durationMillis = speed * 1000)
                )
                scrollState.animateScrollTo(
                    0,
                    animationSpec = tween(durationMillis = speed * 1000)
                ) // Reset Back
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(0.5.dp, lightGray, RoundedCornerShape(12.dp)), // More balanced padding
            colors = CardDefaults.cardColors(Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon =
                    if (transaction.category == "send") painterResource(id = com.repoint.dependencies.R.drawable.ic_send)
                    else painterResource(id = com.repoint.dependencies.R.drawable.ic_receive)


                Image(
                    painter = icon,
                    contentDescription = "transfer",
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                val formatedAddress = formatAddress(transaction.toAddress)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatDate(transaction.blockTimeStamp),
                        fontSize = 14.sp,
                        color = richBlack,
                        style = RepointTypography.titleSmall
                    )
                    Text(
                        text = if (transaction.category == "send") "Sent Transfer" else "Received Transfer",
                        fontSize = 16.sp,
                        style = RepointTypography.bodySmall
                    )
                    Text(
                        text = "To: $formatedAddress",
                        style = RepointTypography.titleSmall,
                        color = Color.Gray,
                    )
                }

                Column(horizontalAlignment = Alignment.End)
                {
                    when {
                        nativeTransfer != null -> {
                            val valueFormatted = nativeTransfer.valueFormatted ?: "0"
                            val symbol = nativeTransfer.tokenSymbol ?: ""




                            Text(
                                text = "$valueFormatted $symbol",
                                color = richBlack,
                                style = RepointTypography.labelSmall,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .padding(end = 8.dp)
                            )
                            val usdValue = valueFormatted.toFloatOrNull()?.times(balance) ?: 0f

                            Text(
                                text = "$${" % .2f".format(usdValue)} ",
                                style = RepointTypography.titleSmall,
                                color = Color.Gray,
                                textAlign = TextAlign.End,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        //erc20
                        erc20Transfer != null -> {
                            val valueFormatted = erc20Transfer.value ?: "0"
                            val symbol = erc20Transfer.tokenSymbol ?: ""

                            Text(
                                text = "$valueFormatted $symbol",
                                color = richBlack,
                                style = RepointTypography.labelSmall,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .padding(end = 8.dp)
                            )

                            val usdValue = valueFormatted.toFloatOrNull()?.times(balance) ?: 0f
                            Text(
                                text = "$${"%.2f".format(usdValue)}",
                                style = RepointTypography.titleSmall,
                                color = Color.Gray,
                                textAlign = TextAlign.End,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        else -> {
                            Text(
                                text = "Unsupported Transfer",
                                style = RepointTypography.bodySmall,
                                color = Color.Red
                            )
                        }
                    }
                }
            }

        }
    }
}

fun formatAddress(address: String): String {
    return if (address.length > 14) {
        "${address.take(7)}...${address.takeLast(7)}"
    } else {
        address // Return full address if too short
    }
}