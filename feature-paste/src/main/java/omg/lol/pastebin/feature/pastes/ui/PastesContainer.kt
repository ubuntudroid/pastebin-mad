package omg.lol.pastebin.feature.pastes.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.ui.UiResource
import omg.lol.pastebin.feature.pastes.R

@Composable
fun PastesContainer(
    pastesResource: PastesResource?,
    onPasteClick: (Paste) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        if (pastesResource != null) {
            when (pastesResource) {
                is UiResource.Failure -> { /* errors are handled by snackbar */ }

                UiResource.Loading -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }

                is UiResource.Success -> {
                    if (pastesResource.data.isEmpty()) {
                        Text(
                            text = stringResource(R.string.pastes_not_found),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    PastesList(
                        pastesResource = pastesResource,
                        onPasteClick = onPasteClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}