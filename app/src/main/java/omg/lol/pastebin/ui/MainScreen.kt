package omg.lol.pastebin.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigation.suite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigation.suite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import omg.lol.pastebin.R
import omg.lol.pastebin.feature.account.ui.AccountScreen
import omg.lol.pastebin.feature.pastes.ui.PastesScreen

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    PureMainScreen(modifier = modifier)
}

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
internal fun PureMainScreen(
    modifier: Modifier = Modifier
) {
    // TODO #1 move selected item info and item definitions to viewModel
    //  This also means that we need to add some kind of ID to each item
    //  as the Composable definition then needs to be done in the NavigationSuiteScaffold
    //  content lambda.
    var selectedItem by rememberSaveable {
        mutableIntStateOf(0)
    }
    val navItems = listOf(
        NavItem(
            label = stringResource(R.string.nav_pastes),
            icon = Icons.AutoMirrored.Filled.List,
            screen = { modifier -> PastesScreen(modifier = modifier) }
        ),
        NavItem(
            label = stringResource(R.string.nav_account),
            icon = Icons.Filled.AccountCircle,
            screen = { modifier -> AccountScreen(modifier = modifier) }
        ),
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            navItems.forEachIndexed { index, navItem ->
                item(
                    icon = { Icon(navItem.icon, contentDescription = navItem.label) },
                    label = { Text(text = navItem.label) },
                    selected = selectedItem == index,
                    onClick = { selectedItem = index }
                )
            }
        },
        modifier = modifier
    ) {
        navItems[selectedItem].screen(Modifier.fillMaxSize())
    }
}

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val screen: @Composable (modifier: Modifier) -> Unit
)