package omg.lol.pastebin.feature.account.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import omg.lol.pastebin.core.model.user.User
import omg.lol.pastebin.core.ui.ShowSnackbarForResource
import omg.lol.pastebin.core.ui.UiResource.Success
import omg.lol.pastebin.core.ui.theme.PastebinTheme
import omg.lol.pastebin.feature.account.R

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    with(state) {
        PureAccountScreen(
            userResource = dataState.userResource,
            logoutRes = dataState.logoutResource,
            onLogout = { viewModel.logout() },
            onUserErrorSnackbarRefreshClick = { viewModel.refreshUser() },
            modifier = modifier
        )
    }
}

@Composable
internal fun PureAccountScreen(
    userResource: UserResource?,
    logoutRes: LogoutResource?,
    onLogout: () -> Unit,
    onUserErrorSnackbarRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ShowSnackbarForResource(
        resource = userResource,
        snackbarHostState = snackbarHostState,
        onRefreshClick = onUserErrorSnackbarRefreshClick
    )
    ShowSnackbarForResource(
        resource = logoutRes,
        snackbarHostState = snackbarHostState
    )
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { scaffoldPadding ->
        Column(modifier = Modifier.padding(scaffoldPadding)) {
            AccountDetails(
                userResource = userResource,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.width(200.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(R.string.logout))
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    PastebinTheme {
        PureAccountScreen(
            modifier = Modifier,
            userResource = Success(User(name = "arthur", apiKey = "123456")),
            logoutRes = null,
            onLogout = {},
            onUserErrorSnackbarRefreshClick = {}
        )
    }
}
