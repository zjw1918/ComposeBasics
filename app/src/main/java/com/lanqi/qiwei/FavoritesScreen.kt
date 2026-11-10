package com.lanqi.qiwei

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 收藏页的 ViewModel：把数据库的 Flow 接成 StateFlow，界面一订阅就有最新的列表。 */
class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {
    val favorites: StateFlow<List<FavoriteEntity>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(recipeId: Int) {
        viewModelScope.launch { repository.remove(recipeId) }
    }
}

/** 收藏页：数据库里有什么，就显示什么。在别的页面点了心，这里自己就更新了。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel, onOpenRecipe: (Int) -> Unit) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("收藏 · ${favorites.size} 道") }) }) { innerPadding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("还没有收藏。在菜谱上点 ♡ 试试", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(favorites, key = { it.recipeId }) { favorite ->
                    ListItem(
                        headlineContent = { Text(favorite.title) },
                        leadingContent = {
                            AsyncImage(
                                model = favorite.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.remove(favorite.recipeId) }) {
                                Icon(Icons.Filled.Favorite, contentDescription = "取消收藏", tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.clickable { onOpenRecipe(favorite.recipeId) }
                    )
                }
            }
        }
    }
}
