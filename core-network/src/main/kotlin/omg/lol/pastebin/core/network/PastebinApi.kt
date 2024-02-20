package omg.lol.pastebin.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.appendEncodedPathSegments
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.internal.writeJson
import omg.lol.pastebin.core.model.paste.Paste
import omg.lol.pastebin.core.network.model.ApiResult
import omg.lol.pastebin.core.network.model.GetPastebinResponse
import omg.lol.pastebin.core.network.model.PostPasteData
import omg.lol.pastebin.core.network.model.PostPasteResponse
import javax.inject.Inject


class PastebinApi @Inject constructor(private val httpClient: HttpClient) {
    // Unfortunately the API doesn't support paging or conditional GET requests yet,
    //  but I've notified them and they've added it to their roadmap.
    suspend fun getPastebin(
        address: String,
        apiKey: String
    ): ApiResult<GetPastebinResponse> =
        httpClient.get {
            url {
                appendEncodedPathSegments(address, "pastebin")
            }
            headers {
                append(HttpHeaders.Authorization, "Bearer $apiKey")
            }
        }.body<ApiResult<GetPastebinResponse>>()

    suspend fun createOrUpdatePaste(
        title: String,
        content: String,
        address: String,
        apiKey: String
    ): ApiResult<PostPasteResponse> =
        httpClient.post {
            url {
                appendEncodedPathSegments(address, "pastebin")
            }
            headers {
                append(HttpHeaders.Authorization, "Bearer $apiKey")
            }
            contentType(ContentType.Application.Json)
            setBody(PostPasteData(title, content))
        }.body<ApiResult<PostPasteResponse>>()
}