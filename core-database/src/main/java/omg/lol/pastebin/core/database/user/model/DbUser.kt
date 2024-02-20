package omg.lol.pastebin.core.database.user.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import omg.lol.pastebin.core.model.user.User

@Entity
data class DbUser(
    val name: String,
    val apiKey: String
) {
    @PrimaryKey
    var uid: Int = 0
}

@Dao
interface UserDao {
    @Query("SELECT * FROM dbuser WHERE uid = 0")
    fun getUser(): Flow<DbUser?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(item: DbUser)

    @Delete
    suspend fun deleteUser(item: DbUser)
}

fun User.mapToDbUser() = DbUser(name = name, apiKey = apiKey)
fun DbUser.mapToUser() = User(name = name, apiKey = apiKey)