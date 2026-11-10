package com.lanqi.qiwei

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    /** 搜索框里的字。用 Compose 的 State 存（文本框要求同步更新，不能等） */
    var query by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** 翻车：每打一个字，就发一个请求 */
    fun onQueryChange(newQuery: String) {
        query = newQuery
        load()
    }

    /** 按当前的搜索词取一次列表；出错页上的「重试」也调用它 */
    fun load() {
        _uiState.value = HomeUiState.Loading
        val q = query
        viewModelScope.launch {
            Log.d("Qiwei", "搜索「$q」：发请求")
            _uiState.value = try {
                HomeUiState.Success(repository.recipes(q))
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
