package com.repoint.app.di

import android.content.Context
import com.repoint.splash.accountmanager.SpManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {



    @Provides
    @Singleton
    fun provideSpManager(@ApplicationContext context : Context) : SpManager{
        return SpManager(context)
    }

}