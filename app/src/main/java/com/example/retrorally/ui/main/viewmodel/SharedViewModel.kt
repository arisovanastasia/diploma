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

    private val _carNumber = MutableLiveData<Int>()
    val carNumber: LiveData<Int> = _carNumber
    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result
    private val _comment = MutableLiveData<String>()
    val comment: LiveData<String> = _comment
    private val _participantsLiveData = MutableLiveData<ArrayList<Participant>>()
    val participantsLiveData: LiveData<ArrayList<Participant>> = _participantsLiveData

    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _error.value = "Exception handled : ${throwable.localizedMessage}"
    }

    private fun getContestData() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = RetroRallyApi.retrofitService.getJudgeWithData()
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

    fun onButtonClick() {
        _loading.value = true
        getContestData()
    }

    fun onSendButtonClick(num: Int) {
        _carNumber.value = num
    }

    fun onClickToAddItem() {
        if (_carNumber.value != "".toInt()) {
            addItemToRecycler()
        }
    }

    private fun addItemToRecycler() {

    }

    fun clearItem() {
        _carNumber.value = "".toInt()
        _result.value = ""
        _comment.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
