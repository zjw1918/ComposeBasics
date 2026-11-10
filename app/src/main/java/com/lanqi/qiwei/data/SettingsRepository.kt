package com.lanqi.qiwei.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 主题：跟随系统、浅色、深色。 */
enum class ThemeMode(val label: String) { SYSTEM("跟随系统"), LIGHT("浅色"), DARK("深色") }

// 整个 App 只有一个设置文件（/data/data/com.lanqi.qiwei/files/datastore/settings.preferences_pb）
private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** 设置：几个小小的键值，用 DataStore 存在手机上。杀掉 App 再打开，还在。 */
class SettingsRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")

    /** 读：一条流。设置一改，流里马上来一个新值 */
    val themeMode: Flow<ThemeMode> = context.settingsStore.data.map { prefs ->
        prefs[themeKey]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
    }

    /** 写：suspend 函数，写磁盘的活儿 DataStore 自己放到后台去做 */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsStore.edit { prefs -> prefs[themeKey] = mode.name }
    }
}
