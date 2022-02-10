package com.example.retrorally.data.models.dto

import com.squareup.moshi.Json

data class ResultsDTO(
    @Json(name = "time") val time : String? = null,
    @Json(name = "cone") val cone: String? = null,
    @Json(name = "button") val button: String? = null,
    @Json(name = "square") val square: String? = null,
    @Json(name = "finish_line") val finish_line: String? = null,
    @Json(name = "stop_line") val stop_line: String? = null,
)