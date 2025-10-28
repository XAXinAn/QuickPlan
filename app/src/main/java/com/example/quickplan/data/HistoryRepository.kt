package com.example.quickplan.data

import androidx.compose.runtime.mutableStateListOf
import java.util.*

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val details: String? = null
)

object HistoryRepository {
    // Compose 可观察的列表
    private val _items = mutableStateListOf<HistoryItem>()
    val items: List<HistoryItem> get() = _items

    fun add(item: HistoryItem) {
        _items.add(0, item)
    }

    fun remove(itemId: String) {
        val idx = _items.indexOfFirst { it.id == itemId }
        if (idx >= 0) _items.removeAt(idx)
    }

    fun removeMany(ids: Set<String>) {
        _items.removeAll { it.id in ids }
    }

    fun clear() {
        _items.clear()
    }

    // helper for demo data
    fun seedDemo() {
        if (_items.isNotEmpty()) return

    }
}
