package com.example.socketconnect

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.socketconnect.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private val viewModel: MainActivityViewModel by viewModels()
    private var snackBar: Snackbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
    }

    fun showErrorSnackBar(errorMessage: String) {
        snackBar = binding?.root?.let {
            Snackbar.make(it, errorMessage, Snackbar.LENGTH_SHORT)
        }
        snackBar?.show()
    }
}