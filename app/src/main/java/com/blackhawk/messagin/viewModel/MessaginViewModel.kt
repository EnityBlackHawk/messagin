package com.blackhawk.messagin.viewModel

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blackhawk.messagin.R
import com.blackhawk.messagin.TAG
import com.blackhawk.messagin.api.RetrofitInstance
import com.blackhawk.messagin.data.Message
import com.blackhawk.messagin.data.NotificationData
import com.blackhawk.messagin.data.PushNotification
import com.blackhawk.messagin.firebase.FirebaseService
import com.blackhawk.messagin.tools.convertToString
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MessaginViewModel(private val resources: Resources) : ViewModel() {

    val messagesList = mutableStateOf(
        listOf(
            Message("Te amo", R.drawable.coracao),
            Message("Estou com saudades", R.drawable.hug),
            Message("Quero te beijar", R.drawable.kiss)
        )
    )

    var selectedMessage = mutableStateOf(messagesList.value[0])


    var messageText = mutableStateOf("")


    fun sendMessage()
    {
        val bit =  BitmapFactory.decodeResource(
            resources, selectedMessage.value.imageResource
        )
        val push = PushNotification(
            NotificationData(selectedMessage.value.title, messageText.value,
                bit.convertToString()),
            FirebaseService.token
        )
        sendNotification(push)
    }


    private fun sendNotification(notification: PushNotification)
    {
        CoroutineScope(Dispatchers.IO).launch {

            try {
                val response = RetrofitInstance.api.sendNotification(notification)
                if(response.isSuccessful)
                {
                    Log.d(TAG, "Response ${Gson().toJson(response)}")
                }
                else Log.e(TAG, response.errorBody().toString())
            }catch (e: Exception)
            {
                Log.e("MainActivity", e.toString())
            }

        }
    }

}

class MessaginViewModelFactory(private val res : Resources) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessaginViewModel(res) as T
    }

}