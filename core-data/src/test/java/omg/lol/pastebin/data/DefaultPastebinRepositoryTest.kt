package omg.lol.pastebin.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import omg.lol.pastebin.core.data.di.FakeUserRepository
import omg.lol.pastebin.core.data.di.fakePastes
import omg.lol.pastebin.core.data.pastebin.DefaultPastebinRepository
import omg.lol.pastebin.core.database.di.FakePasteLocalDataSource
import omg.lol.pastebin.core.model.DataResource
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.network.di.FakePasteRemoteDataSource
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [DefaultPastebinRepository].
 */
class DefaultPastebinRepositoryTest {

    @Test
    fun pastes_noItemsSaved_emptyListIsReturned() = runTest {
        val repository = DefaultPastebinRepository(
            FakePasteLocalDataSource(),
            FakeUserRepository(),
            FakePasteRemoteDataSource(pastes = emptyList())
        )

        assertEquals(
            DataResource.Success(emptyList<Paste>()),
            repository.pastes.first()
        )
    }

    @Test
    fun pastes_itemsExist_itemsAreReturned() = runTest {
        val repository = DefaultPastebinRepository(
            FakePasteLocalDataSource(),
            FakeUserRepository(),
            FakePasteRemoteDataSource(pastes = fakePastes)
        )

        assertEquals(
            DataResource.Success(fakePastes),
            repository.pastes.first()
        )
    }

    @Test
    fun pastes_newItemSaved_titleIsReturned() = runTest {
        val repository = DefaultPastebinRepository(
            FakePasteLocalDataSource(),
            FakeUserRepository(),
            FakePasteRemoteDataSource(pastes = fakePastes)
        )

        repository.insertOrUpdate("Repository", "Test")

        assertEquals(
            "Repository",
            (repository.pastes.first() as DataResource.Success).data.maxByOrNull { it.modifiedOn }!!.title
        )
    }

    @Test
    fun pastes_existingItemUpdated_titleIsReturned() = runTest {
        val repository = DefaultPastebinRepository(
            FakePasteLocalDataSource(),
            FakeUserRepository(),
            FakePasteRemoteDataSource(pastes = fakePastes)
        )

        val firstPaste = fakePastes[0].copy(title = "Repository", content = "Test")
        val updatedPaste = repository.insertOrUpdate(firstPaste.title, firstPaste.content)

        assertEquals(
            firstPaste.title,
            (updatedPaste as DataResource.Success).data
        )
    }
}
