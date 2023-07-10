package com.blackhawk.messagin.api

import com.blackhawk.messagin.firebase.Constants.Companion.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance {

    companion object {

        private val client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.HOURS)
            .readTimeout(1, TimeUnit.HOURS)
            .writeTimeout(1, TimeUnit.HOURS)
            .build()


        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api by lazy {
            retrofit.create(NotificationAPI::class.java)
        }
    }

}