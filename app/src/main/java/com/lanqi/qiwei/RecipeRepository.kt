package com.lanqi.qiwei

/** 菜谱仓库：数据从鳍味后端取。界面和 ViewModel 只认识仓库，不知道后面是网络。 */
class RecipeRepository(private val api: QiweiApi) {
    suspend fun recipes(query: String = ""): List<Recipe> = api.recipes(query = query).map { it.toRecipe() }

    suspend fun recipe(id: Int): Recipe = api.recipe(id).toRecipe()
}
