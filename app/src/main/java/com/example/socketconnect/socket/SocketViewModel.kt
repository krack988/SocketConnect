package com.example.socketconnect.socket

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socketconnect.common.GsonLocalDateTimeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SocketViewModel @Inject constructor() : ViewModel() {


    @RequiresApi(Build.VERSION_CODES.O)
    private val gson: Gson = GsonBuilder().registerTypeAdapter(
        LocalDateTime::class.java,
        GsonLocalDateTimeAdapter()
    ).create()
    private var mStompClient: StompClient? = null
    private var compositeDisposable: CompositeDisposable? = null
    val date = Date().time.toString()
    val SOCKET_URL = "ws://192.168.100.5:8080/ws/websocket"
    val CHAT_TOPIC = "/all/messages"
    val CHAT_LINK_SOCKET = "/app/application"

    private val _socketStatus = MutableLiveData(false)
    val socketStatus: LiveData<Boolean> = _socketStatus

    private val _messages = MutableLiveData<Pair<Boolean, String>>()
    val messages: LiveData<Pair<Boolean, String>> = _messages

    init {
        stompConnect()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun stompConnect() {
        resetSubscriptions()
        val headerMap = mutableMapOf<String, String>().apply {
            put("Authorization", "Basic UG9sYXJCOmdoYmR0bg==")
            put("user-name", "PolarB")
            put("version", "1.1")
            put("Accept-Encoding", "gzip, deflate")
            put("Connection", "Upgrade")
        }


        mStompClient = Stomp.over(Stomp.ConnectionProvider.JWS, SOCKET_URL, headerMap)
            .withServerHeartbeat(30000)

        val topicSubscribe = mStompClient!!.topic(CHAT_TOPIC)
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage: StompMessage ->
                Timber.tag("stompTAG").d(topicMessage.payload)
                val message: ChatSocketMessage =
                    gson.fromJson(topicMessage.payload, ChatSocketMessage::class.java)
            },
                {
                    Timber.tag("stompTAG").e(it, "Error!")
                }
            )

        val lifecycleSubscribe = mStompClient!!.lifecycle()
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lifecycleEvent ->
                when (lifecycleEvent.type!!) {
                    LifecycleEvent.Type.OPENED -> Timber.tag("stompTAG").d( "Stomp connection opened")
                    LifecycleEvent.Type.ERROR -> Timber.tag("stompTAG").e( lifecycleEvent.exception, "Error")
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT,
                    LifecycleEvent.Type.CLOSED -> {
                        Timber.tag("stompTAG").d( "Stomp connection closed")
                    }
                }
            }

        compositeDisposable!!.add(lifecycleSubscribe)
        compositeDisposable!!.add(topicSubscribe)

        if (!mStompClient!!.isConnected) {
            mStompClient!!.connect()
        }
    }

    private fun resetSubscriptions() {
        if (compositeDisposable != null) {
            compositeDisposable!!.dispose()
        }

        compositeDisposable = CompositeDisposable()
    }

    fun sendMessage(text: String) {
        val message = ChatSocketMessage(text = text, author = "Me")
        sendCompletable(mStompClient!!.send(CHAT_LINK_SOCKET, gson.toJson(message)))
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
                    }
                )
        )
    }


    /** Clean socket connect */
    fun sendMessage(message: Pair<Boolean, String>) = viewModelScope.launch(Dispatchers.Main) {
        if (_socketStatus.value == true) {
            _messages.value = message
        }
    }

    fun setSocketStatus(status: Boolean) = viewModelScope.launch(Dispatchers.Main) {
        _socketStatus.value = status
    }

    fun getConnectRequest(): Request {
        val websocketURL = "http://192.168.100.5:8080/ws"

        return Request.Builder()
            .url(websocketURL)
            .build()
    }
    /** End clean socket connect */

    data class ChatSocketMessage(
        val text: String,
        val author: String,
        val datetime: LocalDateTime? = null,
        var receiver: String? = null
    )
}