package com.example.socketconnect

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    private val prefName = "SocketChat"

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        pref = this.getSharedPreferences(
            prefName,
            Context.MODE_PRIVATE
        )
    }

    companion object {
        var pref: SharedPreferences? = null
        const val loginPrefKey = "loginKey"
        const val authPrefKey = "authKey"
    }

}