package com.repoint.basics

import java.util.concurrent.atomic.AtomicReference

object WalletConnectDeepLinkBridge {
    private val pendingUri = AtomicReference<String?>(null)

    fun setPendingUri(uri: String?) {
        if (!uri.isNullOrBlank()) {
            pendingUri.set(uri)
        }
    }

    fun peekPendingUri(): String? = pendingUri.get()

    fun consumePendingUri(): String? = pendingUri.getAndSet(null)
}
