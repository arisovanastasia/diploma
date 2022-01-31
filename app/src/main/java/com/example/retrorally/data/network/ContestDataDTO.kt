package com.example.retrorally.data.network

import com.example.retrorally.data.models.Participant
import com.squareup.moshi.Json

data class ContestDataDTO(
    @Json(name = "name_of_area") val nameOfArea: String,
    @Json(name = "judge_start_time") val timeToStart: String,
    @Json(name = "judge_end_time") val timeToEnd: String,
    @Json(name = "description") val description: String,
    @Json(name = "protocol") val userProtocol: ArrayList<Participant>
)