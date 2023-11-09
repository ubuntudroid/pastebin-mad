package omg.lol.pastebin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    @SerialName("see-also")
    val seeAlso: List<String>? = null,
    override val message: String
) : Response()