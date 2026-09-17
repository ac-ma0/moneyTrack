package ph.moneytrack.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("moneytrack.session", Context.MODE_PRIVATE)
    var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(value) { if (value == null) prefs.edit().remove("access_token").apply() else prefs.edit().putString("access_token", value).apply() }
    var userId: String?
        get() = prefs.getString("user_id", null)
        set(value) { if (value == null) prefs.edit().remove("user_id").apply() else prefs.edit().putString("user_id", value).apply() }
    var refreshToken: String?
        get() = prefs.getString("refresh_token", null)
        set(value) { if (value == null) prefs.edit().remove("refresh_token").apply() else prefs.edit().putString("refresh_token", value).apply() }
    var role: String
        get() = prefs.getString("role", "user") ?: "user"
        set(value) { prefs.edit().putString("role", value).apply() }
    fun setPermissions(values: Set<String>) {
        prefs.edit().putStringSet("permissions", values).apply()
    }
    fun permissions(): Set<String> = prefs.getStringSet("permissions", emptySet()) ?: emptySet()
    fun clear() { prefs.edit().clear().apply() }
}

class SupabaseSyncRepository(
    private val baseUrl: String,
    private val apiKey: String,
    private val session: SessionStore,
    private val client: OkHttpClient = OkHttpClient()
) {
    var lastError: String? = null
        private set

    suspend fun refreshAccessPolicy(): Boolean = withContext(Dispatchers.IO) {
        val token = session.accessToken ?: return@withContext false
        val uid = session.userId ?: return@withContext false
        val profileRequest = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/rest/v1/profiles?id=eq.$uid&select=role")
            .addHeader("apikey", apiKey).addHeader("Authorization", "Bearer " + token).get().build()
        val permissionRequest = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/rest/v1/user_permissions?user_id=eq.$uid&select=permission")
            .addHeader("apikey", apiKey).addHeader("Authorization", "Bearer " + token).get().build()
        try {
            client.newCall(profileRequest).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                val rows = JSONArray(response.body?.string().orEmpty())
                if (rows.length() > 0) session.role = rows.optJSONObject(0)?.optString("role", "user") ?: "user"
            }

            client.newCall(permissionRequest).execute().use { response ->
                if (!response.isSuccessful) return@withContext false
                val rows = JSONArray(response.body?.string().orEmpty())
                val permissions = mutableSetOf<String>()
                for (i in 0 until rows.length()) rows.optJSONObject(i)?.optString("permission")?.let { permissions.add(it) }
                session.setPermissions(permissions)
            }

            true
        } catch (error: IOException) {
            lastError = "Permission fetch failed: ${error.message}"
            false
        }
    }

    suspend fun fetchProfiles(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val token = session.accessToken ?: return@withContext emptyList()
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/rest/v1/profiles?select=id,full_name,role&order=created_at.asc")
            .addHeader("apikey", apiKey).addHeader("Authorization", "Bearer " + token).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("User fetch failed (${response.code})")
            val rows = JSONArray(response.body?.string().orEmpty())
            val result = mutableListOf<Pair<String, String>>()
            for (i in 0 until rows.length()) {
                val row = rows.optJSONObject(i) ?: continue
                result.add((row.optString("id") + " • " + row.optString("full_name", "Unnamed")) to row.optString("role", "user"))
            }
            result
        }
    }

    suspend fun updateProfileRole(userId: String, role: String): Boolean = withContext(Dispatchers.IO) {
        val token = session.accessToken ?: return@withContext false
        val payload = JSONObject().put("id", userId).put("role", role).toString()
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/rest/v1/profiles?on_conflict=id")
            .addHeader("apikey", apiKey).addHeader("Authorization", "Bearer " + token)
            .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
            .addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType())).build()
        client.newCall(request).execute().use { it.isSuccessful }
    }

    suspend fun setPermission(userId: String, permission: String, granted: Boolean): Boolean = withContext(Dispatchers.IO) {
        val token = session.accessToken ?: return@withContext false
        val encodedPermission = java.net.URLEncoder.encode(permission, "UTF-8")
        val request = if (granted) Request.Builder()
            .url(baseUrl.trimEnd('/') + "/rest/v1/user_permissions?on_conflict=user_id,permission")
            .addHeader("apikey", apiKey).addHeader("Authorization", "Bearer " + token)
            .addHeader("Prefer", "resolution=merge-duplicates,return=minimal").addHeader("Content-Type", "application/json")
            .post(JSONObject().put("user_id", userId).put("permission", permission).put("granted_by", session.userId).toString().toRequestBody("application/json".toMediaType())).build()
        else Request.Builder()
            .url(baseUrl.trimEnd('/') + "/rest/v1/user_permissions?user_id=eq.$userId&permission=eq.$encodedPermission")
            .addHeader("apikey", apiKey).addHeader("Authorization", "Bearer " + token).delete().build()
        client.newCall(request).execute().use { it.isSuccessful }
    }

    suspend fun createUser(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val payload = JSONObject().put("email", email).put("password", password).toString()
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/auth/v1/signup")
            .addHeader("apikey", apiKey).addHeader("Content-Type", "application/json")
            .post(payload.toRequestBody("application/json".toMediaType())).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                lastError = "User creation failed (${response.code}): ${response.body?.string().orEmpty().take(160)}"
            }
            response.isSuccessful
        }
    }

    suspend fun refreshSession(): Boolean = withContext(Dispatchers.IO) {
        val refresh = session.refreshToken ?: return@withContext false
        val body = JSONObject().put("refresh_token", refresh).toString()
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/auth/v1/token?grant_type=refresh_token")
            .addHeader("apikey", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                lastError = "Session refresh failed (${response.code}). Sign in again."
                return@withContext false
            }
            val json = JSONObject(text)
            val access = json.optString("access_token", "")
            if (access.isEmpty()) return@withContext false
            session.accessToken = access
            json.optString("refresh_token", "").takeIf { it.isNotEmpty() }?.let { session.refreshToken = it }
            true
        }
    }

    suspend fun upload(item: SyncQueue): Boolean = withContext(Dispatchers.IO) {
        val token = session.accessToken ?: run {
            lastError = "No Supabase access token. Sign in again."
            return@withContext false
        }
        val table = when (item.recordType) {
            "income" -> "income"
            "expense" -> "expenses"
            "debt" -> "debts"
            "debt_monthly_payment" -> "debt_monthly_payments"
            else -> return@withContext false
        }
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/rest/v1/$table?on_conflict=id")
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer " + token)
            .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
            .addHeader("Content-Type", "application/json")
            .post(item.payload.toRequestBody("application/json".toMediaType()))
            .build()
        repeat(3) { attempt ->
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) return@withContext true
                    val detail = response.body?.string().orEmpty()
                    lastError = "Upload $table failed (${response.code}): ${detail.take(180)}"
                    if (response.code !in listOf(408, 425, 429) && response.code < 500) return@withContext false
                }
            } catch (_: IOException) {
                lastError = "Upload connection failed."
                if (attempt == 2) return@withContext false
            }
            delay((attempt + 1) * 250L)
        }
        false
    }

    suspend fun pull(table: String, userId: String, updatedAfter: Long = 0L, allUsers: Boolean = false): String = withContext(Dispatchers.IO) {
        val token = session.accessToken ?: throw IOException("No Supabase access token. Sign in again.")
        val encodedTime = java.net.URLEncoder.encode(iso(updatedAfter), "UTF-8")
        val encodedUser = java.net.URLEncoder.encode(userId, "UTF-8")
        val ownerFilter = if (allUsers) "" else "user_id=eq.$encodedUser&"
        val url = baseUrl.trimEnd('/') + "/rest/v1/$table?$ownerFilter" + "updated_at=gt.$encodedTime&order=updated_at.asc"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer " + token)
            .addHeader("Accept", "application/json")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val detail = response.body?.string().orEmpty()
                if (response.code == 400) {
                    return@withContext pullLegacy(table, encodedUser, token, allUsers)
                }
                throw IOException("Fetch $table failed (${response.code}): ${detail.take(180)}")
            }
            response.body?.string().orEmpty()
        }
    }

    private fun pullLegacy(table: String, encodedUser: String, token: String, allUsers: Boolean): String {
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/rest/v1/$table?" + if (allUsers) "" else "user_id=eq.$encodedUser")
            .addHeader("apikey", apiKey)
            .addHeader("Authorization", "Bearer " + token)
            .addHeader("Accept", "application/json")
            .get()
            .build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Fetch $table failed (${response.code}): ${response.body?.string().orEmpty().take(180)}")
            }
            response.body?.string().orEmpty()
        }
    }

    suspend fun pullAll(db: FinanceDatabase, userId: String, updatedAfter: Long = 0L) {
        try {
            val allUsers = session.role == "admin" || session.permissions().contains("view_all_records")
            listOf("income", "expenses", "debts", "debt_monthly_payments").forEach { table ->
                applyRows(db, userId, allUsers, table, JSONArray(pull(table, userId, updatedAfter, allUsers)))
            }
        } catch (error: Exception) {
            lastError = error.message ?: "Cloud fetch failed."
            throw error
        }
    }

    private suspend fun applyRows(db: FinanceDatabase, userId: String, allUsers: Boolean, table: String, rows: JSONArray) {
        for (i in 0 until rows.length()) {
            val row = rows.optJSONObject(i) ?: continue
            if (!allUsers && row.optString("user_id") != userId) continue
            val ownerId = row.optString("user_id").ifEmpty { userId }
            val at = millis(row, "updated_at")
            when (table) {
                "income" -> {
                    val value = Income(row.optString("id"), ownerId, row.optString("title"), cents(row, "amount"), row.optString("occurred_on"), nullable(row, "category"), nullable(row, "notes"), at, row.optBoolean("deleted"))
                    if ((db.income().get(value.id, ownerId)?.updatedAt ?: Long.MIN_VALUE) <= at) db.income().upsert(value)
                }
                "expenses" -> {
                    val value = Expense(row.optString("id"), ownerId, row.optString("title"), cents(row, "amount"), row.optString("occurred_on"), nullable(row, "category"), nullable(row, "notes"), at, row.optBoolean("deleted"))
                    if ((db.expense().get(value.id, ownerId)?.updatedAt ?: Long.MIN_VALUE) <= at) db.expense().upsert(value)
                }
                "debts" -> {
                    val value = Debt(row.optString("id"), ownerId, row.optString("person"), cents(row, "principal_amount"), row.optDouble("interest_rate"), row.optString("interest_type", "flat"), row.optInt("number_of_months", 1), row.optString("start_date"), nullable(row, "due_date"), optionalCents(row, "monthly_expected_payment"), optionalCents(row, "total_payable"), nullable(row, "notes"), row.optString("status", "active"), at, row.optBoolean("deleted"))
                    if ((db.debt().get(value.id, ownerId)?.updatedAt ?: Long.MIN_VALUE) <= at) db.debt().upsert(value)
                }
                "debt_monthly_payments" -> {
                    val value = DebtMonthlyPayment(row.optString("id"), row.optString("debt_id"), ownerId, row.optString("payment_month"), cents(row, "amount"), row.optString("status", "unpaid"), nullable(row, "notes"), at, row.optBoolean("deleted"))
                    if ((db.payment().get(value.id, ownerId)?.updatedAt ?: Long.MIN_VALUE) <= at) db.payment().upsert(value)
                }
            }
        }
    }

    private fun nullable(row: JSONObject, key: String): String? = if (row.isNull(key)) null else row.optString(key)
    private fun cents(row: JSONObject, key: String): Long = Math.round(row.optDouble(key, 0.0) * 100.0)
    private fun optionalCents(row: JSONObject, key: String): Long? = if (!row.has(key) || row.isNull(key)) null else cents(row, key)

    private fun millis(row: JSONObject, key: String): Long {
        val value = row.opt(key)
        if (value is Number) return if (value.toLong() < 100000000000L) value.toLong() * 1000 else value.toLong()
        var normalized = value.toString()
        if (normalized.endsWith("Z")) normalized = normalized.substring(0, normalized.length - 1) + "+0000"
        if (normalized.length >= 6 && normalized[normalized.length - 3] == ':') {
            normalized = normalized.substring(0, normalized.length - 3) + normalized.substring(normalized.length - 2)
        }
        return listOf("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ssZ").asSequence()
            .mapNotNull { pattern -> runCatching { SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(normalized)?.time }.getOrNull() }
            .firstOrNull() ?: 0L
    }

    private fun iso(time: Long): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }.format(java.util.Date(time))
}

class ConnectivitySyncTrigger(context: Context, private val drain: suspend () -> Unit) {
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val callbackScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { callbackScope.launch { drain() } }
    }
    fun start() { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) manager.registerDefaultNetworkCallback(callback) }
    fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) try { manager.unregisterNetworkCallback(callback) } catch (_: IllegalArgumentException) { }
        callbackScope.cancel()
    }
}
