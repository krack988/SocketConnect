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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import ua.naiksoftware.stomp.StompClient
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SocketViewModel @Inject constructor() : ViewModel() {


    @RequiresApi(Build.VERSION_CODES.O)
    private val gson: Gson = GsonBuilder().registerTypeAdapter(
        LocalDateTime::class.java,
        GsonLocalDateTimeAdapter()
    ).create()
    private var mStompClient: StompClient? = null

    private val _socketStatus = MutableLiveData(false)
    val socketStatus: LiveData<Boolean> = _socketStatus

    private val _messages = MutableLiveData<Pair<Boolean, String>>()
    val messages: LiveData<Pair<Boolean, String>> = _messages

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
//            .addHeader("Authorization","Basic ")
            .build()
    }

    companion object{
        const val SOCKET_URL = "ws://192.168.100.5:8080/ws"
        const val CHAT_TOPIC = "/topic/chat"
        const val CHAT_LINK_SOCKET = "/api/v1/chat/sock"
    }
}