package omg.lol.pastebin.feature.pastes.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.ui.text.input.TextFieldValue
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
import omg.lol.pastebin.core.data.pastebin.PastebinRepository
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.ui.ParcelableTextFieldValue
import omg.lol.pastebin.core.ui.UiResource
import omg.lol.pastebin.core.ui.UiResource.Failure
import omg.lol.pastebin.core.ui.UiResource.Loading
import omg.lol.pastebin.core.ui.UiResource.Success
import omg.lol.pastebin.core.ui.toCatchingUiResourceFlow
import omg.lol.pastebin.core.util.sharedFlowSafeOnStart
import omg.lol.pastebin.platform.ClipboardRepository
import javax.inject.Inject

private const val KEY_SAVED_UI_STATE = "uiState"

@HiltViewModel
class PasteViewModel @Inject constructor(
    private val pastebinRepository: PastebinRepository,
    private val clipboardRepository: ClipboardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // TODO: saved state handling is more or less the same across all ViewModels, extract!
    private val savedUiState: UiState? = run {
        val uiStateBundle = savedStateHandle.get<Bundle>(KEY_SAVED_UI_STATE)
        uiStateBundle?.run {
            uiStateBundle.classLoader = this@PasteViewModel.javaClass.classLoader
            uiStateBundle.getParcelable(null) as? UiState
        }
    }

    init {
        savedStateHandle.setSavedStateProvider(KEY_SAVED_UI_STATE) {
            Bundle().apply { putParcelable(null, state.value.uiState) }
        }
    }

    /*
    Alternatively to storing the text field's state here, one could also store the paste with
    rememberSaveable in the UI as it is ephemereal. The SavedState docs suggest this as the
    preferred approach:
    https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate
    That approach also allows for a potentially more responsive UI and doesn't have issues like
    jumping cursors. And Compose interactive previews will work as expected in more cases.

    However, this pollutes the View code, introduces logic to the View (e.g. clearing when/after saving)
    and scatters state across multiple places (ViewModel & UI).

    A second approach (employed here) is to store the paste in the ViewModel. Here we mitigate the
    jumping cursor issue by using TextFieldValue which holds selection state. This works fine for
    TextFields, but leaks UI specifics into the ViewModel (which can be okay depending on how strict
    you want to decouple your ViewModel from your UI). Most MVI docs and libraries still recommend
    this approach as the ViewModel is responsible for all state of the UI in MVI and is the single
    source of truth. So, purely from an architecturally standpoint, this would be the "correct"
    approach.

    Another advantage of this approach is that it allows the same handling for when ephemereal state
    should modify the representation of non-ephemereal state, such as filtering a list of data
    fetched from lower layers. This can all happen when building the UI state, creating simple to
    grasp, consistent code. So ephemereal state is treated the same as non-ephemereal state when
    it comes to processing.

    A third approach would be to store the paste via mutableStateOf() in the ViewModel(!). This however
    again leaks UI specifics (only works with Compose) into the ViewModel.

    Ultimately, the "right" way to do this is definitely debatable and architecture, code readability
    and user experience (responsiveness, levels of state restoration, etc.) should be considered.
     */
    private val pasteTitleInput =
        MutableStateFlow(savedUiState?.pasteTitleInput ?: UiState().pasteTitleInput)
    private val pasteContentInput =
        MutableStateFlow(savedUiState?.pasteContentInput ?: UiState().pasteContentInput)
    private val refreshPastes = MutableStateFlow(0L)
    private val pasteSavingResource = MutableStateFlow(DataState().pasteSavingResource)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<State> = combine(
        refreshPastes.flatMapLatest {
            pastebinRepository.pastes
                .toCatchingUiResourceFlow() // TODO we could add sophisticated localized error messages here
                .sharedFlowSafeOnStart { emit(Loading) }
                .distinctUntilChanged()
        },
        pasteTitleInput,
        pasteContentInput,
        pasteSavingResource
    ) { pastesRes, pasteTitleInput, pasteContentInput, pasteSavingRes ->
        State(
            uiState = UiState(
                pasteTitleInput = pasteTitleInput,
                pasteContentInput = pasteContentInput,
                saveEnabled = pasteTitleInput.wrappedValue.text.isNotBlank()
                        && pasteContentInput.wrappedValue.text.isNotBlank()
                        && pasteSavingRes !is Loading,
                inputEnabled = pasteSavingRes !is Loading
            ),
            dataState = DataState(
                pastesResource = pastesRes,
                pasteSavingResource = pasteSavingRes
            )
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State())

    fun refreshPastes() {
        viewModelScope.launch {
            refreshPastes.update { it + 1 }
        }
    }

    fun savePaste(title: String, content: String) {
        pasteSavingResource.value = Loading
        viewModelScope.launch {
            try {
                pastebinRepository.add(title, content)
                pasteSavingResource.value = Success(Unit)
                pasteTitleInput.value = ParcelableTextFieldValue(TextFieldValue())
                pasteContentInput.value = ParcelableTextFieldValue(TextFieldValue())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                pasteSavingResource.value = Failure(e)
            }
        }
    }

    fun updatePasteTitleInput(input: TextFieldValue) {
        pasteTitleInput.value = ParcelableTextFieldValue(input)
    }

    fun updatePasteContentInput(input: TextFieldValue) {
        pasteContentInput.value = ParcelableTextFieldValue(input)
    }

    fun copyToClipboard(paste: Paste) {
        clipboardRepository.copyToClipboard(paste.title, paste.content)
    }
}

// TODO: it might be better to not expose the difference between UI state and data state to the view as
//  it actually shouldn't care about this kind of intricacies. However, there is quite a bit of dumb
//  mapping code involved if we want to merge the two in one data class. The concept of two
//  sub-states isn't ideal (although it certainly fits the bill when it comes to making clear what's
//  serialized and saved and what not).
data class State(
    /*
    Ephemereal state (will be saved and restored on process restore via SavedState).

    Usually that's stuff like in-place filtering and sorting settings, input field states,
    expanded/collapsed state, scroll/paging state, etc.
     */
    val uiState: UiState = UiState(),
    /*
    State backed by the data layer (will be reloaded on process restore).

    The data here can quickly take quite a bit of memory, so don't attempt to store it in
    SavedState.
     */
    val dataState: DataState = DataState()
)

@Parcelize
data class UiState(
    val pasteTitleInput: ParcelableTextFieldValue = ParcelableTextFieldValue(TextFieldValue("")),
    val pasteContentInput: ParcelableTextFieldValue = ParcelableTextFieldValue(TextFieldValue("")),
    // TODO: the following properties are just derived state from the other ones and data state, therefore
    //  they are not restored from SavedState. To make this more explicit and prevent unnecessary
    //  restrictions by @Parcelize and overhead while serializing to SavedState, they could also be
    //  moved to an own state class (e.g. DerivedUiState) which would then not have to be parcelable.
    val saveEnabled: Boolean = false,
    val inputEnabled: Boolean = false,
) : Parcelable

data class DataState(
    val pastesResource: PastesResource? = null,
    val pasteSavingResource: PasteSavingResource? = null
)

typealias PastesResource = UiResource<List<Paste>>
typealias PasteSavingResource = UiResource<Unit>
