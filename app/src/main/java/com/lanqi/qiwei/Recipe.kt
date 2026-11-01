package com.lanqi.qiwei

/** 一道菜的数据。只描述“是什么”，不管怎么显示。 */
data class Recipe(
    val id: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val minutes: Int,
    val difficulty: String,
    val rating: Double,
)

val categories = listOf("全部", "家常", "快手", "汤", "硬菜")

/** 示例数据：第 11 期换成从后端取。 */
val sampleRecipes = listOf(
    Recipe(1, "番茄炒蛋", "十分钟上桌的国民家常菜", "家常", 10, "简单", 4.8),
    Recipe(2, "可乐鸡翅", "甜咸口，小朋友最爱", "家常", 30, "简单", 4.7),
    Recipe(3, "酸辣土豆丝", "脆爽开胃的下饭菜", "家常", 15, "中等", 4.6),
    Recipe(4, "葱油拌面", "一勺葱油，一碗面", "快手", 10, "简单", 4.5),
    Recipe(5, "蒜蓉西兰花", "三分钟出锅的绿色", "快手", 8, "简单", 4.4),
    Recipe(6, "紫菜蛋花汤", "最快的一锅汤", "汤", 10, "简单", 4.3),
    Recipe(7, "番茄牛腩汤", "小火慢炖九十分钟", "汤", 90, "中等", 4.9),
    Recipe(8, "红烧鱼块", "周末的硬菜", "硬菜", 40, "较难", 4.6),
)
