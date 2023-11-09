package omg.lol.pastebin.feature.pastes.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.ui.UiResource

@Composable
fun PastesList(
    pastesResource: UiResource.Success<List<Paste>>,
    onPasteClick: (Paste) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp),
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(pastesResource.data) { index, paste ->
            PasteItem(
                paste = paste,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onPasteClick(paste) }
            )
            if (index < pastesResource.data.size - 1) {
                Divider()
            }
        }
    }
}