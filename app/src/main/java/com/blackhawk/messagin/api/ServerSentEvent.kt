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



class ServerSentEvent(private val uid : String, callback : (SSEResponse) -> Unit) {

    data class SSEResponse (val data : Map<String, String>)

    companion object {
        private val callbacks : MutableList<(SSEResponse)->Unit> = mutableListOf()
        private lateinit var connection: HttpURLConnection
        private var thread : Thread? = null
    }

    init {
        callbacks.add(callback)
    }

    fun addCallback(callback : (SSEResponse) -> Unit)
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
                if(!it.isNullOrBlank())
                {
                    val sr = convertJson(it.substring(6))
                    callbacks.forEach { c ->
                        c(sr)
                    }

                }
            }
        }.start()
    }

    fun convertJson(json : String) : SSEResponse
    {
        Log.d("JSON", "Start")


        var editedJson = json.substring(0, json.length - 1)
        var map : MutableMap<String, String> = mutableMapOf()
        var list : MutableList<String> = mutableListOf()
        var breakpointPosition = 0
        var i = 0
        while (true) {
            if (i >= editedJson.length) {
                list.add(editedJson.substring(breakpointPosition, i))
                break
            }
            val char = editedJson[i]
            when (char) {
                ',' -> {
                    list.add(editedJson.substring(breakpointPosition, i))
                    breakpointPosition = i + 1
                    i++
                }
                //TODO -> Se objeto dentro de objeto, haverá bug
                '{' -> {
                    var j = i
                    var char2 = editedJson[j + 1]
                    while (char2 != '}') {
                        char2 = editedJson[j++]
                    }

                    i = j
                }

                else -> i++

            }
        }



        list.forEach { s ->
            val key_value = s.split(":".toRegex(), 2)

            map[key_value[0].filter { it != '"' }] = key_value[1].filter { it != '"' }
        }

        list.forEach {
            Log.d("JSON", it)
        }


        return SSEResponse(map)

    }


    fun disconnect()
    {
        connection.disconnect()
    }

}