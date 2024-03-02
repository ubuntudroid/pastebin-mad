package omg.lol.pastebin.core.model

import android.util.Log

sealed class DataResource<out T> {

    data class Success<out T>(val data: T) : DataResource<T>()

    sealed class Failure<out T> : DataResource<T>() {
        data class ClientError<out T>(
            val error: Throwable,
            val message: String? = error.message
        ) : Failure<T>() {
            override val isRecoverable: Boolean = true
        }

        data class RemoteError<out T>(
            val statusCode: Int,
            val message: String,
        ) : Failure<T>() {
            override val isRecoverable: Boolean = statusCode in listOf(408, 425, 429, 500, 502, 503, 504)
        }

        fun <R> mapFailure(transformMessage: (oldMessage: String) -> String = { it }): DataResource<R> =
            when (this) {
                is ClientError -> ClientError(error, transformMessage(error.message ?: ""))
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

        abstract val isRecoverable: Boolean
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