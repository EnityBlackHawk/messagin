package com.blackhawk.messagin.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

fun Bitmap.convertToString() : String
{
    val arrayOut = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, arrayOut)
    val b = arrayOut.toByteArray()
    return Base64.encodeToString(b, Base64.DEFAULT)
}

fun String.toBitmap() : Bitmap
{
    val imagesByte = Base64.decode(this, 0)
    return BitmapFactory.decodeByteArray(imagesByte, 0, imagesByte.size)
}