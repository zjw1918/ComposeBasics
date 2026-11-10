package com.lanqi.qiwei.di

import com.lanqi.qiwei.data.FavoritesRepository
import com.lanqi.qiwei.data.OfflineFirstRecipeRepository
import com.lanqi.qiwei.data.RecipeRepository
import com.lanqi.qiwei.data.RoomFavoritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** 接口 → 实现的对应表：谁要 RecipeRepository，就给它 OfflineFirstRecipeRepository。 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindRecipeRepository(impl: OfflineFirstRecipeRepository): RecipeRepository

    @Binds
    abstract fun bindFavoritesRepository(impl: RoomFavoritesRepository): FavoritesRepository
}
