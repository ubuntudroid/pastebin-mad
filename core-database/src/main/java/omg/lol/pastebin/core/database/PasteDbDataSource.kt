package omg.lol.pastebin.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import omg.lol.pastebin.core.database.pastebin.model.DbPaste
import omg.lol.pastebin.core.database.pastebin.model.PasteDao
import omg.lol.pastebin.core.model.DataResource
import javax.inject.Inject

interface PasteLocalDataSource {
    fun getPastes(): Flow<DataResource<List<DbPaste>>>
    suspend fun insertPaste(item: DbPaste): DataResource<Unit>
    suspend fun insertPastes(items: List<DbPaste>): DataResource<Unit>
}

class PasteDbDataSource @Inject constructor(private val pasteDao: PasteDao) : PasteLocalDataSource {
    override fun getPastes(): Flow<DataResource<List<DbPaste>>> =
        pasteDao.getPastes()
            .map<List<DbPaste>, DataResource<List<DbPaste>>> { DataResource.Success(it) }
            .catch { emit(DataResource.Failure.ClientError(it)) }

    override suspend fun insertPaste(item: DbPaste): DataResource<Unit> = try {
        pasteDao.insertPaste(item)
        DataResource.Success(Unit)
    } catch (e: Exception) {
        DataResource.Failure.ClientError(e)
    }

    override suspend fun insertPastes(items: List<DbPaste>): DataResource<Unit> = try {
        pasteDao.insertPastes(items)
        DataResource.Success(Unit)
    } catch (e: Exception) {
        DataResource.Failure.ClientError(e)
    }
}