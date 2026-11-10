# 鳍味（ComposeBasics）

「Compose 下厨记」（Jetpack Compose 入门，18 期）的配套代码：从一个空工程开始，一步一步写成一个能上架的菜谱 App「鳍味」。
每一步是一个提交和一个 tag（`b06.2`），每一期的终点是 `bNN`。想从哪一期开始跟着写，就 `git checkout` 那一期的上一期。

## 运行

1. Android Studio（AGP 9.5、Kotlin 2.4.21、JDK 25 随 Studio 自带）打开本目录。
2. 第 11 期起 App 要连「鳍味后端」：
   ```bash
   ./gradlew -p server run                 # 启动后端（8080 端口）；浏览器打开 http://localhost:8080 是故障控制台
   adb reverse tcp:8080 tcp:8080           # 把手机 / 模拟器的 8080 端口接到电脑上
   ```
3. 运行 `app`。单元测试：`./gradlew :app:testDebugUnitTest`（第 17 期起）。
4. 发布版（第 18 期）：在根目录放 `keystore.properties`（`storeFile`、`storePassword`、`keyAlias`、`keyPassword`）和对应的 `.jks`，
   再 `./gradlew :app:assembleRelease`。这两个文件不进 git。

## 每一期

| 期 | tag | 写了什么 |
|---|---|---|
| 01 | `b01` | 第一个 Composable、预览 |
| 02 | `b02` | Column / Row / Box、Modifier 的顺序 |
| 03 | `b03` | Material 3 主题、深色模式、Scaffold |
| 04 | `b04` | 状态与重组：`remember`、`rememberSaveable` |
| 05 | `b05` | 状态提升、单向数据流：搜索框和分类 |
| 06 | `b06` | 懒列表：5000 道菜、`key`、`contentType`、粘性标题 |
| 07 | `b07` | Navigation 3：返回栈、详情页、底部三个标签 |
| 08 | `b08` | ViewModel、`StateFlow`、`UiState`、`SavedStateHandle` |
| 09 | `b09` | 协程：`suspend`、`delay`、`withContext(Dispatchers.Default)` |
| 10 | `b10` | 结构化并发与取消：计时器的 Job、`viewModelScope`、`LaunchedEffect` |
| 11 | `b11` | 网络：Retrofit、OkHttp、kotlinx.serialization、Coil；加载 / 出错 / 重试 |
| 12 | `b12` | Flow：`debounce`、`flatMapLatest`、`stateIn`——边打字边搜 |
| 13 | `b13` | DataStore 设置、Room 3 收藏 |
| 14 | `b14` | 离线优先：界面只读数据库、下拉刷新 |
| 15 | `b15` | 我的菜谱：增删改查、校验、撤销 |
| 16 | `b16` | 动画：`animate*AsState`、`AnimatedVisibility`、`AnimatedContent`、滑动删除、页面转场 |
| 17 | `b17` | 按功能分包、Hilt、第一个单元测试 |
| 18 | `b18` | 发布版：R8、签名——鳍味 1.0 |

带 `-` 的 tag（如 `b06.5-grid`、`b11.3-lan`、`b13.2-blocking`）是片中的实验，不在主线上。

## 结构（b18）

```
app/src/main/java/com/lanqi/qiwei/
├── MainActivity.kt、QiweiApplication.kt
├── navigation/        路由键、底部标签、NavDisplay
├── feature/           home、detail、cook、favorites、mine、settings：每个功能的界面 + ViewModel
├── data/              仓库（接口 + 实现）、Room、DataStore、Retrofit 接口
├── model/             Recipe、Ingredient
├── di/                Hilt 登记表
└── ui/                主题、共用组件
server/                鳍味后端（Ktor，独立的 Gradle 构建）
```
