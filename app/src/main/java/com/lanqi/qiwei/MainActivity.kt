package com.lanqi.qiwei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/** 一张菜谱卡片：它只"描述"卡片长什么样，它本身不是卡片。 */
@Composable
fun RecipeCard(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        modifier = modifier
    )
}

@Preview(name = "普通", showBackground = true)
@Preview(name = "大字体", showBackground = true, fontScale = 1.5f)
@Preview(name = "窄屏", showBackground = true, widthDp = 120)
@Composable
fun RecipeCardPreview() {
    QiweiTheme {
        RecipeCard(title = "番茄炒蛋")
    }
}
