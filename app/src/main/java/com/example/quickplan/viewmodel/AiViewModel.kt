package com.example.quickplan.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quickplan.data.api.*
import com.example.quickplan.data.model.Conversation
import com.example.quickplan.data.model.Message
import com.example.quickplan.utils.MLKitOCRHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AI 对话界面 ViewModel
 * 管理对话状态和后端API交互
 * 
 * 📍 所有后端API调用都通过这个类进行
 */
class AiViewModel(application: Application) : AndroidViewModel(application) {
    
    // API 服务实例
    private val apiService = RetrofitClient.aiApiService
    
    // OCR 助手 (Google ML Kit 会自动管理模型下载)
    private val ocrHelper = MLKitOCRHelper
    
    override fun onCleared() {
        super.onCleared()
        // 释放 ML Kit 资源
        MLKitOCRHelper.release()
    }
    
    // 当前用户ID（实际应用中应该从登录状态获取）
    private val currentUserId = "default_user_001"
    
    // 当前选中的对话ID
    private val _currentMemoryId = MutableStateFlow<String?>(null)
    val currentMemoryId: StateFlow<String?> = _currentMemoryId.asStateFlow()
    
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
     * 📍 API调用位置 #1: 发送消息（流式接收）
     * 调用 POST /api/ai/chat
     * 后端返回 Flux<String> 格式的流式数据
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
                
