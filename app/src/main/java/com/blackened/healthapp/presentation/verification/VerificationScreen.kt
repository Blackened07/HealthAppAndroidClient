package com.blackened.healthapp.presentation.verification

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

const val FIELD_PLACEHOLDER = "000000"
const val BTN_TEXT = "Подтвердить"
const val ACCOUNT_VERIFICATION = "Подтверждение аккаунта"
const val VERIFICATION_MESSAGE = "Мы отправили код подтверждения на почту:\n"

@Preview(showBackground = true)
@Composable
fun VerifyPreview() {
    VerificationScreen(email = "test@example.com")
}

@Composable
fun VerificationScreen(
    email: String,
    viewModel: VerificationViewModel = viewModel(),
    onSuccessRoute: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = ACCOUNT_VERIFICATION,
            /*style = MaterialTheme.typography.headlineMedium,*/
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = VERIFICATION_MESSAGE + email,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        VerificationFormBlock(
            email = email,
            viewModel = viewModel,
            onSuccessRoute = onSuccessRoute
        )
    }
}

@Composable
fun VerificationFormBlock(
    email: String,
    viewModel: VerificationViewModel,
    onSuccessRoute: () -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(viewModel.errorResponse) {
        viewModel.errorResponse?.let { error ->
            Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    OutlinedTextField(
        value = viewModel.code,
        onValueChange = { input ->
            if (input.length <= 6 && input.all { it.isDigit() }) {
                viewModel.onCodeChanged(input)
            }
        },
        placeholder = {
            Text(
                text = FIELD_PLACEHOLDER,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        textStyle = TextStyle(
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 4.sp
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.width(200.dp),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = {
            viewModel.onVerificationClick(email = email, onVerificationSuccess = onSuccessRoute)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(BTN_TEXT)
    }

    ShowLoadingCircle(viewModel)

}

@Composable
fun ShowLoadingCircle(viewModel: VerificationViewModel) {
    if (viewModel.isLoading) {
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator()
    }
}
