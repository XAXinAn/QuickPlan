package com.example.quickplan

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quickplan.data.model.LargeModelService

class UploadImageViewModelFactory(private val application: Application, private val largeModelService: LargeModelService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadImageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UploadImageViewModel(application, largeModelService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
