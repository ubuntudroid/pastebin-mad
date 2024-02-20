package omg.lol.pastebin.feature.account.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import omg.lol.pastebin.core.data.user.UserRepository
import omg.lol.pastebin.core.model.user.User
import omg.lol.pastebin.core.ui.UiResource
import omg.lol.pastebin.core.ui.UiResource.Failure
import omg.lol.pastebin.core.ui.UiResource.Loading
import omg.lol.pastebin.core.ui.UiResource.Success
import omg.lol.pastebin.core.ui.toCatchingUiResourceFlow
import omg.lol.pastebin.core.util.sharedFlowSafeOnStart
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private const val KEY_SAVED_UI_STATE = "uiState"

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRespository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val savedUiState: UiState? = run {
        val uiStateBundle = savedStateHandle.get<Bundle>(KEY_SAVED_UI_STATE)
        uiStateBundle?.run {
            uiStateBundle.classLoader = this@AccountViewModel.javaClass.classLoader
            uiStateBundle.getParcelable(null) as? UiState
        }
    }

    init {
        savedStateHandle.setSavedStateProvider(KEY_SAVED_UI_STATE) {
            Bundle().apply { putParcelable(null, state.value.uiState) }
        }
    }

    private val refreshUser = MutableStateFlow(0L)
    private val logoutResource = MutableStateFlow(DataState().logoutResource)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<State> = combine(
        refreshUser.flatMapLatest {
            userRespository.user
                .toCatchingUiResourceFlow()
                .sharedFlowSafeOnStart { emit(Loading) }
                .distinctUntilChanged()
        },
        logoutResource
    ) { userResource, logoutResource ->
        State(
            uiState = UiState(),
            dataState = DataState(
                userResource = userResource,
                logoutResource = logoutResource
            )
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = State()
        )

    fun refreshUser() {
        viewModelScope.launch {
            refreshUser.update { it + 1 }
        }
    }

    fun logout() {
        logoutResource.value = Loading
        viewModelScope.launch {
            try {
                userRespository.logout()
                logoutResource.value = Success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logoutResource.value = Failure(e)
                TODO("Not yet implemented")
            }
        }
    }
}

data class State(
    val uiState: UiState = UiState(),
    val dataState: DataState = DataState()
)

@Parcelize
data class UiState(val noData: Unit = Unit) : Parcelable

data class DataState(
    val userResource: UserResource? = null,
    val logoutResource: LogoutResource? = null
)

typealias UserResource = UiResource<User?>
typealias LogoutResource = UiResource<Unit>