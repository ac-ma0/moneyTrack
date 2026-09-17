package ph.moneytrack.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("moneytrack.session", Context.MODE_PRIVATE)
    var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(value) { prefs.edit().putString("access_token", value).apply() }
    fun clear() { prefs.edit().clear().apply() }
}

class SupabaseSyncRepository(
    private val baseUrl: String,
    private val apiKey: String,
    private val session: SessionStore,
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun upload(item: SyncQueue): Boolean = withContext(Dispatchers.IO) {
        val token = session.accessToken ?: return@withContext false
        val table = when (item.recordType) {
            "income" -> "income"
            "expense" -> "expenses"
            "debt" -> "debts"
            "debt_monthly_payment" -> "debt_monthly_payments"
            else -> return@withContext false
        }
        val deleting = item.operation == "delete"
        val request = Request.Builder()
            .url("$baseUrl/rest/v1/$table" + if (deleting) "?id=eq.${item.recordId}" else "")
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
            .addHeader("Content-Type", "application/json")
            .method(if (deleting) "PATCH" else "POST", item.payload.toRequestBody("application/json".toMediaType()))
            .build()
        repeat(3) { attempt ->
            try {
                client.newCall(request).execute().use { if (it.isSuccessful) return@withContext true }
            } catch (_: IOException) {
                if (attempt == 2) return@withContext false
            }
            Thread.sleep((attempt + 1) * 250L)
        }
        false
    }

    suspend fun pull(table: String, updatedAfter: Long = 0L): String = withContext(Dispatchers.IO) {
        val token = session.accessToken ?: throw IOException("No authenticated Supabase session")
        val request = Request.Builder()
            .url("$baseUrl/rest/v1/$table?updated_at=gt.${updatedAfter / 1000}")
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        client.newCall(request).execute().use {
            if (!it.isSuccessful) throw IOException("Pull failed ${it.code}")
            it.body?.string().orEmpty()
        }
    }
}

class ConnectivitySyncTrigger(context: Context, private val drain: suspend () -> Unit) {
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val callbackScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            callbackScope.launch { drain() }
        }
    }
    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) manager.registerDefaultNetworkCallback(callback)
    }
    fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try { manager.unregisterNetworkCallback(callback) } catch (_: IllegalArgumentException) { }
        }
        callbackScope.coroutineContext.cancel()
    }
}
