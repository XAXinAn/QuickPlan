package com.example.quickplan.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * AI 对话后端 API 接口定义
 * 
 * ⚠️ 所有后端API调用位置 - 需要修改baseUrl请在 RetrofitClient.kt 中修改
 */
interface AiApiService {
    
    /**
     * 发送聊天消息
     * POST http://localhost:8080/api/ai/chat
     * 
     * 📍 API调用位置 #1: 发送用户消息并获取AI回复
     */
    @POST("api/ai/chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>
    
    /**
     * 获取所有对话列表
     * GET http://localhost:8080/api/ai/conversations
     * 
     * 📍 API调用位置 #2: 加载对话历史列表
     */
    @GET("api/ai/conversations")
    suspend fun getConversations(): Response<ConversationsResponse>
    
    /**
     * 获取指定对话的详细信息
     * GET http://localhost:8080/api/ai/conversations/{conversationId}
     * 
     * 📍 API调用位置 #3: 加载某个对话的完整消息历史
     */
    @GET("api/ai/conversations/{conversationId}")
    suspend fun getConversationDetail(@Path("conversationId") conversationId: String): Response<ConversationDetailResponse>
    
    /**
     * 创建新对话
     * POST http://localhost:8080/api/ai/conversations
     * 
     * 📍 API调用位置 #4: 用户点击"新建对话"时
     */
    @POST("api/ai/conversations")
    suspend fun createConversation(@Body request: CreateConversationRequest): Response<CreateConversationResponse>
    
    /**
     * 删除对话
     * DELETE http://localhost:8080/api/ai/conversations/{conversationId}
     * 
     * 📍 API调用位置 #5: 用户删除某个对话时
     */
    @DELETE("api/ai/conversations/{conversationId}")
    suspend fun deleteConversation(@Path("conversationId") conversationId: String): Response<DeleteConversationResponse>
}
