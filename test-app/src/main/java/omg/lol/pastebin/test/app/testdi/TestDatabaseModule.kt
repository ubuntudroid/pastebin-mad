package omg.lol.pastebin.test.app.testdi

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import omg.lol.pastebin.core.data.di.DataModule
import omg.lol.pastebin.core.data.di.FakePastebinRepository
import omg.lol.pastebin.core.data.di.FakeUserRepository
import omg.lol.pastebin.core.data.pastebin.PastebinRepository
import omg.lol.pastebin.core.data.user.UserRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class]
)
interface FakeDataModule {

    @Binds
    fun bindRepository(
        fakeRepository: FakePastebinRepository
    ): PastebinRepository

    @Binds
    fun bindUserRepository(
        fakeRepository: FakeUserRepository
    ): UserRepository
}
