package com.blackened.healthapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blackened.healthapp.data.SessionManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/*@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    CalendarScreen(
        onNavigateToFamily = {},
        onLogout = {}
    )
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToFamily: () -> Unit,
    onNavigateToReport: () -> Unit,
    onLogout: () -> Unit,
    viewModel: CalendarViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Меню приложения", modifier = Modifier.padding(16.dp), fontSize = 20.sp)
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Семья") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToFamily()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Выход из аккаунта") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Мониторинг здоровья") },
                    navigationIcon = {
                        IconButton(onClick = {

                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Открыть меню"
                            )
                        }
                    },
                    actions = {

                        TextButton(onClick = onNavigateToReport) {
                            Text("Отчет")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    showBottomSheet = true
                }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить запись")
                }
            }
        ) {
            innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CalendarHeaderBlock(
                    currentMonthYear = viewModel.currentMonthYearText,
                    onPreviousMonthClick = { viewModel.previousMonth() },
                    onNextMonthClick = { viewModel.nextMonth() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                CalendarGridBlock(
                    daysList = viewModel.daysInMonthList,
                    selectedDay = viewModel.selectedDay,
                    onDayClick = { clickedDay -> viewModel.onDaySelected(clickedDay) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Записи за :",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(viewModel.recordsList.size) { index ->
                        val record = viewModel.recordsList[index]
                        val russianTitle = MetricType.entries.find { it.serverName == record.type }?.label
                            ?: "Неизвестный замер"
                        val formattedTimestamp = formatServerDate(record.timestamp)

                        val displayValue = if (record.type == MetricType.BLOOD_PRESSURE.toString()) {
                            "${record.value1} / ${record.value2}"
                        } else {
                            "${record.value1}"
                        }

                        MedicalRecordItem(
                            title = russianTitle,
                            timestamp = formattedTimestamp,
                            valueText = displayValue,
                            onInfoClick = {
                                viewModel.showNote(record.note)
                            },
                            onEditClick = {
                                viewModel.startEditing(record.id)
                                showBottomSheet = true
                            },
                            onDeleteClick = {
                                viewModel.deleteRecord(record.id)
                            }
                        )
                    }
                }

                if (viewModel.selectedNoteText != null) {
                    AlertDialog(
                        onDismissRequest = {viewModel.dismissNote()},
                        title = {Text("Примечание")},
                        text = { Text(viewModel.selectedNoteText ?: "")},
                        confirmButton = {
                            TextButton(onClick = {viewModel.dismissNote()}) {Text("Ok") }
                        }
                    )
                }

                if (viewModel.isListLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }


            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    viewModel.stopEditing()
                }
            ) {
                val addRecordViewModel: AddRecordViewModel = viewModel()

                androidx.compose.runtime.LaunchedEffect(viewModel.editingRecordId) {
                    val recordId = viewModel.editingRecordId
                    if (recordId != null) {
                        val recordToEdit = viewModel.recordsList.find { it.id == recordId }
                        if (recordToEdit != null) {
                            addRecordViewModel.prepareForEdit(recordToEdit)
                        }
                    }
                }

                AddRecordBottomSheet(
                    viewModel = addRecordViewModel,
                    onDismiss = {
                        showBottomSheet = false
                        viewModel.stopEditing()
                        viewModel.loadRecordsForDay(viewModel.selectedDay)
                    }
                )
            }
        }
    }
}

@Composable
fun CalendarHeaderBlock(
    currentMonthYear: String,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = onPreviousMonthClick) {
            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Предыдущий месяц")
        }

        Text(
            text = currentMonthYear,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(onClick = onNextMonthClick) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Следующий месяц")
        }
    }
}

@Composable
fun CalendarGridBlock(
    daysList: List<Int?>,
    selectedDay: Int,
    onDayClick: (Int) -> Unit
) {
    val daysOfWeek = listOf("П", "В", "С", "Ч", "П", "С", "В")

   /* val daysInMonth = (1..31).toList()*/

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(220.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(daysList.size) { index ->
                val dayNumber = daysList[index]
                if (dayNumber == null) {
                    Box(modifier = Modifier.size(40.dp))
                } else {
                    val isSelected = dayNumber == selectedDay

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable { onDayClick(dayNumber) }
                    ) {
                        Text(
                            text = dayNumber.toString(),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalRecordItem(
    title: String,
    timestamp: String,
    valueText: String,
    onInfoClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timestamp,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = valueText,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Детали",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun formatServerDate(rawDate: String): String {
    return try {
        val localDateTime = LocalDateTime.parse(rawDate)
        val formatter = DateTimeFormatter.ofPattern("d MMMM, HH:mm", Locale.forLanguageTag("ru"))
        localDateTime.format(formatter)
    } catch (e: Exception) {
        rawDate
    }
}
