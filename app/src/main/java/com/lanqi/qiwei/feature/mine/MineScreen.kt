package com.lanqi.qiwei.feature.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lanqi.qiwei.data.MyRecipeEntity
import com.lanqi.qiwei.data.MyRecipesRepository
import com.lanqi.qiwei.ui.components.SectionTitle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MineViewModel(private val repository: MyRecipesRepository) : ViewModel() {
    val recipes: StateFlow<List<MyRecipeEntity>> = repository.recipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 刚删掉的那道菜：Snackbar 上的「撤销」要用它把菜放回去 */
    var justDeleted by mutableStateOf<MyRecipeEntity?>(null)
        private set

    fun delete(recipe: MyRecipeEntity) {
        viewModelScope.launch {
            repository.delete(recipe)
            justDeleted = recipe
        }
    }

    fun undoDelete() {
        val recipe = justDeleted ?: return
        justDeleted = null
        viewModelScope.launch { repository.restore(recipe) }
    }

    fun deleteMessageShown() {
        justDeleted = null
    }
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
    val snackbarHostState = remember { SnackbarHostState() }
    // 删掉一道菜：底部弹出「已删除」，点「撤销」就放回去
    LaunchedEffect(viewModel.justDeleted) {
        val deleted = viewModel.justDeleted ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar("已删除「${deleted.title}」", actionLabel = "撤销")
        if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete() else viewModel.deleteMessageShown()
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text("我的") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    trailingContent = {
                        // 只有图标的按钮：一定要写 contentDescription，读屏软件才知道它是干什么的
                        IconButton(onClick = { viewModel.delete(recipe) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除 ${recipe.title}")
                        }
                    },
                    modifier = Modifier.clickable { onEditRecipe(recipe.id) }
                )
            }
        }
    }
}
