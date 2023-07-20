package com.blackhawk.messagin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blackhawk.messagin.R
import com.blackhawk.messagin.data.MessagePersist
import com.blackhawk.messagin.tools.toBitmap
import com.blackhawk.messagin.ui.theme.MessaginTheme
import com.blackhawk.messagin.ui.theme.bottomSheetColor
import com.blackhawk.messagin.viewModel.HistoricViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun Historic(viewModel: HistoricViewModel?) {
    Column {
        Header(modifier = Modifier
            .height(100.dp)
            .fillMaxWidth())
        ListOfMessages(viewModel, modifier = Modifier
            .weight(1f)
            .fillMaxWidth())
    }
}

@Composable
fun Header(modifier: Modifier = Modifier) {

    Box(modifier) {
        Surface(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(0f, 0f, 50f, 50f)
            ) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = "",
                    modifier = Modifier
                        .height(32.dp)
                        .width(32.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                )
                Spacer(modifier = Modifier.width(25.dp))
                Text(text = "Mensagens enviadas", style = MaterialTheme.typography.titleLarge)
                
            }
        }
    }

}


@Composable
fun ListOfMessages(viewModel: HistoricViewModel?, modifier: Modifier = Modifier) {


    LaunchedEffect(true)
    {
        viewModel?.loadStoredMessages()
        viewModel?.requestIsDelivered()
    }


    val list : List<MessagePersist> = viewModel?.storedMessages ?: listOf()



    LazyColumn(modifier = modifier) {
        items(list.size) {
            Row(
                modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(8.dp)
                    .background(bottomSheetColor, RoundedCornerShape(15))
            ) {

                Image(
                    painter = BitmapPainter(list[it].imageBitmap.toBitmap().asImageBitmap()),
                    "",
                    modifier = Modifier
                        .height(65.dp)
                        .width(65.dp)
                        .align(Alignment.CenterVertically)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = list[it].title,
                        maxLines = 1,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = list[it].message,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Image(
                    painter = painterResource(
                        if(list[it].wasDelivered) R.drawable.baseline_check_circle_24
                        else R.drawable.baseline_close_24),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(0.dp, 8.dp, 0.dp, 0.dp)
                        .width(24.dp)
                        .height(24.dp),
                    alignment = Alignment.TopEnd,
                    colorFilter = ColorFilter.tint(
                        if(list[it].wasDelivered) Color(0xFF81C784) else Color(0xFFE57373)
                    )
                )


            }
        }
    }

}


@Preview(showBackground = true, widthDp = 500, heightDp = 1000)
@Composable
fun ListPrev() {
    MessaginTheme {
        Historic(viewModel = null)
    }
}