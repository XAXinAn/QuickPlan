package com.example.quickplan.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.io.InputStream

@Composable
fun AIScreen() {
    val context = LocalContext.current
    var recognizedText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    // 相册选择器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val image = InputImage.fromBitmap(bitmap, 0)

                // 创建中文识别器
                val recognizer = TextRecognition.getClient(
                    ChineseTextRecognizerOptions.Builder().build()
                )
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        recognizedText = visionText.text
                        showDialog = true
                    }
                    .addOnFailureListener { e ->
                        recognizedText = "识别失败: ${e.message}"
                        showDialog = true
                    }
            } catch (e: Exception) {
                recognizedText = "读取图片失败: ${e.message}"
                showDialog = true
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "AI",
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = { launcher.launch("image/*") }) {
                    Text(text = "选择图片识别文字")
                }
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "识别结果：")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = recognizedText)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showDialog = false }) {
                        Text(text = "关闭")
                    }
                }
            }
        }
    }
}
