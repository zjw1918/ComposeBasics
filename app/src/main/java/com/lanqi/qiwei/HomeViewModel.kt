package com.lanqi.qiwei

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import retrofit2.HttpException
import java.io.IOException

/** 首页的三种状态：在取、取到了、出错了。 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val recipes: List<Recipe>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

/** 首页的 ViewModel：搜索词变了，就去服务器搜；只搜用户停手后的那个词，旧的请求自动取消。 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repository: RecipeRepository) : ViewModel() {
    /** 搜索框里的字。用 Compose 的 State 存（文本框要求同步更新，不能等） */
    var query by mutableStateOf("")
        private set

    /** 每点一次「重试」加一，让下面的流再搜一遍 */
    private val retries = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> =
        combine(
            snapshotFlow { query }          // 把 Compose 的 State 变成一条流：字一变，流里就来一个新值
                .debounce(300)              // 停手 300 毫秒才放行：打字过程中的半截词，全部丢掉
                .distinctUntilChanged(),    // 和上一个一样就不放行（删一个字又打回来）
            retries,
        ) { q, _ -> q }
            .flatMapLatest { q -> search(q) }  // 新词一来，上一个词的搜索（连同它的请求）立刻取消
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState.Loading,
            )

    fun onQueryChange(newQuery: String) {
        query = newQuery
    }

    fun retry() {
        retries.value++
    }

    /** 搜一个词：先说「在取」，再给结果或错误 */
    private fun search(q: String) = flow {
        emit(HomeUiState.Loading)
        Log.d("Qiwei", "搜索「$q」：发请求")
        try {
            emit(HomeUiState.Success(repository.recipes(q)))
            Log.d("Qiwei", "搜索「$q」：拿到结果")
        } catch (e: CancellationException) {
            Log.d("Qiwei", "搜索「$q」：被取消（来了新的搜索词）")
            throw e  // 取消一定要继续往外抛，不能吞掉
        } catch (e: IOException) {
            emit(HomeUiState.Error("网络不通，检查一下网络再试试"))
        } catch (e: HttpException) {
            emit(HomeUiState.Error("服务器出错了（${e.code()}）"))
        }
    }
}
