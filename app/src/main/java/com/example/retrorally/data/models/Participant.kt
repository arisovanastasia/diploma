package com.example.retrorally.data.models

import com.example.retrorally.data.models.dto.ResultsDTO

data class Participant(
    val idOfString : Int,
    val participant: String,
    val result: ResultsDTO,
    val comment: String
)