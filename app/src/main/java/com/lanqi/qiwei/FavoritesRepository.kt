package com.lanqi.qiwei

import kotlinx.coroutines.flow.Flow

/** 收藏仓库：收藏只存在手机上（冷库），不经过网络。 */
class FavoritesRepository(private val dao: FavoriteDao) {
    val favorites: Flow<List<FavoriteEntity>> = dao.observeAll()

    fun isFavorite(recipeId: Int): Flow<Boolean> = dao.isFavorite(recipeId)

    suspend fun setFavorite(recipe: Recipe, favorite: Boolean) {
        if (favorite) {
            dao.insert(FavoriteEntity(recipe.id, recipe.title, recipe.imageUrl, System.currentTimeMillis()))
        } else {
            dao.delete(recipe.id)
        }
    }
}
