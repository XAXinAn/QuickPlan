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
    val conversationId: String?, // 对话ID，新对话时为null
    val message: String // 用户输入的消息内容
)

/**
 * 发送消息响应体
 */
data class ChatResponse(
    val conversationId: String, // 对话ID（新对话会返回新ID）
    val messageId: String, // AI消息的唯一ID
    val reply: String, // AI的回复内容
    val timestamp: Long // 消息时间戳
)

// ==================== 对话历史相关 ====================

/**
 * 获取对话列表响应体
 * GET /api/ai/conversations
 */
data class ConversationsResponse(
    val conversations: List<ConversationSummary>
)

/**
 * 对话摘要（用于列表显示）
 */
data class ConversationSummary(
    val id: String,
    val title: String,
    val lastMessage: String?, // 最后一条消息预览
    val messageCount: Int, // 消息数量
    val createdAt: Long,
    val updatedAt: Long
)

// ==================== 单个对话详情相关 ====================

/**
 * 获取对话详情响应体
 * GET /api/ai/conversations/{conversationId}
 */
data class ConversationDetailResponse(
    val id: String,
    val title: String,
    val messages: List<MessageDto>,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 消息DTO（用于网络传输）
 */
data class MessageDto(
    val id: String,
    val content: String,
    val role: String, // "user" 或 "assistant"
    val timestamp: Long
)

// ==================== 创建新对话相关 ====================

/**
 * 创建新对话请求体
 * POST /api/ai/conversations
 */
data class CreateConversationRequest(
    val title: String // 对话标题
)

/**
 * 创建新对话响应体
 */
data class CreateConversationResponse(
    val id: String,
    val title: String,
    val createdAt: Long
)

// ==================== 删除对话相关 ====================

/**
 * 删除对话响应体
 * DELETE /api/ai/conversations/{conversationId}
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
