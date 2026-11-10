package com.lanqi.qiwei.data

import com.lanqi.qiwei.model.Recipe
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 收藏仓库：收藏只存在手机上（冷库），不经过网络。 */
interface FavoritesRepository {
    val favorites: Flow<List<FavoriteEntity>>
    val favoriteIds: Flow<Set<Int>>
    fun isFavorite(recipeId: Int): Flow<Boolean>
    suspend fun remove(recipeId: Int)
    suspend fun setFavorite(recipe: Recipe, favorite: Boolean)
}

@Singleton
class RoomFavoritesRepository @Inject constructor(private val dao: FavoriteDao) : FavoritesRepository {
    override val favorites: Flow<List<FavoriteEntity>> = dao.observeAll()

    /** 收藏了哪些菜（只要 id）：首页的心形用它 */
    override val favoriteIds: Flow<Set<Int>> = favorites.map { list -> list.map { it.recipeId }.toSet() }

    override fun isFavorite(recipeId: Int): Flow<Boolean> = dao.isFavorite(recipeId)

    override suspend fun remove(recipeId: Int) = dao.delete(recipeId)

    override suspend fun setFavorite(recipe: Recipe, favorite: Boolean) {
        if (favorite) {
            dao.insert(FavoriteEntity(recipe.id, recipe.title, recipe.imageUrl, System.currentTimeMillis()))
        } else {
            dao.delete(recipe.id)
        }
    }
}
