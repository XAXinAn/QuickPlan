# QuickPlan AI 对话后端 API 文档

## 📌 API 基础信息

- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`
- **字符编码**: UTF-8

---

## 🔍 前端调用位置说明

所有后端 API 调用都集中在以下文件中：

### 1. **RetrofitClient.kt** - 修改后端地址的唯一位置
```kotlin
文件路径: app/src/main/java/com/example/quickplan/data/api/RetrofitClient.kt
修改位置: private const val BASE_URL = "http://10.0.2.2:8080/"
```

### 2. **AiApiService.kt** - API 接口定义
```kotlin
文件路径: app/src/main/java/com/example/quickplan/data/api/AiApiService.kt
包含所有 API 端点的 Retrofit 接口定义
```

### 3. **AiViewModel.kt** - 业务逻辑层（调用 API）
```kotlin
文件路径: app/src/main/java/com/example/quickplan/viewmodel/AiViewModel.kt
所有实际的 API 调用都在这个 ViewModel 中执行
```

**📍 API 调用位置标记：**
- `#1` 发送消息: `AiViewModel.sendMessage()`
- `#2` 加载对话列表: `AiViewModel.loadConversations()`
- `#3` 加载对话详情: `AiViewModel.loadConversation()`
- `#4` 创建新对话: `AiViewModel.createNewConversation()`
- `#5` 删除对话: `AiViewModel.deleteConversation()`

---

## 📡 API 接口详情

### 1. 发送聊天消息

**端点**: `POST /api/ai/chat`

**描述**: 发送用户消息并获取 AI 回复

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "conversationId": "uuid-string-or-null",
  "message": "用户输入的消息内容"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | String \| null | 否 | 对话ID。新对话时传 `null`，继续对话时传现有对话ID |
| message | String | 是 | 用户输入的消息内容，不能为空 |

**请求示例**:
```json
// 新对话
{
  "conversationId": null,
  "message": "你好，请介绍一下自己"
}

// 继续对话
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "那你能做什么呢?"
}
```

**成功响应**: `200 OK`
```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "messageId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "reply": "你好！我是 AI 助手，很高兴为你服务...",
  "timestamp": 1698765432000
}
```

**响应字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| conversationId | String | 对话ID（新对话时会生成新的UUID） |
| messageId | String | AI 消息的唯一ID |
| reply | String | AI 的回复内容 |
| timestamp | Long | 消息时间戳（毫秒） |

**错误响应**: `400 Bad Request`
```json
{
  "error": "BadRequest",
  "message": "消息内容不能为空",
  "timestamp": 1698765432000
}
```

**错误响应**: `500 Internal Server Error`
```json
{
  "error": "InternalServerError",
  "message": "AI 服务调用失败",
  "timestamp": 1698765432000
}
```

---

### 2. 获取对话列表

**端点**: `GET /api/ai/conversations`

**描述**: 获取用户的所有对话列表（按更新时间倒序）

**请求头**:
```
Content-Type: application/json
```

**请求参数**: 无

**成功响应**: `200 OK`
```json
{
  "conversations": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "关于 AI 的讨论",
      "lastMessage": "那你能做什么呢?",
      "messageCount": 5,
      "createdAt": 1698765000000,
      "updatedAt": 1698765432000
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "title": "新对话",
      "lastMessage": null,
      "messageCount": 0,
      "createdAt": 1698764000000,
      "updatedAt": 1698764000000
    }
  ]
}
```

**响应字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| conversations | Array | 对话摘要列表 |
| conversations[].id | String | 对话唯一ID |
| conversations[].title | String | 对话标题 |
| conversations[].lastMessage | String \| null | 最后一条消息预览（可能为空） |
| conversations[].messageCount | Integer | 该对话中的消息数量 |
| conversations[].createdAt | Long | 创建时间戳（毫秒） |
| conversations[].updatedAt | Long | 最后更新时间戳（毫秒） |

**错误响应**: `500 Internal Server Error`
```json
{
  "error": "InternalServerError",
  "message": "数据库查询失败",
  "timestamp": 1698765432000
}
```

---

### 3. 获取对话详情

**端点**: `GET /api/ai/conversations/{conversationId}`

**描述**: 获取指定对话的完整消息历史

