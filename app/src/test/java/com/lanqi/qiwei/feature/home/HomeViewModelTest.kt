package com.lanqi.qiwei.feature.home

import androidx.compose.runtime.snapshots.Snapshot
import com.lanqi.qiwei.data.FavoriteEntity
import com.lanqi.qiwei.data.FavoritesRepository
import com.lanqi.qiwei.data.RecipeRepository
import com.lanqi.qiwei.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.IOException

/** 假的菜谱仓库：数据在内存里；记下每一次刷新搜的是什么词；可以让刷新失败。 */
private class FakeRecipeRepository : RecipeRepository {
    val stored = MutableStateFlow(
        listOf(
            Recipe(1, "番茄炒蛋", "", "家常", 10, "简单", 4.8),
            Recipe(2, "可乐鸡翅", "", "家常", 30, "简单", 4.7),
        )
    )
    val refreshedQueries = mutableListOf<String>()
    var offline = false

    override fun observeRecipes(query: String): Flow<List<Recipe>> =
        stored.map { list -> list.filter { it.title.contains(query) } }

    override suspend fun refreshRecipes(query: String) {
        refreshedQueries += query
        if (offline) throw IOException("没网")
    }

    override fun observeRecipe(id: Int): Flow<Recipe?> = stored.map { list -> list.firstOrNull { it.id == id } }
    override suspend fun refreshRecipe(id: Int) = Unit
    override suspend fun recipe(id: Int): Recipe = stored.value.first { it.id == id }
}

private class FakeFavoritesRepository : FavoritesRepository {
    override val favorites: Flow<List<FavoriteEntity>> = flowOf(emptyList())
    override val favoriteIds: Flow<Set<Int>> = flowOf(emptySet())
    override fun isFavorite(recipeId: Int): Flow<Boolean> = flowOf(false)
    override suspend fun remove(recipeId: Int) = Unit
    override suspend fun setFavorite(recipe: Recipe, favorite: Boolean) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val repository = FakeRecipeRepository()

    // 测试里没有真正的主线程：用一个「虚拟时间」的调度器顶替 Dispatchers.Main
    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    /** 改搜索词，再通知 Compose 的状态系统「改完了」，snapshotFlow 才收得到（在界面里这一步由框架每一帧自动做） */
    private fun HomeViewModel.type(text: String) {
        onQueryChange(text)
        Snapshot.sendApplyNotifications()
    }

    @Test
    fun 打字过程中不刷新_停手300毫秒后只刷新最后一个词() = runTest {
        val viewModel = HomeViewModel(repository, FakeFavoritesRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()  // 一打开：搜一次空字符串
        repository.refreshedQueries.clear()

        viewModel.type("番")
        advanceTimeBy(100)
        viewModel.type("番茄")
        advanceTimeBy(100)
        viewModel.type("番茄炒")
        advanceTimeBy(299)  // 虚拟时间：瞬间「过去」299 毫秒——还没到 300，一个请求都不该发
        assertEquals(emptyList<String>(), repository.refreshedQueries)

        advanceTimeBy(2)    // 过了 300 毫秒：只刷新最后那个词
        assertEquals(listOf("番茄炒"), repository.refreshedQueries)
        assertEquals(listOf("番茄炒蛋"), viewModel.uiState.value.recipes.map { it.title })
    }

    @Test
    fun 刷新失败时_列表还在_并且有一条提示() = runTest {
        repository.offline = true
        val viewModel = HomeViewModel(repository, FakeFavoritesRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.recipes.size)  // 数据库里的菜照样显示
        assertNotNull(state.message)         // 并且告诉用户：连不上
    }
}
