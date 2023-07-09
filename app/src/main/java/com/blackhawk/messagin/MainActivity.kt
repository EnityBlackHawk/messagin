package com.blackhawk.messagin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.blackhawk.messagin.api.RetrofitInstance
import com.blackhawk.messagin.data.Message
import com.blackhawk.messagin.data.NotificationData
import com.blackhawk.messagin.data.PushNotification
import com.blackhawk.messagin.data.User
import com.blackhawk.messagin.firebase.FirebaseService
import com.blackhawk.messagin.tools.convertToString
import com.blackhawk.messagin.ui.theme.MessaginTheme
import com.blackhawk.messagin.ui.theme.bottomSheetColor
import com.blackhawk.messagin.viewModel.MessaginViewModel
import com.blackhawk.messagin.viewModel.MessaginViewModelFactory
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

const val TAG = "MainActivity"

const val TOPIC = "/topics/MyTopic"

class MainActivity : ComponentActivity() {

    private lateinit var notification : NotificationService

    private val viewModel : MessaginViewModel by viewModels {
        MessaginViewModelFactory(resources)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseService.sharedPreferences = getSharedPreferences("TokenPreferences", MODE_PRIVATE)
        notification = NotificationService(this)

        if(!notification.hasNotificationPermission)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(NotificationService.permissionDefine), 1)
            }
            notification.verifyPermission()
        }

        Firebase.messaging.isAutoInitEnabled = true

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, token)
            FirebaseService.token = token

            CoroutineScope(Dispatchers.IO).launch {
                RetrofitInstance.api.registerUser(
                    User(token, null)
                )
            }

        })

        setContent {
            MessaginTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SendMessage(viewModel)
                }
            }
        }
    }


}

@Composable
fun Header() {
    Surface(
        modifier = Modifier.background(
            MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(0f, 0f, 0.5f, 0.5f)
        )
    ) {

    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMessage(viewModel: MessaginViewModel?) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scaffoldState = rememberBottomSheetScaffoldState(
        SheetState(false, SheetValue.PartiallyExpanded)
    )
    val scope = rememberCoroutineScope()



    val messages by remember { viewModel!!.messagesList }

    BottomSheetScaffold(
        sheetContent = {
            BottomSheetContent(viewModel = viewModel)
            {
                LaunchedEffect(Dispatchers.Default) {
                    scope.launch {
                        scaffoldState.bottomSheetState.hide()
                    }
                }
            }
        },
        scaffoldState = scaffoldState,
        sheetSwipeEnabled = true,
        sheetContainerColor = bottomSheetColor,
        modifier = Modifier.pointerInput(Unit)
        {
            detectTapGestures {
                scope.launch {
                    if(scaffoldState.bottomSheetState.isVisible)
                        scaffoldState.bottomSheetState.hide()
                }
            }
        }
    ) {

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if(event == Lifecycle.Event.ON_STOP)
                {
                    Log.d("Main", "Pause")
                    scope.launch {
                        scaffoldState.bottomSheetState.show()
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            //verticalArrangement = Arrangement.Center
        ) {
            items(messages) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clickable {
                            viewModel?.selectedMessage?.value = it
                            scope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
                            println("click")
                        }
                ) {

                    Image(
                        painter = painterResource(id = it.imageResource),
                        contentDescription = "",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterVertically)
                            .padding(10.dp)
                            .weight(2f),
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomSheetContent(
    viewModel: MessaginViewModel?,
    modifier: Modifier = Modifier,
    onClose : @Composable () -> Unit
) {

    var isSended by remember {
        mutableStateOf(false)
    }
    var forExit by remember {
        mutableStateOf(false)
    }

    var scope = rememberCoroutineScope()

    val selectedMessage by remember {
        viewModel!!.selectedMessage
    }

    var messageValue by remember {
         mutableStateOf( TextFieldValue(viewModel?.messageText?.value!!) )
    }

    if(forExit)
        onClose()



    Column(modifier = Modifier.padding(14.dp)) {
        Text(
            text = selectedMessage.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            item {

                Box(
                    modifier = Modifier.height(50.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(text = "Mensagem:")
                }

            }
            item {
                TextField(
                    value = messageValue,
                    onValueChange = {
                        viewModel?.messageText?.value = it.text
                        messageValue = it
                    },
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = bottomSheetColor),
                    placeholder = {
                        Text(text = "Insira a mensagem")
                    }
                )
            }
            item(span = {
                GridItemSpan(2)
            }) {
                
                AnimatedContent(
                    targetState = isSended,
                    transitionSpec = {
                        fadeIn() with fadeOut()
                    }
                ) {
                    if(!it)
                        Button(
                            modifier = Modifier.padding(0.dp, 14.dp, 0.dp, 0.dp),
                            onClick = {
                                viewModel!!.sendMessage()
                                isSended = true

                                CoroutineScope(Dispatchers.IO).launch {
                                    delay(3000)
                                    forExit = true
                                    isSended = false
                                }



                            }) {
                            Text(text = "Enviar mensagem")
                        }
                    else
                        Button(
                            modifier = Modifier.padding(0.dp, 14.dp, 0.dp, 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            onClick = {
                            }) {
                            Text(text = "Mensagem enviada")
                        }
                }
                

            }
        }

    }

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MessaginTheme {
        Column(Modifier.fillMaxSize()) {
            Header()
            SendMessage(viewModel = MessaginViewModel(LocalContext.current.resources))
        }

    }
}