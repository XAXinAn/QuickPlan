package com.example.quickplan.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quickplan.data.HistoryItem
import com.example.quickplan.data.HistoryRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    // seed demo data
    LaunchedEffect(Unit) { HistoryRepository.seedDemo() }

    var selecting by remember { mutableStateOf(false) }
    val items = HistoryRepository.items
    val selectedIds = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史添加") },
                actions = {
                    if (selecting) {
                        IconButton(onClick = {
                            // 删除已选
                            HistoryRepository.removeMany(selectedIds.toSet())
                            selectedIds.clear()
                            selecting = false
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除所选")
                        }
                    } else {
                        IconButton(onClick = { selecting = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("没有历史添加的日程")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                    items(items, key = { it.id }) { item: HistoryItem ->
                        HistoryRow(
                            item = item,
                            selecting = selecting,
                            selected = selectedIds.contains(item.id),
                            onSelectToggle = { checked ->
                                if (checked) selectedIds.add(item.id) else selectedIds.remove(item.id)
                            },
                            onDelete = { HistoryRepository.remove(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: HistoryItem,
    selecting: Boolean,
    selected: Boolean,
    onSelectToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (selecting) {
                Checkbox(checked = selected, onCheckedChange = onSelectToggle)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.bodyLarge)
                if (!item.details.isNullOrEmpty()) Text(text = item.details!!, style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}
