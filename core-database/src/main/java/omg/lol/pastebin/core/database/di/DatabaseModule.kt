

package omg.lol.pastebin.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import omg.lol.pastebin.core.database.AppDatabase
import omg.lol.pastebin.core.database.PasteDbDataSource
import omg.lol.pastebin.core.database.PasteLocalDataSource
import omg.lol.pastebin.core.database.pastebin.model.DbPaste
import omg.lol.pastebin.core.database.pastebin.model.PasteDao
import omg.lol.pastebin.core.database.user.model.UserDao
import omg.lol.pastebin.core.model.DataResource
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DatabaseModule {

    @Binds
    abstract fun bindsPasteLocalDataSource(
        pasteLocalDataSource: PasteDbDataSource
    ): PasteLocalDataSource

    companion object {
        @Provides
        fun providePasteDao(appDatabase: AppDatabase): PasteDao {
            return appDatabase.pasteDao()
        }

        @Provides
        fun provideUserDao(appDatabase: AppDatabase): UserDao {
            return appDatabase.userDao()
        }

        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                "Pastebin"
            ).build()
        }
    }
}

class FakePasteLocalDataSource @Inject constructor() : PasteLocalDataSource {
    private val pastesFlow = MutableStateFlow(mutableMapOf<String, DbPaste>())

    override fun getPastes(): Flow<DataResource<List<DbPaste>>> =
        pastesFlow.map { DataResource.Success(it.values.toList()) }

    override suspend fun insertOrUpdatePaste(item: DbPaste): DataResource<String> {
        pastesFlow.value[item.title] = item
        return DataResource.Success(item.title)
    }

    override suspend fun insertOrUpdatePastes(items: List<DbPaste>): DataResource<List<String>> {
        pastesFlow.value.putAll(items.associateBy { it.title })
        return DataResource.Success(items.map { it.title })
    }

}
