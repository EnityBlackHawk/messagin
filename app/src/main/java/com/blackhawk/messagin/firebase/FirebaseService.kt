package com.blackhawk.messagin.firebase


import android.content.SharedPreferences
import com.blackhawk.messagin.NotificationService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseService : FirebaseMessagingService() {

    companion object {
        var sharedPreferences : SharedPreferences? = null

        var token: String?
            get() {
                return sharedPreferences?.getString("token", "")
            }
            set(value) {
                sharedPreferences?.edit()?.putString("token", value)?.apply()
            }
    }


    private val notificationService by lazy {
        NotificationService(this)
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if(message.data["device_from"] == token)
            return


        val title = message.data["title"]

        title?.let {
            notificationService.pushNotification(it, message.data["message"])
        }
    }

}