package omg.lol.pastebin.core.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import omg.lol.pastebin.core.l10n.R as CR

@Composable
fun ShowSnackbarForResource(
    resource: UiResource<*>?,
    snackbarHostState: SnackbarHostState,
    onRefreshClick: (() -> Unit)? = null
) {
    val unknownErrorMessage = stringResource(CR.string.error_unknown)
    val snackbarRefreshActionLabel = stringResource(CR.string.refresh)
    val userError = remember(resource) {
        (resource as? UiResource.Failure)?.let {
            it.throwable.message ?: unknownErrorMessage
        }
    }
    // We keep the snackbar on as long as the user doesn't dismiss it (independent from whether the
    // actual status of the resource is already back to success).
    if (
        snackbarHostState.currentSnackbarData != null  // keep snackbar visible as long as it's not dismissed
        || userError != null // keep snackbar visible or make it appear if there's an error
    ) {
        LaunchedEffect(snackbarHostState) {
            if (userError != null) { // check again if there's an error (as the resource may have changed since before the LaunchedEffect was queued)
                val result = snackbarHostState.showSnackbar(
                    message = userError,
                    actionLabel = snackbarRefreshActionLabel,
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed && onRefreshClick != null) {
                    onRefreshClick()
                }
            }
        }
    }
}