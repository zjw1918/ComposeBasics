package com.lanqi.qiwei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lanqi.qiwei.ui.theme.QiweiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QiweiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RecipeCard(
                        title = "番茄炒蛋",
                        subtitle = "十分钟上桌的国民家常菜",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/** 一张菜谱卡片：它只"描述"卡片长什么样，它本身不是卡片。 */
@Composable
fun RecipeCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 盘子：先用一个圆形色块占位，第 11 期换成真图片
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFF4A261))
        )
        Column {
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Preview(name = "普通", showBackground = true)
@Preview(name = "大字体", showBackground = true, fontScale = 1.5f)
@Preview(name = "窄屏", showBackground = true, widthDp = 120)
@Composable
fun RecipeCardPreview() {
    QiweiTheme {
        RecipeCard(title = "番茄炒蛋", subtitle = "十分钟上桌的国民家常菜")
    }
}
