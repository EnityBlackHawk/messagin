package com.blackhawk.messagin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_persist")
data class MessagePersist (

    @PrimaryKey
    val id : String,
    val title: String,
    val message : String,
    val imageBitmap : String,
    var wasDelivered : Boolean = false
)