package com.example.socketconnect.start.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.socketconnect.App.Companion.authPrefKey
import com.example.socketconnect.App.Companion.loginPrefKey
import com.example.socketconnect.App.Companion.pref
import com.example.socketconnect.MainActivity
import com.example.socketconnect.MainActivityViewModel
import com.example.socketconnect.R
import com.example.socketconnect.cross.CrossSocketActivity
import com.example.socketconnect.cross.service.TestCrossbowService
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.openChatBtn?.setOnClickListener {
            findNavController().navigate(StartFragmentDirections.toChat())
        }

        binding?.nextActBtn?.setOnClickListener {
            startCrossService()
            openChat()
        }

        binding?.disconnectBtn?.setOnClickListener {
            (activity as? MainActivity)?.stopChatService()
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
        startCrossService()
        openChat()
    }


    private fun saveLoginAndAuth(login: String, auth: String) {
        pref?.edit()?.putString(loginPrefKey, login)?.apply()
        pref?.edit()?.putString(authPrefKey, auth)?.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun startCrossService() {
        Timber.d("start service")
        val serviceIntent = Intent(requireContext(), TestCrossbowService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(serviceIntent)
        } else {
            activity?.startService(serviceIntent)
        }
    }

    private fun openChat() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(requireContext(), CrossSocketActivity::class.java))
        }, 500)
    }

    companion object {
        private const val TIMBER_TEST_TAG = "infoTag"
    }
}