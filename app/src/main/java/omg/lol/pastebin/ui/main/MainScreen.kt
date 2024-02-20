package omg.lol.pastebin.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigation.suite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigation.suite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import omg.lol.pastebin.feature.account.ui.AccountScreen
import omg.lol.pastebin.feature.pastes.ui.PastesScreen

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    with(state) {
        PureMainScreen(
            navItems = dataState.navItems,
            selectedNavItemId = uiState.selectedNavItemId,
            onNavItemClick = { id -> viewModel.selectNavItem(id) },
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
internal fun PureMainScreen(
    navItems: Map<NavItemId, NavItem>,
    selectedNavItemId: NavItemId?,
    modifier: Modifier = Modifier,
    onNavItemClick: (NavItemId) -> Unit = {},
) {

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            navItems.forEach { (id, navItem) ->
                item(
                    icon = { Icon(navItem.icon, contentDescription = stringResource(id = navItem.labelRes)) },
                    label = { Text(text = stringResource(id = navItem.labelRes)) },
                    selected = selectedNavItemId == id,
                    onClick = { onNavItemClick(id) }
                )
            }
        },
        modifier = modifier
    ) {
        when (selectedNavItemId) {
            null -> { LoadingScreen(Modifier.fillMaxSize()) }
            NavItemId.Pastes-> PastesScreen(Modifier.fillMaxSize())
            NavItemId.Account -> AccountScreen(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }
}