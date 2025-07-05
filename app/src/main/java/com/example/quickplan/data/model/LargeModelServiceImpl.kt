package com.example.quickplan.data.model

import com.example.quickplan.ModelResponse
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
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val GSON = Gson()

    override suspend fun processImage(base64Image: String, prompt: String): ModelResponse {
        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("image", base64Image)
                    put("prompt", prompt)
                }.toString()

                val request = Request.Builder()
                    .url("http://192.168.2.10:8080/send/1")
                    .post(jsonBody.toRequestBody(JSON))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code ${response.code}: ${response.body?.string()}")
                    }
                    val responseBody = response.body?.string()
                    GSON.fromJson(responseBody, ModelResponse::class.java)
                }
            } catch (e: Exception) {
                e.printStackTrace() // Print stack trace for debugging
                throw e // Re-throw the exception to be caught by ViewModel
            }
        }
    }
}
