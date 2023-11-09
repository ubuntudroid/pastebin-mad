package omg.lol.pastebin.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import omg.lol.pastebin.core.data.pastebin.DefaultPastebinRepository
import omg.lol.pastebin.core.data.pastebin.PastebinRepository
import omg.lol.pastebin.core.data.user.DbBackedUserRepository
import omg.lol.pastebin.core.data.user.UserRepository
import omg.lol.pastebin.core.model.DataResource
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.model.user.User
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindsPasteRepository(
        pasteRepository: DefaultPastebinRepository
    ): PastebinRepository

    @Singleton
    @Binds
    fun bindsUserRepository(
        userRepository: DbBackedUserRepository
    ): UserRepository
}

@VisibleForTesting
class FakePastebinRepository @Inject constructor() : PastebinRepository {
    private val data = mutableListOf<Paste>()

    override val pastes: Flow<DataResource<List<Paste>>>
        get() = flow { emit(DataResource.Success(data.toList())) }

    override suspend fun add(title: String, content: String) {
        data.add(0, Paste(title, content, System.currentTimeMillis().milliseconds))
    }
}

@VisibleForTesting
val fakePastes = listOf(
    Paste("One Title", "One Content", 1.seconds),
    Paste("Two Title", "Two Content", 2.seconds),
    Paste("Three Title", "Three Content", 3.seconds)
)

@VisibleForTesting
class FakeUserRepository @Inject constructor() : UserRepository {
    private var _user: MutableStateFlow<User?> = MutableStateFlow(fakeUser)
    override val user: Flow<User?> = _user.asStateFlow()

    override suspend fun login(name: String, apiKey: String) {
        _user.value = User(name, apiKey)
    }
}

val fakeUser = User("arthur_dent", "12345")