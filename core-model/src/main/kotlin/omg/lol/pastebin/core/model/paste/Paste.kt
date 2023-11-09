package omg.lol.pastebin.core.model.paste

import kotlin.time.Duration

data class Paste(
    val title: String,
    val content: String,
    val modifiedOn: Duration
)
