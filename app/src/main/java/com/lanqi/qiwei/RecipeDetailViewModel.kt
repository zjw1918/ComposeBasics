package com.lanqi.qiwei

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 详情页只可能处在这两种状态之一：还在等，或者拿到了菜。 */
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val recipe: Recipe, val servings: Int) : DetailUiState {
        /** 按份数换算后的用量（菜谱里写的是两人份）。业务规则放在这里，界面只管显示 */
        val ingredients: List<Ingredient>
            get() = recipe.ingredients.map { it.copy(amount = it.amount * servings / 2) }
    }
}

/** 详情页的值班经理：屏幕旋转时不下班，离开这一页才下班。 */
class RecipeDetailViewModel(
    private val recipeId: Int,
    // SavedStateHandle：系统帮忙保管的一小块状态，App 在后台被杀掉再回来，也还在
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // 可以改的那份只在自己手里；交给界面的是只读的
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        Log.d("Qiwei", "详情 ViewModel 上班：菜 #$recipeId")
        viewModelScope.launch {
            val recipe = RecipeRepository.recipe(recipeId)
            _uiState.value = DetailUiState.Success(recipe, servings = savedStateHandle["servings"] ?: 2)
        }
    }

    /** 界面上点了加减：记下来，再更新状态 */
    fun setServings(servings: Int) {
        if (servings < 1) return
        savedStateHandle["servings"] = servings
        _uiState.update { state ->
            if (state is DetailUiState.Success) state.copy(servings = servings) else state
        }
    }

    override fun onCleared() {
        Log.d("Qiwei", "详情 ViewModel 下班：菜 #$recipeId")
    }
}
