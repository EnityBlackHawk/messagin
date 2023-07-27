package com.blackhawk.messagin.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.blackhawk.messagin.MessaginApplication
import com.blackhawk.messagin.NotificationService
import com.blackhawk.messagin.R
import com.blackhawk.messagin.tools.convertToString
import com.blackhawk.messagin.ui.theme.MessaginTheme
import com.blackhawk.messagin.viewModel.MessaginViewModel
import com.blackhawk.messagin.viewModel.MessaginViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date

class SendCustomMessageActivity : ComponentActivity() {

    val viewModel by viewModels<MessaginViewModel> {
        MessaginViewModelFactory(resources,
            (application as MessaginApplication).database.messagePersistDao())
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        var bit : Bitmap?
        runBlocking {
            bit = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri!!))
        }


        setContent {
            MessaginTheme {

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val context = this
                    SendCustomMessageContent(bit = bit!!) {
                        title, message ->
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.sendCustomMessage(this@SendCustomMessageActivity, title, message, bit!!)
                            finish()
                        }

                    }
                }

            }
        }

    }
}

@Composable
fun SendCustomMessageContent(bit: Bitmap, onSubmit: ((title : String, message : String) -> Unit)? = null) {

    var titleValue by remember { mutableStateOf(TextFieldValue("")) }
    var messageValue by remember { mutableStateOf(TextFieldValue("")) }

    var wasSend by remember {
        mutableStateOf(false)
    }


    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        Image(
            painter = BitmapPainter(bit.asImageBitmap()),
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
            Box(modifier = Modifier
                .height(50.dp)
                .weight(1f),
               contentAlignment = Alignment.CenterStart
               )
               {
                   Text(text = "Titulo:",
                       color = MaterialTheme.colorScheme.onBackground)
               }
            TextField(
                    value = titleValue,
                    onValueChange = {
                                    titleValue = it
                    },
                    placeholder = {
                        Text(text = "Insira um titulo", style = MaterialTheme.typography.bodyMedium)
                    }
                )
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)) {
            Box(modifier = Modifier
                .height(50.dp)
                .weight(1f),
                contentAlignment = Alignment.CenterStart
            )
            {
                Text(text = "Mensagem:", color = MaterialTheme.colorScheme.onBackground)
            }
            TextField(
                value = messageValue,
                onValueChange = {
                                messageValue = it
                },
                placeholder = {
                    Text(text = "Insira uma mensagem")
                }
            )
        }

        AnimatedContent(targetState = wasSend, label = "",
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) {
            if(!it)
            {
                Button(onClick = {
                    wasSend = true
                    onSubmit?.invoke(titleValue.text, messageValue.text)
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp)) {
                    Text(text = "Enviar mensagem")
                }
            }
            else
            {
                Button(onClick = {
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                    ) {
                    Text(text = "Enviar mensagem")
                }
            }
        }



    }

}

@RequiresApi(Build.VERSION_CODES.P)
@Preview(showBackground = true)
@Composable
fun CustomMessagePreview() {

    val context = LocalContext.current
    val bit = BitmapFactory.decodeResource(context.resources, R.drawable.kiss)

    MessaginTheme() {
        SendCustomMessageContent(bit = bit!!)
    }
}