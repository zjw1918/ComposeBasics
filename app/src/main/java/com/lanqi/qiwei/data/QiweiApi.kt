package com.lanqi.qiwei.data

import com.lanqi.qiwei.di.BASE_URL
import com.lanqi.qiwei.model.Ingredient
import com.lanqi.qiwei.model.Recipe
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** 鳍味后端的接口：每个函数对应一个网址。Retrofit 照着这个接口，生成真正发请求的代码。 */
interface QiweiApi {
    /** 菜谱列表；query 不为空时，只返回菜名里有这几个字的 */
    @GET("recipes")
    suspend fun recipes(@Query("query") query: String = "", @Query("category") category: String = ""): List<RecipeSummaryDto>

    @GET("recipes/{id}")
    suspend fun recipe(@Path("id") id: Int): RecipeDto

    /** 把「我的菜谱」备份一份到后端 */
    @POST("my-recipes")
    suspend fun backup(@Body recipe: MyRecipeDto): BackupResult
}

@Serializable
data class MyRecipeDto(val title: String, val minutes: Int, val ingredients: String, val steps: String)

@Serializable
data class BackupResult(val count: Int)

// DTO：和服务器 JSON 一一对应的数据类。服务器怎么写，它就怎么写
@Serializable
data class RecipeSummaryDto(
    val id: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val minutes: Int,
    val difficulty: String,
    val rating: Double,
    val imageUrl: String,
)

@Serializable
data class IngredientDto(val name: String, val amount: Double, val unit: String)

@Serializable
data class RecipeDto(
    val id: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val minutes: Int,
    val difficulty: String,
    val rating: Double,
    val imageUrl: String,
    val ingredients: List<IngredientDto>,
    val steps: List<String>,
)

// 映射：DTO → App 自己的模型。服务器改了字段名，只用改这里，界面一行都不用动
fun RecipeSummaryDto.toRecipe() = Recipe(id, title, subtitle, category, minutes, difficulty, rating, imageUrl = BASE_URL + imageUrl.removePrefix("/"))

fun RecipeDto.toRecipe() = Recipe(
    id, title, subtitle, category, minutes, difficulty, rating,
    imageUrl = BASE_URL + imageUrl.removePrefix("/"),
    ingredients = ingredients.map { Ingredient(it.name, it.amount, it.unit) },
    steps = steps,
)
