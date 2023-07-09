package com.blackhawk.messagin.api

import com.blackhawk.messagin.data.Image
import com.blackhawk.messagin.data.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {


    @Headers("Content-Type: application/json")
    @POST("/api/notify/send")
    suspend fun sendNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>

    @POST("/api/image/get")
    suspend fun getByteArray(
        @Body image: Image
    ): Response<Image>

}