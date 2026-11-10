package com.lanqi.qiwei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lanqi.qiwei.data.SettingsRepository
import com.lanqi.qiwei.data.ThemeMode
import com.lanqi.qiwei.navigation.QiweiNavigation
import com.lanqi.qiwei.ui.theme.QiweiTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // 字段注入：Activity 由系统创建，没法用构造函数，就让 Hilt 在 onCreate 时把它填上
    @Inject lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 主题设置从 DataStore 读出来：设置一改，整个 App 立刻换装
            val themeMode by settings.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            QiweiTheme(
                darkTheme = when (themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }
            ) {
                QiweiNavigation()
            }
        }
    }
}
