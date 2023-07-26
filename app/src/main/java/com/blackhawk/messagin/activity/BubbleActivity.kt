package com.blackhawk.messagin.activity

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import com.blackhawk.messagin.R
import com.blackhawk.messagin.dataStore
import com.blackhawk.messagin.tools.toBitmap
import com.blackhawk.messagin.ui.theme.MessaginTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BubbleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val values = intent.extras

        var bitString : String? = null

        runBlocking {
            val dataKey = stringPreferencesKey(values?.getString("imageKey")!!)
            Log.d("Bubble", values.getString("imageKey")!!)
            bitString = dataStore.data.first()[dataKey]
        }


        setContent {
            MessaginTheme {
                Content(values = values!!, bitString = bitString)
            }
        }
    }
}

@Composable
fun Content(values: Bundle, bitString : String?) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

       Column(modifier = Modifier.padding(14.dp)) {
           Image(
               painter = BitmapPainter(
                   bitString?.toBitmap()?.asImageBitmap() ?: BitmapFactory.decodeResource(
                       LocalContext.current.resources, R.drawable.coracao
                   ).asImageBitmap()
               ), contentDescription = "",
               Modifier.fillMaxWidth()
           )
           Text(
               text = values.getString("title") ?: "Title",
               style = MaterialTheme.typography.headlineLarge
           )
           Text(
               text = values.getString("message") ?: "Message",
               style = MaterialTheme.typography.titleLarge
           )
           Text(
               text = SimpleDateFormat(
                   "dd/MM/YYYY HH:mm",
                   Locale.getDefault()
               ).format(Date(values.getString("dateTime")?.toLong() ?: Date().time)),
               style = MaterialTheme.typography.bodyLarge

           )
       }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    MessaginTheme {
        Content(Bundle(1), null)
    }
}