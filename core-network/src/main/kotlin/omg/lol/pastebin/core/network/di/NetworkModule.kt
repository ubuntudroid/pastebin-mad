package omg.lol.pastebin.core.network.di

import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.DefaultJson
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import omg.lol.pastebin.core.model.DataResource
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.network.PasteApiDataSource
import omg.lol.pastebin.core.network.PasteRemoteDataSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
interface NetworkModule {

    @Binds
    fun bindsPasteRemoteDataSource(
        pasteRemoteDataSource: PasteApiDataSource
    ): PasteRemoteDataSource

    companion object {
        @Provides
        fun provideJson(): Json = DefaultJson

        @Provides
        fun provideHttpClient(json: Json): HttpClient {
            return HttpClient(OkHttp) {
                expectSuccess = true
                defaultRequest {
                    url("https://api.omg.lol/address/")
                }
                install(ContentNegotiation) {
                    json(json = json)
                }
                install(HttpTimeout) {
                    val timeout = 30000L
                    connectTimeoutMillis = timeout
                    requestTimeoutMillis = timeout
                    socketTimeoutMillis = timeout
                }
                install(Logging) {
                    logger = object : io.ktor.client.plugins.logging.Logger {
                        override fun log(message: String) {
                            Log.v("Ktor", message)
                        }
                    }
                    level = LogLevel.HEADERS
                }
            }
        }
    }
}

class FakePasteRemoteDataSource @AssistedInject constructor(
    @Assisted pastes: List<Paste>
) : PasteRemoteDataSource {

    private val pastes = pastes.associateBy { it.title }.toMutableMap()

    override suspend fun getPastebin(
        address: String,
        apiKey: String
    ): DataResource<List<Paste>> =
        DataResource.Success(pastes.values.toList())

    override suspend fun createOrUpdatePaste(
        title: String,
        content: String,
        address: String,
        apiKey: String
    ): DataResource<String> {
        pastes[title] = Paste(
            title = title,
            content = content,
            modifiedOn = System.currentTimeMillis().milliseconds
        )
        return DataResource.Success(title)
    }
}

@AssistedFactory
interface FakePasteRemoteDataSourceFactory {
    fun create(pastes: List<Paste>): FakePasteRemoteDataSource;
}