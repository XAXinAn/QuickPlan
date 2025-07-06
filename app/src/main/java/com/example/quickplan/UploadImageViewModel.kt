package com.example.quickplan

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.quickplan.data.model.LargeModelService
import com.example.quickplan.data.model.ModelResponse
import com.example.quickplan.data.model.ModelServiceResponse

class UploadImageViewModel(application: Application, private val largeModelService: LargeModelService) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _modelResponse = MutableStateFlow<ModelResponse?>(null)
    val modelResponse: StateFlow<ModelResponse?> = _modelResponse

    private val _rawResponse = MutableStateFlow<String?>(null)
    val rawResponse: StateFlow<String?> = _rawResponse

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun uploadImage(imageUri: Uri) {
        _isLoading.value = true
        _error.value = null
        _modelResponse.value = null
        _rawResponse.value = null

        viewModelScope.launch {
            try {
                val base64Image = Base64Converter.uriToBase64(getApplication(), imageUri)
                val prompt = "从这张图片中提取截止日期、截止时间和相关的通知内容。请以 JSON 格式返回，例如：{\"deadlineDate\": \"YYYY-MM-DD\", \"deadlineTime\": \"HH:MM\", \"notificationContent\": \"...\"}。如果信息不存在，请使用 null。"
                val serviceResponse = largeModelService.processImage(base64Image, prompt)
                _modelResponse.value = serviceResponse.modelResponse
                _rawResponse.value = serviceResponse.rawResponse

            } catch (e: Exception) {
                e.printStackTrace() // Print stack trace for debugging
                _error.value = "Error uploading image: ${e::class.simpleName}: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearRawResponse() {
        _rawResponse.value = null
    }
}
