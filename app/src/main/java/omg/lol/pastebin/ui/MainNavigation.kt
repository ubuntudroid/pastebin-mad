package omg.lol.pastebin.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import omg.lol.pastebin.core.ui.UiResource.Failure
import omg.lol.pastebin.core.ui.UiResource.Loading
import omg.lol.pastebin.core.ui.UiResource.Success
import omg.lol.pastebin.feature.login.ui.LoginScreen
import omg.lol.pastebin.feature.pastes.ui.PastesScreen
import omg.lol.pastebin.nav.popUpToTop

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    viewModel: MainNavigationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    with(state) {
        PureMainNavigation(
            modifier = modifier,
            isAuthenticatedResource = dataState.isAuthenticatedResource
        )
    }
}

@Composable
internal fun PureMainNavigation(
    modifier: Modifier = Modifier,
    isAuthenticatedResource: IsAuthenticatedResource?
) {
    when (isAuthenticatedResource) {
        is Success, is Failure -> {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = when (isAuthenticatedResource) {
                    is Failure -> "login"
                    is Success -> if (isAuthenticatedResource.data) "pastes" else "login"
                    Loading -> throw IllegalStateException("This should never happen")
                },
                modifier = modifier.fillMaxSize()
            ) {
                composable("pastes") { PastesScreen(modifier = Modifier.fillMaxSize()) }
                composable("login") {
                    LoginScreen(
                        modifier = Modifier.fillMaxSize(),
                        onLoginDone = {
                            navController.navigate("pastes") {
                                popUpToTop(navController)
                            }
                        }
                    )
                }
            }
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
