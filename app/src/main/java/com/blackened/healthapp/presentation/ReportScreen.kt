package com.blackened.healthapp.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import com.blackened.healthapp.data.HealthRecordResponseDTO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.graphics.Paint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportViewModel = viewModel()
) {

    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Отчёты и аналитика") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },

        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                val context = LocalContext.current

                Button(
                    onClick = { viewModel.exportAndShareExcel(context)},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Сформировать и отправить в Excel", fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            //TYPES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricType.entries.forEach { metric ->
                    val isSelected = viewModel.selectedType == metric
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.onTypeChanged(metric)
                            viewModel.loadReport()
                        },
                        label = { Text(metric.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            //DATES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //FROM
                OutlinedTextField(
                    value = viewModel.dateFrom?.toString()
                        ?: "30 дней назад",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Дата С") },
                    trailingIcon = {
                        IconButton(onClick = { showFromDatePicker = true}) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Выбрать дату"
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)

                )
                //TO
                OutlinedTextField(
                    value = viewModel.dateTo?.toString() ?: "Сегодня", // Если null — пишем дефолт
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Дата По") },
                    trailingIcon = {
                        IconButton(onClick = { showToDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Выбрать дату"
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.loadReport() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Построить график")
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 40.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.reportRecords.isEmpty()) {
                        Text(
                            text = "Нет данных за выбранный период.\nНажмите «Построить график»",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.wrapContentSize(),
                            lineHeight = 20.sp
                        )
                    } else {
                        //PRINT
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Динамика показателя: ${viewModel.selectedType.label}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            val isBP = viewModel.selectedType == MetricType.BLOOD_PRESSURE

                            HealthTrendChart(
                                records = viewModel.reportRecords,
                                isBloodPressure = isBP,
                                onPointClick = { record -> viewModel.showRecordDetails(record) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            )

                            val clickedRecord = viewModel.activeClickedRecord
                            if (clickedRecord != null) {
                                AlertDialog(
                                    onDismissRequest = { viewModel.dismissRecordDetails() },
                                    title = {
                                        val rusName = MetricType.entries.find { it.serverName == clickedRecord.type }?.label ?: "Замер"
                                        Text("$rusName от ${formatServerDate(clickedRecord.timestamp)}")
                                    },
                                    text = {
                                        Column {
                                            val valueStr = if (clickedRecord.value2 != null) {
                                                "${clickedRecord.value1} / ${clickedRecord.value2}"
                                            }
                                            else {
                                                "${clickedRecord.value1}"
                                            }

                                            Text(text = "Показатель: $valueStr", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text(text = "Автор: ${clickedRecord.userName}", fontSize = 14.sp)

                                            Spacer(modifier = Modifier.height(8.dp))

                                            val noteContent = if (clickedRecord.note.isNullOrBlank()) "Примечание отсутствует." else clickedRecord.note
                                            Text(text = "Примечание: $noteContent", fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { viewModel.dismissRecordDetails() }) {
                                            Text("Закрыть")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showFromDatePicker) {

        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {

                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {

                            val localDate = java.time.Instant.ofEpochMilli(selectedMillis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()

                            viewModel.onDateFromChanged(localDate)
                        }
                        showFromDatePicker = false
                    }
                ) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showToDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val localDate = java.time.Instant.ofEpochMilli(selectedMillis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()

                            viewModel.onDateToChanged(localDate)
                        }
                        showToDatePicker = false
                    }
                ) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun HealthTrendChart(
    records: List<HealthRecordResponseDTO>,
    isBloodPressure: Boolean,
    modifier: Modifier = Modifier,
    onPointClick: (HealthRecordResponseDTO) -> Unit
) {
    if (records.size < 2) return


    val maxValue = if (isBloodPressure) {
        records.maxOf { maxOf(it.value1, it.value2 ?: 0.0) }
    } else {
        records.maxOf { it.value1 }
    }

    val minValue = if (isBloodPressure) {
        records.minOf { minOf(it.value1, it.value2 ?: it.value1) }
    } else {
        records.minOf { it.value1 }
    }

    val valueDelta = (maxValue - minValue).coerceAtLeast(1.0)
    val density = LocalDensity.current
    val stepX = with(density) { 60.dp.toPx() }

    val paddingLeft = 55f
    val paddingBottom = 80f
    val paddingTop = 40f
    val paddingRight = 60f

    val totalChartWidth = (records.size - 1) * stepX + paddingRight

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = Color(0xFF2E7D32)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    val textPaint = Paint().apply {
        color = textColor
        textSize = 28f
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
    }

    val datePaint = Paint().apply {
        color = textColor
        textSize = 28f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    Row(modifier = modifier.fillMaxWidth().height(280.dp)) {

        Canvas(modifier = Modifier.width(32.dp).fillMaxHeight()) {
            val canvasHeight = size.height - paddingBottom - paddingTop
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = paddingTop + (canvasHeight / gridLines) * i
                val gridValue = maxValue - ((maxValue - minValue) / gridLines) * i
                val formattedValue = String.format("%.1f", gridValue)

                drawContext.canvas.nativeCanvas.drawText(
                    formattedValue,
                    size.width - 2f,
                    y + 10f,
                    textPaint
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(rememberScrollState())
        ) {
            Canvas(
                modifier = Modifier
                    .width(with(density) { totalChartWidth.toDp() })
                    .fillMaxHeight()
                    .pointerInput(records) {
                        detectTapGestures { tapOffset ->
                            val clickRadius = 30f

                            records.forEachIndexed { index, record ->
                                val x = index * stepX
                                val normY1 = (record.value1 - minValue) / valueDelta
                                val canvasHeight = size.height - paddingBottom - paddingTop
                                val y1 = paddingTop + canvasHeight - (normY1 * canvasHeight).toFloat()

                                val distance1 = sqrt((tapOffset.x - x).pow(2) + (tapOffset.y - y1).pow(2))

                                val distance2 = if (isBloodPressure) {
                                    val v2 = record.value2 ?: 0.0
                                    val normY2 = (v2 - minValue) / valueDelta
                                    val y2 = paddingTop + canvasHeight - (normY2 * canvasHeight).toFloat()
                                    sqrt((tapOffset.x - x).pow(2) + (tapOffset.y - y2).pow(2))
                                } else {
                                    Float.MAX_VALUE
                                }

                                if (distance1 <= clickRadius || distance2 <= clickRadius) {
                                    onPointClick(record)
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
            ) {
                val canvasHeight = size.height - paddingBottom - paddingTop
                val gridLines = 4

                for (i in 0..gridLines) {
                    val y = paddingTop + (canvasHeight / gridLines) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(totalChartWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val pathValue1 = Path()
                val pathValue2 = Path()

                records.forEachIndexed { index, record ->
                    val x = index * stepX

                    val normY1 = (record.value1 - minValue) / valueDelta
                    val y1 = paddingTop + canvasHeight - (normY1 * canvasHeight).toFloat()

                    if (index == 0) pathValue1.moveTo(x, y1) else pathValue1.lineTo(x, y1)

                    if (isBloodPressure) {
                        val v2 = record.value2 ?: 0.0
                        val normY2 = (v2 - minValue) / valueDelta
                        val y2 = paddingTop + canvasHeight - (normY2 * canvasHeight).toFloat()
                        if (index == 0) pathValue2.moveTo(x, y2) else pathValue2.lineTo(x, y2)
                    }

                    try {
                        val parsedDate = LocalDateTime.parse(record.timestamp)
                        val shortDateText = parsedDate.format(DateTimeFormatter.ofPattern("dd.MM"))

                        drawContext.canvas.nativeCanvas.drawText(
                            shortDateText,
                            x,
                            paddingTop + canvasHeight + 50f,
                            datePaint
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                drawPath(path = pathValue1, color = primaryColor, style = Stroke(width = 3.dp.toPx()))
                if (isBloodPressure) {
                    drawPath(path = pathValue2, color = secondaryColor, style = Stroke(width = 3.dp.toPx()))
                }

                records.forEachIndexed { index, record ->
                    val x = index * stepX

                    val normY1 = (record.value1 - minValue) / valueDelta
                    val y1 = paddingTop + canvasHeight - (normY1 * canvasHeight).toFloat()
                    drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = Offset(x, y1))

                    if (isBloodPressure) {
                        val v2 = record.value2 ?: 0.0
                        val normY2 = (v2 - minValue) / valueDelta
                        val y2 = paddingTop + canvasHeight - (normY2 * canvasHeight).toFloat()
                        drawCircle(color = secondaryColor, radius = 5.dp.toPx(), center = Offset(x, y2))
                    }
                }
            }
        }
    }
}


@Composable
fun Color.toArgb(): Int = with(LocalDensity.current) {
    this@toArgb.copy(alpha = 1f).value.toLong().shr(32).toInt()

    android.graphics.Color.argb(
        (this@toArgb.alpha * 255).toInt(),
        (this@toArgb.red * 255).toInt(),
        (this@toArgb.green * 255).toInt(),
        (this@toArgb.blue * 255).toInt()
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ReportScreen(onNavigateBack = {})
}