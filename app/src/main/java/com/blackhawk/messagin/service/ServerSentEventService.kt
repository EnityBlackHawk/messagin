package com.blackhawk.messagin.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.blackhawk.messagin.NotificationService
import com.blackhawk.messagin.R
import com.blackhawk.messagin.api.ServerSentEvent
import com.blackhawk.messagin.dataStore
import com.blackhawk.messagin.tools.convertToString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Date

class ServerSentEventService : Service() {

    companion object {

        var isRunning = false
            private set

        suspend fun isRegistered(context: Context) : Boolean
            = getUid(context) != null

        suspend fun saveUid(context: Context, value: String) {
            context.dataStore.edit {
                val key = stringPreferencesKey("uid")
                it[key] = value
            }
        }

        suspend fun getUid(context: Context): String? =
            context.dataStore.data.first()[
                    stringPreferencesKey("uid")
            ]

    }

    private var thread : Thread? = null


    private var count = 1

    private lateinit var notificationService : NotificationService

    private lateinit var sse : ServerSentEvent

    override fun onBind(intent: Intent): IBinder?
    {
        return null
    }


    override fun onCreate() {
        super.onCreate()
        Log.d("ServerSentEventService", "Created")
        notificationService = NotificationService(applicationContext)
        runBlocking {
            sse = ServerSentEvent(
                getUid(applicationContext)!!
            ) {
                sendNotification(it)
                Log.d("ServerSentEventService", it)
                sse.addCallback {
                    Log.d("TEste", it)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ServerSentEventService", "Started")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("ServerSentEventService", "Stop")
    }

    private fun sendNotification(message : String)
    {
        notificationService.pushNotification(
            "Title",
            "TitleMessage",
            message,
            BitmapFactory.decodeResource(resources, R.drawable.coracao).convertToString(),
            Date().time.toString()
        )
    }

}