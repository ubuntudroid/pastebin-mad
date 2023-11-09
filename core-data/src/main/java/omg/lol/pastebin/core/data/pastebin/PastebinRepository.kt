package omg.lol.pastebin.core.data.pastebin

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import omg.lol.pastebin.core.data.user.UserRepository
import omg.lol.pastebin.core.database.PasteLocalDataSource
import omg.lol.pastebin.core.database.pastebin.model.DbPaste
import omg.lol.pastebin.core.database.pastebin.model.mapToDbPaste
import omg.lol.pastebin.core.database.pastebin.model.mapToPaste
import omg.lol.pastebin.core.model.DataResource
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.network.PasteRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

interface PastebinRepository {
    val pastes: Flow<DataResource<List<Paste>>>

    suspend fun add(title: String, content: String)
}

const val TAG = "PastebinRepository"

@Singleton
class DefaultPastebinRepository @Inject constructor(
    private val pasteLocalDataSource: PasteLocalDataSource,
    userRepository: UserRepository,
    private val pasteRemoteDataSource: PasteRemoteDataSource
) : PastebinRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val pastes: Flow<DataResource<List<Paste>>> =
        userRepository.user.flatMapLatest { user -> // refresh once user data changes
            flow {
                val user = requireNotNull(user) {
                    "No user found, cannot fetch pastes"
                }

                // load data from remote source (API)
                val pastebinDataRes =
                    pasteRemoteDataSource.getPastebin(user.name, user.apiKey).apply {
                        logIfFailure(TAG, "Failed to fetch pastebin")
                    }
                when (pastebinDataRes) {
                    is DataResource.Failure -> {
                        // there was some error fetching data from remote, use local data instead
                        pastebinDataRes.log(TAG, "Failed to fetch pastes from API, using local data")
                        emit(pastebinDataRes.mapFailure(
                            transformMessage = { "Failed to fetch pastes from API, using local data: $it" })
                        )
                        delay(1.seconds) // give the UI some time to show the error (not nice, but good enough for a test project)
                        emitAll(
                            pasteLocalDataSource.getPastes()
                                .map { items -> items.map { it.map { it.mapToPaste() } } }
                                .distinctUntilChanged()
                        )
                    }

                    is DataResource.Success -> try {
                        // insert fetched data into local data source (DB)
                        val dataResource =
                            pasteLocalDataSource.insertPastes(pastebinDataRes.data.map { it.mapToDbPaste() })
                        when (dataResource) {
                            is DataResource.Success -> {
                                emitAll(
                                    pasteLocalDataSource.getPastes()
                                        .map { items -> items.map { it.map { it.mapToPaste() } } }
                                        .distinctUntilChanged()
                                )
                            }

                            is DataResource.Failure -> {
                                // there was an error inserting the fetched data into the local
                                //  data source, give up and let the user refresh manually
                                emit(dataResource.mapFailure())
                            }
                        }
                    } catch (e: Exception) {
                        val errorMessage = "Client error while fetching and/or storing pastes: ${e.message}"
                        Log.e(TAG, errorMessage, e)
                        emit(DataResource.Failure.ClientError(RuntimeException(errorMessage, e)))
                    }
                }
            }
                .catch { emit(DataResource.Failure.ClientError(it)) }
        }

    override suspend fun add(title: String, content: String) {
        // TODO this would normally call the remote API first, but we forgo this for this test project
        //  as this involves a lot of additional handling (e.g. storing temporarily and trying later
        //  if API call fails with a recoverable error, making sure not to override non-synced changes
        //  when refreshing the list from remote etc.).
        //  This means that added pastes will only ever stay in the local database.
        try {
            // TODO the API actually requires more checks and/or clever auto-formatting, but this is
            //  out of scope of this test project
            require(!title.contains(" ")) {
                "Title mustn't contain spaces"
            }

            pasteLocalDataSource.insertPaste(
                DbPaste(
                    title = title,
                    content = content,
                    modifiedOnEpochSec = System.currentTimeMillis().milliseconds.inWholeSeconds
                )
            )
        } catch (e: Exception) {
            val errorMessage = "Client error while adding a paste: ${e.message}"
            Log.w(TAG, errorMessage, e)
            throw RuntimeException(errorMessage, e)
        }
    }
}
