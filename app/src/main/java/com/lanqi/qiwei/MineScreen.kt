package com.lanqi.qiwei

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MineViewModel(repository: MyRecipesRepository) : ViewModel() {
    val recipes: StateFlow<List<MyRecipeEntity>> = repository.recipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

/** 我的：自己写的菜谱 + 设置。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    viewModel: MineViewModel,
    onNewRecipe: () -> Unit,
    onEditRecipe: (Long) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text("我的") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewRecipe) { Icon(Icons.Filled.Add, contentDescription = "新建菜谱") }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                ListItem(
                    headlineContent = { Text("设置") },
                    supportingContent = { Text("主题") },
                    leadingContent = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onOpenSettings)
                )
                SectionTitle("我的菜谱 · ${recipes.size} 道")
            }
            items(recipes, key = { it.id }) { recipe ->
                ListItem(
                    headlineContent = { Text(recipe.title) },
                    supportingContent = { Text("${recipe.minutes} 分钟") },
                    modifier = Modifier.clickable { onEditRecipe(recipe.id) }
                )
            }
        }
    }
}
