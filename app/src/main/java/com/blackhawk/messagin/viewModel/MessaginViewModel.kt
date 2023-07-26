package com.blackhawk.messagin.viewModel

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blackhawk.messagin.NotificationService
import com.blackhawk.messagin.R
import com.blackhawk.messagin.TAG
import com.blackhawk.messagin.api.RetrofitInstance
import com.blackhawk.messagin.data.Message
import com.blackhawk.messagin.data.MessagePersist
import com.blackhawk.messagin.data.NotificationData
import com.blackhawk.messagin.data.PushNotification
import com.blackhawk.messagin.firebase.FirebaseService
import com.blackhawk.messagin.room.MessagePersistDao
import com.blackhawk.messagin.tools.convertToString
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date

class MessaginViewModel(
    private val resources: Resources,
    private val messagePersistDao: MessagePersistDao?
) : ViewModel() {

    val messagesList = mutableStateOf(
        listOf(
            Message("Te amo", R.drawable.coracao),
            Message("Estou com saudades", R.drawable.hug),
            Message("Quero te beijar", R.drawable.kiss)
        )
    )

    var selectedMessage = mutableStateOf(messagesList.value[0])

    var messageText = mutableStateOf(TextFieldValue(""))



    suspend fun sendMessage(context: Context)
    {
        val bit =  BitmapFactory.decodeResource(
            resources, selectedMessage.value.imageResource
        )
        val push = PushNotification(
            NotificationData(selectedMessage.value.title, messageText.value.text,
                bit.convertToString()),
            FirebaseService.getToken(context) ?: throw RuntimeException("Token was null"),
            Date().time
        )
        sendNotification(push)
        messageText.value = TextFieldValue("")
    }

    suspend fun sendCustomMessage(context: Context, title : String, message : String, image : Bitmap)
    {

        val push = PushNotification(
            NotificationData(title, message, image.convertToString()),
            FirebaseService.getToken(context) ?: throw RuntimeException("Token was null"),
            Date().time
        )
        sendNotification(push)
    }


    private fun sendNotification(notification: PushNotification)
    {
        CoroutineScope(Dispatchers.IO).launch {

            try {
                Log.d("MessageViewModel", "Request send")
                val response = RetrofitInstance.api.sendNotification(notification)
                if(response.isSuccessful)
                {
                    messagePersistDao?.insert(
                        MessagePersist(
                            response.body()?.id!!,
                            notification.data.title,
                            notification.date,
                            notification.data.message,
                            notification.data.imageResource
                        )
                    )
                }
                else Log.e("MessageViewModel", response.message())
            }catch (e: Exception)
            {
                Log.e("MessageViewModel", e.toString())
            }

        }
    }

}

class MessaginViewModelFactory(
    private val res: Resources,
    private val messagePersistDao: MessagePersistDao
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessaginViewModel(res, messagePersistDao) as T
    }

}