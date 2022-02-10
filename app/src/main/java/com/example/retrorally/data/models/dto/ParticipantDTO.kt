package com.example.retrorally.data.models.dto

import com.squareup.moshi.Json

data class ParticipantDTO(
    @Json(name = "id") val id: String,
    @Json(name = "orig_id") val origId: String,
    @Json(name = "protocol") val protocolNumber: String,
    @Json(name = "time") val time: String,
    @Json(name = "participant") val participantNumber: String,
    @Json(name = "result") val result: ResultsDTO,
    @Json(name = "comment") val comment: String,
)