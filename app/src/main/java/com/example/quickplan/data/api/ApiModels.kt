package com.example.quickplan.data.api

/**
 * API 请求和响应数据模型
 * 这些类对应后端接口的JSON格式
 */

// ==================== 发送消息相关 ====================

/**
 * 发送消息请求体
 * POST /api/ai/chat
 */
data class ChatRequest(
    val memoryId: String, // 对话ID（必填）
    val message: String, // 用户输入的消息内容
    val userId: String? = null // 用户ID（可选）
)

// ==================== 对话历史相关 ====================

/**
 * 获取对话列表响应体
 * GET /api/conversation/list/{userId}
 */
data class ConversationsResponse(
    val success: Boolean,
    val data: List<ConversationSummary>,
    val total: Int
)

/**
 * 对话摘要（用于列表显示）
 */
data class ConversationSummary(
    val id: String,
    val userId: String,
    val title: String,
    val createdAt: String, // LocalDateTime 格式
    val updatedAt: String,
    val isDeleted: Int
)

// ==================== 单个对话详情相关 ====================

/**
 * 获取对话详情响应体
 * GET /api/conversation/detail/{conversationId}
 */
data class ConversationDetailResponse(
    val success: Boolean,
    val data: ConversationDetail
)

data class ConversationDetail(
    val id: String,
    val userId: String,
    val title: String,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Int
)

/**
 * 获取对话消息列表响应体
 * GET /api/conversation/messages/{conversationId}
 */
data class ConversationMessagesResponse(
    val success: Boolean,
    val data: List<MessageDto>,
    val total: Int
)

/**
 * 消息DTO（用于网络传输）
 */
data class MessageDto(
    val id: Long,
    val conversationId: String,
    val role: String, // "user" 或 "assistant"
    val content: String,
    val createdAt: String, // LocalDateTime 格式
    val isDeleted: Int
)

// ==================== 创建新对话相关 ====================

/**
 * 创建新对话请求体
 * POST /api/ai/chat/new 或 POST /api/conversation/create
 */
data class CreateConversationRequest(
    val userId: String, // 用户ID
    val title: String? = "新对话", // 对话标题
    val message: String? = null // 首条消息（可选）
)

/**
 * 创建新对话响应体
 */
data class CreateConversationResponse(
    val success: Boolean,
    val data: ConversationData,
    val message: String? = null
)

data class ConversationData(
    val id: String,
    val userId: String,
    val title: String,
    val createdAt: String,
    val updatedAt: String,
    val isDeleted: Int
)

// ==================== 删除对话相关 ====================

/**
 * 删除对话响应体
 * DELETE /api/ai/conversations/{memoryId}
 */
data class DeleteConversationResponse(
    val success: Boolean,
    val message: String
)

// ==================== 通用错误响应 ====================

/**
 * API错误响应
 */
data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: Long
)
