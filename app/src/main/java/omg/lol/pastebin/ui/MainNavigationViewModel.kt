package omg.lol.pastebin.ui

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.parcelize.Parcelize
import omg.lol.pastebin.core.data.user.UserRepository
import omg.lol.pastebin.core.ui.UiResource
import omg.lol.pastebin.core.ui.UiResource.Loading
import omg.lol.pastebin.core.ui.toCatchingUiResourceFlow
import omg.lol.pastebin.core.util.sharedFlowSafeOnStart
import javax.inject.Inject

@HiltViewModel
class MainNavigationViewModel @Inject constructor(
    userRepository: UserRepository
) : ViewModel() {

    val state: StateFlow<State> = userRepository.isAuthenticated
        .toCatchingUiResourceFlow().sharedFlowSafeOnStart { emit(Loading) }
        .map { isAuthenticatedRes ->
            State(
                uiState = UiState,
                dataState = DataState(
                    isAuthenticatedResource = isAuthenticatedRes
                )
            )
        }
        /*
        We don't use SharingState.WhileSubscribed here as it would ultimately trigger a complete
        recomposition of the navigation graph when the Activity is stopped, but not destroyed,
        which happens when e.g. the app is just moved to background and after a not too long time
        back to foreground or the screen goes off for a brief period of time.

        The recomposition would happen with WhileSubscribed as:
        1. The Activity is stopped.
        2. MainNavigation unsubscribes.
        3. This flow terminates and looses all state (as MainNavigation is the only subscriber).
        4. After some time the Activity starts again.
        5. MainNavigation subscribes again to this flow.
        6. As this flow has no hold to the old state anymore, it starts with the values `null`, then
        `Loading`.
        7. This causes MainNavigation to recompose and show the loading state.
        8. This removes all other Composables further down as the NavHost is not composed anymore
        which also destroys all attached ViewModels.
        9. This flow returns `Success` or `Failure`.
        10. MainNavigation recomposes and composes the NavHost which in turn creates new ViewModels
        for the content.
        -> All Composables and their ViewModels have lost their states.

        SharingStarted.Lazily fixes this by staying around until the MainActivity is destroyed even
        if no one is subscribed.

        TODO: The only downside is that no authentication check happens again in this scenario. However,
         this could be fixed by manually triggering a resubscription to `userRepository.isAuthenticated`
         via e.g. `LaunchedEffect(Unit)` in the NavHost Composable content. A `MutableStateFlow`
         combined with `flatMapLatest` should help with that.
        */
        .stateIn(viewModelScope, SharingStarted.Lazily, State())
}

data class State(
    val uiState: UiState = UiState,
    val dataState: DataState = DataState()
)

@Parcelize
data object UiState : Parcelable

data class DataState(
    val isAuthenticatedResource: IsAuthenticatedResource? = null
)

typealias IsAuthenticatedResource = UiResource<Boolean>