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
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var pushCounter = 101
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {

            }
        }
    private lateinit var service: ChatClientService
    private var isBindService = false
    private val chatServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Timber.i("service connected")
            service = (binder as ChatClientService.MyBinder).service
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
        createNotificationChannel()
        createDefaultNotificationBuilder()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPushPermission()
        }

        if (!foregroundServiceRunning()) {
            startService()
        }
        bindService(
            Intent(this, ChatClientService::class.java),
            chatServiceConnection,
            Context.BIND_AUTO_CREATE
        )

        /** For test sercive connect */
        Handler(Looper.getMainLooper()).postDelayed({
            service.serviceMessages.observeForever {
                viewModel.messageFromService(it)
            }
        }, 1000)
    }

    fun showNotification(title: String, message: String) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                with(NotificationManagerCompat.from(this)) {
                    notificationBuilder?.let {
                        it.setContentTitle(title)
                        it.setContentText(message)
                        notify(pushCounter++, it.build())
                    }
                }
            }
        }
    }

    fun showErrorSnackBar(errorMessage: String) {
        snackBar = binding?.root?.let {
            Snackbar.make(it, errorMessage, Snackbar.LENGTH_SHORT)
        }
        snackBar?.show()
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

    private fun createDefaultNotificationBuilder() {
        notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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
        val serviceIntent = Intent(this, ChatClientService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBindService) {
            unbindService(chatServiceConnection)
        }
    }
}