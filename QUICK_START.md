# 🚀 AI 对话功能快速开始指南

## 📱 前端已完成

### ✅ 实现的功能
1. **对话界面**：消息气泡、输入框、滚动列表
2. **对话管理**：新建、切换、删除对话
3. **历史记录**：侧边栏显示所有对话
4. **网络层**：Retrofit 配置完成，等待后端连接
5. **状态管理**：ViewModel + StateFlow 响应式架构

### 📍 修改后端地址

**文件**: `app/src/main/java/com/example/quickplan/data/api/RetrofitClient.kt`

**第22行**:
```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"  // 👈 修改这里
```

**配置示例**:
- 模拟器: `http://10.0.2.2:8080/`
- 真机: `http://你的电脑IP:8080/`（如 `http://192.168.1.100:8080/`）
- 生产: `https://your-domain.com/`

---

## 🔧 后端需要实现的5个接口

### 1️⃣ 发送消息
```
POST /api/ai/chat
Body: { "conversationId": "uuid或null", "message": "用户消息" }
Response: { "conversationId": "uuid", "messageId": "uuid", "reply": "AI回复", "timestamp": 1234567890 }
```

### 2️⃣ 获取对话列表
```
GET /api/ai/conversations
Response: { "conversations": [{ "id": "uuid", "title": "标题", "lastMessage": "...", "messageCount": 5, ... }] }
```

### 3️⃣ 获取对话详情
```
GET /api/ai/conversations/{conversationId}
Response: { "id": "uuid", "title": "标题", "messages": [{ "id": "uuid", "content": "...", "role": "user/assistant", ... }], ... }
```

### 4️⃣ 创建新对话
```
POST /api/ai/conversations
Body: { "title": "新对话" }
Response: { "id": "uuid", "title": "新对话", "createdAt": 1234567890 }
```

### 5️⃣ 删除对话
```
DELETE /api/ai/conversations/{conversationId}
Response: { "success": true, "message": "对话已删除" }
```

**📄 完整API文档**: 查看 `API_DOCUMENTATION.md`

---

## 🏗️ 后端推荐架构

### Spring Boot 示例

**1. 添加依赖** (`pom.xml` 或 `build.gradle`):
```xml
<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- LangChain4j OpenAI -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>1.7.1</version>
</dependency>
```

**2. Controller 示例**:
```java
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") // 开发时允许跨域
public class AiController {
    
    @Autowired
    private AiService aiService;
    
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return aiService.chat(request);
    }
    
    @GetMapping("/conversations")
    public ConversationsResponse getConversations() {
        return aiService.getConversations();
    }
    
    // ... 其他接口
}
```

**3. Service 示例**（集成 LangChain4j）:
```java
@Service
public class AiService {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    private ChatLanguageModel model;
    
    @PostConstruct
    public void init() {
        model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .build();
    }
    
    public ChatResponse chat(ChatRequest request) {
        // 1. 获取或创建对话
        Conversation conv = getOrCreateConversation(request.getConversationId());
        
        // 2. 保存用户消息
        saveMessage(conv.getId(), request.getMessage(), "user");
        
        // 3. 调用 AI
        String aiReply = model.generate(request.getMessage());
        
        // 4. 保存 AI 回复
        Message aiMessage = saveMessage(conv.getId(), aiReply, "assistant");
        
        // 5. 返回响应
        return new ChatResponse(
            conv.getId(),
            aiMessage.getId(),
            aiReply,
            System.currentTimeMillis()
        );
    }
}
```

---

## 🗄️ 数据库表结构

```sql
-- 对话表
CREATE TABLE conversations (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

-- 消息表
CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'user' or 'assistant'
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

-- 索引
CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_conversations_updated ON conversations(updated_at DESC);
```

---

## 🧪 测试后端

### 使用 cURL 测试

**1. 发送消息**:
```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId": null, "message": "你好"}'
```

**2. 获取对话列表**:
```bash
curl http://localhost:8080/api/ai/conversations
```

### 使用 Postman
导入以下环境变量：
```
BASE_URL = http://localhost:8080
```

创建5个请求并测试每个接口。

---

## ⚙️ 环境配置

### application.yml (Spring Boot)
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/quickplan
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

# OpenAI API Key (从环境变量读取更安全)
openai:
  api:
    key: ${OPENAI_API_KEY}
```

### 启动后端
```bash
mvn spring-boot:run
# 或
gradle bootRun
```

---

## 🔗 连接前端和后端

### 步骤 1: 确保后端运行
访问 `http://localhost:8080/api/ai/conversations`，应该返回 JSON

### 步骤 2: 修改前端配置
打开 `RetrofitClient.kt`，修改 `BASE_URL`

### 步骤 3: 运行 Android App
在 Android Studio 中点击 Run，打开 AI 界面，尝试发送消息

### 步骤 4: 查看日志
- **Android**: Logcat 过滤 `OkHttp`
- **后端**: 控制台查看请求日志

---

## 🐛 常见问题

### ❌ 模拟器无法连接 localhost:8080
**解决**: 使用 `10.0.2.2` 代替 `localhost`
```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

### ❌ 真机无法连接电脑后端
**解决**:
1. 确保手机和电脑在同一 WiFi
2. 查看电脑 IP (Windows: `ipconfig`, Mac/Linux: `ifconfig`)
3. 使用电脑 IP: `http://192.168.1.xxx:8080/`
4. 关闭电脑防火墙或允许 8080 端口

### ❌ CORS 错误
**解决**: 后端添加 CORS 配置
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*");
            }
        };
    }
}
```

---

## 📚 参考资料

1. **API 文档**: `API_DOCUMENTATION.md`
2. **调用位置说明**: `API_CALL_LOCATIONS.md`
3. **功能总结**: `README_AI_FEATURE.md`
4. **LangChain4j 官方文档**: https://docs.langchain4j.dev/
5. **Spring Boot 官方指南**: https://spring.io/guides

---

## ✅ 检查清单

前端：
- [x] 界面实现完成
- [x] 网络层配置完成
- [x] ViewModel 状态管理完成
- [x] 网络权限已添加

后端（待完成）：
- [ ] 5个接口实现
- [ ] 数据库配置
- [ ] LangChain4j 集成
- [ ] 测试接口正常工作

---

**祝开发顺利！有任何问题随时查看文档或联系。** 🚀
