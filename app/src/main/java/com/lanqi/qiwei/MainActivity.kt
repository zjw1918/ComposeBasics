package com.lanqi.qiwei

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lanqi.qiwei.ui.theme.QiweiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 主题设置从 DataStore 读出来：设置一改，整个 App 立刻换装
            val settings = appContainer().settingsRepository
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

/** 首页：搜索框 + 分类 + 菜谱列表。两个状态都放在首页这一层：状态往下传，事件往上传。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, onOpenRecipe: (Int) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    var category by rememberSaveable { mutableStateOf("全部") }
    // 点了「不感兴趣」的菜
    val hidden = remember { mutableStateListOf<Int>() }
    val shown = uiState.recipes.filter {
        it.id !in hidden && (category == "全部" || it.category == category)
    }
    // 一次性的提示：显示成屏幕底部的 Snackbar，显示完告诉 ViewModel「说过了」
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(message, actionLabel = "重试")
        viewModel.messageShown()
        if (result == SnackbarResult.ActionPerformed) viewModel.refresh()
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("鳍味") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        // 下拉刷新：手指往下拉，转圈，调用 refresh；列表本身一直在，不会被清空
        PullToRefreshBox(
            isRefreshing = uiState.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 懒列表：只组合、测量、绘制看得见的那几项；滑出去的项被回收，新滑进来的才组合
            LazyColumn {
                // 粘性标题：滑动时，搜索框和分类一直停在顶上
                stickyHeader(key = "header", contentType = "header") {
                    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        SearchBox(query = viewModel.query, onQueryChange = viewModel::onQueryChange)
                        CategoryChips(selected = category, onSelect = { category = it })
                        Text(
                            text = "共 ${shown.size} 道",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // key：每一项的身份是菜的 id，不是它排第几；contentType：同一种卡片才互相复用
                items(shown, key = { it.id }, contentType = { "recipe" }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        liked = recipe.id in favoriteIds,
                        onToggleLike = { viewModel.toggleFavorite(recipe) },
                        onClick = { onOpenRecipe(recipe.id) },
                        onHide = { hidden.add(recipe.id) }
                    )
                }
            }
        }
    }
}

/** 出错时的提示：一句人话 + 重试按钮。 */
@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("重试") }
    }
}

/** 搜索框：无状态。显示什么由 query 决定，用户打字时通过 onQueryChange 告诉外面。 */
@Composable
fun SearchBox(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("搜菜名，比如：番茄") },
        singleLine = true,
    )
}

/** 分类筛选：无状态。哪个被选中由 selected 决定，点击时通过 onSelect 告诉外面。 */
@Composable
fun CategoryChips(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { name ->
            FilterChip(
                selected = name == selected,
                onClick = { onSelect(name) },
                label = { Text(name) }
            )
        }
    }
}

/** 一张菜谱卡片：拿到一道菜的数据，描述它长什么样。 */
@Composable
fun RecipeCard(
    recipe: Recipe,
    liked: Boolean,
    onToggleLike: () -> Unit,
    onClick: () -> Unit,
    onHide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 份数的状态，提升到了卡片这一层
    var servings by rememberSaveable { mutableStateOf(2) }
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 盘子：Coil 在后台下载、解码、缓存图片，到了再显示；没到之前是底色
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = null,  // 装饰性的图：菜名就在旁边，读屏软件不用再念一遍
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = recipe.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = recipe.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${recipe.minutes} 分钟 · ${recipe.difficulty}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ServingsStepper(servings = servings, onServingsChange = { servings = it })
                    TextButton(onClick = onHide) { Text("不感兴趣") }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "★ ${recipe.rating}", style = MaterialTheme.typography.labelLarge)
                IconToggleButton(checked = liked, onCheckedChange = { onToggleLike() }) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (liked) "取消收藏" else "收藏",
                        tint = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


/** 份数加减器：无状态。份数从外面传进来，点了以后，通过 onServingsChange 告诉外面。 */
@Composable
fun ServingsStepper(servings: Int, onServingsChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { if (servings > 1) onServingsChange(servings - 1) }) { Text("−") }
        Text(text = "$servings 人份", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = { onServingsChange(servings + 1) }) { Text("+") }
    }
}

@Preview(name = "加减器 · 1 人份", showBackground = true)
@Composable
fun ServingsStepperOnePreview() {
    QiweiTheme { ServingsStepper(servings = 1, onServingsChange = {}) }
}

@Preview(name = "加减器 · 12 人份", showBackground = true)
@Composable
fun ServingsStepperManyPreview() {
    QiweiTheme { ServingsStepper(servings = 12, onServingsChange = {}) }
}

@Preview(name = "浅色", showBackground = true)
@Preview(name = "深色", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecipeCardPreview() {
    QiweiTheme {
        RecipeCard(recipe = sampleRecipes.first(), liked = true, onToggleLike = {}, onClick = {}, onHide = {})
    }
}
