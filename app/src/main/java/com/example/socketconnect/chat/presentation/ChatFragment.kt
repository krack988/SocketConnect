package com.example.socketconnect.chat.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socketconnect.MainActivity
import com.example.socketconnect.MainActivityViewModel
import com.example.socketconnect.chat.ChatViewModel
import com.example.socketconnect.chat.data.ChatSocketMessage
import com.example.socketconnect.cross.service.TestCrossbowService.Companion.chatIntentFilterAction
import com.example.socketconnect.cross.service.TestCrossbowService.Companion.inputChatMessageId
import com.example.socketconnect.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var binding: FragmentChatBinding? = null
    private val viewModel: ChatViewModel by viewModels()
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private var adapter: ChatAdapter? = null
    private var receiver: BroadcastReceiver? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatAdapter(requireContext())
        binding?.chatRV?.layoutManager = LinearLayoutManager(requireContext())
        binding?.chatRV?.adapter = adapter

        binding?.sendMessageBtn?.setOnClickListener {
            val message = binding?.messageEV?.text.toString()
            binding?.messageEV?.text = null
            (activity as? MainActivity)?.sendMessage(message)
        }

        registerBroadcast()

        activityViewModel.messages.observe(viewLifecycleOwner) {
            Timber.d(it.messageText)
            adapter?.updateMessageList(it)
            with(binding?.chatRV) {
                this?.post {
                    smoothScrollToPosition((adapter?.itemCount ?: 1) - 1)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        receiver?.let {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(it)
        }
    }

    private fun registerBroadcast() {
        val filter = IntentFilter()
        filter.addAction(chatIntentFilterAction)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent != null) {
                    val message = intent.getParcelableExtra<ChatSocketMessage>(inputChatMessageId)
                    message?.let {
                        activityViewModel.messageFromService(it)
                    }
                }
            }
        }
        receiver?.let {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(it, filter)
        }
    }
}