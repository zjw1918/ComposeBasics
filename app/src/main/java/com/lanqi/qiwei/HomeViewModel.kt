package com.lanqi.qiwei

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 首页的 ViewModel：从仓库取菜谱列表（服务器已经按评分排好）。null = 还在取。 */
class HomeViewModel(private val repository: RecipeRepository) : ViewModel() {
    private val _recipes = MutableStateFlow<List<Recipe>?>(null)
    val recipes: StateFlow<List<Recipe>?> = _recipes.asStateFlow()

    init {
        viewModelScope.launch {
            _recipes.value = repository.recipes()
        }
    }
}
