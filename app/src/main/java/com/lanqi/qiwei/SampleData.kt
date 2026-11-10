package com.lanqi.qiwei

import kotlin.random.Random

private val flavors = listOf("家常", "川味", "港式", "老妈", "秘制", "江南", "东北", "湘味", "潮汕", "西北")
private val methods = listOf(
    "红烧", "清蒸", "爆炒", "干煸", "糖醋", "酱焖", "椒盐", "蒜蓉", "葱油", "香煎",
    "炖", "卤", "白灼", "油焖", "啫啫", "凉拌", "小炒", "酸汤", "黑椒", "孜然",
)
private val mains = listOf(
    "鸡翅", "排骨", "牛腩", "鱼块", "虾仁", "豆腐", "茄子", "土豆", "五花肉", "鸡腿",
    "羊排", "带鱼", "花菜", "藕片", "鸡蛋", "肥肠", "鸭腿", "豆角", "杏鲍菇", "牛肉",
    "鱿鱼", "猪蹄", "冬瓜", "腐竹", "蛤蜊",
)

/**
 * 生成 count 道示例菜：前 8 道是手写的经典菜，后面按「风味 + 做法 + 主料」组合（10 × 20 × 25 = 5000 种）。
 * 用固定的随机种子，每次生成的结果都一样。
 */
fun generateRecipes(count: Int): List<Recipe> {
    val random = Random(seed = 42)
    val generated = flavors.flatMap { flavor ->
        methods.flatMap { method -> mains.map { main -> "$flavor$method$main" } }
    }.map { title ->
        Recipe(
            id = 0,
            title = title,
            subtitle = "第 ${random.nextInt(1, 99)} 次做就成功",
            category = categories[random.nextInt(1, categories.size)],
            minutes = random.nextInt(2, 19) * 5,
            difficulty = listOf("简单", "中等", "较难")[random.nextInt(3)],
            rating = (35 + random.nextInt(15)) / 10.0,
        )
    }
    return (sampleRecipes + generated).take(count).mapIndexed { i, r -> r.copy(id = i + 1) }
}
