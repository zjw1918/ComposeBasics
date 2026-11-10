package com.lanqi.qiwei

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lanqi.qiwei.ui.theme.QiweiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QiweiTheme {
                HomeScreen()
            }
        }
    }
}

/** 5000 道菜（示例数据，第 11 期换成从后端取）。 */
val allRecipes = generateRecipes(5000)

/** 首页：搜索框 + 分类 + 菜谱列表。两个状态都放在首页这一层：状态往下传，事件往上传。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("全部") }
    // 点了「不感兴趣」的菜
    val hidden = remember { mutableStateListOf<Int>() }
    val shown = allRecipes.filter {
        it.id !in hidden && it.title.contains(query) && (category == "全部" || it.category == category)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("鳍味") }) }
    ) { innerPadding ->
        // 实验：同一份数据，换成两列网格。item / items 的写法和 LazyColumn 一模一样
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(8.dp),
        ) {
            item(key = "header", span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    SearchBox(query = query, onQueryChange = { query = it })
                    CategoryChips(selected = category, onSelect = { category = it })
                }
            }
            items(shown, key = { it.id }) { recipe ->
                RecipeTile(recipe = recipe)
            }
        }
    }
}

/** 网格里的一格：竖着排的小卡片。 */
@Composable
fun RecipeTile(recipe: Recipe, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.padding(6.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = recipe.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(
                text = "${recipe.minutes} 分钟 · ★ ${recipe.rating}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
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
fun RecipeCard(recipe: Recipe, onHide: () -> Unit, modifier: Modifier = Modifier) {
    // 份数的状态，提升到了卡片这一层
    var servings by rememberSaveable { mutableStateOf(2) }
    // 收藏：先记在卡片里（第 13 期存进数据库）
    var liked by remember { mutableStateOf(false) }
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 盘子：先用一个圆形色块占位，第 11 期换成真图片
            Box(
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
                IconToggleButton(checked = liked, onCheckedChange = { liked = it }) {
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
fun HomeScreenPreview() {
    QiweiTheme {
        HomeScreen()
    }
}
