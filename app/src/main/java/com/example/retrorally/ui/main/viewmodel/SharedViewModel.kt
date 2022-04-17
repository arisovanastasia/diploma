package com.example.retrorally.ui.main.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.retrorally.data.models.Participant
import com.example.retrorally.data.models.dto.ContestDataDTO
import com.example.retrorally.data.models.dto.ParticipantDTO
import com.example.retrorally.data.models.dto.ResponseDTO
import com.example.retrorally.data.models.dto.ResultsDTO
import com.example.retrorally.data.network.RetroRallyApi
import com.mbed.coap.exception.CoapCodeException
import com.mbed.coap.packet.Code
import com.mbed.coap.server.CoapExchange
import com.mbed.coap.server.CoapHandler
import com.mbed.coap.server.CoapServer
import com.mbed.coap.utils.CoapResource
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class SharedViewModel : ViewModel() {

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    private val _contestLiveData = MutableLiveData<ContestDataDTO>()
    val contestLiveData: LiveData<ContestDataDTO> = _contestLiveData
    private val _participantsLiveData = MutableLiveData<MutableList<Participant>>()
    val participantsLiveData: LiveData<MutableList<Participant>> = _participantsLiveData

    private var apiKey = ""

    private var job: Job? = null
    private val apiService = RetroRallyApi.retrofitService
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _error.value = "Exception handled : ${throwable.localizedMessage}"
    }

    private fun getContestData(password: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                val getResponse = apiService.getJudgeWithData(password)
                withContext(Dispatchers.Main) {
                    if (getResponse.isSuccessful) {
                        _contestLiveData.value = getResponse.body()
                        _loading.value = false
                    } else {
                        _error.value = getResponse.message()
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
        apiKey = password
        getContestData(password)
    }

    private fun addItemToLiveData(participant: Participant, targetPosition: Int? = null) {
        val participants = _participantsLiveData.value
        if (targetPosition != null) {
            participants?.removeAt(targetPosition)
            participants?.add(targetPosition, participant)
        } else {
            participants?.add(0, participant)
        }
        _participantsLiveData.value = participants ?: mutableListOf()
    }

    private fun mapDtoToParticipant(participantDTO: ResponseDTO): Participant {
        return Participant(
            participantDTO.responseId,
            participantDTO.responseParticipantNumber,
            participantDTO.responseResult,
            participantDTO.responseComment
        )
    }

    fun postParticipant(
        origId: Int,
        idOfProtocol: Int,
        num: String,
        score: ResultsDTO,
        comment: String,
        targetPosition: Int? = null
    ) {
        val time = Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val participantDto = ParticipantDTO(
            origId,
            idOfProtocol,
            sdf.format(time),
            num,
            score,
            comment,
            ""
        )
        postJsonToProtocol(participantDto, targetPosition)
    }

    private fun postJsonToProtocol(data: ParticipantDTO, targetPosition: Int? = null) {
        _loading.value = true
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            try {
                val postResponse = apiService.postItemInProtocol(apiKey, data)
                val responseBody = postResponse.body()
                withContext(Dispatchers.Main) {
                    if (postResponse.isSuccessful && responseBody != null) {
                        val participantDto = mapDtoToParticipant(responseBody)
                        addItemToLiveData(participantDto, targetPosition)
                        _loading.value = false
                    } else {
                        _error.value = postResponse.message()
                        _loading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.d("MyError", "${e.message}")
            }
        }
    }

    fun setInitialParticipantLiveData(participantsDTO: List<ParticipantDTO>) {
        _participantsLiveData.value =
            participantsDTO.map {
                Participant(
                    it.origId,
                    it.participantNumber,
                    it.result,
                    it.comment
                )
            }.toMutableList()
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
