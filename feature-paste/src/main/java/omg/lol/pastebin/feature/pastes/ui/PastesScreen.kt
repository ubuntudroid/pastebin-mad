package omg.lol.pastebin.feature.pastes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.ui.ShowSnackbarForResource
import omg.lol.pastebin.core.ui.UiResource.Failure
import omg.lol.pastebin.core.ui.UiResource.Loading
import omg.lol.pastebin.core.ui.UiResource.Success
import omg.lol.pastebin.core.ui.theme.PastebinTheme
import kotlin.time.Duration.Companion.seconds

@Composable
fun PastesScreen(modifier: Modifier = Modifier, viewModel: PasteViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    with(state) {
        PurePastesScreen(
            pasteTitle = uiState.pasteTitleInput.wrappedValue,
            pasteContent = uiState.pasteContentInput.wrappedValue,
            saveEnabled = uiState.saveEnabled,
            inputEnabled = uiState.inputEnabled,
            pastesResource = dataState.pastesResource,
            pasteSavingResource = dataState.pasteSavingResource,
            onTitleInputChange = { title -> viewModel.updatePasteTitleInput(title.copy()) },
            onContentInputChange = { content -> viewModel.updatePasteContentInput(content.copy()) },
            onSave = { title, content -> viewModel.savePaste(title, content) },
            onPasteClick = { paste -> viewModel.copyToClipboard(paste) },
            onPastesErrorSnackbarRefreshClick = { viewModel.refreshPastes() },
            modifier = modifier.fillMaxSize()
        )
    }
}

@Composable
internal fun PurePastesScreen(
    pasteTitle: TextFieldValue,
    pasteContent: TextFieldValue,
    saveEnabled: Boolean,
    inputEnabled: Boolean,
    pastesResource: PastesResource?,
    pasteSavingResource: PasteSavingResource?,
    onTitleInputChange: (TextFieldValue) -> Unit,
    onContentInputChange: (TextFieldValue) -> Unit,
    onSave: (title: String, content: String) -> Unit,
    onPastesErrorSnackbarRefreshClick: () -> Unit,
    onPasteClick: (Paste) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ShowSnackbarForResource(
        resource = pastesResource,
        snackbarHostState = snackbarHostState,
        onRefreshClick = onPastesErrorSnackbarRefreshClick
    )
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)  },
        modifier = modifier,
    ) { scaffoldPadding ->
        Column(modifier = Modifier.padding(scaffoldPadding)) {
            PasteInput(
                pasteTitle = pasteTitle,
                onTitleInputChange = onTitleInputChange,
                pasteSavingResource = pasteSavingResource,
                saveEnabled = saveEnabled,
                inputEnabled = inputEnabled,
                onSave = onSave,
                pasteContent = pasteContent,
                onContentInputChange = onContentInputChange,
                modifier = Modifier.fillMaxWidth()
            )
            PastesContainer(
                pastesResource = pastesResource,
                onPasteClick = onPasteClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    PastebinTheme {
        PurePastesScreen(
            modifier = Modifier,
            pasteTitle = TextFieldValue(""),
            pasteContent = TextFieldValue(""),
            saveEnabled = false,
            inputEnabled = true,
            pastesResource = Success(
            listOf(
                Paste(
                    "Jean-Baptiste",
                    "Software Engineer - Android @ PhotoRoom, ex Zenly (Snap inc.)",
                    1672531200.seconds
                ),
                Paste(
                    "Matthieu",
                    "Senior Software Engineer, Android @ PhotoRoom, Ex-Zenly",
                    1677628800.seconds
                ),
                Paste(
                    "Sven",
                    "Senior Software Engineer - soon Android @ PhotoRoom? \uD83E\uDD13",
                    1699385031.seconds
                )
            )
            ),
            pasteSavingResource = Success(Unit),
            onTitleInputChange = {},
            onContentInputChange = {},
            onSave = { _, _ -> },
            onPasteClick = {},
            onPastesErrorSnackbarRefreshClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PastesLoadingPreview() {
    PastebinTheme {
        PastesContainer(
            pastesResource = Loading,
            onPasteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PasteSavingLoadingPreview() {
    PastebinTheme {
        PasteInput(
            pasteTitle = TextFieldValue("The Tickler"),
            pasteContent = TextFieldValue("Feeling bored? Need a laugh? Introducing 'The Tickler'—the world's first feather-powered tickle machine! Say goodbye to stress and hello to giggles as 'The Tickler' tickles your funny bone and leaves you in stitches. Warning: Excessive laughter may cause sore cheeks and tears of joy. Use 'The Tickler' responsibly and embrace the hilarity!"),
            pasteSavingResource = Loading,
            onTitleInputChange = {},
            onContentInputChange = {},
            saveEnabled = false,
            inputEnabled = false,
            onSave = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PasteSavingFailurePreview() {
    PastebinTheme {
        PasteInput(
            pasteTitle = TextFieldValue("The Tickler"),
            pasteContent = TextFieldValue("Feeling bored? Need a laugh? Introducing 'The Tickler'—the world's first feather-powered tickle machine! Say goodbye to stress and hello to giggles as 'The Tickler' tickles your funny bone and leaves you in stitches. Warning: Excessive laughter may cause sore cheeks and tears of joy. Use 'The Tickler' responsibly and embrace the hilarity!"),
            pasteSavingResource = Failure(Throwable("Something went wrong")),
            onTitleInputChange = {},
            onContentInputChange = {},
            saveEnabled = true,
            inputEnabled = true,
            onSave = { _, _ -> },
        )
    }
}
