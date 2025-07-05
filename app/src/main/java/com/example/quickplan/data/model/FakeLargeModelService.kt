package com.example.quickplan.data.model

import com.example.quickplan.ModelResponse
import kotlinx.coroutines.delay

class FakeLargeModelService : LargeModelService {
    override suspend fun processImage(base64Image: String, prompt: String): ModelResponse {
        // Simulate network delay
        delay(1000)
        return ModelResponse(
            deadlineDate = "2025-12-31",
            deadlineTime = "23:59",
            notificationContent = "模拟通知内容：请在截止日期前完成任务。"
        )
    }
}
