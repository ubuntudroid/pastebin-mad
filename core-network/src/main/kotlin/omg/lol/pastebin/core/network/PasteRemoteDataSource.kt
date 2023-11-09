package omg.lol.pastebin.core.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import omg.lol.pastebin.core.model.DataResource
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.network.model.ApiError
import omg.lol.pastebin.core.network.model.ApiResult
import omg.lol.pastebin.core.network.model.mapToPaste
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

interface PasteRemoteDataSource {
    suspend fun getPastebin(address: String, apiKey: String): DataResource<List<Paste>>
}

class PasteApiDataSource @Inject constructor(
    private val pastebinApi: PastebinApi,
    private val json: Json
) : PasteRemoteDataSource {

    override suspend fun getPastebin(address: String, apiKey: String): DataResource<List<Paste>> {
        return try {
            val apiResult = pastebinApi.getPastebin(address, apiKey)

            if (apiResult.request.success) {
                DataResource.Success(
                    apiResult.response.pastes.map { it.mapToPaste() }
                )
            } else {
                // server reported an error
                DataResource.Failure.RemoteError(
                    apiResult.request.statusCode,
                    apiResult.response.message
                )
            }
        } catch (e: ClientRequestException) {
            // bad request
            DataResource.Failure.RemoteError(
                e.response.status.value,
                e.response.getErrorMessage()
            )
        } catch (e: ServerResponseException) {
            // server error
            DataResource.Failure.RemoteError(
                e.response.status.value,
                e.response.getErrorMessage()
            )
        } catch (e: RedirectResponseException) {
            // redirect error
            DataResource.Failure.RemoteError(
                e.response.status.value,
                e.response.getErrorMessage()
            )
        } catch (e: ResponseException) {
            // some other request error
            DataResource.Failure.RemoteError(
                e.response.status.value,
                e.response.getErrorMessage()
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // something went wrong while executing the request, likely a network issue
            DataResource.Failure.ClientError(e)
        }
    }

    private suspend fun HttpResponse.getErrorMessage(): String {
        val response = bodyAsText(Charsets.UTF_8)
        return json.decodeFromString<ApiResult<ApiError>>(response).response.message
    }
}