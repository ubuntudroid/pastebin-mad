

package omg.lol.pastebin.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import omg.lol.pastebin.core.data.di.FakeUserRepository
import omg.lol.pastebin.core.data.di.fakePastes
import omg.lol.pastebin.core.data.pastebin.DefaultPastebinRepository
import omg.lol.pastebin.core.database.di.FakePasteLocalDataSource
import omg.lol.pastebin.core.model.DataResource
import omg.lol.pastebin.core.network.di.FakePasteRemoteDataSource
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [DefaultPastebinRepository].
 */
class DefaultPastebinRepositoryTest {

    @Test
    fun pastes_newItemSaved_itemIsReturned() = runTest {
        val repository = DefaultPastebinRepository(
            FakePasteLocalDataSource(),
            FakeUserRepository(),
            FakePasteRemoteDataSource(data = DataResource.Success(fakePastes))
        )

        repository.add("Repository", "Test")

        assertEquals((repository.pastes.first() as DataResource.Success).data[0].title, "Repository")
    }

}
