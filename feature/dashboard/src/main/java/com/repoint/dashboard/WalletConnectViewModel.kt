package com.repoint.dashboard

import androidx.lifecycle.ViewModel
import com.repoint.sources.datarepo.WalletConnectManagerImp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalletConnectViewModel @Inject constructor(
    private val walletConnectManagerImp: WalletConnectManagerImp
) : ViewModel() {

    val uiState = walletConnectManagerImp.uiState

    fun pairWallet(uri: String) = walletConnectManagerImp.pair(uri)
    fun approvePendingProposal() = walletConnectManagerImp.approvePendingProposal()
    fun rejectPendingProposal() = walletConnectManagerImp.rejectPendingProposal()
    fun updateStatus(message: String) = walletConnectManagerImp.updateStatus(message)
    fun refreshActiveSession() = walletConnectManagerImp.refreshSessionStateFromSdk()
    fun syncWalletSessionWithSelectedWallet() = walletConnectManagerImp.reconcileWalletSessionWithSelectedWallet()
    fun disconnect() = walletConnectManagerImp.disconnect()
}
