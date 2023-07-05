package com.example.socketconnect.chat.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socketconnect.R
import com.example.socketconnect.chat.data.ChatSocketMessage
import com.example.socketconnect.chat.data.InputMessageItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(val context: Context) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val messageList = mutableListOf<InputMessageItem>()
    private val messageDateFormat = "dd.MM.yy HH:mm:ss"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false))
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = messageList[position]
        holder.messageText.text = item.message
        holder.authorText.text = item.author
        holder.messageTime.text = item.messageTime
    }

    fun updateMessageList(socketMessage: ChatSocketMessage) {
        val message =
            InputMessageItem(
                socketMessage.author.orEmpty(),
                socketMessage.messageText.orEmpty(),
                formatDate(Date(), messageDateFormat)
            )
        messageList.add(message)
        notifyDataSetChanged()
    }

    private fun formatDate(
        date: Date,
        pattern: String,
        locale: Locale = Locale.getDefault()
    ): String {
        val dateFormat = SimpleDateFormat(pattern, locale)
        return dateFormat.format(date)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText = view.findViewById<TextView>(R.id.messageTV)
        val authorText = view.findViewById<TextView>(R.id.authorTV)
        val messageTime = view.findViewById<TextView>(R.id.messageTimeTV)
    }
}