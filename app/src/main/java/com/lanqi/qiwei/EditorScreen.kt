package com.lanqi.qiwei

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** 新建 / 编辑一道自己的菜。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(viewModel: EditorViewModel, isNew: Boolean, onDone: () -> Unit) {
    val form = viewModel.form
    // 保存完就返回（「保存成功」这件事先记成一个状态，📌 第 15 期的便签）
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(viewModel.saved) {
        if (viewModel.saved) {
            keyboard?.hide()
            onDone()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "新建菜谱" else "编辑菜谱") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Filled.Close, contentDescription = "不保存，关闭") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = form.title,
                onValueChange = { viewModel.onFormChange(form.copy(title = it)) },
                label = { Text("菜名") },
                isError = viewModel.titleError != null,
                supportingText = { viewModel.titleError?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.minutes,
                onValueChange = { viewModel.onFormChange(form.copy(minutes = it.filter(Char::isDigit))) },
                label = { Text("要多少分钟") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.ingredients,
                onValueChange = { viewModel.onFormChange(form.copy(ingredients = it)) },
                label = { Text("食材（每行一样）") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.steps,
                onValueChange = { viewModel.onFormChange(form.copy(steps = it)) },
                label = { Text("步骤（每行一步）") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            // 保存中：按钮不能再点，换成转圈
            Button(onClick = viewModel::save, enabled = !viewModel.saving, modifier = Modifier.fillMaxWidth()) {
                if (viewModel.saving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("保存")
            }
        }
    }
}
