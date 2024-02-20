package omg.lol.pastebin.core.database

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
    suspend fun insertOrUpdatePastes(items: List<DbPaste>): DataResource<List<String>>
}

class PasteDbDataSource @Inject constructor(private val pasteDao: PasteDao) : PasteLocalDataSource {
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

    override suspend fun insertOrUpdatePastes(items: List<DbPaste>): DataResource<List<String>> = try {
        pasteDao.insertOrUpdatePastes(items)
        DataResource.Success(items.map { it.title })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResource.Failure.ClientError(e)
    }
}