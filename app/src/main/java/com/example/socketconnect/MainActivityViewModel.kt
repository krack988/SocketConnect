package com.example.socketconnect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.socketconnect.chat.data.ChatSocketMessage
import com.example.socketconnect.common.SingleLiveEvent
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor() : ViewModel() {

//    private val gson: Gson = GsonBuilder().create()
//    private var mStompClient: StompClient? = null
//    private var compositeDisposable: CompositeDisposable? = null
//    private val SOCKET_URL = "ws://192.168.100.5:8080/ws/websocket"
//    private val CHAT_TOPIC = "/all/messages"
//    private val CHAT_LINK_SOCKET = "/app/application"

    private val _socketStatus = SingleLiveEvent<Boolean>()
    val socketStatus: LiveData<Boolean> = _socketStatus

    private val _messages = MutableLiveData<ChatSocketMessage>()
    val messages: LiveData<ChatSocketMessage> = _messages

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> = _errorMsg

    fun messageFromService(message: ChatSocketMessage) {
        _messages.value = message
    }

//    fun stompConnect() {
//        resetSubscriptions()
//
//        mStompClient = Stomp.over(Stomp.ConnectionProvider.JWS, SOCKET_URL, getHeaders())
//            .withServerHeartbeat(30000)
//
//        val topicSubscribe = mStompClient!!.topic(CHAT_TOPIC)
//            .subscribeOn(Schedulers.io(), false)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ topicMessage: StompMessage ->
//                Timber.tag("stompTAG").d("%s from subscribe", topicMessage.payload)
//                val message: ChatSocketMessage =
//                    gson.fromJson(topicMessage.payload, ChatSocketMessage::class.java)
//                _messages.value = message
//            },
//                {
//                    Timber.tag("stompTAG").e(it, "Error!")
//                    _errorMsg.value = "Error! \n" + it.message.orEmpty()
//                }
//            )
//
//        val lifecycleSubscribe = mStompClient!!.lifecycle()
//            .subscribeOn(Schedulers.io(), false)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { lifecycleEvent ->
//                when (lifecycleEvent.type!!) {
//                    LifecycleEvent.Type.OPENED -> {
//                        Timber.tag("stompTAG").d("Stomp connection opened")
//                        _socketStatus.postValue(true)
//                    }
//
//                    LifecycleEvent.Type.ERROR -> Timber.tag("stompTAG")
//                        .e(lifecycleEvent.exception, "Error")
//
//                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT,
//                    LifecycleEvent.Type.CLOSED -> {
//                        Timber.tag("stompTAG").d("Stomp connection closed")
//                        _socketStatus.postValue(false)
//                    }
//                }
//            }
//
//        compositeDisposable!!.add(lifecycleSubscribe)
//        compositeDisposable!!.add(topicSubscribe)
//
//        if (!mStompClient!!.isConnected) {
//            mStompClient!!.connect()
//        }
//    }

//    fun sendMessage(text: String) {
//        if (mStompClient != null) {
//            val message = ChatSocketMessage(messageText = text, author = "Me")
//            sendCompletable(mStompClient!!.send(CHAT_LINK_SOCKET, gson.toJson(message)))
//        }
//    }

//    fun disconnect() {
//        mStompClient?.disconnect()
//        compositeDisposable?.dispose()
//    }

//    private fun sendCompletable(request: Completable) {
//        compositeDisposable?.add(
//            request.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                    {
//                        Timber.tag("stompTAG").d("Stomp sended")
//                    },
//                    {
//                        Timber.tag("stompTAG").e(it, "Stomp error")
//                        _errorMsg.value = "Send Error! \n" + it.message.orEmpty()
//                    }
//                )
//        )
//    }

//    private fun resetSubscriptions() {
//        if (compositeDisposable != null) {
//            compositeDisposable!!.dispose()
//        }
//
//        compositeDisposable = CompositeDisposable()
//    }
//
//    private fun getHeaders() =
//        mutableMapOf<String, String>().apply {
//            put("Authorization", "Basic UG9sYXJCOmdoYmR0bg==")
//            put("user-name", "PolarB")
//            put("version", "1.1")
//            put("Accept-Encoding", "gzip, deflate")
//            put("Connection", "Upgrade")
//        }

}