package omg.lol.pastebin.platform

import android.content.ClipData
import android.content.ClipboardManager
import javax.inject.Inject

interface ClipboardRepository {
    fun copyToClipboard(label: String, text: String)
    val primaryClip: ClipData?
}

class PlatformClipboardRepository @Inject constructor(
    private val clipboardManager: ClipboardManager
) : ClipboardRepository {

    override val primaryClip: ClipData?
        get() = clipboardManager.primaryClip

    override fun copyToClipboard(label: String, text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}