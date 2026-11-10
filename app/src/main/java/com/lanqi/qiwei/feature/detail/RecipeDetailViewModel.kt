package com.lanqi.qiwei.feature.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lanqi.qiwei.data.FavoritesRepository
import com.lanqi.qiwei.data.RecipeRepository
import com.lanqi.qiwei.model.Ingredient
import com.lanqi.qiwei.model.Recipe
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** 详情页只可能处在这两种状态之一：还在等，或者拿到了菜。 */
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Error(val message: String) : DetailUiState
    data class Success(val recipe: Recipe, val servings: Int) : DetailUiState {
        /** 按份数换算后的用量（菜谱里写的是两人份）。业务规则放在这里，界面只管显示 */
        val ingredients: List<Ingredient>
            get() = recipe.ingredients.map { it.copy(amount = it.amount * servings / 2) }
    }
}

/** 详情页的值班经理：屏幕旋转时不下班，离开这一页才下班。 */
@HiltViewModel(assistedFactory = RecipeDetailViewModel.Factory::class)
class RecipeDetailViewModel @AssistedInject constructor(
    // 菜的 id 来自导航（路由键），Hilt 不知道：开店时登记不了，只能在用的时候「现场填」
    @Assisted private val recipeId: Int,
    private val repository: RecipeRepository,
    private val favorites: FavoritesRepository,
    // SavedStateHandle：系统帮忙保管的一小块状态，App 在后台被杀掉再回来，也还在
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val loadError = MutableStateFlow<String?>(null)

    /** 读：菜来自数据库，份数来自 SavedStateHandle，出错信息来自刷新。三样合成一个状态 */
    val uiState: StateFlow<DetailUiState> = combine(
        repository.observeRecipe(recipeId),
        savedStateHandle.getStateFlow("servings", 2),
        loadError,
    ) { recipe, servings, error ->
        when {
            recipe != null -> DetailUiState.Success(recipe, servings)  // 冷库里有：先显示，哪怕刷新失败
            error != null -> DetailUiState.Error(error)
            else -> DetailUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState.Loading)

    /** 这道菜收藏了没有：直接来自数据库的 Flow */
    val isFavorite: StateFlow<Boolean> = favorites.isFavorite(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        Log.d("Qiwei", "详情 ViewModel 上班：菜 #$recipeId")
        load()
    }

    /** 写：去后端刷新这道菜，写进数据库 */
    fun load() {
        loadError.value = null
        viewModelScope.launch {
            try {
                repository.refreshRecipe(recipeId)
            } catch (e: IOException) {
                loadError.value = "网络不通，检查一下网络再试试"
            } catch (e: HttpException) {
                loadError.value = "服务器出错了（${e.code()}）"
            }
        }
    }

    fun toggleFavorite() {
        val state = uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch { favorites.setFavorite(state.recipe, !isFavorite.value) }
    }

    /** 界面上点了加减：存进 SavedStateHandle，上面的 combine 自己就会算出新状态 */
    fun setServings(servings: Int) {
        if (servings >= 1) savedStateHandle["servings"] = servings
    }

    /** 现场填 id 的那张表：Hilt 生成它的实现，其余参数自动注入 */
    @AssistedFactory
    interface Factory {
        fun create(recipeId: Int): RecipeDetailViewModel
    }

    override fun onCleared() {
        Log.d("Qiwei", "详情 ViewModel 下班：菜 #$recipeId")
    }
}
