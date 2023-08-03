package com.blackhawk.messagin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.blackhawk.messagin.api.RetrofitInstance
import com.blackhawk.messagin.api.ServerSentEvent
import com.blackhawk.messagin.data.NotificationData
import com.blackhawk.messagin.data.User
import com.blackhawk.messagin.firebase.FirebaseService
import com.blackhawk.messagin.service.ServerSentEventService
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

const val TAG = "MainActivity"

const val TOPIC = "NewVersion"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "test")

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


        notification = NotificationService(this)


        if(!notification.hasNotificationPermission)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(NotificationService.permissionDefine), 1)
            }
            notification.verifyPermission()
        }

        runBlocking {

            if(!ServerSentEventService.isRegistered(this@MainActivity))
            {
                val resp = RetrofitInstance.api.registerUser(
                    User(null, null)
                )

                if(!resp.isSuccessful || resp.body() == null)
                {
                    Toast.makeText(this@MainActivity, "Registration failed", Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
                else
                {
                    ServerSentEventService.saveUid(this@MainActivity, resp.body()!!.token!!)

                    Toast.makeText(this@MainActivity, "Registration complete", Toast.LENGTH_LONG)
                        .show()
                }

            }
            Log.d("UID", ServerSentEventService.getUid(this@MainActivity) ?: "null")
        }

        Intent(this, ServerSentEventService::class.java).also {
            startService(it)
        }

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