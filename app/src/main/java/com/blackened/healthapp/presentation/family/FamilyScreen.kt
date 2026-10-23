package com.blackened.healthapp.presentation.family

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blackened.healthapp.domain.model.FamilyMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    onNavigateToCalendar: () -> Unit,
    onLogout: () -> Unit,
    viewModel: FamilyViewModel = viewModel()
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.errorResponse) {
        viewModel.errorResponse?.let { error ->
            Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GetContent(drawerState, scope, onNavigateToCalendar, onLogout)
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val titleText = when (val state = viewModel.familyUiState) {
                            is FamilyUiState.HasFamily -> state.info.name
                            else -> "Моя Семья"
                        }
                        Text(titleText, fontWeight = FontWeight.Bold)
                    },

                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Меню")
                        }
                    },

                    actions = {
                        val isOwner = when (val state = viewModel.familyUiState) {
                            is FamilyUiState.HasFamily -> state.info.role == "ADMIN"
                            else -> false
                        }

                        if (isOwner) {
                            IconButton(onClick = {
                                Toast.makeText(
                                    context,
                                    "Управление семьей (Скоро)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Управление"
                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when (val state = viewModel.familyUiState) {
                    is FamilyUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is FamilyUiState.NoFamily -> {
                        NoFamilyLayout(
                            onCreateFamilyClick = {},
                            onJoinFamilyClick = {}
                        )
                    }

                    is FamilyUiState.HasFamily -> {
                        HasFamilyLayout(state)
                    }

                    is FamilyUiState.Error -> {
                        Button(onClick = { viewModel.checkUserFamilyState() }) {
                            Text("Повторить попытку")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoFamilyLayout(
    onCreateFamilyClick: () -> Unit,
    onJoinFamilyClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Вы пока не состоите в семье",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Создайте свой семейный круг для совместного контроля здоровья или присоединитесь по приглашению.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onCreateFamilyClick, modifier = Modifier.fillMaxWidth()) {
            Text("Создать новую семью")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onJoinFamilyClick, modifier = Modifier.fillMaxWidth()) {
            Text("Присоединиться по коду")
        }
    }
}

@Composable
fun HasFamilyLayout(state: FamilyUiState.HasFamily) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Моя Семья: ${state.info.name}",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Ваша роль: ${state.info.role}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // userList
        FamilyMembersList(members = state.members)
        Text(text = "Список участников загружается...", fontSize = 16.sp)
    }
}

@Composable
fun FamilyMembersList(members: List<FamilyMember>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(members) { member ->
            FamilyMemberCard(member = member)
        }
    }
}

@Composable
fun FamilyMemberCard(member: FamilyMember) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))


            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.firstName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Роль: ${member.familyRole}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Активность: ${member.lastActivityFormatted}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GetContent(
    drawerState: DrawerState,
    scope: CoroutineScope,
    onNavigateToCalendar: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(16.dp))
        val currentUserName =
            com.blackened.healthapp.data.SessionManager.userName ?: "Пользователь"
        Text(
            text = "Привет, $currentUserName!",
            modifier = Modifier.padding(16.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text("Календарь") },
            selected = false,
            onClick = {
                scope.launch { drawerState.close() }
                onNavigateToCalendar()
            }
        )

        NavigationDrawerItem(
            label = { Text("Выйти") },
            selected = false,
            onClick = {
                scope.launch { drawerState.close() }
                onLogout()
            }
        )
    }
}