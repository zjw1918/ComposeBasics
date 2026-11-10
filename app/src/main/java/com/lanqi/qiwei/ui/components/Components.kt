package com.lanqi.qiwei.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 几个页面共用的小组件。 */

/** 出错时的提示：一句人话 + 重试按钮。 */
@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("重试") }
    }
}

@Composable
fun SectionTitle(text: String) {
    HorizontalDivider()
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

/** 份数加减器：无状态。份数从外面传进来，点了以后，通过 onServingsChange 告诉外面。 */
@Composable
fun ServingsStepper(servings: Int, onServingsChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { if (servings > 1) onServingsChange(servings - 1) }) { Text("−") }
        // 数字换了：旧数字滑出去，新数字滑进来。加的时候往上滑，减的时候往下滑
        AnimatedContent(
            targetState = servings,
            transitionSpec = {
                val up = targetState > initialState
                (slideInVertically { h -> if (up) h else -h } + fadeIn()) togetherWith
                    (slideOutVertically { h -> if (up) -h else h } + fadeOut())
            },
            label = "份数"
        ) { n ->
            Text(text = "$n 人份", style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = { onServingsChange(servings + 1) }) { Text("+") }
    }
}

/** 2.0 显示成 2，0.5 显示成 0.5 */
fun formatAmount(amount: Double): String =
    if (amount % 1.0 == 0.0) amount.toInt().toString() else "%.1f".format(amount).trimEnd('0').trimEnd('.')

/** 90 → "01:30" */
fun formatTime(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
