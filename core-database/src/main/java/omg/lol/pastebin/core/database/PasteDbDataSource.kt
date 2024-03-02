package omg.lol.pastebin.core.database

import android.provider.ContactsContract.Data
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import omg.lol.pastebin.core.database.pastebin.model.DbPaste
import omg.lol.pastebin.core.database.pastebin.model.PasteDao
import omg.lol.pastebin.core.model.DataResource
import javax.inject.Inject

interface PasteLocalDataSource {
    fun getPastes(): Flow<DataResource<List<DbPaste>>>
    suspend fun insertOrUpdatePaste(item: DbPaste): DataResource<String>
    suspend fun insertOrUpdatePastes(items: List<DbPaste>, overrideUnsynced: Boolean = true): DataResource<List<String>>
    suspend fun deletePaste(title: String): DataResource<Unit>

    /**
     * Deletes the given Pastes and returns the number of deleted Pastes.
     *
     * @param items the IDs of the Pastes to delete
     * @return the number of Pastes deleted, wrapped in a [DataResource]
     */
    suspend fun deletePastes(items: List<String>): DataResource<Int>

    /**
     * Deletes all Pastes and returns the number of deleted Pastes.
     *
     * @return the number of Pastes deleted, wrapped in a [DataResource]
     */
    suspend fun deleteAllPastes(): DataResource<Int>
    suspend fun markAsSynced(title: String): DataResource<String>
}

class PasteDbDataSource @Inject constructor(private val pasteDao: PasteDao, private val appDatabase: AppDatabase) : PasteLocalDataSource {
    override fun getPastes(): Flow<DataResource<List<DbPaste>>> =
        pasteDao.getPastes()
            .map<List<DbPaste>, DataResource<List<DbPaste>>> { DataResource.Success(it) }
            .catch { emit(DataResource.Failure.ClientError(it)) }

    override suspend fun insertOrUpdatePaste(item: DbPaste): DataResource<String> = try {
        pasteDao.insertOrUpdatePaste(item)
        DataResource.Success(item.title)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResource.Failure.ClientError(e)
    }

    override suspend fun insertOrUpdatePastes(
        items: List<DbPaste>,
        overrideUnsynced: Boolean
    ): DataResource<List<String>> = try {
        if (overrideUnsynced) {
            pasteDao.insertOrUpdatePastes(items)
        } else {
            appDatabase.withTransaction {
                val unsyncedPasteIds = pasteDao.getUnSyncedPastes().map { it.title }
                pasteDao.insertOrUpdatePastes(items.filter { it.title !in unsyncedPasteIds })
            }
        }
        DataResource.Success(items.map { it.title })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResource.Failure.ClientError(e)
    }

    override suspend fun deletePaste(title: String): DataResource<Unit> = try {
        pasteDao.deletePaste(title)
        DataResource.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResource.Failure.ClientError(e)
    }

    override suspend fun deletePastes(items: List<String>): DataResource<Int> = try {
        DataResource.Success(pasteDao.deletePastes(items))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResource.Failure.ClientError(e)
    }

    override suspend fun deleteAllPastes(): DataResource<Int> = try {
        DataResource.Success(pasteDao.deleteAllPastes())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResource.Failure.ClientError(e)
    }

    override suspend fun markAsSynced(title: String): DataResource<String> = try {
        pasteDao.markAsSynced(title)
        DataResource.Success(title)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResource.Failure.ClientError(e)
    }
}