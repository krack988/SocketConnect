package com.example.socketconnect

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.socketconnect.chat.data.ChatSocketMessage
import com.example.socketconnect.chat.service.ChatClientService
import com.example.socketconnect.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private val viewModel: MainActivityViewModel by viewModels()
    private var snackBar: Snackbar? = null
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {

            }
        }
    private var service: ChatClientService? = null
    private var isBindService = false
        set(value) {
            if (value) {
                serviceSubscription()
            }
            field = value
        }
    private val chatServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Timber.i("service connected")
            service = (binder as ChatClientService.ChatServiceBinder).service
            isBindService = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBindService = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPushPermission()
        }

//        if (!foregroundServiceRunning()) {
//            startService()
//        }
        bindService()
    }

    fun startServiceWithCheck() {
        if (!foregroundServiceRunning()) {
            startService()
        }
    }

    fun sendMessage(text: String) {
        service?.sendMessage(text)
    }

    fun showErrorSnackBar(errorMessage: String) {
        snackBar = binding?.root?.let {
            Snackbar.make(it, errorMessage, Snackbar.LENGTH_SHORT)
        }
        snackBar?.show()
    }

    private fun serviceSubscription() {
        service?.serviceMessages?.observe(this) {
            viewModel.messageFromService(it)
        }

        service?.errorMsg?.observe(this) {
            showErrorSnackBar(it)
        }

        service?.socketStatus?.observe(this) {
            if (it) {
                showErrorSnackBar(resources.getString(R.string.open_connect))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPushPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private fun foregroundServiceRunning(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (ChatClientService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun startService() {
        Timber.d("start service")
        val serviceIntent = Intent(this, ChatClientService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        bindService()
    }

    private fun bindService() {
        if (foregroundServiceRunning()) {
            bindService(
                Intent(this, ChatClientService::class.java),
                chatServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBindService) {
            unbindService(chatServiceConnection)
        }
    }
}