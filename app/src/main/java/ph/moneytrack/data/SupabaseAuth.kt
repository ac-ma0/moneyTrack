package ph.moneytrack.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SupabaseAuth(
    private val url: String,
    private val publishableKey: String,
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun signIn(email: String, password: String): Result<String> = request(
        "/auth/v1/token?grant_type=password",
        "{\"email\":\"${escape(email)}\",\"password\":\"${escape(password)}\"}"
    )

    suspend fun signUp(email: String, password: String): Result<String> = request(
        "/auth/v1/signup",
        "{\"email\":\"${escape(email)}\",\"password\":\"${escape(password)}\"}"
    )

    private suspend fun request(path: String, json: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url.trimEnd('/') + path)
                .addHeader("apikey", publishableKey)
                .addHeader("Content-Type", "application/json")
                .post(json.toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(response.body?.string().orEmpty())
                else Result.failure(IOException("Supabase authentication failed (${response.code})"))
            }
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun escape(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")
}
