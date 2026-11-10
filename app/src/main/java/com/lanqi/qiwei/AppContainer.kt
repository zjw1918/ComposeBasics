package com.lanqi.qiwei

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

/**
 * 鳍味后端跑在电脑的 8080 端口上。开发时用 `adb reverse tcp:8080 tcp:8080` 把手机（或模拟器）自己的 8080 端口
 * 接到电脑上，所以 App 访问的是 localhost。真机用 USB 连着也一样能用。
 */
const val BASE_URL = "http://localhost:8080/"

/**
 * 手动依赖注入：整个 App 共用的对象，在这里各创建一次。
 * 谁需要仓库，就从这里拿，而不是自己 new 一个。第 17 期换成 Hilt。
 */
class AppContainer(context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    // OkHttp：真正收发 HTTP 的那一层。日志拦截器把每个请求打印到 Logcat
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor { Log.d("Qiwei-HTTP", it) }.apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    // Retrofit：把 QiweiApi 接口变成发请求的代码；JSON 用 kotlinx.serialization 解析
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val settingsRepository = SettingsRepository(context)

    // 数据库文件：/data/data/com.lanqi.qiwei/databases/qiwei.db
    private val database = Room.databaseBuilder<QiweiDatabase>(context, "qiwei.db")
        .setDriver(BundledSQLiteDriver())
        .build()

    private val api = retrofit.create<QiweiApi>()

    val recipeRepository = RecipeRepository(api, database.recipeDao())

    val myRecipesRepository = MyRecipesRepository(database.myRecipeDao(), api)

    val favoritesRepository = FavoritesRepository(database.favoriteDao())
}

/** App 一启动就创建的 Application 对象：容器挂在它身上，整个 App 只有一份。 */
class QiweiApplication : Application() {
    val container by lazy { AppContainer(this) }
}

/** 在 Composable 里拿到容器。 */
@Composable
fun appContainer(): AppContainer = (LocalContext.current.applicationContext as QiweiApplication).container
