package com.example.socketconnect.cross

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.socketconnect.databinding.ActivityCrossBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CrossSocketActivity : AppCompatActivity() {

    private var binding: ActivityCrossBinding? = null
    private val viewModel: CrossSocketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrossBinding.inflate(layoutInflater)
        setContentView(binding?.root)


    }
}