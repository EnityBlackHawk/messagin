package com.blackhawk.messagin.firebase


import android.content.SharedPreferences
import android.util.Log
import com.blackhawk.messagin.NotificationService
import com.blackhawk.messagin.api.RetrofitInstance
import com.blackhawk.messagin.data.Image
import com.blackhawk.messagin.data.User
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


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
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitInstance.api.registerUser(
                User(newToken, null)
            )
        }
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FirebaseService", "Message Receved")
        if(message.data["device_from"] == token)
            return


        val title = message.data["title"]
        var imageByte : Image? = null
        runBlocking {
            imageByte = RetrofitInstance.api.getByteArray(
                Image(
                    message.data["imageResource"]
                        ?: throw RuntimeException("ImageResource was null"),
                    null
                )
            ).body()
        }

        title?.let {
            notificationService.pushNotification(
                it,
                message.data["messageTitle"]!!,
                message.data["message"],
                imageByte!!.imageBytes!!
            )
        }
    }

}