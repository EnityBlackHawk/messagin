package com.blackhawk.messagin.api

import android.util.Log
import com.blackhawk.messagin.firebase.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Connection
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.jetbrains.annotations.ApiStatus.Experimental
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.time.Duration

class ServerSentEvent(private val uid : String, callback : (String) -> Unit) {

//    private val sseClient = OkHttpClient.Builder()
//        .connectTimeout(Duration.ofMinutes(5))
//        .readTimeout(Duration.ofMinutes(10))
//        .writeTimeout(Duration.ofMinutes(10))
//        .build()
//
//    private val sseRequest = Request.Builder()
//        .url(Constants.BASE_URL + "/api/sse/subscribe/${uid}")
//        .header("Accept", "application/json")
//        .addHeader("Accept", "text/event-stream")
//        .build()
//
//    private val sseEventSourceListener = object : EventSourceListener()
//    {
//
//        var callback : ((String) -> Unit)? = null
//
//        override fun onOpen(eventSource: EventSource, response: Response) {
//            super.onOpen(eventSource, response)
//            Log.d("SSE", "Connection succeeded")
//        }
//
//        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
//            super.onEvent(eventSource, id, type, data)
//            Log.d("SSE", data)
//            callback?.invoke(data)
//        }
//
//        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
//            super.onFailure(eventSource, t, response)
//            Log.d("SSE", "Failure: ${t?.message}, ${response?.message}")
//        }
//
//        override fun onClosed(eventSource: EventSource) {
//            super.onClosed(eventSource)
//            Log.d("SSE", "Connection Closed")
//        }
//
//        fun addCallback(callback : (String) -> Unit)
//        {
//            this.callback = callback
//        }
//
//    }
//
//    fun connect(callback : (String) -> Unit) {
//        sseEventSourceListener.addCallback(callback)
//        EventSources.createFactory(sseClient)
//            .newEventSource(sseRequest, sseEventSourceListener)
//    }



    companion object {
        private val callbacks : MutableList<(String)->Unit> = mutableListOf()
        private lateinit var connection: HttpURLConnection
        private var thread : Thread? = null
    }

    init {
        callbacks.add(callback)

    }

    fun addCallback(callback : (String) -> Unit)
    {
        callbacks.add(callback)
    }

    fun receiveEvents()
    {
        Thread {
            Log.d("SSE", "Thread running")
            val url = URL(Constants.BASE_URL + "/api/sse/subscribe/$uid")
            connection = url.openConnection() as HttpURLConnection

            val inputStream = connection.inputStream

            BufferedReader(InputStreamReader(inputStream)).lines().forEach {
                Log.d("SSE", it)
                callbacks.forEach { c ->
                    c(it)
                }
            }
        }.start()
    }

    fun disconnect()
    {
        connection.disconnect()
    }

}