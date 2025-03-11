package com.repoint.network.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MoralisOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MoralisRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkRetrofit