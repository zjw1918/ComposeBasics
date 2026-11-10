package com.lanqi.qiwei

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/** 表单里的四个格子。 */
data class EditorForm(
    val title: String = "",
    val minutes: String = "",
    val ingredients: String = "",
    val steps: String = "",
)

/** 编辑页的 ViewModel：表单放在这里——旋转屏幕不丢，界面只管显示和转告输入。 */
class EditorViewModel(private val recipeId: Long?, private val repository: MyRecipesRepository) : ViewModel() {
    var form by mutableStateOf(EditorForm())
        private set

    /** 正在保存：按钮变灰，再点也没用 */
    var saving by mutableStateOf(false)
        private set

    /** 菜名的错误提示；null = 没问题 */
    var titleError by mutableStateOf<String?>(null)
        private set

    /** 保存完了：界面看到它变成 true，就返回上一页 */
    var saved by mutableStateOf(false)
        private set

    init {
        // 编辑已有的菜：把它读出来填进表单
        if (recipeId != null) {
            viewModelScope.launch {
                repository.get(recipeId)?.let { form = EditorForm(it.title, it.minutes.toString(), it.ingredients, it.steps) }
            }
        }
    }

    fun onFormChange(newForm: EditorForm) {
        if (newForm.title != form.title) titleError = null
        form = newForm
    }

    fun save() {
        if (saving) return  // 已经在存了：第二下点击直接忽略
        if (form.title.isBlank()) {
            titleError = "菜名不能为空"
            return
        }
        saving = true
        viewModelScope.launch {
            try {
                repository.save(
                    MyRecipeEntity(
                        id = recipeId ?: 0,
                        title = form.title.trim(),
                        minutes = form.minutes.toIntOrNull() ?: 0,
                        ingredients = form.ingredients.trim(),
                        steps = form.steps.trim(),
                        updatedAt = 0,
                    )
                )
                saved = true
            } finally {
                saving = false
            }
        }
    }
}
