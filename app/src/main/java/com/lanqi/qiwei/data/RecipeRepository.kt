package com.lanqi.qiwei.data

import com.lanqi.qiwei.BASE_URL
import com.lanqi.qiwei.model.Ingredient
import com.lanqi.qiwei.model.Recipe
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * 菜谱仓库，离线优先：
 * - 界面只读数据库（observe 开头的函数，返回 Flow）；
 * - 网络只负责把新数据写进数据库（refresh 开头的函数）。
 * 数据只有一个来源：数据库。网络通不通，界面都有东西可看。
 */
class RecipeRepository(private val api: QiweiApi, private val dao: RecipeDao) {

    fun observeRecipes(query: String): Flow<List<Recipe>> =
        dao.search(query).map { list -> list.map { it.toRecipe() } }

    /** 去后端取最新的列表，写进数据库。界面不用管：数据库一变，上面那条 Flow 自己就来新值 */
    suspend fun refreshRecipes(query: String) {
        val fresh = api.recipes(query = query)
        dao.upsertAll(fresh.map { it.toEntity() })
    }

    /** 一道菜：基本信息 + 详情。详情还没缓存过时，是 null */
    fun observeRecipe(id: Int): Flow<Recipe?> =
        combine(dao.observe(id), dao.observeDetail(id)) { recipe, detail ->
            if (recipe == null || detail == null) null
            else recipe.toRecipe().copy(
                ingredients = json.decodeFromString(ingredientsSerializer, detail.ingredientsJson)
                    .map { Ingredient(it.name, it.amount, it.unit) },
                steps = json.decodeFromString(stepsSerializer, detail.stepsJson),
            )
        }

    suspend fun refreshRecipe(id: Int) {
        val dto = api.recipe(id)
        dao.upsertAll(listOf(dto.toEntity()))
        dao.upsertDetail(
            RecipeDetailEntity(
                id = dto.id,
                ingredientsJson = json.encodeToString(ingredientsSerializer, dto.ingredients),
                stepsJson = json.encodeToString(stepsSerializer, dto.steps),
            )
        )
    }

    /** 做饭模式用：先试着刷新；刷新失败也没关系，只要冷库里有就行 */
    suspend fun recipe(id: Int): Recipe {
        try {
            refreshRecipe(id)
        } catch (e: IOException) {
            // 离线：用缓存
        }
        return observeRecipe(id).first() ?: throw IOException("没有网络，也没有缓存")
    }

    private companion object {
        val json = Json
        val ingredientsSerializer = ListSerializer(IngredientDto.serializer())
        val stepsSerializer = ListSerializer(String.serializer())
    }
}

private fun RecipeSummaryDto.toEntity() = RecipeEntity(id, title, subtitle, category, minutes, difficulty, rating, BASE_URL + imageUrl.removePrefix("/"))

private fun RecipeDto.toEntity() = RecipeEntity(id, title, subtitle, category, minutes, difficulty, rating, BASE_URL + imageUrl.removePrefix("/"))

private fun RecipeEntity.toRecipe() = Recipe(id, title, subtitle, category, minutes, difficulty, rating, imageUrl = imageUrl)
