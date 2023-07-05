package com.example.socketconnect.chat.data

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ChatSocketMessage(
    @SerializedName("text")
    val messageText: String? = null,
    @SerializedName("from")
    val author: String? = null,
    val to: String? = null,
    val datetime: LocalDateTime? = null
)