package com.blackhawk.messagin.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.blackhawk.messagin.data.MessagePersist

@Database(entities = [MessagePersist::class], version = 1)
abstract class AppDatabase : RoomDatabase() {


    abstract fun messagePersistDao() : MessagePersistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase
        {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = inst
                inst
            }
        }

    }

}