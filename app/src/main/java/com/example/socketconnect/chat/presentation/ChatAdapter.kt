package com.example.socketconnect.chat.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socketconnect.R
import com.example.socketconnect.chat.data.InputMessageItem

class ChatAdapter(val context: Context) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val messageList = mutableListOf<InputMessageItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false))
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = messageList[position]
        holder.messageText.text = item.message
    }

    fun updateMessageList(messageText: String) {
        val message = InputMessageItem("", messageText)
        messageList.add(message)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val messageText = view.findViewById<TextView>(R.id.messageTV)

    }
}