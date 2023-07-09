package com.example.socketconnect.cross


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socketconnect.App
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
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.config.StompConfig
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class CrossSocketViewModel @Inject constructor(): ViewModel() {

    private val SOCKET_URL = "ws://192.168.100.5:8080/ws/websocket"
    private val CHAT_TOPIC = "/all/messages"


    fun crossConnect() {
        viewModelScope.launch(Dispatchers.Default) {

//            val httpClient = HttpClient {
//                install(WebSockets)
//            }
//            val wsClient = KtorWebSocketClient(httpClient)
//            val stompClient = StompClient(wsClient)


            val stompConfig = StompConfig().apply {
                connectionTimeout = 3.seconds
                gracefulDisconnect = false

            }

            val client = StompClient(KtorWebSocketClient().apply {
                getHeaders().toMutableMap().apply {
                    put("Authorization", "Basic UG9sYXJCOmdoYmR0bg==")
                    put("user-name", "PolarB")
                }
            })
            val session: StompSession = client.connect(url = SOCKET_URL, customStompConnectHeaders = getHeaders())

            val subscription: Flow<String> = session.subscribeText(CHAT_TOPIC)

            val collectorJob = launch {
                subscription.collect { msg ->
                    Timber.d(msg)
                }
            }


//            delay(5000)
//            collectorJob.cancel()
//            session.disconnect()
        }
    }

    private fun getHeaders(): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            put("Authorization", "Basic UG9sYXJCOmdoYmR0bg==")
            put("user-name", "PolarB")
            put("Connection", "Upgrade")
        }
    }

}