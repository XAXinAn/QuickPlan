package com.example.quickplan.data.model

import com.example.quickplan.data.model.ModelResponse
import com.example.quickplan.data.model.ModelServiceResponse

interface LargeModelService {
    /**
     * Processes an image using a large model to extract information.
     *
     * @param base64Image The Base64 encoded string of the image.
     * @param prompt The text prompt to send to the large model.
     * @return A ModelServiceResponse object containing the extracted deadline date, time, and notification content, along with the raw response.
     * @throws Exception if an error occurs during the API call or response parsing.
     */
    suspend fun processImage(base64Image: String, prompt: String): ModelServiceResponse
}
