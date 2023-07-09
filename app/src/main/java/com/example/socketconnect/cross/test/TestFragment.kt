package com.example.socketconnect.cross.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.socketconnect.cross.CrossSocketViewModel
import com.example.socketconnect.databinding.FragmentTestBinding
import com.example.socketconnect.socket.SocketViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestFragment: Fragment() {

    private var binding: FragmentTestBinding? = null
    private val viewModel: SocketViewModel by viewModels()
    private val activityViewModel: CrossSocketViewModel by activityViewModels()

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
            activityViewModel.crossConnect()
        }
    }
}