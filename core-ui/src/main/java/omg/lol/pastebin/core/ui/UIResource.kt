package omg.lol.pastebin.core.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import omg.lol.pastebin.core.model.DataResource

sealed interface UiResource<out T> {
    // TODO having a loading state with optional data can be beneficial, e.g. when refreshing data
    data object Loading : UiResource<Nothing>
    data class Failure(val throwable: Throwable) : UiResource<Nothing>
    data class Success<T>(val data: T) : UiResource<T>
}

fun <T> Flow<T>.toCatchingUiResourceFlow(): Flow<UiResource<T>> =
    map<T, UiResource<T>> { UiResource.Success(it) }
        .catch { emit(UiResource.Failure(it)) }

fun <T> Flow<DataResource<T>>.toCatchingUiResourceFlow(
    clientErrorMessage: (throwable: Throwable) -> String = { "${it.message}: ${it.cause?.message ?: ""}"},
    remoteErrorMessage: (statusCode: Int, message: String) -> String = { statusCode, message -> "$statusCode: $message" }
): Flow<UiResource<T>> = map {
    it.mapToUiResource(clientErrorMessage, remoteErrorMessage)
}
    .catch { emit(UiResource.Failure(it)) }

fun <T> DataResource<T>.mapToUiResource(
    clientErrorMessage: (throwable: Throwable) -> String = { "${it.message}: ${it.cause?.message ?: ""}"},
    remoteErrorMessage: (statusCode: Int, message: String) -> String = { statusCode, message -> "$statusCode: $message" }
): UiResource<T> = when (this) {
    is DataResource.Success -> UiResource.Success(data)
    is DataResource.Failure.ClientError -> UiResource.Failure(
        ClientException(clientErrorMessage(error), error)
    )
    is DataResource.Failure.RemoteError -> UiResource.Failure(
        ServerException(remoteErrorMessage(statusCode, message))
    )
}

class ClientException(message: String, cause: Throwable) : RuntimeException(message, cause)
class ServerException(message: String) : RuntimeException(message)
