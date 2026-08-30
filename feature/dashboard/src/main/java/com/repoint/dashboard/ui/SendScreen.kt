package com.repoint.dashboard.ui

import com.repoint.dashboard.activity.QrScannerActivity
import android.Manifest
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.DropdownMenuWithSelection
import com.repoint.basics.atoms.GasTierDropdown
import com.repoint.basics.atoms.LoaderAnimation
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.basics.atoms.shimmerEffect
import com.repoint.basics.logic.SendRoutes
import com.repoint.basics.logic.coinTypeFromSlug
import com.repoint.dashboard.AlchemyViewModel
import com.repoint.dashboard.CmcTokenViewModel
import com.repoint.dashboard.TokenViewModel
import com.repoint.dashboard.Web3ViewModel
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.FernGreen
import com.repoint.dependencies.theme.PurpleGrey80
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.RoseWood
import com.repoint.dependencies.theme.darkGray
import com.repoint.dependencies.theme.grayHound
import com.repoint.dependencies.theme.lightGray
import com.repoint.dependencies.theme.richBlack
import com.repoint.dependencies.theme.transparentColor
import com.repoint.models.sharedmodels.remote.TokenMetaData
import com.repoint.models.sharedmodels.ui.TxState
import com.repoint.models.sharedmodels.ui.UiState
import kotlinx.coroutines.launch
import org.web3j.crypto.Credentials
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode


