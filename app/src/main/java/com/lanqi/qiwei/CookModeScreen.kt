package com.lanqi.qiwei

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

/** 每一步的计时：60 秒（示例） */
const val STEP_SECONDS = 60

/** 做饭模式：一屏一步，大字；每一步可以开一个计时器，几个计时器可以同时走。 */
@Composable
fun CookModeScreen(viewModel: CookModeViewModel, onClose: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        CookUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is CookUiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
        }
        is CookUiState.Cooking -> CookingContent(
            state = state,
            onGoTo = viewModel::goTo,
            onStart = viewModel::start,
            onPause = viewModel::pause,
            onClose = onClose
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingContent(
    state: CookUiState.Cooking,
    onGoTo: (Int) -> Unit,
    onStart: (Int) -> Unit,
    onPause: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val recipe = state.recipe
    val step = state.step
    val timer = state.timers[step] ?: CookTimer(STEP_SECONDS, running = false)
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
            // 每换一步，提示条出现 3 秒。LaunchedEffect(step)：step 一变，旧的协程取消，新的重新开始数
            var showTip by remember(step) { mutableStateOf(true) }
            LaunchedEffect(step) {
                delay(3000)
                showTip = false
            }
            Box(modifier = Modifier.height(32.dp), contentAlignment = Alignment.Center) {
                if (showTip) {
                    Text(
                        "小提示：开了计时器就可以去下一步，它会接着走",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Text(
                text = recipe.steps[step],
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(text = formatTime(timer.secondsLeft), fontSize = 72.sp, style = MaterialTheme.typography.displayLarge)
            if (timer.running) {
                FilledTonalButton(onClick = { onPause(step) }) { Text("暂停") }
            } else {
                Button(onClick = { onStart(step) }) {
                    Text(if (timer.secondsLeft < STEP_SECONDS) "继续" else "开始计时")
                }
            }
            // 别的步骤正在走的计时器：点一下跳过去
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.timers.filter { (i, t) -> i != step && t.running }.forEach { (i, t) ->
                    AssistChip(onClick = { onGoTo(i) }, label = { Text("第 ${i + 1} 步 ${formatTime(t.secondsLeft)}") })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(enabled = step > 0, onClick = { onGoTo(step - 1) }) { Text("上一步") }
                Button(enabled = step < recipe.steps.lastIndex, onClick = { onGoTo(step + 1) }) { Text("下一步") }
            }
        }
    }
}

/** 90 → "01:30" */
fun formatTime(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
