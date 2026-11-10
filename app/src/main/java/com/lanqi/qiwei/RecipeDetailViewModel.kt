package com.lanqi.qiwei

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 详情页要显示的全部内容。 */
data class DetailUiState(
    val loading: Boolean = true,
    val recipe: Recipe? = null,
)

/** 详情页的值班经理：屏幕旋转时不下班，离开这一页才下班。 */
class RecipeDetailViewModel(private val recipeId: Int) : ViewModel() {
    // 可以改的那份只在自己手里；交给界面的是只读的
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        Log.d("Qiwei", "详情 ViewModel 上班：菜 #$recipeId")
        viewModelScope.launch {
            val recipe = RecipeRepository.recipe(recipeId)
            _uiState.value = DetailUiState(loading = false, recipe = recipe)
        }
    }

    override fun onCleared() {
        Log.d("Qiwei", "详情 ViewModel 下班：菜 #$recipeId")
    }
}
