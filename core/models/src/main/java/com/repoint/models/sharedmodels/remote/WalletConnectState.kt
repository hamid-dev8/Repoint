package com.repoint.models.sharedmodels.remote

data class WalletConnectState(
    val isInitialized: Boolean = false,
    val isPairing: Boolean = false,
    val status: String = "WalletConnect is idle",
    val pendingProposal: PendingProposal? = null,
    val error: String? = null,
    val activeSession : ActiveSession? = null
)