**请求头**:
```
Content-Type: application/json
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | String | 是 | 对话的唯一ID |

**请求示例**:
```
GET /api/ai/conversations/550e8400-e29b-41d4-a716-446655440000
```

**成功响应**: `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "关于 AI 的讨论",
  "messages": [
    {
      "id": "msg-001",
      "content": "你好，请介绍一下自己",
      "role": "user",
      "timestamp": 1698765000000
    },
    {
      "id": "msg-002",
      "content": "你好！我是 AI 助手...",
      "role": "assistant",
      "timestamp": 1698765001000
    },
    {
      "id": "msg-003",
      "content": "那你能做什么呢?",
      "role": "user",
      "timestamp": 1698765432000
    }
  ],
  "createdAt": 1698765000000,
  "updatedAt": 1698765432000
}
```

**响应字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 对话唯一ID |
| title | String | 对话标题 |
| messages | Array | 消息列表（按时间升序） |
| messages[].id | String | 消息唯一ID |
| messages[].content | String | 消息内容 |
| messages[].role | String | 角色：`"user"` 或 `"assistant"` |
| messages[].timestamp | Long | 消息时间戳（毫秒） |
| createdAt | Long | 对话创建时间戳 |
| updatedAt | Long | 对话最后更新时间戳 |

**错误响应**: `404 Not Found`
```json
{
  "error": "NotFound",
  "message": "对话不存在",
  "timestamp": 1698765432000
}
```

---

### 4. 创建新对话

**端点**: `POST /api/ai/conversations`

**描述**: 创建一个新的空对话

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "title": "新对话"
}
```

**字段说明**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 对话标题，默认可以是 "新对话" |

**请求示例**:
```json
{
  "title": "周末计划讨论"
}
```

**成功响应**: `201 Created`
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "title": "周末计划讨论",
  "createdAt": 1698765999000
}
```

**响应字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 新创建的对话ID |
| title | String | 对话标题 |
| createdAt | Long | 创建时间戳（毫秒） |

**错误响应**: `400 Bad Request`
```json
{
  "error": "BadRequest",
  "message": "标题不能为空",
  "timestamp": 1698765432000
}
```

---

### 5. 删除对话

**端点**: `DELETE /api/ai/conversations/{conversationId}`

**描述**: 删除指定对话及其所有消息

**请求头**:
```
Content-Type: application/json
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | String | 是 | 要删除的对话ID |

**请求示例**:
```
DELETE /api/ai/conversations/550e8400-e29b-41d4-a716-446655440000
```

**成功响应**: `200 OK`
```json
{
  "success": true,
  "message": "对话已删除"
}
```

**响应字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| success | Boolean | 操作是否成功 |
| message | String | 操作结果描述 |

**错误响应**: `404 Not Found`
```json
{
  "error": "NotFound",
  "message": "对话不存在",
  "timestamp": 1698765432000
}
```

---

## 🔧 后端实现建议

### 技术栈推荐
- **框架**: Spring Boot / Quarkus / Micronaut
- **数据库**: PostgreSQL / MySQL（存储对话和消息）
- **AI 集成**: LangChain4j（调用 OpenAI/其他大模型）

### 数据库表结构建议

**conversations 表**:
```sql
CREATE TABLE conversations (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);
```

**messages 表**:
```sql
CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'user' or 'assistant'
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);
```

### Spring Boot Controller 示例

```java
@RestController
@RequestMapping("/api/ai")
public class AiController {
    
    @Autowired
    private AiService aiService;
    
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(aiService.chat(request));
    }
    
    @GetMapping("/conversations")
    public ResponseEntity<ConversationsResponse> getConversations() {
        return ResponseEntity.ok(aiService.getConversations());
    }
    
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationDetailResponse> getConversation(
        @PathVariable String conversationId
    ) {
        return ResponseEntity.ok(aiService.getConversationDetail(conversationId));
    }
    
    @PostMapping("/conversations")
    public ResponseEntity<CreateConversationResponse> createConversation(
        @RequestBody CreateConversationRequest request
    ) {
        return ResponseEntity.status(201).body(aiService.createConversation(request));
    }
    
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<DeleteConversationResponse> deleteConversation(
        @PathVariable String conversationId
    ) {
        return ResponseEntity.ok(aiService.deleteConversation(conversationId));
    }
}
```

---

## 🚀 测试建议

### 使用 Postman/cURL 测试

**1. 发送消息**:
```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": null,
    "message": "你好"
  }'
```

**2. 获取对话列表**:
```bash
curl -X GET http://localhost:8080/api/ai/conversations
```

**3. 创建新对话**:
```bash
curl -X POST http://localhost:8080/api/ai/conversations \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试对话"
  }'
```

---

## 📝 注意事项

1. **CORS 配置**: 如果前端和后端不在同一域名，需要配置 CORS
2. **错误处理**: 所有 API 应返回统一的错误格式
3. **日志记录**: 记录所有 API 调用和错误，便于调试
4. **性能优化**: 
   - 对话列表添加分页
   - 消息历史可以分批加载
   - 考虑使用 Redis 缓存热门对话
5. **安全性**:
   - 添加用户认证（JWT Token）
   - 验证对话所有权
   - 限制请求频率（Rate Limiting）

---

## 📞 联系与反馈

如有 API 相关问题或建议，请随时沟通调整接口设计。

**生成时间**: 2025-10-23  
**文档版本**: v1.0
