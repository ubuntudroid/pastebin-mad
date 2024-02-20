package omg.lol.pastebin.feature.pastes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import omg.lol.pastebin.core.ui.UiResource
import omg.lol.pastebin.feature.pastes.R
import omg.lol.pastebin.core.l10n.R as CR

@Composable
fun PasteInput(
    pasteTitle: TextFieldValue,
    onTitleInputChange: (TextFieldValue) -> Unit,
    pasteSavingResource: PasteSavingResource?,
    saveEnabled: Boolean,
    inputEnabled: Boolean,
    onSave: (title: String, content: String) -> Unit,
    pasteContent: TextFieldValue,
    onContentInputChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = pasteTitle,
                    onValueChange = { onTitleInputChange(it) },
                    label = { Text(stringResource(id = R.string.title_input_label)) },
                    enabled = inputEnabled,
                    isError = pasteSavingResource is UiResource.Failure,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    enabled = saveEnabled,
                    modifier = Modifier.width(96.dp).align(Alignment.CenterVertically),
                    onClick = { onSave(pasteTitle.text, pasteContent.text) }
                ) {
                    when (pasteSavingResource) {
                        is UiResource.Loading -> CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )

                        else -> Text(stringResource(CR.string.save))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = pasteContent,
                onValueChange = { onContentInputChange(it) },
                label = { Text(stringResource(id = R.string.content_input_label)) },
                enabled = inputEnabled,
                isError = pasteSavingResource is UiResource.Failure,
                supportingText = {
                    if (pasteSavingResource is UiResource.Failure) {
                        Text(
                            pasteSavingResource.throwable.message
                                ?: stringResource(CR.string.error_unknown)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}