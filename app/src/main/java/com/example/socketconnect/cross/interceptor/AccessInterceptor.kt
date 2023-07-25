package com.example.socketconnect.cross.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AccessInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val original = chain.request()

        val request = original.newBuilder()
            .header("Authorization", "Basic UG9sYXJCOmdoYmR0bg==")
            .header("user-name", "UserName")
            .header("Connection", "Upgrade")

        return chain.proceed(request.build())
    }
}