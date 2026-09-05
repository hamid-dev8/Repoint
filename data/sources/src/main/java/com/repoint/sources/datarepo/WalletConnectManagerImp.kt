package com.repoint.sources.datarepo

import android.app.Application
import android.content.Context
import android.util.Log
import com.repoint.sources.BuildConfig
import com.repoint.dependencies.accountmanager.SpManager
import com.repoint.sources.datarepo.datasource.AuthDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.walletkit.client.Wallet
import com.reown.walletkit.client.WalletKit
import com.repoint.models.sharedmodels.remote.ActiveSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import javax.inject.Inject
import javax.inject.Singleton
import com.repoint.sources.datarepo.security.SecureKeyStore
import com.repoint.models.sharedmodels.remote.WalletConnectState
import com.repoint.models.sharedmodels.remote.PendingProposal
import com.repoint.sources.datarepo.datasource.WalletConnectManagerDataSource

private const val TAG = "WalletConnectMgr"



@Singleton
class WalletConnectManagerImp @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authDataSource: AuthDataSource,
    private val spManager: SpManager,
    private val secureKeyStore: SecureKeyStore
)  : WalletConnectManagerDataSource{

    private val _uiState = MutableStateFlow(WalletConnectState())
    val uiState: StateFlow<WalletConnectState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isInitialized = false
    private var pendingSessionProposal: Wallet.Model.SessionProposal? = null
    private var sessionStateSyncJob: kotlinx.coroutines.Job? = null

    fun refreshSessionStateFromSdk() {
        if (!isInitialized) return

        try {
            val sessions = WalletKit.getListOfActiveSessions()
            val active = sessions.firstOrNull()

            if (active == null) {
                _uiState.update { it.copy(activeSession = null) }
                return
            }

            val namespace = active.namespaces["eip155"]
            val address = namespace?.accounts?.firstOrNull()?.split(":")?.lastOrNull().orEmpty()
            val chains = namespace?.chains ?: emptyList()

            val validSession = if (address.isNotBlank()) {
                ActiveSession(
                    dAppName = active.metaData?.name ?: "Unknown dApp",
                    dAppUrl = active.metaData?.url ?: "",
                    connectedAddress = address,
                    chainIds = chains
                )
            } else null

            _uiState.update {
                it.copy(
                    activeSession = validSession,
                    status = if (validSession != null && (it.status.startsWith("Connected to", ignoreCase = true) || it.activeSession != null)) {
                        it.status
                    } else if (validSession != null) {
                        "Connected to ${active.metaData?.name ?: "dApp"}"
                    } else {
                        "WalletConnect session is incomplete or not connected."
                    },
                    error = null
                )
            }
        } catch (t: Throwable) {
            Log.w(TAG, "refreshSessionStateFromSdk failed: ${t.message}", t)
        }
    }

    private fun scheduleSessionStateSync(delayMs: Long = 750L) {
        if (!isInitialized) return

        sessionStateSyncJob?.cancel()
        sessionStateSyncJob = scope.launch {
            kotlinx.coroutines.delay(delayMs)
            refreshSessionStateFromSdk()
        }
    }

    fun reconcileWalletSessionWithSelectedWallet() {
        scope.launch {
            if (!isInitialized) return@launch

            val selectedWalletId = spManager.getActiveWalletId().firstOrNull() ?: return@launch
            val selectedWalletAddress = authDataSource.getChainWallet(masterWalletId = selectedWalletId, coinType = 60)?.address?.lowercase() ?: return@launch

            try {
                val activeSession = WalletKit.getListOfActiveSessions().firstOrNull() ?: run {
                    _uiState.update { it.copy(activeSession = null) }
                    return@launch
                }

                val sessionAddress = activeSession.namespaces["eip155"]
                    ?.accounts
                    ?.firstOrNull()
                    ?.split(":")
                    ?.lastOrNull()
                    ?.lowercase()

                if (sessionAddress != null && sessionAddress != selectedWalletAddress) {
                    Log.d(TAG, "Active WalletConnect session belongs to $sessionAddress but selected wallet is $selectedWalletAddress; disconnecting stale session.")
                    disconnect()
                    return@launch
                }

                refreshSessionStateFromSdk()
            } catch (t: Throwable) {
                Log.w(TAG, "reconcileWalletSessionWithSelectedWallet failed: ${t.message}", t)
            }
        }
    }

    private fun restoreActiveSessionIfAny() {
        refreshSessionStateFromSdk()
    }

    private fun cleanupStalePairing(uri: String) {
        if (!isInitialized) return

        try {
            val pairingTopic = uri
                .removePrefix("wc:")
                .substringBefore("@", missingDelimiterValue = "")
                .ifBlank { null }

            pairingTopic?.let {
                Log.d(TAG, "cleanupStalePairing: deleting stale pairing for topic=$it")
                CoreClient.PairingController.deleteAndUnsubscribePairing(
                    Core.Params.Delete(it),
                    onError = { error ->
                        Log.w(TAG, "cleanupStalePairing delete failed: ${error.throwable.message}", error.throwable)
                    }
                )
            }

            val stalePairings = CoreClient.Pairing.getPairings()
                .filter { it.uri == uri || it.topic == pairingTopic || it.expiry <= System.currentTimeMillis() / 1000 }

            stalePairings.forEach { pairing ->
                Log.d(TAG, "cleanupStalePairing: removing pairing topic=${pairing.topic}")
                CoreClient.PairingController.deleteAndUnsubscribePairing(
                    Core.Params.Delete(pairing.topic),
                    onError = { error ->
                        Log.w(TAG, "cleanupStalePairing delete failed for ${pairing.topic}: ${error.throwable.message}", error.throwable)
                    }
                )
            }
        } catch (t: Throwable) {
            Log.w(TAG, "cleanupStalePairing failed: ${t.message}", t)
        }
    }

    private fun parseRequestParamList(params: String?): List<Any?> {
        if (params.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(params)
            (0 until array.length()).map { index -> array.get(index) }
        } catch (error: JSONException) {
            throw IllegalArgumentException("WalletConnect request parameters are invalid JSON.", error)
        }
    }

    private fun requestParam(params: List<Any?>, index: Int, label: String): String {
        return params.getOrNull(index)
            ?.takeUnless { it == org.json.JSONObject.NULL }
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("No $label was provided.")
    }

    private fun decodeMessagePayload(payload: String): ByteArray {
        val trimmed = payload.trim()
        return if (trimmed.startsWith("0x", ignoreCase = true)) {
            Numeric.hexStringToByteArray(trimmed.removePrefix("0x").removePrefix("0X"))
        } else {
            trimmed.toByteArray(Charsets.UTF_8)
        }
    }

    private fun signatureToHex(signatureData: Sign.SignatureData): String {
        // Ensure v is in Ethereum format (27 or 28)
        val v = if (signatureData.v[0] >= 27.toByte()) signatureData.v[0] else (signatureData.v[0].toInt() + 27).toByte()
        
        val hex = "0x" +
            Numeric.toHexStringNoPrefix(signatureData.r) +
            Numeric.toHexStringNoPrefix(signatureData.s) +
            Numeric.toHexStringNoPrefix(byteArrayOf(v))
        
//        Log.d(TAG, "signatureToHex: v=${v.toInt()} r=${Numeric.toHexStringNoPrefix(signatureData.r).take(10)}... s=${Numeric.toHexStringNoPrefix(signatureData.s).take(10)}...")
        return hex
    }

    private suspend fun signWalletConnectRequest(sessionRequest: Wallet.Model.SessionRequest): Wallet.Model.JsonRpcResponse {
        val method = sessionRequest.request.method
        val params = parseRequestParamList(sessionRequest.request.params)
 
        Log.d(TAG, "🔑 signWalletConnectRequest START: method=$method, paramsCount=${params.size}")
        params.forEachIndexed { idx, param ->
            Log.d(TAG, "  param[$idx]: ${param?.toString()?.take(100)}")
        }
 
        val masterWalletId = spManager.getActiveWalletId().firstOrNull()
        if (masterWalletId.isNullOrBlank()) {
            Log.e(TAG, "❌ No active wallet selected")
            return Wallet.Model.JsonRpcResponse.JsonRpcError(
                sessionRequest.request.id,
                4001,
                "No active wallet selected"
            )
        }
 
        val evmWallet = authDataSource.getChainWallet(masterWalletId = masterWalletId, coinType = 60)
        if (evmWallet == null || evmWallet.privateKey.isBlank()) {
            Log.e(TAG, "❌ No EVM private key available")
            return Wallet.Model.JsonRpcResponse.JsonRpcError(
                sessionRequest.request.id,
                4001,
                "No EVM private key available for signing"
            )
        }
 
        Log.d(TAG, "✅ Signing with address: ${evmWallet.address}")
        val credentials = Credentials.create(evmWallet.privateKey)
 
        val signatureData = try {
            when (method) {
                "personal_sign" -> {
                    val message = requestParam(params, 0, "message payload")
                    val decoded = decodeMessagePayload(message)
                    Log.d(TAG, "  personal_sign: decoded message length=${decoded.size}")
                    Sign.signPrefixedMessage(decoded, credentials.ecKeyPair)
                }
 
                "eth_sign" -> {
                    val message = requestParam(params, 1, "message payload")
                    val decoded = decodeMessagePayload(message)
                    Log.d(TAG, "  eth_sign: decoded message length=${decoded.size}")
                    Sign.signMessage(decoded, credentials.ecKeyPair)
                }
 
                "eth_signTypedData", "eth_signTypedData_v3", "eth_signTypedData_v4" -> {
                    val typedData = requestParam(params, 1, "EIP-712 typed data")
                    Log.d(TAG, "  $method: typed data length=${typedData.length}")
                    Sign.signTypedData(typedData, credentials.ecKeyPair)
                }
 
                else -> {
                    Log.e(TAG, "❌ Unsupported method: $method")
                    return Wallet.Model.JsonRpcResponse.JsonRpcError(
                        sessionRequest.request.id,
                        4200,
                        "Signing method not supported: $method"
                    )
                }
            }
        } catch (error: IllegalArgumentException) {
            Log.e(TAG, "❌ Parameter parsing error: ${error.message}")
            return Wallet.Model.JsonRpcResponse.JsonRpcError(
                sessionRequest.request.id,
                4001,
                error.message ?: "Invalid signing request."
            )
        } catch (error: Exception) {
            Log.e(TAG, "❌ Signing error: ${error.message}", error)
            return Wallet.Model.JsonRpcResponse.JsonRpcError(
                sessionRequest.request.id,
                4001,
                "Signing failed: ${error.message ?: "Unknown error"}"
            )
        }
 
        val signature = signatureToHex(signatureData)
        Log.d(TAG, "✅ Signature produced: ${signature.take(20)}... (length=${signature.length})")
         
        return Wallet.Model.JsonRpcResponse.JsonRpcResult(
            id = sessionRequest.request.id,
            result = signature
        )
    }

    private val walletDelegate = object : WalletKit.WalletDelegate {

        override fun onSessionProposal(
            sessionProposal: Wallet.Model.SessionProposal,
            verifyContext: Wallet.Model.VerifyContext
        ) {
            Log.d(TAG, "onSessionProposal: name=${sessionProposal.name} url=${sessionProposal.url}")
            Log.d(TAG, "onSessionProposal: requiredNamespaces=${sessionProposal.requiredNamespaces}")
            pendingSessionProposal = sessionProposal
            val eip155 = sessionProposal.requiredNamespaces["eip155"]
            _uiState.update {
                it.copy(
                    pendingProposal = PendingProposal(
                        name = sessionProposal.name,
                        url = sessionProposal.url,
                        chains = eip155?.chains ?: emptyList(),
                        methods = eip155?.methods ?: emptyList(),
                        events = eip155?.events ?: emptyList()
                    ),
                    isPairing = false,
                    status = "Session proposal received from ${sessionProposal.name}. Review and approve."
                )
            }
        }

        override fun onSessionRequest(
            sessionRequest: Wallet.Model.SessionRequest,
            verifyContext: Wallet.Model.VerifyContext
        ) {
            Log.d(TAG, "🔔 onSessionRequest RECEIVED: method=${sessionRequest.request.method} topic=${sessionRequest.topic} id=${sessionRequest.request.id}")
            Log.d(TAG, "   params: ${sessionRequest.request.params?.take(150)}")
            _uiState.update { it.copy(status = "Session request: ${sessionRequest.request.method}") }

            scope.launch {
                Log.d(TAG, "🔑 Starting signature process for ${sessionRequest.request.method}")
                val response = try {
                    signWalletConnectRequest(sessionRequest)
                } catch (t: Throwable) {
                    Log.e(TAG, "❌ onSessionRequest signing failed for ${sessionRequest.request.method}: ${t.message}", t)
                    Wallet.Model.JsonRpcResponse.JsonRpcError(
                        id = sessionRequest.request.id,
                        code = 4001,
                        message = "Wallet signing failed: ${t.message ?: t.javaClass.simpleName}"
                    )
                }

                Log.d(TAG, "📤 Sending response back to dApp for method=${sessionRequest.request.method}")
                WalletKit.respondSessionRequest(
                    params = Wallet.Params.SessionRequestResponse(
                        sessionTopic = sessionRequest.topic,
                        jsonRpcResponse = response
                    ),
                    onSuccess = {
                        Log.d(TAG, "✅ respondSessionRequest success for method=${sessionRequest.request.method} id=${sessionRequest.request.id}")
                        _uiState.update { it.copy(status = "WalletConnect request signed successfully.") }
                    },
                    onError = { error ->
                        Log.e(TAG, "❌ respondSessionRequest failed: ${error.throwable.message}", error.throwable)
                        _uiState.update {
                            it.copy(
                                error = "WalletConnect signing failed: ${error.throwable.message ?: error.throwable.javaClass.simpleName}",
                                status = "Failed to respond to WalletConnect request."
                            )
                        }
                    }
                )
            }
        }

        override fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete) {
            Log.d(TAG, "onSessionDelete: $sessionDelete")
            _uiState.update {
                it.copy(
                    activeSession = null,
                    status = "WalletConnect session disconnected.",
                    error = null
                )
            }
        }

        override fun onSessionSettleResponse(sessionSettleResponse: Wallet.Model.SettledSessionResponse) {
            Log.d(TAG, "onSessionSettleResponse: $sessionSettleResponse")

            val session = (sessionSettleResponse as? Wallet.Model.SettledSessionResponse.Result)?.session ?: return
            val eip155 = session.namespaces["eip155"]
            val address = eip155?.accounts?.firstOrNull()?.split(":")?.lastOrNull() ?: ""
            val chains = eip155?.chains ?: emptyList()

            _uiState.update {
                it.copy(
                    activeSession = ActiveSession(
                        dAppName = session.metaData?.name ?: "Unknown dApp",
                        dAppUrl = session.metaData?.url ?: "",
                        connectedAddress = address,
                        chainIds = chains
                    ),
                    status = "Connected to ${session.metaData?.name ?: "dApp"}"
                )
            }
            scheduleSessionStateSync()
        }

        override fun onConnectionStateChange(state: Wallet.Model.ConnectionState) {
            Log.d(TAG, "onConnectionStateChange: isAvailable=${state.isAvailable} reason=${state.reason}")
            if (!state.isAvailable) {
                Log.w(TAG, "⚠️  Relay connection lost: ${state.reason} — reconnecting")
                _uiState.update {
                    it.copy(status = "Relay connection lost. Reconnecting...", error = "Connection interrupted")
                }
                CoreClient.Relay.connect { error: Core.Model.Error ->
                    Log.e(TAG, "Relay reconnect error: ${error.throwable.message}", error.throwable)
                }
            } else {
                Log.d(TAG, "✅ Relay connection restored")
                if (_uiState.value.error?.contains("connection", ignoreCase = true) == true) {
                    _uiState.update { 
                        it.copy(error = null, status = "Relay reconnected")
                    }
                }
                scheduleSessionStateSync()
            }
        }

        override fun onError(error: Wallet.Model.Error) {
            val message = error.throwable.message ?: error.throwable.javaClass.simpleName
            Log.e(TAG, "❌ onError: $message", error.throwable)
            
            // Log relay errors specifically
            if (message.contains("unknown object type", ignoreCase = true)) {
                Log.e(TAG, "🔴 RELAY CORRUPTION DETECTED: $message - triggering relay recovery")
                // The relay has a corrupted state; let it settle then try to recover
                scope.launch {
                    kotlinx.coroutines.delay(3000L)
                    Log.d(TAG, "🔄 Attempting relay state recovery after corruption")
                    try {
                        refreshSessionStateFromSdk()
                        _uiState.update { it.copy(error = null) }
                    } catch (t: Throwable) {
                        Log.e(TAG, "Relay recovery failed: ${t.message}")
                    }
                }
            }
            
            _uiState.update {
                it.copy(
                    isPairing = false,
                    error = "WalletConnect error: $message"
                )
            }
        }

        override fun onProposalExpired(proposal: Wallet.Model.ExpiredProposal) {
            Log.d(TAG, "onProposalExpired: ${proposal.proposerPublicKey}")
            if (pendingSessionProposal?.proposerPublicKey == proposal.proposerPublicKey) {
                pendingSessionProposal = null
                _uiState.update { it.copy(pendingProposal = null, status = "Proposal expired.") }
            }
        }

        override fun onRequestExpired(request: Wallet.Model.ExpiredRequest) {
            Log.d(TAG, "onRequestExpired: $request")
        }

        override fun onSessionExtend(session: Wallet.Model.Session) {
            Log.d(TAG, "onSessionExtend: ${session.topic}")
        }

        override fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse) {
            Log.d(TAG, "onSessionUpdateResponse: $sessionUpdateResponse")
        }
    }

    override fun initialize() {
        Log.d(TAG, "initialize() called, isInitialized=$isInitialized")
        if (isInitialized) return

        try {
            val projectId = BuildConfig.WALLETCONNECT_PROJECT_ID
            Log.d(TAG, "initialize: projectId blank=${projectId.isBlank()}")

            val appMetaData = Core.Model.AppMetaData(
                name = "Repoint",
                description = "Repoint Wallet",
                url = "https://repoint.app",
                icons = listOf("https://repoint.app/icon.png"),
                redirect = "repoint://walletconnect"
            )

            Log.d(TAG, "Calling CoreClient.initialize()")
            CoreClient.initialize(
                application = context.applicationContext as Application,
                projectId = projectId,
                metaData = appMetaData,
                connectionType = ConnectionType.MANUAL,
                onError = { error ->
                    Log.e(TAG, "CoreClient.initialize error: ${error.throwable.message}", error.throwable)
                    _uiState.update {
                        it.copy(
                            isInitialized = false,
                            error = "WalletConnect core setup failed: ${error.throwable.message ?: error.throwable.javaClass.simpleName}"
                        )
                    }
                }
            )

            Log.d(TAG, "Connecting relay (manual mode)")
            CoreClient.Relay.connect { error: Core.Model.Error ->
                Log.e(TAG, "Relay.connect error: ${error.throwable.message}", error.throwable)
            }

            Log.d(TAG, "Calling WalletKit.initialize()")
            WalletKit.initialize(
                params = Wallet.Params.Init(core = CoreClient),
                onSuccess = {
                    Log.d(TAG, "WalletKit.initialize() onSuccess — setting delegate")
                    WalletKit.setWalletDelegate(walletDelegate)
                    isInitialized = true
                    Log.d(TAG, "WalletKit delegate set — SDK ready")
                    restoreActiveSessionIfAny()
                    scheduleSessionStateSync(1200L)
                    _uiState.update {
                        it.copy(
                            isInitialized = true,
                            error = null,
                            status = if (_uiState.value.activeSession != null) {
                                "WalletConnect initialized and restored"
                            } else {
                                "WalletConnect initialized"
                            }
                        )
                    }
                },
                onError = { error ->
                    Log.e(TAG, "WalletKit.initialize() onError: ${error.throwable.message}", error.throwable)
                    _uiState.update {
                        it.copy(
                            isInitialized = false,
                            error = "WalletConnect setup failed: ${error.throwable.message ?: error.throwable.javaClass.simpleName}"
                        )
                    }
                }
            )
        } catch (t: Throwable) {
            Log.e(TAG, "initialize() threw exception: ${t.message}", t)
            _uiState.update {
                it.copy(
                    isInitialized = false,
                    error = "WalletConnect setup failed: ${t.message ?: t.javaClass.simpleName}"
                )
            }
        }
    }

    fun pair(uri: String) {
        val wcUri = uri.trim()
        Log.d(TAG, "pair() called, uri=$wcUri")
        Log.d(TAG, "pair() isInitialized=$isInitialized, isPairing=${_uiState.value.isPairing}")

        if (wcUri.isBlank()) {
            Log.w(TAG, "pair() uri is blank, aborting")
            _uiState.update { it.copy(isPairing = false, status = "No WalletConnect URI found.") }
            return
        }

        if (!isInitialized) {
            Log.w(TAG, "pair() SDK not initialized yet")
            _uiState.update { it.copy(isPairing = false, status = "WalletConnect is not initialized yet!") }
            return
        }

        // Prevent concurrent pairing attempts
        if (_uiState.value.isPairing) {
            Log.w(TAG, "pair() already pairing, ignoring new request")
            _uiState.update { it.copy(status = "Already pairing. Please wait...") }
            return
        }

        pendingSessionProposal = null
        cleanupStalePairing(wcUri)
        _uiState.update { it.copy(isPairing = true, status = "Pairing with WalletConnect session...") }

        scope.launch {
            // Small delay to ensure relay is ready
            kotlinx.coroutines.delay(500L)
            
            WalletKit.pair(
                params = Wallet.Params.Pair(wcUri),
                onSuccess = {
                    Log.d(TAG, "WalletKit.pair() onSuccess — waiting for proposal")
                    _uiState.update { it.copy(isPairing = false, status = "Pairing request sent. Waiting for proposal...") }
                },
                onError = { error ->
                    Log.e(TAG, "WalletKit.pair() onError: ${error.throwable.message}", error.throwable)
                    val message = error.throwable.message ?: error.throwable.javaClass.simpleName
                    val friendlyMessage = if (message.contains("No proposal") || message.contains("pending session") || message.contains("expired") || message.contains("pairing topic") || message.contains("unknown object type") || message.contains("relay")) {
                        "This QR code is expired or already used. Please scan a fresh WalletConnect QR from the dApp."
                    } else {
                        "Pairing failed."
                    }
                    _uiState.update {
                        it.copy(
                            isPairing = false,
                            status = friendlyMessage,
                            error = message
                        )
                    }
                }
            )
        }
    }

    fun approvePendingProposal() {
        Log.d(TAG, "approvePendingProposal() called, pendingProposal=${pendingSessionProposal?.name}")
        val proposal = pendingSessionProposal ?: run {
            Log.w(TAG, "approvePendingProposal() no pending proposal")
            _uiState.update { it.copy(status = "No pending proposal to approve.") }
            return
        }

        scope.launch {
            val masterWalletId = spManager.getActiveWalletId().firstOrNull()
            Log.d(TAG, "approvePendingProposal: masterWalletId=$masterWalletId")

            if (masterWalletId.isNullOrBlank()) {
                Log.e(TAG, "approvePendingProposal: no active wallet")
                _uiState.update {
                    it.copy(status = "Session approval failed.", error = "No active wallet selected.")
                }
                return@launch
            }

            val evmWallet = authDataSource.getChainWallet(masterWalletId = masterWalletId, coinType = 60)
            Log.d(TAG, "approvePendingProposal: evmWallet address=${evmWallet?.address}")

            if (evmWallet == null) {
                Log.e(TAG, "approvePendingProposal: no EVM wallet found")
                _uiState.update {
                    it.copy(status = "Session approval failed.", error = "No Ethereum wallet found.")
                }
                return@launch
            }

            val evmAddress = evmWallet.address
            val required = proposal.requiredNamespaces["eip155"]
            val optional = proposal.optionalNamespaces["eip155"]

            // Approve the union of required + optional methods so the dApp can use
            // any method it announced (e.g. personal_sign for Sign-And-Login).
            val requestedMethods = ((required?.methods ?: emptyList()) + (optional?.methods ?: emptyList())).distinct()
            val methods = requestedMethods.takeIf { it.isNotEmpty() }
                ?: listOf("eth_sendTransaction", "personal_sign", "eth_sign", "eth_signTypedData", "eth_signTypedData_v3", "eth_signTypedData_v4")

            val requestedEvents = ((required?.events ?: emptyList()) + (optional?.events ?: emptyList())).distinct()
            val events = requestedEvents.takeIf { it.isNotEmpty() }
                ?: listOf("chainChanged", "accountsChanged")

            // Approve both Ethereum mainnet and the dApp's chains (e.g. Polygon eip155:137).
            // The EVM key is identical on every EVM chain, so this is safe.
            val requestedChains = ((required?.chains ?: emptyList()) + (optional?.chains ?: emptyList())).distinct()
            val chains = (requestedChains + "eip155:1").distinct()
            val accounts = chains.map { "$it:$evmAddress" }

            Log.d(TAG, "approvePendingProposal: chains=$chains accounts=$accounts methods=$methods")

            // When requiredNamespaces is empty the dApp uses optionalNamespaces or no constraints.
            // generateApprovedNamespaces() throws on empty required, so build namespaces directly.
            val sessionNamespace = mapOf(
                "eip155" to Wallet.Model.Namespace.Session(
                    chains = chains,
                    accounts = accounts,
                    methods = methods,
                    events = events
                )
            )
            val namespaces = if (proposal.requiredNamespaces.isEmpty()) {
                Log.d(TAG, "requiredNamespaces is empty — using manual namespaces")
                sessionNamespace
            } else {
                WalletKit.generateApprovedNamespaces(
                    sessionProposal = proposal,
                    supportedNamespaces = sessionNamespace
                )
            }

            WalletKit.approveSession(
                params = Wallet.Params.SessionApprove(
                    proposerPublicKey = proposal.proposerPublicKey,
                    namespaces = namespaces,
                    relayProtocol = proposal.relayProtocol
                ),
                onSuccess = {
                    Log.d(TAG, "approveSession onSuccess")
                    pendingSessionProposal = null
                    // Set activeSession immediately from the data we already have.
                    // onSessionSettleResponse may overwrite this with richer data later.
                    val immediateAddress = evmAddress
                    val immediateChains = chains
                    _uiState.update {
                        it.copy(
                            pendingProposal = null,
                            status = "Connected to ${proposal.name}",
                            error = null,
                            activeSession = it.activeSession ?: ActiveSession(
                                dAppName = proposal.name,
                                dAppUrl = proposal.url,
                                connectedAddress = immediateAddress,
                                chainIds = immediateChains
                            )
                        )
                    }
                    // Also refresh from the SDK shortly after to get the settled session data
                    scope.launch {
                        kotlinx.coroutines.delay(2000L)
                        refreshSessionStateFromSdk()
                    }
                },
                onError = { error ->
                    Log.e(TAG, "approveSession onError: ${error.throwable.message}", error.throwable)
                    _uiState.update {
                        it.copy(status = "Session approval failed.", error = error.throwable.message ?: error.throwable.javaClass.simpleName)
                    }
                }
            )
        }
    }

    fun rejectPendingProposal() {
        Log.d(TAG, "rejectPendingProposal() called")
        val proposal = pendingSessionProposal ?: run {
            Log.w(TAG, "rejectPendingProposal() no pending proposal")
            _uiState.update { it.copy(status = "No pending proposal to reject.") }
            return
        }

        WalletKit.rejectSession(
            params = Wallet.Params.SessionReject(
                proposerPublicKey = proposal.proposerPublicKey,
                reason = "User rejected the connection request."
            ),
            onSuccess = {
                Log.d(TAG, "rejectSession onSuccess")
                pendingSessionProposal = null
                _uiState.update { it.copy(pendingProposal = null, status = "Session rejected.", error = null) }
            },
            onError = { error ->
                Log.e(TAG, "rejectSession onError: ${error.throwable.message}", error.throwable)
                _uiState.update {
                    it.copy(status = "Failed to reject session.", error = error.throwable.message ?: error.throwable.javaClass.simpleName)
                }
            }
        )
    }

    fun disconnect() {
        Log.d(TAG, "disconnect() called")
        if (!isInitialized) return

        try {
            val activeSessions = WalletKit.getListOfActiveSessions()
            if (activeSessions.isEmpty()) {
                Log.w(TAG, "disconnect: no active sessions")
                _uiState.update { it.copy(activeSession = null, status = "No active session.") }
                return
            }

            activeSessions.forEach { session ->
                Log.d(TAG, "disconnect: disconnecting topic=${session.topic}")
                WalletKit.disconnectSession(
                    params = Wallet.Params.SessionDisconnect(sessionTopic = session.topic),
                    onSuccess = {
                        Log.d(TAG, "disconnect: onSuccess topic=${session.topic}")
                    },
                    onError = { error ->
                        Log.e(TAG, "disconnect: onError ${error.throwable.message}", error.throwable)
                    }
                )
            }

            _uiState.update {
                it.copy(
                    activeSession = null,
                    status = "Disconnected.",
                    error = null
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "disconnect() failed: ${t.message}", t)
            _uiState.update { it.copy(activeSession = null, status = "Disconnected.", error = null) }
        }
    }

    fun updateStatus(message: String) {
        Log.d(TAG, "updateStatus: $message")
        _uiState.update { it.copy(status = message) }
    }
}
