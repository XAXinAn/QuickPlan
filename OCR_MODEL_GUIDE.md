# OCR 模型文件获取指南

## 问题说明

当前 `app/src/main/assets/` 目录下的 ONNX 模型文件大小为 0 字节：
- ❌ `det_model.onnx` - 0 字节
- ❌ `rec_model.onnx` - 0 字节
- ✅ `ppocr_keys_v1.txt` - 26,249 字节

这会导致应用在进入 AI 页面时崩溃。

## 已实施的临时修复

1. **延迟加载机制**：模型只在真正使用时才加载，避免页面初始化崩溃
2. **错误处理**：添加了完整的异常捕获和用户提示
3. **禁用功能**：当前 AI 页面会显示警告信息，提示用户模型文件缺失

## 如何获取正确的 ONNX 模型

### 方案 1：从 PaddleOCR 官方获取

1. 访问 PaddleOCR GitHub：
   ```
   https://github.com/PaddlePaddle/PaddleOCR
   ```

2. 下载预训练模型：
   - 中文检测模型：`ch_PP-OCRv4_det_infer.tar`
   - 中文识别模型：`ch_PP-OCRv4_rec_infer.tar`

3. 使用 `paddle2onnx` 转换为 ONNX 格式：
   ```bash
   pip install paddle2onnx
   paddle2onnx --model_dir ./ch_PP-OCRv4_det_infer \
               --model_filename inference.pdmodel \
               --params_filename inference.pdiparams \
               --save_file det_model.onnx \
               --opset_version 11 \
               --enable_onnx_checker True
   ```

### 方案 2：从 ONNX Model Zoo 获取

访问 ONNX 模型库寻找现成的 OCR 模型：
```
https://github.com/onnx/models
```

### 方案 3：使用轻量级替代方案

如果模型文件过大，可以考虑：
- 使用 ML Kit 的文字识别 API（Google 提供）
- 使用 Tesseract OCR（开源库）
- 使用云端 OCR 服务（百度 OCR、腾讯 OCR 等）

## 替换模型文件

1. 将下载/转换好的模型文件放入：
   ```
   app/src/main/assets/
   ├── det_model.onnx    (检测模型，约 3-10 MB)
   ├── rec_model.onnx    (识别模型，约 5-15 MB)
   └── ppocr_keys_v1.txt (字符字典，已存在)
   ```

2. 修改 `AIScreen.kt`，启用 OCR 功能：
   ```kotlin
   // 恢复 OcrProcessor 初始化
   val ocrProcessor = remember {
       try {
           OcrProcessor(context)
       } catch (e: Exception) {
           null
       }
   }
   ```

3. 重新构建项目并运行

## 注意事项

- ⚠️ ONNX 模型文件通常较大（5-50 MB），会增加 APK 体积
- ⚠️ 模型推理需要较多计算资源，可能影响性能
- ⚠️ 确保模型输入输出格式与代码中的预处理逻辑匹配

## 当前状态

✅ **应用已修复，不会再崩溃**
- AI 页面可以正常打开
- 显示警告信息提示用户模型缺失
- 按钮处于禁用状态

⏳ **待完成**
- 添加有效的 ONNX 模型文件
- 测试 OCR 识别功能
- 优化识别结果解析逻辑
