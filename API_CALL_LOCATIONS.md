# 后端 API 调用位置总结

## 📍 需要修改后端地址的位置

### **唯一需要修改的文件**：`RetrofitClient.kt`

**文件路径**:
```
app/src/main/java/com/example/quickplan/data/api/RetrofitClient.kt
```

**修改位置**（第22行左右）:
```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"  // 👈 这里修改
```

**不同环境的配置示例**:

1. **Android 模拟器访问本机后端**:
   ```kotlin
   private const val BASE_URL = "http://10.0.2.2:8080/"
   ```

2. **Android 真机访问局域网后端**:
   ```kotlin
   private const val BASE_URL = "http://192.168.1.100:8080/"  // 替换为你的电脑 IP
   ```

3. **生产环境**:
   ```kotlin
   private const val BASE_URL = "https://your-domain.com/"
   ```

---

## 📡 所有后端 API 调用位置

### 1. API 接口定义：`AiApiService.kt`

**文件路径**:
```
app/src/main/java/com/example/quickplan/data/api/AiApiService.kt
```

**包含的接口**:
- `POST /api/ai/chat` - 发送消息
- `GET /api/ai/conversations` - 获取对话列表
- `GET /api/ai/conversations/{id}` - 获取对话详情
- `POST /api/ai/conversations` - 创建新对话
- `DELETE /api/ai/conversations/{id}` - 删除对话

---

### 2. 实际调用位置：`AiViewModel.kt`

**文件路径**:
```
app/src/main/java/com/example/quickplan/viewmodel/AiViewModel.kt
```

**所有 API 调用方法**:

#### 📍 调用 #1: 发送消息
- **方法**: `sendMessage(content: String)`
- **行数**: 约第 65-105 行
- **API**: `POST /api/ai/chat`
- **触发时机**: 用户点击发送按钮

```kotlin
fun sendMessage(content: String) {
    // ...
    val response = apiService.sendMessage(request)  // 👈 调用位置
    // ...
}
```

#### 📍 调用 #2: 加载对话列表
- **方法**: `loadConversations()`
- **行数**: 约第 107-120 行
- **API**: `GET /api/ai/conversations`
- **触发时机**: 
  - App 启动时（init 块）
  - 发送消息后
  - 创建新对话后
  - 删除对话后

```kotlin
fun loadConversations() {
    // ...
    val response = apiService.getConversations()  // 👈 调用位置
    // ...
}
```

#### 📍 调用 #3: 加载对话详情
- **方法**: `loadConversation(conversationId: String)`
- **行数**: 约第 122-150 行
- **API**: `GET /api/ai/conversations/{conversationId}`
- **触发时机**: 用户从侧边栏点击某个对话

```kotlin
fun loadConversation(conversationId: String) {
    // ...
    val response = apiService.getConversationDetail(conversationId)  // 👈 调用位置
    // ...
}
```

#### 📍 调用 #4: 创建新对话
- **方法**: `createNewConversation()`
- **行数**: 约第 152-180 行
- **API**: `POST /api/ai/conversations`
- **触发时机**: 用户点击侧边栏的"新建对话"按钮

```kotlin
fun createNewConversation() {
    // ...
    val response = apiService.createConversation(request)  // 👈 调用位置
    // ...
}
```

#### 📍 调用 #5: 删除对话
- **方法**: `deleteConversation(conversationId: String)`
- **行数**: 约第 182-205 行
- **API**: `DELETE /api/ai/conversations/{conversationId}`
- **触发时机**: 用户确认删除某个对话

```kotlin
fun deleteConversation(conversationId: String) {
    // ...
    val response = apiService.deleteConversation(conversationId)  // 👈 调用位置
    // ...
}
```

---

## 🔍 如何快速定位调用位置

### 方法 1: 使用搜索
在 Android Studio 中按 `Ctrl+Shift+F`（Windows）或 `Cmd+Shift+F`（Mac），搜索：
- `apiService.sendMessage`
- `apiService.getConversations`
- `apiService.getConversationDetail`
- `apiService.createConversation`
- `apiService.deleteConversation`

### 方法 2: 查看注释
所有调用位置都有明确的注释标记：
```kotlin
/**
 * 📍 API调用位置 #1: 发送消息
 * 调用 POST /api/ai/chat
 */
```

---

## 🛠️ 修改建议

### 如果需要修改 API 路径

**示例**: 将 `/api/ai/chat` 改为 `/api/v1/chat`

**修改文件**: `AiApiService.kt`
```kotlin
// 修改前
@POST("api/ai/chat")
suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>

// 修改后
@POST("api/v1/chat")
suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>
```

### 如果需要添加请求头（如 Token）

**修改文件**: `RetrofitClient.kt`

在 `okHttpClient` 中添加拦截器：
```kotlin
private val okHttpClient: OkHttpClient by lazy {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // 添加 Token 拦截器
    val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer YOUR_TOKEN")
            .build()
        chain.proceed(request)
    }
    
    OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)  // 👈 添加这里
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

---

## 📊 文件结构总览

```
app/src/main/java/com/example/quickplan/
├── data/
│   ├── api/
│   │   ├── ApiModels.kt          # API 请求/响应数据类
│   │   ├── AiApiService.kt       # API 接口定义（5个端点）
│   │   └── RetrofitClient.kt     # 🔴 修改 BASE_URL 的位置
│   └── model/
│       ├── Message.kt            # 消息数据模型
│       └── Conversation.kt       # 对话数据模型
├── viewmodel/
│   └── AiViewModel.kt            # 🔴 所有 API 实际调用的位置
└── ui/
    └── screens/
        └── AIScreen.kt           # AI 对话界面 UI
```

---

## 🚨 重要提醒

1. **只需修改一个文件的 BASE_URL**: `RetrofitClient.kt`
2. **所有 API 调用都在**: `AiViewModel.kt`
3. **不需要修改 UI 代码**: UI 层已经通过 ViewModel 隔离
4. **后端地址变更后**: 只需修改 `BASE_URL` 常量即可

---

## 📞 技术支持

如果遇到网络请求问题，检查：
1. ✅ 后端服务是否正常运行
2. ✅ BASE_URL 是否正确配置
3. ✅ 网络权限是否已添加到 AndroidManifest.xml
4. ✅ 查看 Logcat 中的网络请求日志（搜索 "OkHttp"）

**日志过滤器**: `OkHttp` 或 `Retrofit`
