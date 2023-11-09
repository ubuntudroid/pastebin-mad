package omg.lol.pastebin.feature.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import omg.lol.pastebin.core.ui.UiResource.Failure
import omg.lol.pastebin.core.ui.UiResource.Loading
import omg.lol.pastebin.core.ui.UiResource.Success
import omg.lol.pastebin.feature.login.R
import omg.lol.pastebin.core.l10n.R as CR

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginDone: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    with(state) {
        if (dataState.loginResource is Success) {
            LaunchedEffect(Unit) { onLoginDone() }
        } else {
            PureLoginScreen(
                name = uiState.nameInput.wrappedValue,
                apiKey = uiState.apiKeyInput.wrappedValue,
                enableLogin = uiState.enableLogin,
                showApiKey = uiState.showApiKey,
                loginRes = dataState.loginResource,
                onNameInputChange = { name -> viewModel.updateNameInput(name.copy()) },
                onApiKeyInputChange = { apiKey -> viewModel.updateApiKeyInput(apiKey.copy()) },
                onApiKeyVisibilityClicked = { showApiKey -> viewModel.updateApiKeyVisibility(showApiKey) },
                onLogin = { name, apiKey -> viewModel.login(name, apiKey) },
                modifier = modifier
            )
        }
    }
}

@Composable
internal fun PureLoginScreen(
    modifier: Modifier = Modifier,
    name: TextFieldValue = TextFieldValue("Arthur Dent"),
    apiKey: TextFieldValue = TextFieldValue("12345"),
    enableLogin: Boolean = true,
    showApiKey: Boolean = false,
    loginRes: LoginResource? = null,
    onNameInputChange: (TextFieldValue) -> Unit = {},
    onApiKeyInputChange: (TextFieldValue) -> Unit = {},
    onApiKeyVisibilityClicked: (Boolean) -> Unit = {},
    onLogin: (String, String) -> Unit = { _, _ -> }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            label = { Text(text = "Name") },
            value = name,
            onValueChange = { onNameInputChange(it) },
            isError = loginRes is Failure,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            label = { Text(text = "API key") },
            value = apiKey,
            onValueChange = { onApiKeyInputChange(it) },
            isError = loginRes is Failure,
            supportingText = {
                if (loginRes is Failure) {
                    Text(
                        loginRes.throwable.message
                            ?: stringResource(CR.string.error_unknown)
                    )
                }
             },
            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (showApiKey) Icons.Filled.VisibilityOff
                else Icons.Filled.Visibility

                val description =
                    if (showApiKey) stringResource(R.string.password_hide)
                    else stringResource(R.string.password_show)

                IconButton(onClick = { onApiKeyVisibilityClicked(!showApiKey) }) {
                    Icon(imageVector = image, description)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            enabled = enableLogin,
            onClick = { onLogin(name.text, apiKey.text) },
            modifier = Modifier.width(200.dp)
        ) {
            if (loginRes is Loading) {
                CircularProgressIndicator(
                    trackColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(text = "Submit")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    PureLoginScreen(
        name = TextFieldValue("Arthur Dent"),
        apiKey = TextFieldValue("12345"),
        enableLogin = true,
        showApiKey = false,
        loginRes = null,
        onNameInputChange = {},
        onApiKeyInputChange = {},
        onApiKeyVisibilityClicked = {},
        onLogin = { _, _ -> }
    )
}