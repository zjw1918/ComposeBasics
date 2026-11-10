package com.lanqi.qiwei.di

import android.content.Context
import android.util.Log
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lanqi.qiwei.data.FavoriteDao
import com.lanqi.qiwei.data.MyRecipeDao
import com.lanqi.qiwei.data.QiweiApi
import com.lanqi.qiwei.data.QiweiDatabase
import com.lanqi.qiwei.data.RecipeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

/**
 * 鳍味后端跑在电脑的 8080 端口上。开发时用 `adb reverse tcp:8080 tcp:8080` 把手机（或模拟器）自己的 8080 端口
 * 接到电脑上，所以 App 访问的是 localhost。
 */
const val BASE_URL = "http://localhost:8080/"

/**
 * 登记表：那些「没法在构造函数上写 @Inject」的东西（别人的类、要用 Builder 造的对象）怎么造。
 * @Singleton：整个 App 只造一个，谁要都给同一个。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor { Log.d("Qiwei-HTTP", it) }.apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    @Provides
    @Singleton
    fun provideApi(okHttp: OkHttpClient): QiweiApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QiweiDatabase =
        Room.databaseBuilder<QiweiDatabase>(context, "qiwei.db")
            .setDriver(BundledSQLiteDriver())
            .build()

    @Provides
    fun provideFavoriteDao(db: QiweiDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideRecipeDao(db: QiweiDatabase): RecipeDao = db.recipeDao()

    @Provides
    fun provideMyRecipeDao(db: QiweiDatabase): MyRecipeDao = db.myRecipeDao()
}
