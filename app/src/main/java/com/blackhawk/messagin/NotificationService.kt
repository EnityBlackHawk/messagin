package com.blackhawk.messagin

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.blackhawk.messagin.activity.BubbleActivity
import com.blackhawk.messagin.tools.toBitmap
import com.blackhawk.messagin.ui.theme.primaryColor
import kotlinx.coroutines.runBlocking
import java.lang.RuntimeException
import java.util.Date
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

    fun pushNotification(title: String, messageTitle: String, message: String?, imageByteArray: String?, dateTime: String)
    {

        val intent = Intent(context, MainActivity::class.java)

        val notificationId = 100


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            FLAG_MUTABLE
        )
        val bit = imageByteArray?.toBitmap() ?: throw RuntimeException("Bit was null")

        // Bobbles
        val targetBobble = Intent(context, BubbleActivity::class.java)

        val key = Random.nextInt()
        runBlocking {
            val dataKey = stringPreferencesKey(key.toString())
            context.dataStore.edit {
                if(it.asMap().size == 10)
                    it.clear()
                it[dataKey] = imageByteArray
            }
        }

        targetBobble.apply {
            putExtra("title", messageTitle)
            putExtra("message", message)
            putExtra("imageKey", key.toString())
            putExtra("dateTime", dateTime)
            action = Intent.ACTION_MAIN
        }



        val bubbleIntent = PendingIntent.getActivity(
            context,
            2,
            targetBobble,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val category = "com.blackhawk.messagin.MESSAGE_RECEVED"

        val chatPartner =
            androidx.core.app.Person.Builder()
                .setName("Bebê")
                .setImportant(true)
                .setIcon(IconCompat.createWithBitmap(bit))
                .build()

        ShortcutManagerCompat.removeAllDynamicShortcuts(context)

        // Create sharing shortcut
        val shortcutId = "abc"
        val l = ShortcutManagerCompat.getDynamicShortcuts(context)

        val shortcut = if(l.isEmpty())
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ShortcutInfoCompat.Builder(context, shortcutId)
                        .setCategories(setOf(category))
                        .setIntent(targetBobble)
                        .setLongLived(true)
                        .setShortLabel(chatPartner.name!!)
                        .setIcon(IconCompat.createWithBitmap(bit))
                        .setPerson(chatPartner)
                        .build().also {
                            ShortcutManagerCompat.setDynamicShortcuts(context, listOf(it))
                        }
                } else {
                    ShortcutInfoCompat.Builder(context, shortcutId)
                        .setCategories(setOf(category))
                        .setIntent(Intent(Intent.ACTION_DEFAULT))
                        .setShortLabel(chatPartner.name!!)
                        .setIcon(IconCompat.createWithBitmap(bit))
                        .build().also {
                            ShortcutManagerCompat.setDynamicShortcuts(context, listOf(it))
                        }
                }
            }
            else l[0]


        // Create bubble metadata
        val bubbleMetadata =
            NotificationCompat.BubbleMetadata.Builder(bubbleIntent,
            IconCompat.createWithBitmap(bit))
                .setDesiredHeight(600)
                .setSuppressNotification(true)
                .build()


        val notification = NotificationCompat.Builder(context, "main")
            .setBubbleMetadata(bubbleMetadata)
            .setContentTitle(title)
            .setContentText(messageTitle)
            .setSmallIcon(R.drawable.coracao)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setShowWhen(true)
            .setLargeIcon(bit)
            .setColor(primaryColor.toArgb())
            .setShortcutId(shortcutId)
            .setStyle(
                NotificationCompat.MessagingStyle(chatPartner)
                    .setConversationTitle("Conversation")
                    .addMessage(
                        NotificationCompat.MessagingStyle.Message(
                            message, Date().time, chatPartner
                        )
                    )
            )
            .build()



        notificationManager.notify(notificationId, notification)
    }


}