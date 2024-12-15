package com.repoint.app

import android.app.Application
import android.content.Context
import androidx.core.content.ContextCompat
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application() {

    companion object {

        var appContext: Context? = null
            private set

        fun getContext(): Context? = appContext
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }


}