package com.example.quickplan.data.model

import android.util.Log

import com.example.quickplan.data.model.ModelResponse
import com.example.quickplan.data.model.ModelServiceResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

import java.util.concurrent.TimeUnit

class LargeModelServiceImpl : LargeModelService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val GSON = Gson()

    override suspend fun processImage(base64Image: String, prompt: String): ModelServiceResponse {
        return withContext(Dispatchers.IO) {
            try {
                val url = "http://192.168.43.227:8081/send/1"

                Log.d("ImageUploadService", "Client sending base64Image length: ${base64Image.length}")
                Log.d("ImageUploadService", "Client sending base64Image start: ${base64Image.take(100)}")

                val jsonObject = JSONObject().apply {
                    put("image", base64Image) // Directly sending base64Image
                    put("prompt", prompt)
                }
                val requestBody = jsonObject.toString().toRequestBody(JSON)

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val rawResponseBody = response.body?.string()
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code ${response.code}: ${rawResponseBody}")
                    }
                    if (rawResponseBody.isNullOrEmpty()) {
                        throw IOException("Server returned empty or null response body.")
                    }
                    val modelResponse = GSON.fromJson(rawResponseBody, ModelResponse::class.java)
                    ModelServiceResponse(modelResponse, rawResponseBody)
                }
            } catch (e: Exception) {
                Log.e("ImageUploadService", "Error in processImage", e) // Use Log.e for errors
                throw e // Re-throw the exception to be caught by ViewModel
            }
        }
    }
}

