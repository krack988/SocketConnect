package com.example.socketconnect.chat.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.socketconnect.MainActivityViewModel
import com.example.socketconnect.R
import com.example.socketconnect.chat.data.ChatSocketMessage
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException


@AndroidEntryPoint
class ChatClientService : Service() {

    private val chanelId = "SERVICE_CHANNEL_ID"
    private var viewModel: MainActivityViewModel? = null
    private var chatNotificationBuilder: NotificationCompat.Builder? = null
    private var pushCounter = 101
    private val mBinder: IBinder = MyBinder()
    private val _serviceMessages = MutableLiveData<ChatSocketMessage>()
    val serviceMessages: LiveData<ChatSocketMessage> = _serviceMessages

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createServiceNotificationChannel()
        createChatNotificationChannel()
        createDefaultNotificationBuilder()

        val notificationBuilder = NotificationCompat.Builder(this, chanelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(resources.getString(R.string.chat_service_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)

        startForeground(1, notificationBuilder.build())

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        viewModel = MainActivityViewModel()
        viewModel?.stompConnect()
        viewModel?.messages?.observeForever {
            showNotification(it.author.orEmpty(), it.messageText.orEmpty())
            _serviceMessages.postValue(it)
        }
    }

    private fun showNotification(title: String, message: String) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                with(NotificationManagerCompat.from(this)) {
                    chatNotificationBuilder?.let {
                        it.setContentTitle(title)
                        it.setContentText(message)
                        notify(pushCounter++, it.build())
                    }
                }
            }
        }
    }

    private fun createDefaultNotificationBuilder() {
        chatNotificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
    }

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

    private fun createChatNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    inner class MyBinder : Binder() {
        val service: ChatClientService
            get() = this@ChatClientService
    }
}