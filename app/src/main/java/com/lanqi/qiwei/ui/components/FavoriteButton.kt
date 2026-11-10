package com.lanqi.qiwei.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

/**
 * 收藏的心形。动画也是状态驱动的：liked 一变，目标值跟着变（大小 1 → 1.25，颜色 灰 → 红），
 * animate*AsState 每一帧把当前值往目标值推一点，推到为止。
 */
@Composable
fun FavoriteButton(liked: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (liked) 1.25f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        label = "心形大小"
    )
    val tint by animateColorAsState(
        targetValue = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "心形颜色"
    )
    IconToggleButton(checked = liked, onCheckedChange = { onToggle() }, modifier = modifier) {
        Icon(
            imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (liked) "取消收藏" else "收藏",
            tint = tint,
            modifier = Modifier.scale(scale)
        )
    }
}
