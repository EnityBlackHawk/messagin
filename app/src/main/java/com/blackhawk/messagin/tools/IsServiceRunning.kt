package com.blackhawk.messagin.tools

import android.app.ActivityManager
import android.content.Context

fun isServiceRunning(context: Context, serviceClass : Class<Any>) : Boolean
{
   val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return false
}