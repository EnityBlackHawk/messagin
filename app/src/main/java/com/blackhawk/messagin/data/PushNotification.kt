package com.blackhawk.messagin.data


data class PushNotification (
    val data: NotificationData,
    val from: String?,
    val date: Long
)