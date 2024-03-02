package omg.lol.pastebin.core.database.pastebin.model

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import omg.lol.pastebin.core.model.paste.Paste
import kotlin.time.Duration.Companion.seconds

@Entity
data class DbPaste(
    @PrimaryKey
    val title: String,
    val content: String,
    val modifiedOnEpochSec: Long,
    @ColumnInfo(defaultValue = "0")
    val isSynced: Boolean = false
)

fun Paste.mapToDbPaste() = DbPaste(
    title = title,
    content = content,
    modifiedOnEpochSec = modifiedOn.inWholeSeconds,
    isSynced = isSynced
)
fun DbPaste.mapToPaste() = Paste(
    title = title,
    content = content,
    modifiedOn = modifiedOnEpochSec.seconds,
    isSynced = isSynced
)

@Dao
interface PasteDao {
    @Query("SELECT * FROM dbpaste ORDER BY modifiedOnEpochSec DESC LIMIT 25")
    fun getPastes(): Flow<List<DbPaste>>

    @Query("SELECT * FROM dbpaste WHERE isSynced = 0")
    fun getUnSyncedPastes(): List<DbPaste>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePaste(item: DbPaste)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePastes(items: List<DbPaste>)

    @Query("DELETE FROM dbpaste WHERE title = :title")
    suspend fun deletePaste(title: String): Int

    @Query("DELETE FROM dbpaste WHERE title IN (:items)")
    suspend fun deletePastes(items: List<String>): Int

    @Query("DELETE FROM dbpaste")
    suspend fun deleteAllPastes(): Int

    @Query("UPDATE dbpaste SET isSynced = 1 WHERE title = :title")
    suspend fun markAsSynced(title: String)
}
