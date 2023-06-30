package com.example.socketconnect.socket.listener

import com.example.socketconnect.socket.SocketViewModel
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber

class WebSocketListener(private val viewModel: SocketViewModel) : WebSocketListener() {

    private val TAG = "SocketTest"

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        viewModel.setSocketStatus(true)
        webSocket.send("Android Device Connected")
        Timber.tag(TAG).d("onOpen:")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        viewModel.sendMessage(Pair(false, text))
        Timber.tag(TAG).d("onMessage: $text")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Timber.tag(TAG).d("onClosing: $code $reason")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        viewModel.setSocketStatus(false)
        Timber.tag(TAG).d("onClosed: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Timber.tag(TAG).d("onFailure: ${t.message} $response")
        super.onFailure(webSocket, t, response)
    }

}