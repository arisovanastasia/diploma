package com.example.retrorally.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.retrorally.data.network.ContestDataDTO

class JudgeViewModelFactory(private val contestData : ContestDataDTO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(JudgeViewModel::class.java)) {
            return JudgeViewModel(contestData) as T
        }
        else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
