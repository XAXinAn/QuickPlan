package com.example.quickplan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.example.quickplan.model.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

// 自定义序列化器
object ScheduleListSerializer : Serializer<List<Schedule>> {
    override val defaultValue: List<Schedule> = emptyList()

    override suspend fun readFrom(input: InputStream): List<Schedule> {
        return try {
            val text = input.readBytes().decodeToString()
            if (text.isBlank()) emptyList()
            else Json.decodeFromString(ListSerializer(Schedule.serializer()), text)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun writeTo(t: List<Schedule>, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(Json.encodeToString(ListSerializer(Schedule.serializer()), t).encodeToByteArray())
        }
    }
}

// 创建 DataStore
val Context.scheduleDataStore: DataStore<List<Schedule>> by dataStore(
    fileName = "schedules.json",
    serializer = ScheduleListSerializer
)

// 仓库类
class ScheduleRepository(private val context: Context) {
    val schedules: Flow<List<Schedule>> = context.scheduleDataStore.data

    // 添加日程
    suspend fun addSchedule(schedule: Schedule) {
        context.scheduleDataStore.updateData { old -> old + schedule }
    }

    // 删除日程
    suspend fun deleteSchedule(id: Long) {
        context.scheduleDataStore.updateData { old -> old.filterNot { it.id == id } }
    }

    // 更新日程
    suspend fun updateSchedule(updated: Schedule) {
        context.scheduleDataStore.updateData { old ->
            old.map { if (it.id == updated.id) updated else it }
        }
    }

    // 清空全部日程（可选）
    suspend fun clearAll() {
        context.scheduleDataStore.updateData { emptyList() }
    }
}
