# 鳍味 · Compose 基础教程配套代码

B 站视频教程「Compose 下厨记」（Jetpack Compose 入门，18 期）的全部代码。
从 Android Studio 新建的空工程开始，一期一期写成一个能上架的菜谱 App「鳍味」：5000 道菜的列表、详情页、收藏、
联网、离线、动画、依赖注入、单元测试、发布版。

📺 **视频合集：[Compose基础教程](https://space.bilibili.com/22241645/lists/9546799?type=season)**（B 站 · 蓝鳍金枪鱼维维）

## 怎么跟着学

仓库的每一步都是一个提交，并且打了 tag：

- `b08`：第 8 期写完时的样子（每一期的终点）。
- `b08.1`、`b08.2`……：这一期里的每一小步，和视频里「写一写」的顺序一致。
- `b00`：Android Studio 新建项目的原样，第 1 期从这里开始。

```bash
git clone https://github.com/zjw1918/ComposeBasics.git
cd ComposeBasics

git checkout b07             # 想从第 8 期开始跟着写：先回到第 7 期的终点
git log --oneline b07..b08   # 第 8 期一共写了哪几步
git diff b08.1 b08.2         # 某一步具体改了什么
git checkout main            # 回到最终版
```

建议的用法：看一期视频，自己在上一期的终点上照着写；写不出来或跑不起来时，再和这一期的 tag 对比。

### 片中的「翻车实验」

名字里带 `-` 的 tag 是视频里故意演示的错误或对比，**不在主线上**。有的编译不过、有的一跑就崩，这是故意的：

| tag | 演示什么 |
|---|---|
| `b02.4-fill` | `fillMaxWidth` 换成 `fillMaxSize`：一张卡片撑满整个屏幕 |
| `b03.4-dynamic` | 打开动态取色（Android 12+），主题颜色跟着壁纸走 |
| `b04.3-noimport` | `by` 委托少了 `getValue` / `setValue` 两行 import：编译不过 |
| `b04.5-log` | 每个函数执行时打一行日志，看点一下加号到底重跑了谁 |
| `b04.6-saver` | `rememberSaveable` 里放自己写的 data class：一启动就崩 |
| `b04.6-saver-fix` | 给它一个 `Saver`：拆开存，取回来再装好 |
| `b05.3-frozen` | `onQueryChange` 里忘了更新状态：怎么敲，输入框都是空的 |
| `b06.5-grid` | 同一份数据换成两列 `LazyVerticalGrid` |
| `b11.3-lan` | 直接访问 `10.0.2.2`：Android 17 的本地网络保护拦下连接，10 秒后超时 |
| `b13.2-blocking` | DAO 的写函数去掉 `suspend`：编译能过，点收藏时崩溃 |
| `b17.3-nodebounce` | 删掉 `debounce(300)`：单元测试当场抓住 |

## 18 期一览

| 期 | 视频 | 学什么 | 代码 |
|---|---|---|---|
| 01 | [一个函数，就是一块屏幕](https://www.bilibili.com/video/BV1twxd6zE2Y) | 第一个 Composable、预览 | `b01` |
| 02 | [Modifier 的顺序，是一道菜的做法](https://www.bilibili.com/video/BV1Dwxd6zEur) | Column / Row / Box、Modifier 的顺序 | `b02` |
| 03 | [给 App 穿上制服](https://www.bilibili.com/video/BV1F4W16dEC2) | Material 3 主题、深色模式、Scaffold | `b03` |
| 04 | [我点了，它没变](https://www.bilibili.com/video/BV1DKW16uE7n) | 状态与重组：`remember`、`rememberSaveable` | `b04` |
| 05 | [状态往下，事件往上](https://www.bilibili.com/video/BV1WJW168Enr) | 状态提升、单向数据流：搜索框和分类 | `b05` |
| 06 | [五千道菜，一块屏幕](https://www.bilibili.com/video/BV1ogWy62EKw) | 懒列表：`key`、`contentType`、粘性标题 | `b06` |
| 07 | [返回栈只是一个列表](https://www.bilibili.com/video/BV14pWQ6QE33) | Navigation 3：返回栈、详情页、底部三个标签 | `b07` |
| 08 | [值班经理不下班](https://www.bilibili.com/video/BV1XoW96TEq2) | ViewModel、`StateFlow`、`UiState`、`SavedStateHandle` | `b08` |
| 09 | [suspend 不是「去后台」](https://www.bilibili.com/video/BV11mWX6WEby) | 协程：`suspend`、`delay`、`withContext(Dispatchers.Default)` | `b09` |
| 10 | [主厨一声收工](https://www.bilibili.com/video/BV1DrWR6KEme) | 结构化并发与取消：`Job`、`viewModelScope`、`LaunchedEffect` | `b10` |
| 11 | [去鱼市进货](https://www.bilibili.com/video/BV1wsWo6dEwF) | 网络：Retrofit、OkHttp、kotlinx.serialization、Coil；加载 / 出错 / 重试 | `b11` |
| 12 | [传送带](https://www.bilibili.com/video/BV1QTWB6sEpD) | Flow：`debounce`、`flatMapLatest`、`stateIn`，边打字边搜 | `b12` |
| 13 | 冷库 | DataStore 设置、Room 3 收藏 | `b13` |
| 14 | 冷库才是唯一的真相 | 离线优先：界面只读数据库、下拉刷新 | `b14` |
| 15 | 我的菜谱 | 增删改查、表单校验、撤销 | `b15` |
| 16 | 让它活过来 | 动画：`animate*AsState`、`AnimatedVisibility`、`AnimatedContent`、滑动删除、页面转场 | `b16` |
| 17 | 请人来帮忙 | 按功能分包、Hilt、第一个单元测试 | `b17` |
| 18 | 鳍味 1.0 | 发布版：R8、签名 | `b18` |

没有链接的几期视频还在陆续更新，代码已经全部写好。

## 运行

需要：Android Studio（AGP 9.5、Kotlin 2.4.21，JDK 用 Studio 自带的就行），一台 Android 7.0（API 24）以上的手机或模拟器。

1. 用 Android Studio 打开仓库根目录，等 Gradle 同步完，运行 `app`。
2. **第 11 期起**，App 要连「鳍味后端」：一个单独的小服务器，在 [qiwei-server](https://github.com/zjw1918/qiwei-server) 仓库。
   到它的 [Releases](https://github.com/zjw1918/qiwei-server/releases) 下载对应系统的压缩包，解压后运行（不用装 Java）：
   ```bash
   ./qiwei-server                   # 启动后端（8080 端口）；Windows：qiwei-server.exe
   adb reverse tcp:8080 tcp:8080    # 把手机 / 模拟器的 8080 端口接到电脑上
   ```
   浏览器打开 http://localhost:8080 是后端的故障控制台：调延迟、调错误率、直接让它挂掉，视频里用它演示慢网、出错和离线。
3. **第 17 期起**有单元测试：`./gradlew :app:testDebugUnitTest`。
4. **第 18 期**的发布版：在根目录放 `keystore.properties`（`storeFile`、`storePassword`、`keyAlias`、`keyPassword`）和对应的 `.jks` 签名文件，
   再 `./gradlew :app:assembleRelease`。这两个文件是你自己的，不要提交进 git（`.gitignore` 已经排除）。

## 最终的项目结构（b18）

```
app/src/main/java/com/lanqi/qiwei/
├── MainActivity.kt、QiweiApplication.kt
├── navigation/        路由键、底部标签、NavDisplay
├── feature/           home、detail、cook、favorites、mine、settings：每个功能的界面 + ViewModel
├── data/              仓库（接口 + 实现）、Room、DataStore、Retrofit 接口
├── model/             Recipe、Ingredient
├── di/                Hilt 登记表
└── ui/                主题、共用组件
```

## 许可

[MIT](LICENSE)：随便用、随便改，保留版权声明就行。
