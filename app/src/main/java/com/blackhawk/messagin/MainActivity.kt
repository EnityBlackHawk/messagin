package com.blackhawk.messagin

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blackhawk.messagin.api.RetrofitInstance
import com.blackhawk.messagin.data.NotificationData
import com.blackhawk.messagin.data.PushNotification
import com.blackhawk.messagin.firebase.FirebaseService
import com.blackhawk.messagin.ui.theme.MessaginTheme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TAG = "MainActivity"

const val TOPIC = "/topics/MyTopic"

class MainActivity : ComponentActivity() {

    private lateinit var notification : NotificationService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseService.sharedPreferences = getSharedPreferences("TokenPreferences", MODE_PRIVATE)
        notification = NotificationService(this)
        if(!notification.hasNotificationPermission)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(NotificationService.permissionDefine), 1)
            }
            notification.verifyPermission()
        }

        Firebase.messaging.isAutoInitEnabled = true

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, token)
            FirebaseService.token = token
        })


        setContent {
            MessaginTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SendMessage()
                }
            }
        }
    }
}

fun sendNotification(notification: PushNotification)
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


@Composable
fun SendMessage() {

    var title by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var message by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var token by remember {
        mutableStateOf(TextFieldValue(""))
    }


    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        TextField(
            value = title,
            onValueChange = {
                title = it
            },
            placeholder = {
                Text(text = "Title")
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = message,
            onValueChange = {
                message = it
            },
            placeholder = {
                Text(text = "Message")
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = token,
            onValueChange = {
                  token = it
            },
            placeholder = {
                Text(text = "Token")
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {

                PushNotification(
                    NotificationData(title.text, message.text),
                    FirebaseService.token
                ).also {
                    sendNotification(it)
                }

            }) {
            Text(text = "Send")
        }

    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MessaginTheme {
        SendMessage()
    }
}