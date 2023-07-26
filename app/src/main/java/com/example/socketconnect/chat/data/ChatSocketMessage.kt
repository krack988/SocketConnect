package com.example.socketconnect.chat.data

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

class ChatSocketMessage(
    @SerializedName("text")
    val messageText: String? = null,
    @SerializedName("from")
    val author: String? = null,
    val to: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(messageText)
        parcel.writeString(author)
        parcel.writeString(to)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChatSocketMessage> {
        override fun createFromParcel(parcel: Parcel): ChatSocketMessage {
            return ChatSocketMessage(parcel)
        }

        override fun newArray(size: Int): Array<ChatSocketMessage?> {
            return arrayOfNulls(size)
        }
    }
}