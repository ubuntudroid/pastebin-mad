package omg.lol.pastebin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import omg.lol.pastebin.core.model.paste.Paste
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ApiPaste(
    @SerialName("title")
    val title: String,
    @SerialName("content")
    val content: String,
    @SerialName("listed")
    val isListed: Int?,
    @SerialName("modified_on")
    val modifiedOnEpochSec: Long
)

fun ApiPaste.mapToPaste() = Paste(title = title, content = content, modifiedOn = modifiedOnEpochSec.seconds)
fun Paste.mapToApiPaste() = ApiPaste(title = title, content = content, isListed = null, modifiedOnEpochSec = modifiedOn.inWholeSeconds)