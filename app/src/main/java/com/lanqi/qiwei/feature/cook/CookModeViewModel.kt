package com.lanqi.qiwei.feature.cook

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lanqi.qiwei.data.RecipeRepository
import com.lanqi.qiwei.model.Recipe
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** 一个计时器：属于第几步、还剩几秒、正在走还是停着。 */
data class CookTimer(val secondsLeft: Int, val running: Boolean)

sealed interface CookUiState {
    data object Loading : CookUiState
    data class Error(val message: String) : CookUiState
    data class Cooking(
        val recipe: Recipe,
        val step: Int,
        /** 每一步一个计时器（按步骤编号），可以同时走好几个 */
        val timers: Map<Int, CookTimer> = emptyMap(),
    ) : CookUiState
}

/** 做饭模式的 ViewModel。计时器都在 viewModelScope 里跑：离开做饭模式，它们一起停。 */
class CookModeViewModel(recipeId: Int, repository: RecipeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<CookUiState>(CookUiState.Loading)
    val uiState: StateFlow<CookUiState> = _uiState.asStateFlow()

    /** 每个计时器对应一个 Job：手里握着 Job，才能让它停 */
    private val jobs = mutableMapOf<Int, Job>()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                CookUiState.Cooking(repository.recipe(recipeId), step = 0)
            } catch (e: IOException) {
                CookUiState.Error("网络不通，退出再进来试试")
            } catch (e: HttpException) {
                CookUiState.Error("服务器出错了（${e.code()}）")
            }
        }
    }

    fun goTo(step: Int) = updateCooking { it.copy(step = step) }

    /** 开始或继续第 step 步的计时 */
    fun start(step: Int) {
        if (jobs[step]?.isActive == true) return  // 已经在走了，不重复启动
        val from = (uiState.value as? CookUiState.Cooking)?.timers?.get(step)?.secondsLeft ?: STEP_SECONDS
        jobs[step] = viewModelScope.launch {
            Log.d("Qiwei", "计时器 #${step + 1} 开始：$from 秒")
            var left = from
            try {
                setTimer(step, CookTimer(left, running = true))
                while (left > 0) {
                    delay(1000)  // 挂起点：取消就发生在这里
                    left--
                    setTimer(step, CookTimer(left, running = true))
                }
                Log.d("Qiwei", "计时器 #${step + 1} 时间到！")
            } finally {
                // 不管是走完了，还是被取消了，都会来到这里
                Log.d("Qiwei", "计时器 #${step + 1} 停了，还剩 $left 秒")
                setTimer(step, CookTimer(left, running = false))
            }
        }
    }

    /** 暂停 = 取消这个计时器的协程；剩下的秒数留在状态里，继续时接着数 */
    fun pause(step: Int) {
        jobs.remove(step)?.cancel()
    }

    private fun setTimer(step: Int, timer: CookTimer) = updateCooking { it.copy(timers = it.timers + (step to timer)) }

    private fun updateCooking(change: (CookUiState.Cooking) -> CookUiState.Cooking) {
        _uiState.update { state -> if (state is CookUiState.Cooking) change(state) else state }
    }

    override fun onCleared() {
        Log.d("Qiwei", "做饭模式 ViewModel 下班：viewModelScope 取消，所有计时器一起停")
    }
}
