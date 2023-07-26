package com.example.socketconnect.cross


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socketconnect.cross.interceptor.AccessInterceptor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class CrossSocketViewModel @Inject constructor(
    private val accessInterceptor: AccessInterceptor
) : ViewModel() {

    private val SOCKET_URL = "ws://192.168.100.5:8080/ws/websocket"
    private val CHAT_TOPIC = "/all/messages"


    fun crossConnect() {
        viewModelScope.launch(Dispatchers.Default) {

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(accessInterceptor)
                .callTimeout(100000, TimeUnit.MILLISECONDS)
                .pingInterval(30000, TimeUnit.MILLISECONDS)
                .connectTimeout(30000, TimeUnit.MILLISECONDS)
                .build()

            try {
                val httpClient = StompClient(OkHttpWebSocketClient(okHttpClient))
                val session: StompSession = httpClient.connect(url = SOCKET_URL)

                val subscription: Flow<String> = session.subscribeText(CHAT_TOPIC)

                val collectorJob = launch {
                    subscription.collect { msg ->
                        Timber.d("$msg - from crossbow")
                    }
                }

//            delay(5000)
//            collectorJob.cancel()
//            session.disconnect()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}