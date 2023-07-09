package com.blackhawk.messagin

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources.Theme
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.blackhawk.messagin.tools.toBitmap
import com.blackhawk.messagin.ui.theme.primaryColor
import kotlin.random.Random


class NotificationService(private val context : Context) {

     var hasNotificationPermission = false
         private set

    private val notificationManager : NotificationManager

    companion object {
        val is33SDK = Build.VERSION.SDK_INT >= 33

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        val permissionDefine = Manifest.permission.POST_NOTIFICATIONS
    }

    init {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(notificationManager.notificationChannels.size < 1)
        {
            val channel = NotificationChannel (
                "main",
                "Main",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableVibration(true)
            channel.enableLights(true)
            notificationManager.createNotificationChannel(channel)
        }



        verifyPermission()
    }

    fun verifyPermission()
    {
        hasNotificationPermission =
            if(is33SDK)
                context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            else true
    }

    fun pushNotification(title: String, messageTitle: String, message: String?, imageByteArray: String)
    {

        val intent = Intent(context, MainActivity::class.java)

        val notificationId = Random.nextInt()


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val bit = imageByteArray.toBitmap()
        val notification = NotificationCompat.Builder(context, "main")
            .setContentTitle(title)
            .setContentText(messageTitle)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setShowWhen(true)
            .setLargeIcon(bit)
            .setColor(primaryColor.toArgb())
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bit)
                    .bigLargeIcon(null as Bitmap?)
                    .setBigContentTitle(messageTitle)
                    .setSummaryText(message)
            )
            .build()


        notificationManager.notify(notificationId, notification)
    }


}