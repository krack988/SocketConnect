package com.example.socketconnect.chat.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socketconnect.MainActivity
import com.example.socketconnect.MainActivityViewModel
import com.example.socketconnect.chat.ChatViewModel
import com.example.socketconnect.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var binding: FragmentChatBinding? = null
    private val viewModel: ChatViewModel by viewModels()
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private var adapter: ChatAdapter? = null

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
            activityViewModel.sendMessage(message)
        }

        activityViewModel.messages.observe(viewLifecycleOwner) {
            Timber.d(it.messageText)
            showPush(it.author.orEmpty(), it.messageText.orEmpty())
            adapter?.updateMessageList(it)
            with(binding?.chatRV) {
                this?.post {
                    smoothScrollToPosition((adapter?.itemCount ?: 1) - 1)
                }
            }
        }
    }

    private fun showPush(title: String, message: String) {
        (activity as? MainActivity)?.showNotification(title, message)
    }
}