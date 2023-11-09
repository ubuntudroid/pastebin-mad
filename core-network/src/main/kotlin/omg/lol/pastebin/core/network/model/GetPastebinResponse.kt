package omg.lol.pastebin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPastebinResponse(
    @SerialName("pastebin")
    val pastes: List<ApiPaste>,
    override val message: String
) : Response()
