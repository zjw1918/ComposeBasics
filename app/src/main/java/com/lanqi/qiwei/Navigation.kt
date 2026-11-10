package com.lanqi.qiwei

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

// 路由键：每个页面一个。详情页只带菜的 id，不带整道菜。
// @Serializable + NavKey：返回栈能被写进 Bundle，旋转屏幕、甚至进程被杀，都能还原
@Serializable
data object Home : NavKey

@Serializable
data class Detail(val recipeId: Int) : NavKey

@Serializable
data object Favorites : NavKey

@Serializable
data object Mine : NavKey

/** 底部的三个标签。每个标签有自己的根页面。 */
enum class Tab(val label: String, val icon: ImageVector, val root: NavKey) {
    HOME("首页", Icons.Filled.Home, Home),
    FAVORITES("收藏", Icons.Filled.Favorite, Favorites),
    MINE("我的", Icons.Filled.Person, Mine),
}

/** 鳍味的导航：每个标签一摞自己的返回栈；NavDisplay 画出当前标签那一摞最上面的一页。 */
@Composable
fun QiweiNavigation() {
    val stacks = mapOf(
        Tab.HOME to rememberNavBackStack(Home),
        Tab.FAVORITES to rememberNavBackStack(Favorites),
        Tab.MINE to rememberNavBackStack(Mine),
    )
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    val backStack = stacks.getValue(tab)

    // 在别的标签的根页面按返回：先回到首页标签，而不是直接退出
    BackHandler(enabled = tab != Tab.HOME && backStack.size == 1) { tab = Tab.HOME }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = t == tab,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = null) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(WindowInsets.navigationBars),
            onBack = { backStack.removeLastOrNull() },
            // 让每个条目有自己的 rememberSaveable 存储和 ViewModel 存储
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<Home> {
                    HomeScreen(onOpenRecipe = { id -> backStack.add(Detail(id)) })
                }
                entry<Detail> { key ->
                    // 每个详情页条目一个 ViewModel：条目在返回栈里，它就在；条目被弹出，它才下班
                    val viewModel = viewModel { RecipeDetailViewModel(key.recipeId, createSavedStateHandle()) }
                    DetailScreen(viewModel = viewModel, onBack = { backStack.removeLastOrNull() })
                }
                entry<Favorites> { FavoritesScreen() }
                entry<Mine> { MineScreen() }
            }
        )
    }
}
