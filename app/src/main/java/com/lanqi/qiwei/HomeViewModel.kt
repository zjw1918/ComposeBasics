package com.lanqi.qiwei

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 首页的 ViewModel：负责把菜按「猜你喜欢」排好。null = 还在算。 */
class HomeViewModel : ViewModel() {
    private val _recipes = MutableStateFlow<List<Recipe>?>(null)
    val recipes: StateFlow<List<Recipe>?> = _recipes.asStateFlow()

    init {
        viewModelScope.launch {
            _recipes.value = RecipeRepository.recommended()
        }
    }
}
