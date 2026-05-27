package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LearningViewModelFactory(
    private val initialStarCount: Int,
    private val saveStarCount: (Int) -> Unit
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LearningViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LearningViewModel(initialStarCount, saveStarCount) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
