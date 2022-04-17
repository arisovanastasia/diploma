package com.example.retrorally.data.models.dto

import com.squareup.moshi.Json

data class ContestDataDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "name_of_area") val nameOfArea: String,
    @Json(name = "judge_start_time") val timeToStart: String,
    @Json(name = "judge_end_time") val timeToEnd: String,
    @Json(name = "description") val description: String,
    @Json(name = "strings") val usersProtocol: List<ParticipantDTO>,
    @Json(name = "inputs") val inputs: List<String>,
    @Json(name = "fast_comments") val fastComments: List<String>,
    @Json(name = "has_sensors") val hasSensors: Boolean
)