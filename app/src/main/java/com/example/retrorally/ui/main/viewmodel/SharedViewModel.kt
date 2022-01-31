package com.example.retrorally.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.retrorally.data.models.Participant
import com.example.retrorally.data.network.ContestDataDTO
import com.example.retrorally.data.network.RetroRallyApi
import kotlinx.coroutines.*

class SharedViewModel : ViewModel() {

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    private val _contestData = MutableLiveData<ContestDataDTO>()
    val contestData: LiveData<ContestDataDTO> = _contestData

    private val _participantsLiveData = MutableLiveData<MutableList<Participant>>()
    val participantsLiveData: LiveData<MutableList<Participant>> = _participantsLiveData

    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _error.value = "Exception handled : ${throwable.localizedMessage}"
    }

    private fun getContestData(password: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = RetroRallyApi.retrofitService.getJudgeWithData(password)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    _contestData.value = response.body()
                    _loading.value = false
                } else {
                    _error.value = response.message()
                    _loading.value = false
                }
            }
        }
    }

    fun onButtonClick(password : String) {
        _loading.value = true
        getContestData(password)
    }


    fun addItemToLiveData(num: String, score: String, comment: String) {
        _participantsLiveData.value?.add(
            0,
            Participant(num,score,comment)
        )
        _participantsLiveData.value = _participantsLiveData.value
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
