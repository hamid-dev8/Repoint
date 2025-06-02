package com.repoint.dashboard.ui

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.logic.formatDate
import com.repoint.dashboard.HistoryViewModel
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.lightGray
import com.repoint.dependencies.theme.richBlack
import com.repoint.models.sharedmodels.local.RepointWallet
import com.repoint.models.sharedmodels.remote.RepointTransactions
import com.repoint.models.sharedmodels.remote.moralisChainMap
import kotlin.math.exp

@Composable
fun TransactionHistoryScreen(
    navController: NavController,
    historyViewModel: HistoryViewModel = hiltViewModel<HistoryViewModel>(),
    walletViewModel: WalletViewModel = hiltViewModel<WalletViewModel>(),
    userViewmodel: UserViewModel = hiltViewModel<UserViewModel>(),
    walletAddress: String,
    balance: Float
) {

    var wallets by remember { mutableStateOf<List<RepointWallet>>(emptyList()) }
    val allChains = moralisChainMap.values.toList()

    RepointAppBar("Transactions history", exp = {_,_,_ ->


        val transactions by historyViewModel.transactions.observeAsState() // ✅ Observe StateFlow properly

        Log.d("history", " wallet address is : $walletAddress")
        Log.d("history", "transaction is :$transactions")

        LaunchedEffect(Unit) {
            val user = userViewmodel.fetchUser()
            //wallets = user.let { it?.userId?.let { it1 -> walletViewModel.getChainWallet(userit1) }!! }
            // transactions  = historyViewModel.getNativeHistory(address = wallets[0].address, chain = "eth", order = "DESC")
            //historyViewModel.getNativeHistory(walletAddress, chain = "eth", "DESC")
            historyViewModel.getAllChainHistory(address = walletAddress, chains = allChains,"DESC")
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {

            items(transactions ?: emptyList()) { transaction ->
                TransactionItem(transaction, balance)
            }

        }
    }, navController = navController)

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