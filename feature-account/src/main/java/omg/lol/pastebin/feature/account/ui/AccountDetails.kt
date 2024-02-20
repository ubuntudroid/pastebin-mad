package omg.lol.pastebin.feature.account.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import omg.lol.pastebin.core.model.user.User
import omg.lol.pastebin.core.ui.UiResource
import omg.lol.pastebin.core.ui.theme.PastebinTheme
import omg.lol.pastebin.feature.account.R

@Composable
fun AccountDetails(
    userResource: UserResource?,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        if (userResource != null) {
            when (userResource) {
                // TODO instead of using the snackbar we could also show the error text and refresh
                //  button right here in the UI
                is UiResource.Failure -> { /* errors are handled by snackbar */ }

                UiResource.Loading -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }

                is UiResource.Success -> {
                    when (val user = userResource.data) {
                        null -> {
                            Text(
                                text = stringResource(R.string.user_not_found),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        else -> {
                            AccountInfo(
                                user = user,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountInfo(
    user: User,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // horizontally centered text
        Text(
            text = user.name,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountDetailsDefaultPreview() {
    PastebinTheme {
        AccountDetails(
            userResource = UiResource.Success(User(name = "name", apiKey = "123456"))
        )
    }
}