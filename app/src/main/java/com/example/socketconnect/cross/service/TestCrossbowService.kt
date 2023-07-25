package com.example.socketconnect.cross.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.socketconnect.R
import com.example.socketconnect.cross.interceptor.AccessInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class TestCrossbowService : Service() {

    private val SOCKET_URL = "ws://192.168.100.5:8080/ws/websocket"
    private val CHAT_TOPIC = "/all/messages"
    private val chanelId = "SERVICE_CHANNEL_ID"
    private val gson: Gson = GsonBuilder().create()
    private var chatNotificationBuilder: NotificationCompat.Builder? = null
    private var pushCounter = 101
    private var session: StompSession? = null
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createServiceNotificationChannel()

        val serviceNotificationBuilder = NotificationCompat.Builder(this, chanelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(resources.getString(R.string.chat_service_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)
        startForeground(1, serviceNotificationBuilder.build())

        scope.launch {
            connect()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.launch {
            session?.disconnect()
        }
        job.cancel()
    }

    private suspend fun connect() {
        try {
            session = getStompClient().connect(url = SOCKET_URL)
            val subscription: Flow<String>? = session?.subscribeText(CHAT_TOPIC)
            Timber.d("Success connect!")
            subscription?.collect { msg ->
                Timber.d("$msg - from crossbow")
            }
        } catch (e: Exception) {
            Timber.e(e, "Connect error!")
        }
    }

    private fun getStompClient() =
        StompClient(OkHttpWebSocketClient(getOkHttpClient()))

    private fun getOkHttpClient() =
        OkHttpClient.Builder()
            .addInterceptor(AccessInterceptor())
            .callTimeout(100000, TimeUnit.MILLISECONDS)
            .pingInterval(30000, TimeUnit.MILLISECONDS)
            .connectTimeout(30000, TimeUnit.MILLISECONDS)
            .build()

    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.chanel_service_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(chanelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}