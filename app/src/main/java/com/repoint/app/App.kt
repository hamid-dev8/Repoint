package com.repoint.app

import android.app.Application
import android.content.Context
import androidx.core.content.ContextCompat

class App : Application() {

    companion object {

        var appContext : Context? = null
            private set

        fun getContext() : Context? = appContext
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }



}