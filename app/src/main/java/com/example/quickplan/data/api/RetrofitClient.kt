package com.example.quickplan.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端单例
 * 
 * ⚠️⚠️⚠️ 重要：修改后端地址的位置 ⚠️⚠️⚠️
 * 
 * 当你的后端服务部署完成后，请修改下面的 BASE_URL
 * 
 * 本地开发示例：
 * - Android模拟器访问本机: "http://10.0.2.2:8080/"
 * - Android真机访问本机: "http://你的电脑IP:8080/"  (如 "http://192.168.1.100:8080/")
 * 
 * 生产环境示例：
 * - "https://your-domain.com/"
 */
object RetrofitClient {
    
    // 📍📍📍 修改后端地址的位置 - 这是唯一需要修改的地方 📍📍📍
    private const val BASE_URL = "http://10.0.2.2:8080/"  // Android模拟器访问localhost
    
    // 如果使用真机调试，改为：
    // private const val BASE_URL = "http://192.168.1.xxx:8080/"  // 替换为你的电脑IP
    
    /**
     * OkHttp 客户端（带日志拦截器）
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 开发时使用BODY，生产环境改为NONE
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Retrofit 实例
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * AI API 服务实例
     */
    val aiApiService: AiApiService by lazy {
        retrofit.create(AiApiService::class.java)
    }
}
