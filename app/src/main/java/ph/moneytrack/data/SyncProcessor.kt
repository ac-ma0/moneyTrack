package ph.moneytrack.data

import android.util.Log

/**
 * Safe sync foundation: nothing is sent unless the host supplies an authenticated uploader.
 * Failed items stay queued with an error and can be retried when connectivity/auth is restored.
 */
class SyncProcessor(private val db: FinanceDatabase) {
    suspend fun drain(uploader: suspend (SyncQueue) -> Boolean, limit: Int = 25) {
        db.sync().next(limit).forEach { item ->
            try {
                if (uploader(item)) db.sync().remove(item)
                else db.sync().failed(item.id, "Uploader rejected operation")
            } catch (error: Exception) {
                db.sync().failed(item.id, error.message ?: "sync failed")
            }
        }
    }

    suspend fun synchronize(
        uploader: suspend (SyncQueue) -> Boolean,
        puller: (suspend () -> Unit)? = null,
        limit: Int = 25
    ) {
        drain(uploader, limit)
        try {
            puller?.invoke()
        } catch (error: Exception) {
            // Keep local data and queued writes; the next connectivity event retries the pull.
            Log.w("MoneyTrackSync", "Cloud pull failed: ${error.message}", error)
        }
    }
}
