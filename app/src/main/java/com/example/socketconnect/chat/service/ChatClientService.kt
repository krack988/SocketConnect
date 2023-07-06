package com.example.socketconnect.chat.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.socketconnect.App
import com.example.socketconnect.MainActivity
import com.example.socketconnect.MainActivityViewModel
import com.example.socketconnect.R
import com.example.socketconnect.chat.data.ChatSocketMessage
import com.example.socketconnect.common.SingleLiveEvent
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage
import java.io.IOException


@AndroidEntryPoint
class ChatClientService : Service() {

    private val chanelId = "SERVICE_CHANNEL_ID"
    private var chatNotificationBuilder: NotificationCompat.Builder? = null
    private var pushCounter = 101
    private val mBinder: IBinder = ChatServiceBinder()


    private val gson: Gson = GsonBuilder().create()
    private var mStompClient: StompClient? = null
    private var compositeDisposable: CompositeDisposable? = null
    private val SOCKET_URL = "ws://192.168.100.5:8080/ws/websocket"
    private val CHAT_TOPIC = "/all/messages"
    private val CHAT_LINK_SOCKET = "/app/application"

    private var pref: SharedPreferences? = null

    private val _socketStatus = SingleLiveEvent<Boolean>()
    val socketStatus: LiveData<Boolean> = _socketStatus

    private val _serviceMessages = MutableLiveData<ChatSocketMessage>()
    val serviceMessages: LiveData<ChatSocketMessage> = _serviceMessages

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> = _errorMsg

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createServiceNotificationChannel()
        createChatNotificationChannel()
        createDefaultNotificationBuilder()

        val serviceNotificationBuilder = NotificationCompat.Builder(this, chanelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(resources.getString(R.string.chat_service_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)

        startForeground(1, serviceNotificationBuilder.build())

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
//        pref = context.getSharedPreferences(
//            prefName,
//            Context.MODE_PRIVATE
//        )
        stompConnect()
    }

    override fun onDestroy() {
        disconnect()
        super.onDestroy()
    }

    fun sendMessage(text: String) {
        if (mStompClient != null) {
            val message = ChatSocketMessage(messageText = text, author = "Me")
            sendCompletable(mStompClient!!.send(CHAT_LINK_SOCKET, gson.toJson(message)))
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
        val intent = Intent(this, MainActivity::class.java)
            .apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        chatNotificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(resultPendingIntent)
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

    private fun stompConnect() {
        resetSubscriptions()

        mStompClient = Stomp.over(Stomp.ConnectionProvider.JWS, SOCKET_URL, getHeaders())
            .withServerHeartbeat(30000)

        val topicSubscribe = mStompClient!!.topic(CHAT_TOPIC)
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage: StompMessage ->
                Timber.tag("stompTAG").d("%s from subscribe", topicMessage.payload)
                val message: ChatSocketMessage =
                    gson.fromJson(topicMessage.payload, ChatSocketMessage::class.java)
                showNotification(message.author.orEmpty(), message.messageText.orEmpty())
                _serviceMessages.value = message
            },
                {
                    Timber.tag("stompTAG").e(it, "Error!")
                    _errorMsg.value = "Error! \n" + it.message.orEmpty()
                }
            )

        val lifecycleSubscribe = mStompClient!!.lifecycle()
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lifecycleEvent ->
                when (lifecycleEvent.type!!) {
                    LifecycleEvent.Type.OPENED -> {
                        Timber.tag("stompTAG").d("Stomp connection opened")
                        _socketStatus.postValue(true)
                    }

                    LifecycleEvent.Type.ERROR -> Timber.tag("stompTAG")
                        .e(lifecycleEvent.exception, "Error")

                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT,
                    LifecycleEvent.Type.CLOSED -> {
                        Timber.tag("stompTAG").d("Stomp connection closed")
                        _socketStatus.postValue(false)
                    }
                }
            }

        compositeDisposable!!.add(lifecycleSubscribe)
        compositeDisposable!!.add(topicSubscribe)

        if (!mStompClient!!.isConnected) {
            mStompClient!!.connect()
        }
    }

    private fun disconnect() {
        mStompClient?.disconnect()
        compositeDisposable?.dispose()
    }

    private fun sendCompletable(request: Completable) {
        compositeDisposable?.add(
            request.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Timber.tag("stompTAG").d("Stomp sended")
                    },
                    {
                        Timber.tag("stompTAG").e(it, "Stomp error")
                        _errorMsg.value = "Send Error! \n" + it.message.orEmpty()
                    }
                )
        )
    }

    private fun resetSubscriptions() {
        if (compositeDisposable != null) {
            compositeDisposable!!.dispose()
        }

        compositeDisposable = CompositeDisposable()
    }

    private fun getHeaders(): Map<String, String> {
        val login = App.pref?.getString(App.loginPrefKey, "").orEmpty()
        val authToken = App.pref?.getString(App.authPrefKey, "").orEmpty()

        return mutableMapOf<String, String>().apply {
            put("Authorization", "Basic $authToken")
            put("user-name", login)
//            put("Authorization", "Basic UG9sYXJCOmdoYmR0bg==")
//            put("user-name", "PolarB")

            put("version", "1.1")
            put("Accept-Encoding", "gzip, deflate")
            put("Connection", "Upgrade")
        }
    }

    inner class ChatServiceBinder : Binder() {
        val service: ChatClientService
            get() = this@ChatClientService
    }
}