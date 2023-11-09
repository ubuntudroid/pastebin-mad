package omg.lol.pastebin.feature.pastes.ui

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import omg.lol.pastebin.core.model.paste.Paste

@Composable
fun PasteItem(
    paste: Paste,
    onClick: (Paste) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick(paste) }
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        val relativeModifiedOn = remember(paste.modifiedOn) {
            DateUtils.getRelativeTimeSpanString(
                paste.modifiedOn.inWholeMilliseconds,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
        }
        Text(
            text = relativeModifiedOn.toString(),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = paste.title,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = paste.content,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
        )
    }
}