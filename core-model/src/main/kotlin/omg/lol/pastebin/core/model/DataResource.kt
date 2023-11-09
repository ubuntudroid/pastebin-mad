package omg.lol.pastebin.core.model

import android.util.Log

sealed class DataResource<out T> {

    data class Success<out T>(val data: T) : DataResource<T>()

    sealed class Failure<out T> : DataResource<T>() {
        data class ClientError<out T>(
            val error: Throwable
        ) : Failure<T>()

        data class RemoteError<out T>(
            val statusCode: Int,
            val message: String,
        ) : Failure<T>()

        fun <R> mapFailure(transformMessage: (oldMessage: String) -> String = { it }): DataResource<R> =
            when (this) {
                is ClientError -> ClientError(error)
                is RemoteError -> RemoteError(statusCode, transformMessage(message))
            }

        fun log(tag: String, message: String) {
            when (this) {
                is ClientError -> {
                    Log.e(tag, message, error)
                }

                is RemoteError -> {
                    Log.e(tag, "${message}: $statusCode - ${this.message}")
                }
            }
        }
    }

    fun logIfFailure(tag: String, message: String) {
        if (this is Failure) {
            log(tag, message)
        }
    }

    fun <R> map(transform: (T) -> R): DataResource<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure.ClientError -> Failure.ClientError(error)
        is Failure.RemoteError -> Failure.RemoteError(statusCode, message)
    }
}