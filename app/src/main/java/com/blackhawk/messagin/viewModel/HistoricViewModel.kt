package com.blackhawk.messagin.viewModel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blackhawk.messagin.api.RetrofitInstance
import com.blackhawk.messagin.data.MessagePersist
import com.blackhawk.messagin.data.User
import com.blackhawk.messagin.firebase.FirebaseService
import com.blackhawk.messagin.room.MessagePersistDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HistoricViewModel(private val dao: MessagePersistDao) : ViewModel() {

    //val storedMessages : MutableState<List<MessagePersist>> = mutableStateOf(mutableListOf())
    private var _storedMessages = mutableStateListOf<MessagePersist>()
    val storedMessages : List<MessagePersist> = _storedMessages

    fun loadStoredMessages() : List<MessagePersist>
    {
        _storedMessages.clear()
        val x = dao.getAll()
        var ret : List<MessagePersist> = listOf()
        runBlocking {
            _storedMessages.addAll(x.first())
            ret = x.first()
        }
        return ret
    }

    fun requestIsDelivered() {


        CoroutineScope(Dispatchers.IO).launch {
            val confirmations = RetrofitInstance.api.getConfirmations(FirebaseService.token!!)

            confirmations.body()?.forEach {tmp->
                val x = _storedMessages.find { it.id == tmp.id }
                x?.let { x.wasDelivered = true }
            }
        }

    }

}


class HistoricViewModelFactory(private val dao: MessagePersistDao) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoricViewModel(dao) as T
    }

}