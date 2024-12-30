package com.repoint.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application() {
    lateinit var sharedPreferences: SharedPreferences


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