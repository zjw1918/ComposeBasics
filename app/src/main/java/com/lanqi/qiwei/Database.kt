package com.lanqi.qiwei

import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import kotlinx.coroutines.flow.Flow

/** 实体：数据库里的一张表。这一张存收藏，一行一道菜。 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val recipeId: Int,
    val title: String,
    val imageUrl: String,
    val addedAt: Long,
)

/** DAO：怎么读、怎么写这张表。SQL 写在注解里，Room 在编译时检查它、生成实现。 */
@Dao
interface FavoriteDao {
    /** 读：返回 Flow。表一变，Flow 里自动来一份新的列表 */
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE recipeId = :recipeId)")
    fun isFavorite(recipeId: Int): Flow<Boolean>

    /** 写：suspend 函数。Room 3 里，DAO 不是 suspend 就是返回 Flow，在主线程上同步查库，根本写不出来 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(favorite: FavoriteEntity)  // 实验：去掉 suspend

    @Query("DELETE FROM favorites WHERE recipeId = :recipeId")
    suspend fun delete(recipeId: Int)
}

/** 数据库：列出有哪些表、版本号是几。 */
@Database(entities = [FavoriteEntity::class], version = 1)
abstract class QiweiDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