                // 创建一个临时的 AI 消息用于显示流式内容
                val aiMessageId = "ai-msg-${System.currentTimeMillis()}"
                val aiMessage = Message(
                    id = aiMessageId,
                    content = "",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + aiMessage
                
                // 如果没有当前会话，先创建一个新会话
                if (_currentMemoryId.value == null) {
                    createNewConversation()
                    // 等待会话创建完成
                    if (_currentMemoryId.value == null) {
                        _errorMessage.value = "创建会话失败，请重试"
                        _isLoading.value = false
                        // 移除临时消息
                        _messages.value = _messages.value.dropLast(2)
                        return@launch
                    }
                }
                
                // 调用后端API（流式）
                val request = ChatRequest(
                    memoryId = _currentMemoryId.value!!,
                    message = content,
                    userId = currentUserId
                )
                
                val response = apiService.sendMessageStream(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    val inputStream = responseBody.byteStream()
                    val reader = inputStream.bufferedReader()
                    
                    val fullReply = StringBuilder()
                    
                    try {
                        // 逐字符读取流式数据
                        val buffer = CharArray(256)  // 增大缓冲区，提高效率
                        var charsRead: Int
                        
                        while (reader.read(buffer).also { charsRead = it } != -1) {
                            val chunk = String(buffer, 0, charsRead)
                            fullReply.append(chunk)
                            
                            // 实时更新界面上的 AI 消息
                            _messages.value = _messages.value.map { msg ->
                                if (msg.id == aiMessageId) {
                                    msg.copy(content = fullReply.toString())
                                } else {
                                    msg
                                }
                            }
                        }
                        
                        // 如果读取完成但没有内容，显示错误
                        if (fullReply.isEmpty()) {
                            fullReply.append("抱歉，AI 服务暂时不可用，请稍后重试")
                            _messages.value = _messages.value.map { msg ->
                                if (msg.id == aiMessageId) {
                                    msg.copy(content = fullReply.toString())
                                } else {
                                    msg
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // 如果出现异常，在消息中显示错误提示
                        val errorMsg = when {
                            fullReply.isEmpty() -> "抱歉，AI 服务连接失败，请稍后重试"
                            else -> "${fullReply}\n\n[连接中断]"
                        }
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(content = errorMsg)
                            } else {
                                msg
                            }
                        }
                        _errorMessage.value = "AI服务连接异常: ${e.message}"
                    } finally {
                        reader.close()
                        inputStream.close()
                    }
                    
                    // 流式传输完成，刷新对话列表
                    loadConversations()
                } else {
                    _errorMessage.value = "发送失败: ${response.code()}"
                    // 移除临时的 AI 消息
                    _messages.value = _messages.value.filter { it.id != aiMessageId }
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
     * 调用 GET /api/conversation/list/{userId}
     */
    fun loadConversations() {
        viewModelScope.launch {
            try {
                val response = apiService.getConversations(currentUserId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _conversations.value = body.data
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 📍 API调用位置 #3: 加载对话详情和消息
     * 调用 GET /api/conversation/messages/{conversationId}
     */
    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // 获取消息列表
                val response = apiService.getConversationMessages(conversationId)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _currentMemoryId.value = conversationId
                        
                        // 转换消息格式
                        _messages.value = body.data.map { dto ->
                            Message(
                                id = dto.id.toString(),
                                content = dto.content,
                                isUser = dto.role == "user",
                                timestamp = System.currentTimeMillis() // 简化处理
                            )
                        }
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
     * 调用 POST /api/ai/chat/new
     */
    fun createNewConversation() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val request = CreateConversationRequest(
                    userId = currentUserId,
                    title = "新对话"
                )
                
                val response = apiService.createConversation(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _currentMemoryId.value = body.data.id
                        _messages.value = emptyList()
                        
                        // 刷新对话列表
                        loadConversations()
                        
                        // 关闭侧边栏
                        _showSidebar.value = false
                    } else {
                        _errorMessage.value = body.message ?: "创建对话失败"
                    }
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
     * 调用 DELETE /api/conversation/delete/{conversationId}
     */
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteConversation(conversationId)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        // 如果删除的是当前对话，清空状态
                        if (_currentMemoryId.value == conversationId) {
                            _currentMemoryId.value = null
                            _messages.value = emptyList()
                        }
                        
                        // 刷新对话列表
                        loadConversations()
                    }
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
        _currentMemoryId.value = null
        _messages.value = emptyList()
        _showSidebar.value = false
    }
    
    /**
     * 📍 API调用位置 #6: OCR 识别并创建提醒
     * 1. 使用 PaddleOCR 识别图片文字
     * 2. 调用 POST /api/ai/ocr/reminder
     * @param bitmap 要识别的图片
     */
    fun processOCRImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // 添加一个临时的提示消息
                val tempMessage = Message(
                    content = "🔍 正在识别图片内容...",
                    isUser = false
                )
                _messages.value = _messages.value + tempMessage
                
                // 在 IO 线程进行 OCR 识别
                val ocrText = withContext(Dispatchers.IO) {
                    ocrHelper.recognizeText(bitmap)
                }
                
                // 移除临时消息
                _messages.value = _messages.value.dropLast(1)
                
                if (ocrText.isNotBlank()) {
                    // 调用后端处理 OCR 文本
                    processOCRText(ocrText)
                } else {
                    _errorMessage.value = "OCR 识别失败,未能识别出文字"
                    val errorMsg = Message(
                        content = "❌ OCR 识别失败,图片中没有识别到文字内容",
                        isUser = false
                    )
                    _messages.value = _messages.value + errorMsg
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "OCR 识别出错: ${e.message}"
                android.util.Log.e("AiViewModel", "OCR recognition error", e)
                
                // 显示错误消息
                val errorMsg = Message(
                    content = "❌ OCR 识别异常: ${e.message}",
                    isUser = false
                )
                _messages.value = _messages.value.dropLast(1) + errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 处理 OCR 识别出的文本
     * 改为通过对话接口让 AI 自主调用工具创建日程
     * @param ocrText OCR 识别出的文本
     */
    private fun processOCRText(ocrText: String) {
        if (ocrText.isBlank()) {
            _errorMessage.value = "OCR 识别文本为空"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // 添加用户消息显示 OCR 结果
                val ocrMessage = Message(
                    content = "📷 图片识别内容:\n$ocrText",
                    isUser = true
                )
                _messages.value = _messages.value + ocrMessage
                
                // 如果没有当前会话，先创建一个新会话
                if (_currentMemoryId.value == null) {
                    createNewConversation()
                    // 等待会话创建完成
                    if (_currentMemoryId.value == null) {
                        _errorMessage.value = "创建会话失败，请重试"
                        _isLoading.value = false
                        return@launch
                    }
                }
                
                // 构造消息：让 AI 通过工具调用自动添加日程
                val messageToAI = "帮我添加日程：$ocrText"
                
                // 创建一个临时的 AI 消息用于显示流式内容
                val aiMessageId = "ai-msg-ocr-${System.currentTimeMillis()}"
                val aiMessage = Message(
                    id = aiMessageId,
                    content = "",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + aiMessage
                
                // 调用流式聊天接口,让 AI 自己使用工具
                val chatRequest = ChatRequest(
                    message = messageToAI,
                    memoryId = _currentMemoryId.value!!,
                    userId = currentUserId
                )
                
                val response = apiService.sendMessageStream(chatRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    val inputStream = responseBody.byteStream()
                    val reader = inputStream.bufferedReader()
                    
                    val fullReply = StringBuilder()
                    
                    try {
                        // 逐字符读取流式数据
                        val buffer = CharArray(256)
                        var charsRead: Int
                        
                        while (reader.read(buffer).also { charsRead = it } != -1) {
                            val chunk = String(buffer, 0, charsRead)
                            fullReply.append(chunk)
                            
                            // 实时更新界面上的 AI 消息
                            _messages.value = _messages.value.map { msg ->
                                if (msg.id == aiMessageId) {
                                    msg.copy(content = fullReply.toString())
                                } else {
                                    msg
                                }
                            }
                        }
                        
                        // 如果读取完成但没有内容，显示错误
                        if (fullReply.isEmpty()) {
                            fullReply.append("抱歉，AI 服务暂时不可用，请稍后重试")
                            _messages.value = _messages.value.map { msg ->
                                if (msg.id == aiMessageId) {
                                    msg.copy(content = fullReply.toString())
                                } else {
                                    msg
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // 如果出现异常，在消息中显示错误提示
                        val errorMsg = when {
                            fullReply.isEmpty() -> "抱歉，AI 服务连接失败，请稍后重试"
                            else -> "${fullReply}\n\n[连接中断]"
                        }
                        _messages.value = _messages.value.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(content = errorMsg)
                            } else {
                                msg
                            }
                        }
                        _errorMessage.value = "AI服务连接异常: ${e.message}"
                    } finally {
                        reader.close()
                        inputStream.close()
                    }
                    
                    // 流式传输完成，刷新对话列表
                    loadConversations()
                } else {
                    _errorMessage.value = "AI 请求失败: ${response.code()}"
                    // 移除临时的 AI 消息
                    _messages.value = _messages.value.filter { it.id != aiMessageId }
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "AI 处理出错: ${e.message}"
                android.util.Log.e("AiViewModel", "AI chat error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
