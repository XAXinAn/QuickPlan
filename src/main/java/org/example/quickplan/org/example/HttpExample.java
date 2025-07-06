package org.example.quickplan.org.example;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@RequestMapping("/send")
@RestController
public class HttpExample {

    @GetMapping("/1")
    public String send(String base64String) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

//        // 转换为Base64字符串
//        String base64String = convertImageToBase64UsingSpring("test.jpg");

//        if (base64String == null) {
//            return "图片转换失败";
//        }

        String cleanPrompt = "身份\n\t作为一个专业的通知内容识别专家，你能够精确分析手机截屏图片，从中识别各类通知、提醒、公告等信息，并准确提取关键的时间和事件信息。你具备强大的图像理解能力和时间解析能力，能够处理各种格式的日期时间表达，为用户提供准确可靠的日程管理服务。\n\t能力：\n\t\n\t图像内容识别：准确识别截屏中的文字内容，包括各类App通知、短信、邮件、公告等\n\t时间信息提取：从文本中精确提取截止日期、提醒时间、活动时间等时间信息，支持多种时间格式\n\t事件内容总结：将复杂的通知内容概括为简洁清晰的事件描述\n\t数据结构化输出：将识别结果按照标准JSON格式输出，便于系统处理\n\n\t细节：\n\t处理流程：\n\n\t仔细分析提供的截屏图片\n\t识别图片中所有可见的文本内容\n\t判断是否包含时间相关的通知或提醒信息\n\t提取关键信息：截止日期、事件标题、重要程度\n\t按照指定格式返回结构化数据\n\n\t输出格式要求：\n\tjson{\n  \"hasNotification\": true/false,\n  \"events\": [\n    {\n      \"title\": \"事件标题\",\n      \"deadline\": \"YYYY-MM-DD HH:mm\",\n      \"description\": \"详细描述\",\n      \"priority\": \"高/中/低\",\n      \"source\": \"来源应用名称\"\n    }\n  ],\n  \"confidence\": 0.95\n}\n\n\t特殊处理规则：\n\n\t如果图片中没有明确的时间信息，deadline字段设为null\n\t优先识别具有明确截止时间的任务和提醒\n\t对于模糊的时间表达（如\"明天\"、\"下周\"），需要根据当前时间推算具体日期\n\t如果识别不确定，诚实说明并降低confidence值\n\t忽略纯广告性质的推送通知\n\n\t注意事项：\n\n\t准确性优先：宁可不识别，也不要给出错误信息\n\t时间格式统一：统一使用24小时制，格式为YYYY-MM-DD HH:mm\n\t内容简洁：事件标题控制在20字以内，描述控制在50字以内\n\t隐私保护：不记录或存储用户的个人敏感信息";
        String escapedPrompt = cleanPrompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");

        String jsonBody = String.format(
                "{\"appId\":\"%s\",\"prompt\":\"%s\",\"history\":[],\"stream\":false,"
                        + "\"imgName\":\"%s\",\"imgBase64\":\"%s\"}",
                "685d4866b381ac407d0bb961",
                "今天是"+ LocalDateTime.now().toString(),
                "test.jpg",
                base64String
        );




        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://jiutian.10086.cn/largemodel/api/v2/completions"))
                .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcGlfa2V5IjoiNjg1Y2I4ZjBiMzgxYWM0MDdkMDU4NmVmIiwiZXhwIjoxNzUxNzgxMDg2LCJ0aW1lc3RhbXAiOjE3NTE3MjEwODZ9.f_WVe6q-qiOMsJXBS006k31csP1zHU9J-b0df9lDn1U")  // 修正：Authentication -> Authorization，并添加Bearer前缀
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest,
                HttpResponse.BodyHandlers.ofString());

        System.out.println("状态码: " + postResponse.statusCode());
        String json=postResponse.body();
        String rawText = extractTextWithGson(json);
        String result=cleanJsonText(rawText);
        System.out.println("响应: " + result);
        return result;
    }

    private String convertImageToBase64UsingSpring(String imageName) {
        try {
            // 从 classpath (resources目录) 加载文件
            ClassPathResource resource = new ClassPathResource(imageName);

            if (!resource.exists()) {
                System.err.println("资源文件不存在: " + imageName);
                return null;
            }

            // 读取文件内容
            InputStream inputStream = resource.getInputStream();
            byte[] imageBytes = inputStream.readAllBytes();
            inputStream.close();

            System.out.println("成功加载图片: " + imageName + ", 大小: " + imageBytes.length + " bytes");

            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (IOException e) {
            System.err.println("读取图片失败: " + e.getMessage());
            return null;
        }
    }

    public static String extractTextWithGson(String jsonString) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            // 获取choices数组中第一个元素的text字段
            if (jsonObject.has("choices")) {
                JsonObject firstChoice = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject();
                if (firstChoice.has("text")) {
                    return firstChoice.get("text").getAsString();
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String cleanJsonText(String rawText) {
        if (rawText == null) return null;

        // 移除markdown代码块标记
        String cleaned = rawText.replaceAll("```json\\n?", "")
                .replaceAll("\\n?```", "")
                .trim();

        return cleaned;
    }
}