package com.lanqi.qiwei

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/** 首页的三种状态：在取、取到了、出错了。 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val recipes: List<Recipe>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/** 首页的 ViewModel：从仓库取菜谱列表（服务器已经按评分排好）。 */
class HomeViewModel(private val repository: RecipeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** 取一次列表；出错页上的「重试」也调用它 */
    fun load() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                HomeUiState.Success(repository.recipes())
            } catch (e: IOException) {
                // 连不上、超时、断网
                HomeUiState.Error("网络不通，检查一下网络再试试")
            } catch (e: HttpException) {
                // 连上了，但服务器回了 4xx / 5xx
                HomeUiState.Error("服务器出错了（${e.code()}）")
            }
        }
    }
}
