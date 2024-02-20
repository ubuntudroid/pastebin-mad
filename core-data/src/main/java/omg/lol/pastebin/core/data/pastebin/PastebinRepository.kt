package omg.lol.pastebin.core.data.pastebin

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import omg.lol.pastebin.core.data.user.UserRepository
import omg.lol.pastebin.core.database.PasteLocalDataSource
import omg.lol.pastebin.core.database.pastebin.model.DbPaste
import omg.lol.pastebin.core.database.pastebin.model.mapToDbPaste
import omg.lol.pastebin.core.database.pastebin.model.mapToPaste
import omg.lol.pastebin.core.model.DataResource
import omg.lol.pastebin.core.model.DataResource.*
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.network.PasteRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

interface PastebinRepository {
    val pastes: Flow<DataResource<List<Paste>>>

    suspend fun insertOrUpdate(title: String, content: String): DataResource<String>
}

const val TAG = "PastebinRepository"

@Singleton
class DefaultPastebinRepository @Inject constructor(
    private val pasteLocalDataSource: PasteLocalDataSource,
    private val userRepository: UserRepository,
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
                    is Failure -> {
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

                    is Success -> try {
                        // insert fetched data into local data source (DB)
                        val dataResource =
                            pasteLocalDataSource.insertOrUpdatePastes(pastebinDataRes.data.map { it.mapToDbPaste() })
                        when (dataResource) {
                            is Success -> {
                                // collect from local data source
                                emitAll(
                                    pasteLocalDataSource.getPastes()
                                        .map { items -> items.map { it.map { it.mapToPaste() } } }
                                        .distinctUntilChanged()
                                )
                            }

                            is Failure -> {
                                // there was an error inserting the fetched data into the local
                                //  data source, give up and let the user refresh manually
                                emit(dataResource.mapFailure())
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        val errorMessage = "Client error while fetching and/or storing pastes: ${e.message}"
                        Log.e(TAG, errorMessage, e)
                        emit(Failure.ClientError(RuntimeException(errorMessage, e)))
                    }
                }
            }
                .catch { emit(Failure.ClientError(it)) }
        }

    override suspend fun insertOrUpdate(title: String, content: String): DataResource<String> {
        try {
            val user = requireNotNull(userRepository.user.firstOrNull())  {
                "No user found, cannot add a paste"
            }

            // TODO the API actually requires more checks and/or clever auto-formatting, but this is
            //  out of scope of this test project
            require(!title.contains(" ")) {
                "Title mustn't contain spaces"
            }

            val localResult = pasteLocalDataSource.insertOrUpdatePaste(
                DbPaste(
                    title = title,
                    content = content,
                    modifiedOnEpochSec = System.currentTimeMillis().milliseconds.inWholeSeconds
                )
            )

            return when (localResult) {
                is Failure -> {
                    // there was an error inserting the paste into the local
                    //  data source, give up and let the user retry
                    localResult
                }
                is Success -> {
                    val remoteResult = pasteRemoteDataSource.createOrUpdatePaste(
                        title = title,
                        content = content,
                        address = user.name,
                        apiKey = user.apiKey
                    )
                    when (remoteResult) {
                        is Failure -> {
                            // TODO #2 mark in DB to try again later if the error is recoverable
                            // - client error -> recoverable
                            // - remote error -> check based on status code (maybe introduce an
                            //     isRecoverable property in RemoteError which handles this
                            //     centrally?
                            // Marking should happen in the database. We will periodically walk
                            //   through marked entries in #7.

                            // not necessary to tell user about a recoverable error
                            return Success(title)
                        }
                        is Success -> {
                            // TODO #2 mark as synced in DB
                            return remoteResult
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val errorMessage = "Client error while adding a paste: ${e.message}"
            Log.e(TAG, errorMessage, e)
            return Failure.ClientError(RuntimeException(errorMessage, e))
        }
    }
}
