package com.lanqi.qiwei

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** 收藏页：先占个位，第 13 期接上数据库。 */
@Composable
fun FavoritesScreen() = ComingSoon(title = "收藏", text = "收藏的菜会出现在这里（第 13 期）")

/** 我的：先占个位，第 15 期写「我的菜谱」。 */
@Composable
fun MineScreen() = ComingSoon(title = "我的", text = "我的菜谱（第 15 期）")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComingSoon(title: String, text: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
