package com.blackhawk.messagin

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.blackhawk.messagin.api.RetrofitInstance
import com.blackhawk.messagin.data.User
import com.blackhawk.messagin.firebase.FirebaseService
import com.blackhawk.messagin.ui.Navigation
import com.blackhawk.messagin.ui.theme.MessaginTheme
import com.blackhawk.messagin.viewModel.HistoricViewModel
import com.blackhawk.messagin.viewModel.HistoricViewModelFactory
import com.blackhawk.messagin.viewModel.MessaginViewModel
import com.blackhawk.messagin.viewModel.MessaginViewModelFactory
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TAG = "MainActivity"

const val TOPIC = "NewVersion"

class MainActivity : ComponentActivity() {

    private lateinit var notification : NotificationService

    val viewModel : MessaginViewModel by viewModels {
        MessaginViewModelFactory(
            resources,
            (application as MessaginApplication).database.messagePersistDao()
        )
    }

    val historicViewModel : HistoricViewModel by viewModels {
        HistoricViewModelFactory(
            (application as MessaginApplication).database.messagePersistDao()
        )
    }


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
                val d = AlertDialog.Builder(this).setMessage("Erro na registro do token!").create()
                d.show()
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, token)
            FirebaseService.token = token

            CoroutineScope(Dispatchers.IO).launch {
                RetrofitInstance.api.registerUser(
                    User(token, null)
                )
            }

        })

        setContent {
            MessaginTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation(viewModel, historicViewModel)
                }
            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MessaginTheme {

    }
}