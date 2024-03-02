package omg.lol.pastebin.core.model.paste

import kotlin.time.Duration

data class Paste(
    val title: String,
    val content: String,
    /**
     * Last modification time as [Duration]. Must only be set by server!
     */
    val modifiedOn: Duration = Duration.ZERO,
    val isSynced: Boolean = false
)
