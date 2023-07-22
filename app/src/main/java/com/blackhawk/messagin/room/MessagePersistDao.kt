package com.blackhawk.messagin.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.blackhawk.messagin.data.MessagePersist
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagePersistDao {

    @Insert
    suspend fun insert(messagePersist: MessagePersist)

    @Query("SELECT * FROM message_persist")
    fun getAll() : Flow<List<MessagePersist>>

    @Update
    suspend fun update(messagePersist: MessagePersist)

}