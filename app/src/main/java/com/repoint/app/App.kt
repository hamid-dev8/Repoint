package com.repoint.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.webkit.WebView
import com.repoint.sources.datarepo.datasource.WalletConnectManagerDataSource
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var walletConnectManager:  WalletConnectManagerDataSource

    companion object {
        var appContext: Context? = null
            private set

        fun getContext(): Context? = appContext
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        WebView.setWebContentsDebuggingEnabled(true)
        walletConnectManager.initialize()
    }
}
