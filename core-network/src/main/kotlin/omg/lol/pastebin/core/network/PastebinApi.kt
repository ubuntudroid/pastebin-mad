package omg.lol.pastebin.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.http.appendEncodedPathSegments
import io.ktor.http.takeFrom
import omg.lol.pastebin.core.network.model.ApiResult
import omg.lol.pastebin.core.network.model.GetPastebinResponse
import javax.inject.Inject

private const val baseUrl = "https://api.omg.lol/address/"


class PastebinApi @Inject constructor(private val httpClient: HttpClient) {
    // Unfortunately the API doesn't support paging or conditional GET requests yet,
    //  but I've notified them and they've added it to their roadmap.
    suspend fun getPastebin(address: String, apiKey: String): ApiResult<GetPastebinResponse> {
        return httpClient.get {
            url {
                takeFrom(baseUrl)
                appendEncodedPathSegments(address, "pastebin")
            }
            headers {
                append(HttpHeaders.Authorization, "Bearer $apiKey")
            }
        }.body<ApiResult<GetPastebinResponse>>()
    }
}