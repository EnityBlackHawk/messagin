package com.blackhawk.messagin.data

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class NotificationData (
    val title: String,
    val message : String,
    val imageResource: String
)