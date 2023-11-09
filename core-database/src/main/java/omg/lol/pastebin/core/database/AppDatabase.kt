

package omg.lol.pastebin.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import omg.lol.pastebin.core.database.pastebin.model.DbPaste
import omg.lol.pastebin.core.database.pastebin.model.PasteDao
import omg.lol.pastebin.core.database.user.model.DbUser
import omg.lol.pastebin.core.database.user.model.UserDao

@Database(entities = [DbPaste::class, DbUser::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pasteDao(): PasteDao
    abstract fun userDao(): UserDao
}
