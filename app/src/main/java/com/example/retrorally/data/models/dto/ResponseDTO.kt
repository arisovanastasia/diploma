package com.example.retrorally.data.models.dto

import com.squareup.moshi.Json

data class ResponseDTO(
    @Json(name = "id") val responseId: Int,
    @Json(name = "orig_id") val responseOrigId: Int,
    @Json(name = "protocol") val responseProtocolNumber: Int,
    @Json(name = "time") val responseTime: String,
    @Json(name = "participant") val responseParticipantNumber: String,
    @Json(name = "result") val responseResult: ResultsDTO,
    @Json(name = "comment") val responseComment: String,
    @Json(name = "image") val responseImage: String,
)