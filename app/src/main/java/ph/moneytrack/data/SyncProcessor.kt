package ph.moneytrack.data

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
}
