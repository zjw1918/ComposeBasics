package com.lanqi.qiwei

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lanqi.qiwei.ui.theme.QiweiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QiweiTheme {
                HomeScreen()
            }
        }
    }
}

/** 首页：顶栏 + 几张菜谱卡片。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("鳍味") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            RecipeCard(title = "番茄炒蛋", subtitle = "十分钟上桌的国民家常菜", meta = "10 分钟 · 简单", rating = "4.8")
            RecipeCard(title = "可乐鸡翅", subtitle = "甜咸口，小朋友最爱", meta = "30 分钟 · 简单", rating = "4.7")
            RecipeCard(title = "酸辣土豆丝", subtitle = "脆爽开胃的下饭菜", meta = "15 分钟 · 中等", rating = "4.6")
        }
    }
}

/** 一张菜谱卡片：颜色和字体都从主题里取，不再写死。 */
@Composable
fun RecipeCard(title: String, subtitle: String, meta: String, rating: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 盘子：先用一个圆形色块占位，第 11 期换成真图片
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                ServingsStepper()
            }
            Text(text = "★ $rating", style = MaterialTheme.typography.labelLarge)
        }
    }
}


/** 份数加减器：rememberSaveable 让状态在旋转屏幕（Activity 重建）之后也活下来。 */
@Composable
fun ServingsStepper() {
    var portion by rememberSaveable { mutableStateOf(Portion(2, "人份")) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { if (portion.count > 1) portion = portion.copy(count = portion.count - 1) }) { Text("−") }
        Text(text = "${portion.count} ${portion.unit}", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = { portion = portion.copy(count = portion.count + 1) }) { Text("+") }
    }
}

/** 份数 + 单位（“人份”“碗”“盘”）：一个自己写的类。 */
data class Portion(val count: Int, val unit: String)

@Preview(name = "浅色", showBackground = true)
@Preview(name = "深色", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    QiweiTheme {
        HomeScreen()
    }
}
