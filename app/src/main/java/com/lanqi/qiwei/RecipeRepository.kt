package com.lanqi.qiwei

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** 菜谱仓库：现在数据还在内存里，假装要等 1 秒（像在网上取）。第 11 期换成真的后端。 */
object RecipeRepository {
    suspend fun recipe(id: Int): Recipe {
        delay(1000)
        return allRecipes.first { it.id == id }
    }

    /**
     * 「猜你喜欢」排序：一道菜和越多高分菜用了同样的食材，越靠前。
     * 要把每两道菜都比一遍：5000 × 5000，两千五百万次比较。
     */
    suspend fun recommended(): List<Recipe> = withContext(Dispatchers.Default) {
        // withContext：把这段活儿交给 Default 线程池（后厨切配工位），算完再回到调用者的线程。
        // 这样无论谁在主线程上调用它，都不会卡：这叫「主线程安全」
        val started = System.currentTimeMillis()
        Log.d("Qiwei", "推荐排序开始，线程：${Thread.currentThread().name}")
        val names = allRecipes.map { recipe -> recipe.ingredients.map { it.name }.toSet() }
        val scores = DoubleArray(allRecipes.size)
        for (i in allRecipes.indices) {
            for (j in allRecipes.indices) {
                if (i != j) scores[i] += names[i].count { it in names[j] } * allRecipes[j].rating
            }
        }
        val result = allRecipes.indices.sortedByDescending { scores[it] }.map { allRecipes[it] }
        Log.d("Qiwei", "推荐排序完成，用了 ${System.currentTimeMillis() - started} 毫秒，线程：${Thread.currentThread().name}")
        result
    }
}
