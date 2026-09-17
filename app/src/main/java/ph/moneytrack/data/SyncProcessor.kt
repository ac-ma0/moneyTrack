package ph.moneytrack.data

import android.util.Log

/**
 * Safe sync foundation: nothing is sent unless the host supplies an authenticated uploader.
 * Failed items stay queued with an error and can be retried when connectivity/auth is restored.
 */
class SyncProcessor(private val db: FinanceDatabase) {
    suspend fun drain(uploader: suspend (SyncQueue) -> Boolean, limit: Int = 25): Boolean {
        var allUploaded = true
        db.sync().next(limit).forEach { item ->
            try {
                if (uploader(item)) db.sync().remove(item)
                else {
                    allUploaded = false
                    db.sync().failed(item.id, "Uploader rejected operation")
                }
            } catch (error: Exception) {
                allUploaded = false
                db.sync().failed(item.id, error.message ?: "sync failed")
            }
        }
        return allUploaded
    }

    suspend fun synchronize(
        uploader: suspend (SyncQueue) -> Boolean,
        puller: (suspend () -> Unit)? = null,
        limit: Int = 25
    ): Boolean {
        var uploaded = true
        var rounds = 0
        while (uploaded && db.sync().count() > 0 && rounds < 20) {
            uploaded = drain(uploader, limit)
            rounds++
        }
        try {
            puller?.invoke()
            return uploaded
        } catch (error: Exception) {
            // Keep local data and queued writes; the next connectivity event retries the pull.
            Log.w("MoneyTrackSync", "Cloud pull failed: ${error.message}", error)
            return false
        }
    }
}
