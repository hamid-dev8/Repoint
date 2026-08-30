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

            _uiState.update {
                it.copy(
                    activeSession = ActiveSession(
                        dAppName = active.metaData?.name ?: "Unknown dApp",
                        dAppUrl = active.metaData?.url ?: "",
                        connectedAddress = address,
                        chainIds = chains
                    ),
                    status = if (it.status.startsWith("Connected to", ignoreCase = true) || it.activeSession != null) {
                        it.status
                    } else {
                        "Connected to ${active.metaData?.name ?: "dApp"}"
                    },
                    error = null
                )
            }
        } catch (t: Throwable) {
            Log.w(TAG, "refreshSessionStateFromSdk failed: ${t.message}", t)
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
            Log.d(TAG, "onSessionRequest: method=${sessionRequest.request.method}")
            _uiState.update { it.copy(status = "Session request: ${sessionRequest.request.method}") }
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
        }

        override fun onConnectionStateChange(state: Wallet.Model.ConnectionState) {
            Log.d(TAG, "onConnectionStateChange: isAvailable=${state.isAvailable} reason=${state.reason}")
        }

        override fun onError(error: Wallet.Model.Error) {
            Log.e(TAG, "onError: ${error.throwable.message}", error.throwable)
            _uiState.update {
                it.copy(
                    isPairing = false,
                    error = "WalletConnect error: ${error.throwable.message ?: error.throwable.javaClass.simpleName}"
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
                connectionType = ConnectionType.AUTOMATIC,
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

            Log.d(TAG, "Calling WalletKit.initialize()")
            WalletKit.initialize(
                params = Wallet.Params.Init(core = CoreClient),
                onSuccess = {
                    Log.d(TAG, "WalletKit.initialize() onSuccess — setting delegate")
                    WalletKit.setWalletDelegate(walletDelegate)
                    isInitialized = true
                    Log.d(TAG, "WalletKit delegate set — SDK ready")
                    restoreActiveSessionIfAny()
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
        Log.d(TAG, "pair() isInitialized=$isInitialized")

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

        pendingSessionProposal = null
        cleanupStalePairing(wcUri)
        _uiState.update { it.copy(isPairing = true, status = "Pairing with WalletConnect session...") }

        WalletKit.pair(
            params = Wallet.Params.Pair(wcUri),
            onSuccess = {
                Log.d(TAG, "WalletKit.pair() onSuccess — waiting for proposal")
                _uiState.update { it.copy(isPairing = false, status = "Pairing request sent. Waiting for proposal...") }
            },
            onError = { error ->
                Log.e(TAG, "WalletKit.pair() onError: ${error.throwable.message}", error.throwable)
                val message = error.throwable.message ?: error.throwable.javaClass.simpleName
                val friendlyMessage = if (message.contains("No proposal") || message.contains("pending session") || message.contains("expired") || message.contains("pairing topic")) {
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
            val methods = required?.methods?.takeIf { it.isNotEmpty() }
                ?: listOf("eth_sendTransaction", "personal_sign", "eth_signTypedData_v4")
            val events = required?.events?.takeIf { it.isNotEmpty() }
                ?: listOf("chainChanged", "accountsChanged")
            val chains = required?.chains?.takeIf { it.isNotEmpty() }
                ?: listOf("eip155:1")
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
