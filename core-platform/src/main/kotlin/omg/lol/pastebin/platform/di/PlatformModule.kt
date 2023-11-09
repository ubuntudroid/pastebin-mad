package omg.lol.pastebin.platform.di

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import omg.lol.pastebin.platform.ClipboardRepository
import omg.lol.pastebin.platform.PlatformClipboardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlatformServiceModule {

    @Provides
    fun provideClipboardManager(@ApplicationContext context: Context): ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}

@Module
@InstallIn(SingletonComponent::class)
interface PlatformModule {

    @Singleton
    @Binds
    fun bindClipboardRepository(
        clipboardRepository: PlatformClipboardRepository
    ): ClipboardRepository
}

class FakeClipboardRepository @Inject constructor() : ClipboardRepository {
    private val data = mutableListOf<ClipData>()

    override val primaryClip: ClipData?
        get() = data.firstOrNull()

    override fun copyToClipboard(label: String, text: String) {
        data.add(ClipData.newPlainText(label, text))
    }
}