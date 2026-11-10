package com.lanqi.qiwei

import android.util.Log
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
            // 这里在主线程；recommended() 自己会切到后台去算，算完回到这里
            _recipes.value = RecipeRepository.recommended()
            Log.d("Qiwei", "排序结果交给界面，线程：${Thread.currentThread().name}")
        }
    }
}
