package com.blackhawk.messagin.ui

sealed class Screen(var route : String)
{
    object MainScreen : Screen("MainScreen")
    object Historic : Screen("Historic")
}
