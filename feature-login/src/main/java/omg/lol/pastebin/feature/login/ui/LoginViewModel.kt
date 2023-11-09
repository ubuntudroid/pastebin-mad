package omg.lol.pastebin.feature.login.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import omg.lol.pastebin.core.data.user.UserRepository
import omg.lol.pastebin.core.model.user.User
import omg.lol.pastebin.core.ui.ParcelableTextFieldValue
import omg.lol.pastebin.core.ui.UiResource
import omg.lol.pastebin.core.ui.UiResource.Failure
import omg.lol.pastebin.core.ui.UiResource.Loading
import omg.lol.pastebin.core.ui.UiResource.Success
import javax.inject.Inject

private const val KEY_SAVED_UI_STATE = "uiState"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val savedUiState: UiState? = run {
        val uiStateBundle = savedStateHandle.get<Bundle>(KEY_SAVED_UI_STATE)
        uiStateBundle?.run {
            uiStateBundle.classLoader = this@LoginViewModel.javaClass.classLoader
            uiStateBundle.getParcelable(null) as? UiState
        }
    }

    init {
        savedStateHandle.setSavedStateProvider(KEY_SAVED_UI_STATE) {
            Bundle().apply { putParcelable(null, state.value.uiState) }
        }
    }

    private val nameInput =
        MutableStateFlow(savedUiState?.nameInput ?: UiState().nameInput)
    private val apiKeyInput =
        MutableStateFlow(savedUiState?.apiKeyInput ?: UiState().apiKeyInput)
    private val showApiKey =
        MutableStateFlow(savedUiState?.showApiKey ?: UiState().showApiKey)
    private val loginResource = MutableStateFlow(DataState().loginResource)

    val state: StateFlow<State> = combine(
        nameInput,
        apiKeyInput,
        showApiKey,
        loginResource
    ) { nameInput, apiKeyInput, showApiKey, loginRes ->
        State(
            uiState = UiState(
                nameInput = nameInput,
                apiKeyInput = apiKeyInput,
                showApiKey = showApiKey,
                enableLogin = nameInput.wrappedValue.text.isNotBlank() && apiKeyInput.wrappedValue.text.isNotBlank()
            ),
            dataState = DataState(
                loginResource = loginRes
            )
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State())

    fun login(name: String, apiKey: String) {
        loginResource.value = Loading
        viewModelScope.launch {
            try {
                userRepository.login(name, apiKey)
                loginResource.value = Success(User(name, apiKey))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                loginResource.value = Failure(e)
            }
        }
    }

    fun updateNameInput(nameInput: TextFieldValue) {
        this.nameInput.value = ParcelableTextFieldValue(nameInput)
    }

    fun updateApiKeyInput(apiKeyInput: TextFieldValue) {
        this.apiKeyInput.value = ParcelableTextFieldValue(apiKeyInput)
    }

    fun updateApiKeyVisibility(showApiKey: Boolean) {
        this.showApiKey.value = showApiKey
    }
}

data class State(
    val uiState: UiState = UiState(),
    val dataState: DataState = DataState()
)

@Parcelize
data class UiState(
    val nameInput: ParcelableTextFieldValue = ParcelableTextFieldValue(TextFieldValue("")),
    val apiKeyInput: ParcelableTextFieldValue = ParcelableTextFieldValue(TextFieldValue("")),
    val showApiKey: Boolean = false,
    val enableLogin: Boolean = false
) : Parcelable

data class DataState(
    val loginResource: LoginResource? = null
)

typealias LoginResource = UiResource<User>