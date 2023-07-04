package com.example.socketconnect.start.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.socketconnect.databinding.FragmentStartBinding
import com.example.socketconnect.socket.SocketViewModel
import com.example.socketconnect.socket.listener.WebSocketListener
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import timber.log.Timber
import androidx.fragment.app.viewModels
import com.example.socketconnect.MainActivityViewModel

@AndroidEntryPoint
class StartFragment : Fragment() {

    private var binding: FragmentStartBinding? = null
    private val viewModel: SocketViewModel by viewModels()
    private val activityViewModel: MainActivityViewModel by activityViewModels()

    private lateinit var webSocketListener: WebSocketListener
    private val okHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webSocketListener = WebSocketListener(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.connectBtn?.setOnClickListener {
            activityViewModel.stompConnect()
        }

        binding?.testTV?.setOnClickListener {
            val testMessage = "Test message from android app"
//            webSocket?.send(testMessage)
//            viewModel.sendMessage(Pair(true, testMessage))
            activityViewModel.sendMessage(testMessage)
        }

        binding?.disconnectBtn?.setOnClickListener {
            activityViewModel.disconnect()
        }

//        viewModel.socketStatus.observe(viewLifecycleOwner) {
//            Timber.tag(TIMBER_TEST_TAG).i(if (it) "Connected" else "Disconnected")
//        }
    }

    private fun connect() {
        try {
            val request = viewModel.getConnectRequest()
            webSocket = okHttpClient.newWebSocket(request, webSocketListener)
        } catch (e: Exception) {
            Timber.tag(TIMBER_TEST_TAG).e(e, "Connect error")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        okHttpClient.dispatcher.executorService.shutdown()
        binding = null
    }

    companion object {
        private const val TIMBER_TEST_TAG = "infoTag"
    }
}