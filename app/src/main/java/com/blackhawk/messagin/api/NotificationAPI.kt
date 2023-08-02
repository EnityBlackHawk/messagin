package com.blackhawk.messagin.api

import com.blackhawk.messagin.data.Confirmation
import com.blackhawk.messagin.data.Image
import com.blackhawk.messagin.data.NotificationSendResponse
import com.blackhawk.messagin.data.PushNotification
import com.blackhawk.messagin.data.RequestConfirmation
import com.blackhawk.messagin.data.User
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotificationAPI {


    @Headers("Content-Type: application/json")
    @POST("/api/notify/send")
    suspend fun sendNotification(
        @Body notification: PushNotification
    ): Response<NotificationSendResponse>

    @POST("/api/image/get")
    suspend fun getByteArray(
        @Body image: Image
    ): Response<Image>

    @POST("/api/user/register")
    suspend fun registerUser(
        @Body user : User
    ) : Response<User>

    @PUT("/api/request/confirm")
    suspend fun confirmMessage(
        @Body confirmation: Confirmation
    ) : Response<ResponseBody>

    @GET("/api/request/getConfirmations/{token}")
    suspend fun getConfirmations(
        @Path("token") token : String
    ) : Response<List<RequestConfirmation>>


    @GET("/api/request/getMessageStatus/{messageId}")
    suspend fun getMessageStatus(
        @Path("messageId") messageId : String
    ) : Response<RequestConfirmation>

}