package omg.lol.pastebin.core.database.pastebin.model

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
    val modifiedOnEpochSec: Long
)

fun Paste.mapToDbPaste() = DbPaste(title = title, content = content, modifiedOnEpochSec = modifiedOn.inWholeSeconds)
fun DbPaste.mapToPaste() = Paste(title = title, content = content, modifiedOn = modifiedOnEpochSec.seconds)

@Dao
interface PasteDao {
    @Query("SELECT * FROM dbpaste ORDER BY modifiedOnEpochSec DESC LIMIT 25")
    fun getPastes(): Flow<List<DbPaste>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePaste(item: DbPaste)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePastes(items: List<DbPaste>)

    @Delete
    suspend fun deletePaste(item: DbPaste)

    @Delete
    suspend fun deletePastes(items: List<DbPaste>)

    @Query("DELETE FROM dbpaste")
    suspend fun deleteAllPastes()
}
