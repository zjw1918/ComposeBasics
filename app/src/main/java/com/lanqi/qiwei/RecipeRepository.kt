package com.lanqi.qiwei

import kotlinx.coroutines.delay

/** 菜谱仓库：现在数据还在内存里，假装要等 1 秒（像在网上取）。第 11 期换成真的后端。 */
object RecipeRepository {
    suspend fun recipe(id: Int): Recipe {
        delay(1000)
        return allRecipes.first { it.id == id }
    }
}
