package omg.lol.pastebin.feature.pastes.ui

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.ui.theme.PastebinTheme
import omg.lol.pastebin.feature.pastes.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
            if (paste.modifiedOn == Duration.ZERO) {
                null
            } else {
                DateUtils.getRelativeTimeSpanString(
                    paste.modifiedOn.inWholeMilliseconds,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = relativeModifiedOn?.toString() ?: stringResource(R.string.paste_not_synced),
                style = MaterialTheme.typography.labelSmall
            )
            if (!paste.isSynced) {
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    imageVector = Icons.Filled.Sync,
                    contentDescription = "Syncing icon",
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier
                        .width(12.dp)
                        .height(12.dp)
                )
            }
        }
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

@Preview
@Composable
fun PasteItemPreview() {
    PastebinTheme {
        PasteItem(
            paste = Paste(
                title = "Test title",
                content = "Test content",
                modifiedOn = System.currentTimeMillis().milliseconds,
                isSynced = false
            ),
            onClick = {}
        )
    }
}