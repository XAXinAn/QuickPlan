package com.example.quickplan.data.model

import kotlinx.coroutines.delay

class FakeLargeModelService : LargeModelService {
    override suspend fun processImage(base64Image: String, prompt: String): ModelServiceResponse {
        // Simulate network delay
        delay(1000)
        val modelResponse = ModelResponse(
            hasNotification = true,
            events = listOf(
                Event(
                    title = "模拟日程标题",
                    deadline = "2025-12-31 23:59",
                    description = "这是一个模拟的日程描述。",
                    priority = "高",
                    source = "模拟来源"
                )
            ),
            confidence = 0.99
        )
        return ModelServiceResponse(modelResponse, "模拟原始响应：{\"hasNotification\": true, \"events\": [{\"title\": \"模拟日程标题\", \"deadline\": \"2025-12-31 23:59\", \"description\": \"这是一个模拟的日程描述。\", \"priority\": \"高\", \"source\": \"模拟来源\"}], \"confidence\": 0.99}")
    }
}
