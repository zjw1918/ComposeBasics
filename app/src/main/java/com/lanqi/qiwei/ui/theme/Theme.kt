package com.lanqi.qiwei.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FinBlueLight,
    onPrimary = FinBlueDeep,
    primaryContainer = FinBlueContainerDark,
    onPrimaryContainer = FinBlueContainer,
    secondary = TangerineLight,
    onSecondary = TangerineDeep,
    secondaryContainer = TangerineContainerDark,
    onSecondaryContainer = TangerineContainer,
    background = Night,
    onBackground = Snow,
    surface = Night,
    onSurface = Snow,
    onSurfaceVariant = SnowSoft,
    surfaceContainerLow = NightLow,
)

private val LightColorScheme = lightColorScheme(
    primary = FinBlue,
    onPrimary = Color.White,
    primaryContainer = FinBlueContainer,
    onPrimaryContainer = FinBlueDeep,
    secondary = Tangerine,
    onSecondary = Color.White,
    secondaryContainer = TangerineContainer,
    onSecondaryContainer = TangerineDeep,
    background = Rice,
    onBackground = Ink,
    surface = Rice,
    onSurface = Ink,
    onSurfaceVariant = InkSoft,
    surfaceContainerLow = RiceLow,
)

@Composable
fun QiweiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 动态取色（Android 12+ 跟随壁纸）。鳍味默认用自己的品牌色，想跟随壁纸时传 true
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}