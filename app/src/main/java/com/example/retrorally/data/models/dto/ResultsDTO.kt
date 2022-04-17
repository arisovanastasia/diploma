package com.example.retrorally.data.models.dto

import com.squareup.moshi.Json

data class ResultsDTO(
    @Json(name = "time_hm") var time_hm : String? = null,
    @Json(name = "time_hms") var time_hms : String? = null,
    @Json(name = "set_time") var set_time : String? = null,
    @Json(name = "start_time") var start_time : String? = null,
    @Json(name = "finish_time") var finish_time : String? = null,
    @Json(name = "cones") var cones: Int? = null,
    @Json(name = "buttons") var buttons: Int? = null,
    @Json(name = "squares") var squares: Int? = null,
    @Json(name = "stop_line") var stop_line: Int? = null,
    @Json(name = "base") var base: Int? = null,
    @Json(name = "scheme") var scheme: Int? = null
)