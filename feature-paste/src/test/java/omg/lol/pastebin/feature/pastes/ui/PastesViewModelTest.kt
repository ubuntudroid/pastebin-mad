package omg.lol.pastebin.feature.pastes.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import omg.lol.pastebin.core.data.di.FakePastebinRepository
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.testing.MainCoroutineRule
import omg.lol.pastebin.core.ui.UiResource.Loading
import omg.lol.pastebin.core.ui.UiResource.Success
import omg.lol.pastebin.platform.di.FakeClipboardRepository
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class PastesViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun state_initiallyPastesLoadingThenSuccess() = runTest {
        val viewModel = PasteViewModel(FakePastebinRepository(), FakeClipboardRepository(), SavedStateHandle())
        assertEquals(null, viewModel.state.value.dataState.pastesResource)
        val values = mutableListOf<PastesResource?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.map { it.dataState.pastesResource }
                .toList(values)
        }

        advanceUntilIdle()

        assertEquals(3, values.count())

        assertEquals(null, values[0]) // initial state, pre-subscription
        assertEquals(Loading, values[1]) // loading
        assertEquals(Success(emptyList<String>()), values[2]) // success
    }

    @Test
    fun state_onItemSaved_isDisplayed() = runTest {
        val viewModel = PasteViewModel(FakePastebinRepository(), FakeClipboardRepository(), SavedStateHandle())
        val title = "test"
        val content = "Lorem ipsum"
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect()
        }

        viewModel.savePaste(title, content)
        advanceUntilIdle()
        assertEquals(Success(Unit), viewModel.state.value.dataState.pasteSavingResource)
        val pastesRes = viewModel.state.value.dataState.pastesResource
        assert(pastesRes is Success) { "pastesRes is not Success, instead it is $pastesRes" }
        val pastes = (pastesRes as Success<List<Paste>>).data
        assertEquals(1, pastes.size)
        assertEquals(title, pastes[0].title)
        assertEquals(content, pastes[0].content)
    }
}
