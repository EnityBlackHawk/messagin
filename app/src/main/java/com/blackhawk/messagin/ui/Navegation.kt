package com.blackhawk.messagin.ui

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.blackhawk.messagin.viewModel.MessaginViewModel
import com.blackhawk.messagin.viewModel.MessaginViewModelFactory

@androidx.compose.runtime.Composable
fun Navigation(viewModel: MessaginViewModel) {


    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route)
    {
        composable(Screen.MainScreen.route)
        {
            SendMessage(viewModel = viewModel)
        }
    }

}