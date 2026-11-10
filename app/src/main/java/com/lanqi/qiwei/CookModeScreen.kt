package com.lanqi.qiwei

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 每一步的计时：60 秒（示例） */
const val STEP_SECONDS = 60

/** 做饭模式：一屏一步，大字，带一个计时器。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookModeScreen(recipe: Recipe, onClose: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var secondsLeft by rememberSaveable { mutableIntStateOf(STEP_SECONDS) }
    // 和这个页面同生共死的协程作用域：页面离开组合，它启动的协程全部取消
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("做饭模式 · ${recipe.title}") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "退出做饭模式") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { (step + 1f) / recipe.steps.size },
                modifier = Modifier.fillMaxWidth()
            )
            Text("第 ${step + 1} 步 / 共 ${recipe.steps.size} 步", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = recipe.steps[step],
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(text = formatTime(secondsLeft), fontSize = 72.sp, style = MaterialTheme.typography.displayLarge)
            Button(onClick = {
                // 启动一个协程：delay 只是把这个协程挂起一秒，主线程空出来，继续画界面、响应点击
                scope.launch {
                    while (secondsLeft > 0) {
                        delay(1000)
                        secondsLeft--
                    }
                }
            }) { Text("开始计时") }
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(enabled = step > 0, onClick = { step--; secondsLeft = STEP_SECONDS }) { Text("上一步") }
                Button(enabled = step < recipe.steps.lastIndex, onClick = { step++; secondsLeft = STEP_SECONDS }) { Text("下一步") }
            }
        }
    }
}

/** 90 → "01:30" */
fun formatTime(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
