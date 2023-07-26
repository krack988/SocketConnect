package com.example.socketconnect.cross.test

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.socketconnect.cross.CrossSocketViewModel
import com.example.socketconnect.cross.service.TestCrossbowService
import com.example.socketconnect.databinding.FragmentTestBinding
import com.example.socketconnect.socket.SocketViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class TestFragment : Fragment() {

    private var binding: FragmentTestBinding? = null
    private val viewModel: SocketViewModel by viewModels()
    private val activityViewModel: CrossSocketViewModel by activityViewModels()
    private var receiver: BroadcastReceiver? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.btnTest?.setOnClickListener {
//            activityViewModel.crossConnect()
            startCrossService()
        }

        val filter = IntentFilter()
        filter.addAction("test.action")

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent != null) {
                    Timber.d("Message from receiver - " + intent.getStringExtra("msg").toString())
                }
            }
        }
        receiver?.let {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(it, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        receiver?.let {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(it)
        }
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
}