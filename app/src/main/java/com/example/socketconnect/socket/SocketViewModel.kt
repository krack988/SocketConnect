package com.example.socketconnect.socket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import java.time.LocalDateTime
import javax.inject.Inject


@HiltViewModel
class SocketViewModel @Inject constructor() : ViewModel() {

    private val _socketStatus = MutableLiveData(false)
    val socketStatus: LiveData<Boolean> = _socketStatus

    private val _messages = MutableLiveData<Pair<Boolean, String>>()
    val messages: LiveData<Pair<Boolean, String>> = _messages


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