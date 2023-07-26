package com.example.socketconnect.cross.interceptor

import android.content.Context
import com.example.socketconnect.App
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AccessInterceptor @Inject constructor(private val ctx: Context) : Interceptor {

    private val prefName = "SocketChat"
    override fun intercept(chain: Interceptor.Chain): Response {

        val original = chain.request()

        val pref = ctx.getSharedPreferences(
            prefName,
            Context.MODE_PRIVATE
        )

        val login = pref?.getString(App.loginPrefKey, "").orEmpty()
        val authToken = pref?.getString(App.authPrefKey, "").orEmpty()

        val request = original.newBuilder()
            .header("Authorization", "Basic $authToken")
            .header("user-name", login)
//            .header("Authorization", "Basic UG9sYXJCOmdoYmR0bg==")
//            .header("user-name", "UserName")
            .header("Connection", "Upgrade")

        return chain.proceed(request.build())
    }
}