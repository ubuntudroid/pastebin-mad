package omg.lol.pastebin.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PostPasteData(
    val title: String,
    val content: String
)
