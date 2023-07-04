package com.example.socketconnect.chat.data

import java.time.LocalDateTime

data class ChatSocketMessage(
    val text: String,
    val author: String,
    val datetime: LocalDateTime? = null,
    var receiver: String? = null
)