@Composable
fun SendTokenScreen(
    walletAddress: String,
    balance: String,
    coinType: Int,
    contractAddress: String,
    chainId: Int,
    tokenName: String,
    tokenId: Int,
    navController: NavController,
    web3ViewModel: Web3ViewModel = hiltViewModel<Web3ViewModel>(),
    walletViewModel: WalletViewModel = hiltViewModel<WalletViewModel>(),
    alchemyViewModel: AlchemyViewModel = hiltViewModel<AlchemyViewModel>(),
    cmcTokenViewModel: CmcTokenViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel<UserViewModel>()
) {


    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val spManager = SpManager(context)
    val userIdFlow = spManager.getUserIdFlow().collectAsState(initial = null)
    var credentials by remember { mutableStateOf<Credentials?>(null) }
    val masterWalletId by spManager.getActiveWalletId().collectAsState(initial = null)
    //todo
    val nativeBalanceState by alchemyViewModel.nativeBalance.collectAsState()
    val nativeBalanceForChain by alchemyViewModel.nativeBalanceByChain.collectAsState()

    var isSending by remember { mutableStateOf(false) }
    var txHash by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val txState by web3ViewModel.txState.collectAsState()
    val gasTierState by alchemyViewModel.gasPriceTier.collectAsState()
    var selectedTier by remember { mutableStateOf("Average") } // Default tier

    val nativeUsdState by alchemyViewModel.nativeUsdPrice.collectAsState()

    val tokenMeta = cmcTokenViewModel.getTokenMetaFlow(tokenId).collectAsState(initial = null).value
    tokenMeta?.contractAddress?.forEach {
        Log.d(
            "tokenMetaCheck",
            "Known contract: ${it.contractAddress}, slug: ${it.platform?.coin?.slug ?: "unknown"}"
        )
    }
    val balancesState by alchemyViewModel.tokenBalances.collectAsState()
    val alchemyList = (balancesState as? UiState.Success)?.data.orEmpty()
    Log.d(
        "SlugMatch",
        "⚙️ Attempting getSlugFor with: tokenMeta=${tokenMeta}, contract=$contractAddress"
    )
    Log.d("SlugMatch", "🎯 tokenMeta.value = ${tokenMeta}")
    val nativeTokenSymbol = remember(chainId) {
        when (chainId) {
            1 -> "ETH"
            137 -> "MATIC"
            56 -> "BNB"
            else -> "NATIVE"
        }
    }
    val isLoading = gasTierState is UiState.Loading || nativeUsdState is UiState.Loading
    val shimmerOnce = remember { mutableStateOf(true) }
    LaunchedEffect(isLoading) {
        if (!isLoading) shimmerOnce.value = false
    }
    val chainSlug = tokenMeta?.getSlugFor(contractAddress)
    val myBalance = if (tokenMeta != null && chainSlug != null) {
        matchBalance(
            tokenMeta = tokenMeta, balances = alchemyList, currentChain = chainSlug
        )
    } else null
    Log.d("sendScreen", "📦 Available balances:")
    (balancesState as? UiState.Success)?.data?.forEach {
        Log.d(
            "sendScreen",
            "Balance Token: ${it.name}, Addr: ${it.contractAddress}, Chain: ${it.chainSlug}, RawBalance: ${it.tokenBalance} or : $myBalance"
        )
    }

    Log.d("sendScreen", "🧩 Target contract: $contractAddress")
    Log.d("sendScreen", "🔗 Using slug: ${tokenMeta?.getSlugFor(contractAddress)}")

    val balances = tokenMeta?.let {
        val matched = matchBalance(
            tokenMeta = it,
            balances = (balancesState as? UiState.Success)?.data.orEmpty(),
            currentChain = tokenMeta!!.getSlugFor(contractAddress).toString()
        )

        Log.d("SendScreen", "the match balance is : $matched")
    }

    LaunchedEffect(tokenId) {
        if (cmcTokenViewModel.tokenMetasFlow.value.none { it.id == tokenId }) {
            cmcTokenViewModel.loadTokenMetaById(tokenId)
        }
    }
    LaunchedEffect(chainId) {
        alchemyViewModel.fetchGasPriceTiers(chainId.toLong())
        alchemyViewModel.loadNativeUsdPrice(chainId)


    }

    LaunchedEffect(masterWalletId, walletAddress, contractAddress, chainId) {
        if (!walletAddress.isNullOrBlank()) {
            // alchemyViewModel.loadNativeBalance(walletAddress)
            alchemyViewModel.loadNativeBalanceForChain(walletAddress, chainId)
            masterWalletId?.let { alchemyViewModel.loadTokenBalances(it, walletAddress) }
        }
    }
    Log.d(
        "sendScreen",
        "the values that send from chooseTokenScreen are : wallet address is $walletAddress ," + "the balance is $balance + $balances the coinType is $coinType contract address and chain id : $contractAddress + $chainId" + "token name and token id : $tokenName + $tokenId" + "master wallet id : $masterWalletId"
    )

    val balanceState by alchemyViewModel.tokenBalances.collectAsState()

    val currentSlug = remember(tokenMeta, contractAddress) {
        tokenMeta?.getSlugFor(contractAddress) ?: ""
    }

    val resolvedBalance = remember(tokenMeta, balancesState, currentSlug) {
        if (tokenMeta != null && balancesState is UiState.Success) {
            matchBalance(
                tokenMeta = tokenMeta,
                balances = (balancesState as UiState.Success).data,
                currentChain = currentSlug
            )
        } else null
    }

    Log.d("SendScreen", "balance  State is $balanceState")
    Log.d("SendScreen", "resolveBalance  is $resolvedBalance")

    LaunchedEffect(Unit) {
        val userId = userViewModel.fetchUser()
        Log.d("transaction", "user id is : $userId")
        Log.d("transaction", "user id flow is : ${userIdFlow.value}")

        Log.d("transaction", "coinType is : $coinType")

        web3ViewModel.fetchGasPrice(chainId.toLong())

        val chainWallet =
            masterWalletId?.let { walletViewModel.getChainWallet(masterWalletId = it, coinType) }
        val privateKey = chainWallet?.privateKey
        if (!privateKey.isNullOrEmpty()) {
            credentials = Credentials.create(privateKey)
            //todo remove this later on release
            Log.d("transaction", "🧾 ChainWallet address: ${chainWallet?.address}")
            Log.d("transaction", "🔑 Credentials address: ${credentials?.address}")
            Log.d("SendToken", "Credentials created for $walletAddress")
        } else {
            Log.e("SendToken", "Missing private key for wallet $walletAddress")
        }

        //newApproach todo
        when (txState) {
            is TxState.Success -> {
                val txHash = (txState as TxState.Success).txHash
                Toast.makeText(context, "Transaction sent : $txHash", Toast.LENGTH_SHORT).show()
            }

            is TxState.Error -> {
                Toast.makeText(
                    context,
                    "Transaction failed : ${(txState as TxState.Error).message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> Unit
        }
    }

    val isButtonEnabled = recipientAddress.isNotBlank() && amount.isNotBlank()
    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val scannedAddress = result.data?.getStringExtra("SCANNED_ADDRESS")
            if (!scannedAddress.isNullOrEmpty()) recipientAddress = scannedAddress
            else Toast.makeText(context, " Invalid Qr Code", Toast.LENGTH_SHORT).show()
        } else Toast.makeText(context, "QR Scan Canceled", Toast.LENGTH_SHORT).show()
    }

    // Permission Request Launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    val intent = Intent(context, QrScannerActivity::class.java)
                    qrScannerLauncher.launch(intent)
                } else {
                    Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    Log.d("gasDebug", "nativeBalanceState = $nativeBalanceState")
    val estimatedGasFee = run {
        val nativePrice = (nativeUsdState as? UiState.Success)?.data
        val tierData = (gasTierState as? UiState.Success)?.data

        if (nativePrice != null && tierData != null) {
            val selectedPriceGwei = when (selectedTier.lowercase()) {
                "slow" -> tierData.slow
                "fast" -> tierData.fast
                else -> tierData.average
            }

            val gasFee = alchemyViewModel.calculateGasFeeUsd(
                gasLimit = BigInteger.valueOf(21999),
                gasPriceGwei = selectedPriceGwei,
                nativeTokenUsdPrice = nativePrice.toFloat()
            ).setScale(4, RoundingMode.HALF_UP).toPlainString()

            Log.d("GasFeeCalc", "Recomputed fee: $gasFee for $selectedTier")
            gasFee
        } else null
    }
    val nativeGasAmount = run {
        val tierData = (gasTierState as? UiState.Success)?.data
        val gwei = when (selectedTier.lowercase()) {
            "slow" -> tierData?.slow
            "fast" -> tierData?.fast
            else -> tierData?.average
        }

        gwei?.multiply(BigDecimal(21999))
            ?.divide(BigDecimal(1_000_000_000), 9, RoundingMode.HALF_UP)?.toPlainString()
    }

    RepointAppBar("send", navController = navController, exp = { _, _, _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            when {
                isSending -> {
                    LoaderAnimation(modifier = Modifier.align(Alignment.Center))
                }

                txHash != null -> {
                    LaunchedEffect(txHash) {
                        navController.currentBackStackEntry?.savedStateHandle?.set("txHash", txHash)
                        navController.navigate(SendRoutes.ROUTE_SEND_SUCCESS) {
                            popUpTo("sendToken") { inclusive = true }
                        }
                    }
                }

                errorMessage != null -> {
                    LaunchedEffect(errorMessage) {
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "error", errorMessage
                        )
                        navController.navigate(SendRoutes.ROUTE_SEND_ERROR) {
                            popUpTo("sendToken") { inclusive = true }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Send $tokenName", style = RepointTypography.titleMedium)

                        OutlinedTextField(value = recipientAddress,
                            onValueChange = { recipientAddress = it },
                            label = { Text("recipient address") },
                            trailingIcon = {
                                Row {
                                    TextButton(onClick = {
                                        // Get clipboard text
                                        val clipboard =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clipData = clipboard.primaryClip
                                        if (clipData != null && clipData.itemCount > 0) {
                                            val clipboardText =
                                                clipData.getItemAt(0)?.text?.toString()
                                                    ?: "" // Safe null check
                                            if (clipboardText.isNotEmpty()) {
                                                if (isValidWalletAddress(clipboardText)) {
                                                    recipientAddress = clipboardText
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Invalid Wallet Address",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    }) { Text("Paste") }
                                    IconButton(onClick = {
                                        when {
                                            ContextCompat.checkSelfPermission(
                                                context, Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED -> {
                                                // Permission already granted, launch QR Scanner
                                                val intent =
                                                    Intent(context, QrScannerActivity::class.java)
                                                qrScannerLauncher.launch(intent)
                                            }

                                            else -> {
                                                // Request Camera Permission
                                                permissionLauncher.launch(Manifest.permission.CAMERA)
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = "Scan QR"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(value = amount,
                            onValueChange = { input ->
                                // Allow only digits and at most one decimal point
                                if (input.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                    amount = input
                                }
                            },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                TextButton(onClick = {
                                    amount = balance
                                }) {
                                    Text("Max")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Available $tokenName : $balance",
                            style = RepointTypography.bodySmall,
                            color = PurpleGrey80
                        )
                        if (gasTierState is UiState.Success) {
                            val tiers = listOf("Slow", "Average", "Fast")

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(grayHound)
                                        .border(0.5.dp, lightGray, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Gas Speed", style = RepointTypography.bodyMedium)
                                    GasTierDropdown(options = tiers,
                                        selected = selectedTier,
                                        onSelect = { selectedTier = it })
                                }
                                if (isLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(18.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .shimmerEffect()
                                    )
                                } else {
                                    Text(
                                        text = if (nativeGasAmount != null && estimatedGasFee != null) {
                                            "Estimated Gas: $nativeGasAmount $nativeTokenSymbol ≈ $$estimatedGasFee USD"
                                        } else {
                                            "Estimating fee..."
                                        },
                                        style = RepointTypography.bodyMedium,
                                        color = richBlack,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(18.dp)
                                    )
                                }


                                when (nativeBalanceForChain) {
                                    is UiState.Loading -> {
                                        Text("Fetching balance...")
                                    }

                                    is UiState.Success -> {
                                        val nativeBalance =
                                            (nativeBalanceForChain as UiState.Success<BigDecimal>).data
                                        val fee =
                                            estimatedGasFee?.toBigDecimalOrNull() ?: BigDecimal.ZERO
// Normalize decimals to avoid crashes or false comparisons
                                        val normalizedBalance =
                                            nativeBalance.setScale(18, RoundingMode.DOWN)
                                        val normalizedFee = fee.setScale(18, RoundingMode.UP)

                                        Log.d("BalanceCheck", "native: $normalizedBalance vs gas: $normalizedFee")


                                            if (normalizedBalance < normalizedFee) {
                                                Text("⚠️ Not enough native tokens to cover gas fee",style = RepointTypography.titleMedium, color = RoseWood)
                                            } else {
                                                Text("Balance is sufficient for gas", style = RepointTypography.titleMedium, color = FernGreen)
                                                Text(
                                                    "Available balance: ${nativeBalance.toPlainString()} $chainSlug",
                                                    style = RepointTypography.titleMedium,
                                                    color = darkGray
                                                )
                                            }
                                    }

                                    is UiState.Error -> {
                                        Log.d(
                                            "gasUI",
                                            "error is: $errorMessage | USD: $estimatedGasFee"
                                        )
                                        Text(
                                            "Balance unavailable",
                                            style = RepointTypography.titleMedium,
                                            color = RoseWood
                                        )
                                    }
                                }
                            }

                            Log.d("gasUI", "SelectedTier: $selectedTier | USD: $estimatedGasFee")
                        }

                    }

                    RepointCommonButton(
                        text = "Confirm",
                        onClick = {
                            if (recipientAddress.isNotEmpty() && amount.isNotEmpty() && isValidWalletAddress(
                                    recipientAddress
                                )
                            ) {
                                isSending = true
                                coroutineScope.launch {
                                    val result = credentials?.let {
                                        web3ViewModel.sendTokenDynamic(
                                            credentials = it,
                                            recipientAddress = recipientAddress,
                                            tokenMeta = tokenMeta!!,
                                            amount = amount.toBigDecimal(),
                                            contractAddress = contractAddress,
                                            chainId = chainId.toLong(),
                                            chainSlug = chainSlug!!
                                        )
                                    }
                                    Log.d(
                                        "transaction",
                                        "transaction is going to start with credentials : ${credentials?.address} && amount is : $amount && amount.toBigDecimal is : ${amount.toBigDecimal()}"
                                    )
                                    Log.d(
                                        "transaction",
                                        "transaction is going to start with to ADddress : $recipientAddress"
                                    )
                                    Log.d(
                                        "transaction",
                                        "transaction is going to start with tokenMeta of : $tokenMeta"
                                    )
                                    Log.d(
                                        "transaction",
                                        "transaction is going to start with chainSlug of : $chainSlug"
                                    )
                                    isSending = false
                                    if (result != null) {
                                        txHash = result
                                    } else {
                                        errorMessage = " Transaction Failed "
                                    }
                                }
                            } else Toast.makeText(
                                context, "fields are empty or Not Correct", Toast.LENGTH_SHORT
                            ).show()

                        },
                        enabled = isButtonEnabled && credentials != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
            }

        }
    })

}

@Composable
fun SendLoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LoaderAnimation()
        Text("Sending transaction...", modifier = Modifier.padding(top = 150.dp))
    }
}


fun isValidWalletAddress(address: String): Boolean {
    // Example: Validate Ethereum Wallet Address (42 characters, starts with '0x')
    val ethWalletRegex = Regex("^0x[a-fA-F0-9]{40}$")

    return ethWalletRegex.matches(address)
}

fun TokenMetaData.getSlugFor(contractAddress: String): String? {
    Log.w("SlugMatch", "contract Address  is :  $contractAddress")

    val contracts = this.contractAddress.orEmpty()
    val match = contracts.find {
        it.contractAddress.equals(contractAddress, ignoreCase = true)
    }
    Log.w("SlugMatch", "match is :  $match")

    if (match == null) {
        Log.w(
            "SlugMatch",
            "❌ Could not find matching contract for $contractAddress in token ${this.symbol}"
        )
        contracts.forEach {
            Log.d("SlugMatch", "➕ Candidate: ${it.contractAddress}")
        }
    }

    return match?.platform?.coin?.slug ?: match?.platform?.name
}