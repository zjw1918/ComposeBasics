package com.lanqi.qiwei

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** 菜谱详情：只管显示 ViewModel 给的状态。数据从哪来、等多久，它不知道。 */
@Composable
fun DetailScreen(viewModel: RecipeDetailViewModel, onCook: () -> Unit, onBack: () -> Unit) {
    // 带生命周期地收集：App 退到后台就停止收集，回到前台再接着收
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // sealed interface：when 必须把每一种状态都处理到，漏了编译不过
    when (val state = uiState) {
        DetailUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is DetailUiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ErrorMessage(message = state.message, onRetry = viewModel::load)
        }
        is DetailUiState.Success -> RecipeDetail(
            state = state,
            onServingsChange = viewModel::setServings,
            onCook = onCook,
            onBack = onBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetail(
    state: DetailUiState.Success,
    onServingsChange: (Int) -> Unit,
    onCook: () -> Unit,
    onBack: () -> Unit,
) {
    val recipe = state.recipe
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onCook) { Text("开始做饭") }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(recipe.subtitle, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${recipe.minutes} 分钟 · ${recipe.difficulty} · ★ ${recipe.rating}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                SectionTitle("食材")
                ServingsStepper(
                    servings = state.servings,
                    onServingsChange = onServingsChange,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            items(state.ingredients) { ingredient ->
                ListItem(
                    headlineContent = { Text(ingredient.name) },
                    trailingContent = { Text(formatAmount(ingredient.amount) + " " + ingredient.unit) }
                )
            }
            item { SectionTitle("步骤") }
            itemsIndexed(recipe.steps) { index, step ->
                ListItem(
                    leadingContent = { Text("${index + 1}", style = MaterialTheme.typography.titleLarge) },
                    headlineContent = { Text(step) }
                )
            }
        }
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

/** 2.0 显示成 2，0.5 显示成 0.5 */
fun formatAmount(amount: Double): String =
    if (amount % 1.0 == 0.0) amount.toInt().toString() else "%.1f".format(amount).trimEnd('0').trimEnd('.')
