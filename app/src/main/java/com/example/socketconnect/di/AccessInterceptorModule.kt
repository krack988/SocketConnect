package com.example.socketconnect.di

import android.content.Context
import com.example.socketconnect.cross.interceptor.AccessInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AccessInterceptorModule @Inject constructor() {

    @Provides
    @Singleton
    fun provideAccessInterceptor(@ApplicationContext ctx: Context) : AccessInterceptor {
        return AccessInterceptor(ctx)
    }
}