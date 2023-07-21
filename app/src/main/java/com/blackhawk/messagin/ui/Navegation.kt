package com.blackhawk.messagin.ui

import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.blackhawk.messagin.viewModel.HistoricViewModel
import com.blackhawk.messagin.viewModel.MessaginViewModel
import com.blackhawk.messagin.viewModel.MessaginViewModelFactory

@androidx.compose.runtime.Composable
fun Navigation(viewModel: MessaginViewModel, historicViewModel: HistoricViewModel) {


    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route,
        enterTransition = {
            slideInHorizontally() {
                it
            }
        },
        exitTransition = {
            slideOutHorizontally {
                it
            }
        }
    )
    {


        composable(Screen.MainScreen.route,
            enterTransition = {
                slideInHorizontally {
                    -it
                }
            },
            exitTransition = {
                slideOutHorizontally {
                    -it
                }
            }
            )
        {
            SendMessage(navController, viewModel)
        }
        composable(Screen.Historic.route)
        {
            Historic(navController, historicViewModel)
        }
    }

}