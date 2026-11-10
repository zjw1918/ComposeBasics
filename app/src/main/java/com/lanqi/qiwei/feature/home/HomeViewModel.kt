package com.lanqi.qiwei.feature.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lanqi.qiwei.data.FavoritesRepository
import com.lanqi.qiwei.data.RecipeRepository
import com.lanqi.qiwei.model.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * 首页状态：离线优先之后，不再是「在取 / 成功 / 失败」三选一，
 * 而是：手上有的菜（来自数据库）+ 正在不在刷新 + 有没有一条要告诉用户的话。
 */
data class HomeUiState(
    val recipes: List<Recipe> = emptyList(),
    val refreshing: Boolean = false,
    val message: String? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {
    var query by mutableStateOf("")
        private set

    // 实验：有人「顺手」把 debounce(300) 删掉了
    private val searchTerm = snapshotFlow { query }.distinctUntilChanged()
    private val refreshing = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> =
        combine(
            searchTerm.flatMapLatest { q -> repository.observeRecipes(q) },  // 读：只读数据库
            refreshing,
            message,
        ) { recipes, refreshing, message -> HomeUiState(recipes, refreshing, message) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(refreshing = true))

    init {
        // 写：搜索词一变，就去后端刷新一次（新词来了，旧的刷新取消）
        viewModelScope.launch {
            searchTerm.collectLatest { q -> refresh(q) }
        }
    }

    val favoriteIds: StateFlow<Set<Int>> = favorites.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch { favorites.setFavorite(recipe, recipe.id !in favoriteIds.value) }
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
    }

    /** 下拉刷新、提示条上的「重试」都调用它 */
    fun refresh() {
        viewModelScope.launch { refresh(query) }
    }

    private suspend fun refresh(q: String) {
        refreshing.value = true
        try {
            repository.refreshRecipes(q)
            Log.d("Qiwei", "刷新「$q」：写进数据库")
        } catch (e: IOException) {
            message.value = "连不上服务器，先给你看上次存下的菜谱"
        } catch (e: HttpException) {
            message.value = "服务器出错了（${e.code()}），先给你看上次存下的菜谱"
        } finally {
            refreshing.value = false
        }
    }

    /** 提示已经给用户看过了 */
    fun messageShown() {
        message.update { null }
    }
}
