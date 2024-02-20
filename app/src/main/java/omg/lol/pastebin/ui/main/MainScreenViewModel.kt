package omg.lol.pastebin.ui.main

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.parcelize.Parcelize
import omg.lol.pastebin.R
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private const val KEY_SAVED_UI_STATE = "uiState"

class MainScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val savedUiState: UiState? = run {
        val uiStateBundle = savedStateHandle.get<Bundle>(KEY_SAVED_UI_STATE)
        uiStateBundle?.run {
            uiStateBundle.classLoader = this@MainScreenViewModel.javaClass.classLoader
            uiStateBundle.getParcelable(null) as? UiState
        }
    }

    init {
        savedStateHandle.setSavedStateProvider(KEY_SAVED_UI_STATE) {
            Bundle().apply { putParcelable(null, state.value.uiState) }
        }
    }

    private val selectedNavItemId = MutableStateFlow(
        savedUiState?.selectedNavItemId ?: UiState().selectedNavItemId
    )
    private val navItems = flowOf(
        mapOf(
            NavItemId.Pastes to NavItem(
                id = NavItemId.Pastes,
                labelRes = R.string.nav_pastes,
                icon = Icons.AutoMirrored.Filled.List,
            ),
            NavItemId.Account to NavItem(
                id = NavItemId.Account,
                labelRes = R.string.nav_account,
                icon = Icons.Filled.AccountCircle,
            ),
        )
    )

    val state: StateFlow<State> = combine(
        selectedNavItemId,
        navItems
    ) { navBarSelectedItemId, navItems ->
        State(
            uiState = UiState(
                selectedNavItemId = navBarSelectedItemId.takeIf { id -> navItems.containsKey(id) }
                    ?: navItems.keys.firstOrNull()
            ),
            dataState = DataState(
                navItems = navItems
            )
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = State()
        )

    fun selectNavItem(id: NavItemId?) {
        selectedNavItemId.value = id
    }
}

@Parcelize
sealed class NavItemId : Parcelable {
    data object Pastes : NavItemId()
    data object Account : NavItemId()
}

data class NavItem(
    val id: NavItemId,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

data class State(
    val uiState: UiState = UiState(),
    val dataState: DataState = DataState()
)

@Parcelize
data class UiState(val selectedNavItemId: NavItemId? = null) : Parcelable

data class DataState(
    val navItems: Map<NavItemId, NavItem> = emptyMap()
)