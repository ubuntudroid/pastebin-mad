package omg.lol.pastebin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResult<T: Response>(
    @SerialName("request")
    val request: Request,
    @SerialName("response")
    val response: T,
) {
    @Serializable
    data class Request(
        @SerialName("status_code")
        val statusCode: Int,
        @SerialName("success")
        val success: Boolean
    )
}

@Serializable
abstract class Response {
    @SerialName("message")
    abstract val message: String
}