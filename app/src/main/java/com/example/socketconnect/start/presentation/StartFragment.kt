package com.example.socketconnect.start.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.socketconnect.App
import com.example.socketconnect.App.Companion.authPrefKey
import com.example.socketconnect.App.Companion.loginPrefKey
import com.example.socketconnect.App.Companion.pref
import com.example.socketconnect.MainActivity
import com.example.socketconnect.MainActivityViewModel
import com.example.socketconnect.R
import com.example.socketconnect.databinding.FragmentStartBinding
import com.example.socketconnect.socket.SocketViewModel
import com.example.socketconnect.socket.listener.WebSocketListener
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.util.encodeBase64
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import timber.log.Timber


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
//        pref = requireContext().getSharedPreferences(
//            prefName,
//            Context.MODE_PRIVATE
//        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.openChatBtn?.setOnClickListener {
            findNavController().navigate(StartFragmentDirections.toChat())
        }

        binding?.connectBtn?.setOnClickListener {
        }

        binding?.disconnectBtn?.setOnClickListener {
        }

        if (pref?.getString(authPrefKey, "").isNullOrEmpty()) {
            showAuthAlert()
        }
    }

    fun clearData() {
        pref?.edit()?.clear()?.apply()
    }

    private fun showAuthAlert() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(R.string.sign_in)
            .setCancelable(false)

        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_sign_in, null)
        builder.setView(dialogView)
        val loginView = dialogView.findViewById<TextInputEditText>(R.id.loginET)
        val passView = dialogView.findViewById<TextInputEditText>(R.id.passwordET)
        builder.setPositiveButton(R.string.connect) { _, _ ->
            val loginText = loginView.text.toString()
            val passwordText = passView.text.toString()
            setUpAuthData(loginText, passwordText)
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun setUpAuthData(login: String, password: String) {
        val authToken = "$login:$password".encodeBase64()
        saveLoginAndAuth(login, authToken)
//        (activity as? MainActivity)?.startServiceWithCheck()
    }


    private fun saveLoginAndAuth(login: String, auth: String) {
        pref?.edit()?.putString(loginPrefKey, login)?.apply()
        pref?.edit()?.putString(authPrefKey, auth)?.apply()
    }


    @Deprecated("Old socket connect")
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
//        okHttpClient.dispatcher.executorService.shutdown()
        binding = null
    }

    companion object {
        private const val TIMBER_TEST_TAG = "infoTag"
    }
}