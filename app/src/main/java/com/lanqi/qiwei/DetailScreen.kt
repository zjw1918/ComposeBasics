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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp

/** 菜谱详情：凭 id 去仓库取这道菜（要等一会儿），取到之前转圈。 */
@Composable
fun DetailScreen(recipeId: Int, onBack: () -> Unit) {
    // 翻车：加载结果放在 remember 里，加载放在 LaunchedEffect 里
    var recipe by remember { mutableStateOf<Recipe?>(null) }
    LaunchedEffect(recipeId) {
        recipe = RecipeRepository.recipe(recipeId)
    }
    val loaded = recipe
    if (loaded == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        RecipeDetail(recipe = loaded, onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetail(recipe: Recipe, onBack: () -> Unit) {
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
                SectionTitle("食材（两人份）")
            }
            items(recipe.ingredients) { ingredient ->
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
