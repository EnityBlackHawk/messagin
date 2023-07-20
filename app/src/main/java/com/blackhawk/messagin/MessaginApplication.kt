package com.blackhawk.messagin

import android.app.Application
import com.blackhawk.messagin.room.AppDatabase

class MessaginApplication : Application() {

    val database : AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

}