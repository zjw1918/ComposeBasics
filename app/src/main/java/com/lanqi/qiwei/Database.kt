package com.lanqi.qiwei

import androidx.room3.AutoMigration
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.Upsert
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

    /** 写：suspend 函数。Room 自己切到后台线程去写；要是写成普通函数，在主线程上一调用就崩 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE recipeId = :recipeId")
    suspend fun delete(recipeId: Int)
}

/** 菜谱缓存：从后端拿到的列表，先存进来。界面只读这张表 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val minutes: Int,
    val difficulty: String,
    val rating: Double,
    val imageUrl: String,
)

/** 菜谱详情的缓存：食材和步骤（存成 JSON 文本），打开过的菜才有 */
@Entity(tableName = "recipe_details")
data class RecipeDetailEntity(
    @PrimaryKey val id: Int,
    val ingredientsJson: String,
    val stepsJson: String,
)

@Dao
interface RecipeDao {
    /** 按菜名搜。LIKE '%番茄%'：菜名里有「番茄」就算 */
    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' ORDER BY rating DESC")
    fun search(query: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observe(id: Int): Flow<RecipeEntity?>

    @Query("SELECT * FROM recipe_details WHERE id = :id")
    fun observeDetail(id: Int): Flow<RecipeDetailEntity?>

    /** 有就更新，没有就插入 */
    @Upsert
    suspend fun upsertAll(recipes: List<RecipeEntity>)

    @Upsert
    suspend fun upsertDetail(detail: RecipeDetailEntity)
}

/**
 * 数据库：列出有哪些表、版本号是几。
 * 这一期加了两张表，所以版本从 1 升到 2；AutoMigration 让 Room 对照导出的 schema，自动生成升级的 SQL，
 * 已经装了旧版的手机，收藏不会丢。
 */
@Database(
    entities = [FavoriteEntity::class, RecipeEntity::class, RecipeDetailEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class QiweiDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recipeDao(): RecipeDao
}
