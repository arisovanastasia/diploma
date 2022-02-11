package com.example.retrorally.ui.main.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.retrorally.data.models.Participant
import com.example.retrorally.data.models.dto.ContestDataDTO
import com.example.retrorally.data.models.dto.ParticipantDTO
import com.example.retrorally.data.models.dto.ResultsDTO
import com.example.retrorally.data.network.RetroRallyApi
import kotlinx.coroutines.*
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SharedViewModel : ViewModel() {

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    private val _contestLiveData = MutableLiveData<ContestDataDTO>()
    val contestLiveData: LiveData<ContestDataDTO> = _contestLiveData

    private val _participantsLiveData = MutableLiveData<MutableList<Participant>>()
    val participantsLiveData: LiveData<MutableList<Participant>> = _participantsLiveData

    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _error.value = "Exception handled : ${throwable.localizedMessage}"
    }

    private fun getContestData(password: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                val response = RetroRallyApi.retrofitService.getJudgeWithData(password)
                Log.d("code", "response code is ${response.code()}")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        _contestLiveData.value = response.body()
                        _loading.value = false
                    } else {
                        _error.value = response.message()
                        _loading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.d("MyError", "${e.message}")
            }
        }
    }

    fun onButtonClick(password: String) {
        _loading.value = true
        getContestData(password)
    }

    fun addItemToLiveData(num: String, score: String, comment: String) {
        postParticipantDataToProtocol(num, score, comment)
        _participantsLiveData.value?.add(
            0,
            Participant(num, score, comment)
        )
        _participantsLiveData.value = _participantsLiveData.value
    }

    private fun postParticipantDataToProtocol(num: String, score: String, comment: String) {

    }

    fun setInitialParticipantLiveData(participantsDTO: List<ParticipantDTO>) {
        _participantsLiveData.value =
            participantsDTO.map {
                Participant(
                    it.participantNumber,
                    getParticipantResult(it.result),
                    it.comment
                )
            }
                .toMutableList()
    }

    private fun getParticipantResult(resultsDTO: ResultsDTO): String {
        var result = ""
        if (resultsDTO.time != null) {
            result += "Время: ${resultsDTO.time} \n"
        }
        return result
    }

    fun getLocalTime(dateTime: String): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val time = OffsetDateTime.parse(dateTime).atZoneSameInstant(ZoneId.systemDefault())
        return formatter.format(time)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}
