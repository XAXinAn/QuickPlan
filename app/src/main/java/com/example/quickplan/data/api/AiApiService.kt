package com.example.quickplan.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * AI 对话后端 API 接口定义
 * 
 * ⚠️ 所有后端API调用位置 - 需要修改baseUrl请在 RetrofitClient.kt 中修改
 */
interface AiApiService {
    
    /**
     * 发送聊天消息（流式响应 - SSE）
     * POST http://localhost:8080/api/ai/chat
     * 
     * 📍 API调用位置 #1: 发送用户消息并获取AI流式回复
     * 
     * 注意：后端返回 text/event-stream 格式的 Flux<String>
     */
    @Streaming
    @POST("api/ai/chat")
    suspend fun sendMessageStream(@Body request: ChatRequest): Response<ResponseBody>
    
    /**
     * 获取用户的所有对话列表
     * GET http://localhost:8080/api/conversation/list/{userId}
     * 
     * 📍 API调用位置 #2: 加载对话历史列表
     */
    @GET("api/conversation/list/{userId}")
    suspend fun getConversations(@Path("userId") userId: String): Response<ConversationsResponse>
    
    /**
     * 获取指定对话的详细信息
     * GET http://localhost:8080/api/conversation/detail/{conversationId}
     * 
     * 📍 API调用位置 #3: 加载某个对话的基本信息
     */
    @GET("api/conversation/detail/{conversationId}")
    suspend fun getConversationDetail(@Path("conversationId") conversationId: String): Response<ConversationDetailResponse>
    
    /**
     * 获取对话的消息列表
     * GET http://localhost:8080/api/conversation/messages/{conversationId}
     * 
     * 📍 API调用位置 #3-2: 加载某个对话的完整消息历史
     */
    @GET("api/conversation/messages/{conversationId}")
    suspend fun getConversationMessages(@Path("conversationId") conversationId: String): Response<ConversationMessagesResponse>
    
    /**
     * 创建新对话
     * POST http://localhost:8080/api/ai/chat/new
     * 
     * 📍 API调用位置 #4: 用户点击"新建对话"时
     */
    @POST("api/ai/chat/new")
    suspend fun createConversation(@Body request: CreateConversationRequest): Response<CreateConversationResponse>
    
    /**
     * 删除对话
     * DELETE http://localhost:8080/api/conversation/delete/{conversationId}
     * 
     * 📍 API调用位置 #5: 用户删除某个对话时
     */
    @DELETE("api/conversation/delete/{conversationId}")
    suspend fun deleteConversation(@Path("conversationId") conversationId: String): Response<DeleteConversationResponse>
}
