package com.blackened.healthapp.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/*@Preview(showBackground = true)
@Composable
fun AddRecordBottomSheetPreview() {
    AddRecordBottomSheet(
        0L,
        viewModel() ,
        {

        }
    )
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordBottomSheet(
    viewModel: AddRecordViewModel = viewModel(),
    onDismiss: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (viewModel.editingRecordId != null) "Редактирование записи" else "Новая запись здоровья",
            fontSize = 20.sp,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            viewModel.availableMetrics.forEach { metric ->
                val isSelected = viewModel.selectedType == metric

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onTypeChanged(metric) },
                    label = { Text(metric.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.value1Text,
                onValueChange = { viewModel.onValue1Changed(it) },
                label = {
                    Text(if (viewModel.selectedType == MetricType.BLOOD_PRESSURE) "Систолическое" else "Значение")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            if (viewModel.selectedType == MetricType.BLOOD_PRESSURE) {
                OutlinedTextField(
                    value = viewModel.value2Text,
                    onValueChange = { viewModel.onValue2Changed(it) },
                    label = { Text("Диастолическое") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.noteText,
            onValueChange = { viewModel.onNoteChanged(it) },
            label = { Text("Примечание (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.saveRecord(/*targetId = targetId,*/ onRecordAdded = onDismiss)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    if (viewModel.editingRecordId != null) "Обновить запись" else "Добавить запись"
                )
            }
        }
    }
}