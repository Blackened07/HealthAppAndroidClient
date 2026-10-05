package com.blackened.healthapp.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blackened.healthapp.R

const val LOGO = "Логотип приложения"
const val APP_DESCRIPTION = "Приложение для контроля здоровья"
const val ROLE = "Роль"
const val REGISTRATION_BTN = "Войти"
const val LOGIN_BTN = "Зарегистрироваться"
const val REGISTRATION_TOGGLE = "Регистрация"
const val LOGIN_TOGGLE = "Вход"
const val BOTTOM_TEXT = "Войдите или создайте аккаунт, чтобы начать"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToVerification: (email: String) -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HeaderBlock()

        CardBlock(viewModel, onNavigateToVerification)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = BOTTOM_TEXT,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardBlock(viewModel: LoginViewModel, onNavigateToVerification: (String) -> Unit) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (viewModel.isRegisterMode) {
                OutlinedTextField(
                    value = viewModel.firstName,
                    onValueChange = { viewModel.onFirstNameChanged(it) },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                RoleDropDownMenu(viewModel = viewModel)
            }

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChanged(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            ModeToggleButtons(viewModel = viewModel, onNavigateToVerification = onNavigateToVerification)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onActionClick(onRegistrationSuccess = onNavigateToVerification) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (viewModel.isRegisterMode) REGISTRATION_BTN else LOGIN_BTN)
            }

            if (viewModel.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun ModeToggleButtons(viewModel: LoginViewModel, onNavigateToVerification: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        OutlinedButton(onClick = { viewModel.switchToLogin() },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (!viewModel.isRegisterMode)
                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
        )
        {
            Text(LOGIN_TOGGLE)
        }

        OutlinedButton(
            onClick = { viewModel.onActionClick(onNavigateToVerification) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (viewModel.isRegisterMode)
                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
        ) {
            Text(REGISTRATION_TOGGLE)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropDownMenu(viewModel: LoginViewModel) {

    var isMenuExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isMenuExpanded,
        onExpandedChange = { isMenuExpanded = it }
    ) {
        OutlinedTextField(
            value = viewModel.selectedRole.label,
            onValueChange = {},
            readOnly = true,
            label = { Text(ROLE) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuExpanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
        )

        ExposedDropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false }
        ) {
            viewModel.roleList.forEach { role ->
                DropdownMenuItem(
                    text = { Text(text = role.label) },
                    onClick = {
                        viewModel.onRoleSelected(role)
                        isMenuExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun HeaderBlock() {
    Spacer(modifier = Modifier.height(40.dp))

    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = LOGO
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = APP_DESCRIPTION,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(48.dp))
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}