package com.lanqi.qiwei

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

// 路由键：每个页面一个。详情页只带菜的 id，不带整道菜。
// @Serializable + NavKey：返回栈能被写进 Bundle，旋转屏幕、甚至进程被杀，都能还原
@Serializable
data object Home : NavKey

@Serializable
data class Detail(val recipeId: Int) : NavKey

/** 鳍味的导航：返回栈就是一个列表，NavDisplay 把列表最上面那一项画出来。 */
@Composable
fun QiweiNavigation() {
    // rememberNavBackStack：可以保存的返回栈
    val backStack = rememberNavBackStack(Home)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(onOpenRecipe = { id -> backStack.add(Detail(id)) })
            }
            entry<Detail> { key ->
                DetailScreen(recipeId = key.recipeId, onBack = { backStack.removeLastOrNull() })
            }
        }
    )
}
