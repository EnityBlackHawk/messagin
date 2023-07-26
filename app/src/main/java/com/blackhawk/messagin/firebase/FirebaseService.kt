package com.blackhawk.messagin.firebase


import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.blackhawk.messagin.NotificationService
import com.blackhawk.messagin.api.RetrofitInstance
import com.blackhawk.messagin.data.Confirmation
import com.blackhawk.messagin.data.Image
import com.blackhawk.messagin.data.User
import com.blackhawk.messagin.dataStore
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class FirebaseService : FirebaseMessagingService() {

    companion object{
        suspend fun setToken(context : Context, value: String) {
            context.dataStore.edit {
                val key = stringPreferencesKey("token")
                it[key] = value
            }
        }

        suspend fun getToken(context : Context) : String?
        {
            val key = stringPreferencesKey("token")
            return context.dataStore.data.first()[key]
        }
    }


    private val notificationService by lazy {
        NotificationService(this)
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)

        CoroutineScope(Dispatchers.IO).launch {
            setToken(this@FirebaseService, newToken)
            val resp = RetrofitInstance.api.registerUser(
                User(newToken, null)
            )
            if(!resp.isSuccessful)
            {
                Log.e("Service", resp.message())
            }
        }
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FirebaseService", "Message received")

        if(message.data["device_from"] == null)
        {
            Log.e("Firebase Service","Invalid message received")
            return
        }


        val token = runBlocking {
            getToken(this@FirebaseService)
        }


        if(message.data["device_from"] == token)
            return

        CoroutineScope(Dispatchers.IO).launch {
            message.data["messageId"]?.let {id ->
                token?.let {
                    RetrofitInstance.api.confirmMessage(
                        Confirmation(id, it)
                    )
                }

            }
        }


        val title = message.data["title"]
        var imageByte : Image? = null
        runBlocking {
            val resp = RetrofitInstance.api.getByteArray(
                Image(
                    message.data["imageResource"]
                        ?: throw RuntimeException("ImageResource was null"),
                    null
                )
            )
            if(!resp.isSuccessful)
            {
                Log.e("Service", resp.message())
                Log.e("Service", resp.errorBody().toString())
            }
            imageByte = resp.body()
        }

        if(imageByte == null) return
        if(imageByte!!.imageBytes == null) return

        title?.let {
            notificationService.pushNotification(
                it,
                message.data["messageTitle"]!!,
                message.data["message"],
                imageByte?.imageBytes,
                message.data["sendDate"]!!
            )
        }
    }

}