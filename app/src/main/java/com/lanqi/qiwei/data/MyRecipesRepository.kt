package com.lanqi.qiwei.data

import android.util.Log
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

/** 我的菜谱：存在本机数据库里；每次保存后，顺手往后端备份一份。 */
@Singleton
class MyRecipesRepository @Inject constructor(private val dao: MyRecipeDao, private val api: QiweiApi) {
    val recipes: Flow<List<MyRecipeEntity>> = dao.observeAll()

    suspend fun get(id: Long): MyRecipeEntity? = dao.get(id)

    /** 新建（id = 0）或更新。先存本机，再备份 */
    suspend fun save(recipe: MyRecipeEntity) {
        val stamped = recipe.copy(updatedAt = System.currentTimeMillis())
        if (stamped.id == 0L) dao.insert(stamped) else dao.update(stamped)
        try {
            api.backup(MyRecipeDto(stamped.title, stamped.minutes, stamped.ingredients, stamped.steps))
        } catch (e: IOException) {
            // 备份失败就算了：本机已经存好了（离线时的备份怎么补发，进阶篇讲）
            Log.d("Qiwei", "备份失败：${e.message}")
        } catch (e: HttpException) {
            Log.d("Qiwei", "备份失败：${e.code()}")
        }
    }

    suspend fun delete(recipe: MyRecipeEntity) = dao.delete(recipe)

    /** 撤销删除：原样插回去（id 不变） */
    suspend fun restore(recipe: MyRecipeEntity) {
        dao.insert(recipe)
    }
}
