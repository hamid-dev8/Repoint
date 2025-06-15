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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.repoint.account.UserViewModel
import com.repoint.account.WalletViewModel
import com.repoint.basics.atoms.LoaderAnimation
import com.repoint.basics.atoms.RepointAppBar
import com.repoint.basics.atoms.RepointCommonButton
import com.repoint.basics.logic.SendRoutes
import com.repoint.dashboard.TokenViewModel
import com.repoint.dashboard.Web3ViewModel
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.dependencies.theme.PurpleGrey80
import com.repoint.dependencies.theme.RepointTypography
import com.repoint.dependencies.theme.richBlack
import com.repoint.models.sharedmodels.ui.TxState
import kotlinx.coroutines.launch
import org.web3j.crypto.Credentials
import java.math.BigInteger
import java.math.RoundingMode


@Composable
fun SendTokenScreen(
    walletAddress: String,
    tokenBalance: String,
    coinType: Int,
    contractAddress: String,
    chainId: Int,
    tokenName : String,
    navController: NavController,
    web3ViewModel: Web3ViewModel = hiltViewModel<Web3ViewModel>(),
    walletViewModel: WalletViewModel = hiltViewModel<WalletViewModel>(),
    tokenViewModel: TokenViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel<UserViewModel>()
) {


    Log.d("transaction", "wallet Address is : $walletAddress and  token balance is : $tokenBalance")
    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val context = LocalContext.current
    val gasPriceInGwei by web3ViewModel.gasPrice.collectAsState() // ✅ Observe StateFlow properly

    val coroutineScope = rememberCoroutineScope()

    val spManager = SpManager(context)
    val userIdFlow = spManager.getUserIdFlow().collectAsState(initial = null)
    var credentials by remember { mutableStateOf<Credentials?>(null) }
    val masterWalletId by spManager.getActiveWalletId().collectAsState(initial = null)

    val nativePrice by tokenViewModel.nativeTokenPriceUsd.collectAsState()

    var isSending by remember { mutableStateOf(false) }
    var txHash by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val txState by web3ViewModel.txState.collectAsState()

    //
    LaunchedEffect(walletAddress) {
        val userId = userViewModel.fetchUser()
        Log.d("transaction", "user id is : $userId")
        //  val privateKey = userIdFlow.value?.let { walletViewModel.getAllMasterWallets(it)[0]. }
        Log.d("transaction", "user id flow is : ${userIdFlow.value}")
        Log.d("transaction", "coinType is : $coinType")

        web3ViewModel.fetchGasPrice(chainId.toLong())
        tokenViewModel.getNativeTokenPrice(chainId)

        val chainWallet =
            masterWalletId?.let { walletViewModel.getChainWallet(masterWalletId = it, coinType) }
        val privateKey = chainWallet?.privateKey
       /* val decimalKey = privateKey?.let { BigInteger(it) }
        val hexKey = decimalKey?.toString(16)*/
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
        when(txState){
            is TxState.Success -> {
                val txHash = (txState as TxState.Success).txHash
                Toast.makeText(context,"Transaction sent : $txHash",Toast.LENGTH_SHORT).show()
            }
            is TxState.Error -> {
                Toast.makeText(context,"Transaction failed : ${(txState as TxState.Error).message}",Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }

        //    Log.d("transaction", "crendentials is : $credentials and private key is  : $privateKey")
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
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Start QR Scanner after permission granted
                val intent = Intent(context, QrScannerActivity::class.java)
                qrScannerLauncher.launch(intent)
            } else {
                Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    )
    val estimatedGasFee = remember(gasPriceInGwei, nativePrice) {
        if (gasPriceInGwei != null && nativePrice != null) {
            tokenViewModel.calculateGasFeeUsd(
                gasLimit = BigInteger.valueOf(21999), // Use your actual gasLimit if available
                gasPriceGwei = gasPriceInGwei!!,
                nativeTokenUsdPrice = nativePrice!!
            ).setScale(4, RoundingMode.HALF_UP).toPlainString()
        } else null
    }

    RepointAppBar("send", navController = navController, exp = {_,_,_ ->
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
                            "error",
                            errorMessage
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

                        OutlinedTextField(
                            value = recipientAddress,
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
                                                context,
                                                Manifest.permission.CAMERA
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
                            }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = amount,
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
                                    amount = tokenBalance
                                }) {
                                    Text("Max")
                                }
                            }, modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Available $tokenName : $tokenBalance",
                            style = RepointTypography.bodySmall,
                            color = PurpleGrey80
                        )

                        Text(text = estimatedGasFee?.let { "Estimated Gas Fee: $it USD" }
                            ?: "Estimating fee...",
                            style = RepointTypography.bodyMedium, color = richBlack)
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
                                            amount = amount.toBigDecimal(),
                                            contractAddress = contractAddress,
                                            chainId = chainId.toLong()
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
                                    isSending = false
                                    if (result != null) {
                                        txHash = result
                                    } else {
                                        errorMessage = " Transaction Failed "
                                    }
                                }
                            } else Toast.makeText(
                                context,
                                "fields are empty or Not Correct",
                                Toast.LENGTH_SHORT
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
