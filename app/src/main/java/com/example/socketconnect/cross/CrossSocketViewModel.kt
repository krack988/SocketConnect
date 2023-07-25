package com.example.socketconnect.cross


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socketconnect.App
import com.example.socketconnect.cross.interceptor.AccessInterceptor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.headers
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.config.StompConfig
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import timber.log.Timber
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class CrossSocketViewModel @Inject constructor() : ViewModel() {

    private val SOCKET_URL = "ws://192.168.100.5:8080/ws/websocket"
    private val CHAT_TOPIC = "/all/messages"


    fun crossConnect() {
        viewModelScope.launch(Dispatchers.Default) {

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AccessInterceptor())
                .callTimeout(100000, TimeUnit.MICROSECONDS)
                .pingInterval(30000, TimeUnit.MICROSECONDS)
                .build()

            val httpClient = StompClient(OkHttpWebSocketClient(okHttpClient))

            try {
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