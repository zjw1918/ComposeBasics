package com.lanqi.qiwei

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay

// 路由键：每个页面一个。详情页只带菜的 id，不带整道菜
data object Home
data class Detail(val recipeId: Int)

/** 鳍味的导航：返回栈就是一个列表，NavDisplay 把列表最上面那一项画出来。 */
@Composable
fun QiweiNavigation() {
    // 翻车：返回栈用普通的 remember 记着
    val backStack = remember { mutableStateListOf<Any>(Home) }
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
