package com.example.quickplan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickplan.data.api.*
import com.example.quickplan.data.model.Conversation
import com.example.quickplan.data.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AI 对话界面 ViewModel
 * 管理对话状态和后端API交互
 * 
 * 📍 所有后端API调用都通过这个类进行
 */
class AiViewModel : ViewModel() {
    
    // API 服务实例
    private val apiService = RetrofitClient.aiApiService
    
    // 当前选中的对话ID
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()
    
    // 当前对话的消息列表
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    // 对话历史列表
    private val _conversations = MutableStateFlow<List<ConversationSummary>>(emptyList())
    val conversations: StateFlow<List<ConversationSummary>> = _conversations.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 是否显示侧边栏
    private val _showSidebar = MutableStateFlow(false)
    val showSidebar: StateFlow<Boolean> = _showSidebar.asStateFlow()
    
    init {
        // 启动时加载对话列表
        loadConversations()
    }
    
    /**
     * 📍 API调用位置 #1: 发送消息
     * 调用 POST /api/ai/chat
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // 添加用户消息到界面
                val userMessage = Message(content = content, isUser = true)
                _messages.value = _messages.value + userMessage
                
                // 调用后端API
                val request = ChatRequest(
                    conversationId = _currentConversationId.value,
                    message = content
                )
                
                val response = apiService.sendMessage(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val chatResponse = response.body()!!
                    
                    // 更新当前对话ID（新对话时）
                    if (_currentConversationId.value == null) {
                        _currentConversationId.value = chatResponse.conversationId
                    }
                    
                    // 添加AI回复到界面
                    val aiMessage = Message(
                        id = chatResponse.messageId,
                        content = chatResponse.reply,
                        isUser = false,
                        timestamp = chatResponse.timestamp
                    )
                    _messages.value = _messages.value + aiMessage
                    
                    // 刷新对话列表
                    loadConversations()
                } else {
                    _errorMessage.value = "发送失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 📍 API调用位置 #2: 加载对话列表
     * 调用 GET /api/ai/conversations
     */
    fun loadConversations() {
        viewModelScope.launch {
            try {
                val response = apiService.getConversations()
                if (response.isSuccessful && response.body() != null) {
                    _conversations.value = response.body()!!.conversations
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 📍 API调用位置 #3: 加载对话详情
     * 调用 GET /api/ai/conversations/{conversationId}
     */
    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = apiService.getConversationDetail(conversationId)
                
                if (response.isSuccessful && response.body() != null) {
                    val detail = response.body()!!
                    _currentConversationId.value = detail.id
                    
                    // 转换消息格式
                    _messages.value = detail.messages.map { dto ->
                        Message(
                            id = dto.id,
                            content = dto.content,
                            isUser = dto.role == "user",
                            timestamp = dto.timestamp
                        )
                    }
                } else {
                    _errorMessage.value = "加载对话失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 📍 API调用位置 #4: 创建新对话
     * 调用 POST /api/ai/conversations
     */
    fun createNewConversation() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val request = CreateConversationRequest(
                    title = "新对话"
                )
                
                val response = apiService.createConversation(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val newConversation = response.body()!!
                    _currentConversationId.value = newConversation.id
                    _messages.value = emptyList()
                    
                    // 刷新对话列表
                    loadConversations()
                    
                    // 关闭侧边栏
                    _showSidebar.value = false
                } else {
                    _errorMessage.value = "创建对话失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 📍 API调用位置 #5: 删除对话
     * 调用 DELETE /api/ai/conversations/{conversationId}
     */
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteConversation(conversationId)
                
                if (response.isSuccessful) {
                    // 如果删除的是当前对话，清空消息
                    if (_currentConversationId.value == conversationId) {
                        _currentConversationId.value = null
                        _messages.value = emptyList()
                    }
                    
                    // 刷新对话列表
                    loadConversations()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 切换侧边栏显示状态
     */
    fun toggleSidebar() {
        _showSidebar.value = !_showSidebar.value
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 开始新对话（不调用API，仅清空本地状态）
     */
    fun startNewConversation() {
        _currentConversationId.value = null
        _messages.value = emptyList()
        _showSidebar.value = false
    }
}
