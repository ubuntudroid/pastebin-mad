package omg.lol.pastebin.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PostPasteResponse(
    val title: String,
    override val message: String
) : Response()
