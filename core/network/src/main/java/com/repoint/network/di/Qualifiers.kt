package com.repoint.network.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CmcOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CmcRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkRetrofit