package ph.moneytrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

enum class LocalRole { ADMIN, EDITOR, VIEWER }
data class LocalPermission(
    val role: LocalRole,
    val canRead: Boolean = true,
    val canAddIncome: Boolean = role != LocalRole.VIEWER,
    val canEditIncome: Boolean = role != LocalRole.VIEWER,
    val canDeleteIncome: Boolean = role == LocalRole.ADMIN,
    val canAddExpenses: Boolean = role != LocalRole.VIEWER,
    val canEditExpenses: Boolean = role != LocalRole.VIEWER,
    val canDeleteExpenses: Boolean = role == LocalRole.ADMIN,
    val canManageDebts: Boolean = role != LocalRole.VIEWER,
    val canViewReports: Boolean = true,
    val canViewHistory: Boolean = role == LocalRole.ADMIN,
    val canManageUsers: Boolean = role == LocalRole.ADMIN
) {
    val canWrite: Boolean get() = canAddIncome || canEditIncome || canAddExpenses || canEditExpenses || canManageDebts
}

private fun id() = UUID.randomUUID().toString()

@Entity(tableName = "income")
data class Income(
    @PrimaryKey val id: String = id(), val userId: String, val title: String,
    val amount: Long, val occurredOn: String, val category: String? = null, val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis(), val deleted: Boolean = false
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = id(), val userId: String, val title: String,
    val amount: Long, val occurredOn: String, val category: String? = null, val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis(), val deleted: Boolean = false
)

@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey val id: String = id(), val userId: String, val person: String,
    val principalAmount: Long, val interestRate: Double = 0.0, val interestType: String = "flat",
    val numberOfMonths: Int = 1, val startDate: String, val dueDate: String? = null,
    val monthlyExpectedPayment: Long? = null, val totalPayable: Long? = null,
    val notes: String? = null, val status: String = "active",
    val updatedAt: Long = System.currentTimeMillis(), val deleted: Boolean = false
)

@Entity(tableName = "debt_monthly_payments",
    foreignKeys = [ForeignKey(entity = Debt::class, parentColumns = ["id"], childColumns = ["debtId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("debtId")])
data class DebtMonthlyPayment(
    @PrimaryKey val id: String = id(), val debtId: String, val userId: String,
    val paymentMonth: String, val amount: Long, val status: String = "unpaid",
    val notes: String? = null, val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)

object DebtCalculator {
    fun total(principal: Long, ratePercent: Double, type: String, months: Int): Long {
        val n = months.coerceAtLeast(1)
        val rate = ratePercent / 100.0
        val value = when (type) {
            "reducing" -> if (rate == 0.0) principal.toDouble()
            else principal * (rate / 12.0) * Math.pow(1 + rate / 12.0, n.toDouble()) /
                    (Math.pow(1 + rate / 12.0, n.toDouble()) - 1) * n
            "simple" -> principal * (1 + rate * n / 12.0)
            else -> principal * (1 + rate)
        }
        return Math.round(value)
    }
    fun monthly(total: Long, months: Int): Long = Math.round(total.toDouble() / months.coerceAtLeast(1))
}

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String = id(), val userId: String?, val action: String,
    val recordType: String?, val recordId: String?, val oldValue: String?,
    val newValue: String?, val timestamp: Long = System.currentTimeMillis(),
    val uploaded: Boolean = false
)

@Entity(tableName = "sync_queue", indices = [Index("createdAt")])
data class SyncQueue(
    @PrimaryKey val id: String = id(), val userId: String, val operation: String,
    val recordType: String, val recordId: String, val payload: String,
    val createdAt: Long = System.currentTimeMillis(), val attempts: Int = 0,
    val lastError: String? = null
)

@Dao interface IncomeDao {
    @Query("SELECT * FROM income WHERE userId=:userId AND deleted=0 ORDER BY occurredOn DESC") fun observe(userId: String): Flow<List<Income>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(value: Income)
    @Query("SELECT * FROM income WHERE id=:id AND userId=:userId LIMIT 1") suspend fun get(id:String,userId:String):Income?
    @Query("UPDATE income SET deleted=1, updatedAt=:at WHERE id=:id AND userId=:userId") suspend fun softDelete(id: String,userId: String,at: Long=System.currentTimeMillis())
}
@Dao interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE userId=:userId AND deleted=0 ORDER BY occurredOn DESC") fun observe(userId: String): Flow<List<Expense>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(value: Expense)
    @Query("SELECT * FROM expenses WHERE id=:id AND userId=:userId LIMIT 1") suspend fun get(id:String,userId:String):Expense?
    @Query("UPDATE expenses SET deleted=1, updatedAt=:at WHERE id=:id AND userId=:userId") suspend fun softDelete(id: String,userId: String,at: Long=System.currentTimeMillis())
}
@Dao interface DebtDao {
    @Query("SELECT * FROM debts WHERE userId=:userId AND deleted=0 ORDER BY startDate DESC") fun observe(userId: String): Flow<List<Debt>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(value: Debt)
    @Query("SELECT * FROM debts WHERE id=:id AND userId=:userId LIMIT 1") suspend fun get(id:String,userId:String):Debt?
    @Query("UPDATE debts SET status=:status, updatedAt=:at WHERE id=:id AND userId=:userId") suspend fun setStatus(id:String,userId:String,status:String,at:Long=System.currentTimeMillis())
    @Query("UPDATE debts SET deleted=1, updatedAt=:at WHERE id=:id AND userId=:userId") suspend fun softDelete(id:String,userId:String,at:Long=System.currentTimeMillis())
}
@Dao interface PaymentDao {
    @Query("SELECT * FROM debt_monthly_payments WHERE debtId=:debtId AND userId=:userId AND deleted=0 ORDER BY paymentMonth") fun observe(debtId:String,userId:String): Flow<List<DebtMonthlyPayment>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(value: DebtMonthlyPayment)
    @Query("SELECT * FROM debt_monthly_payments WHERE id=:id AND userId=:userId LIMIT 1") suspend fun get(id:String,userId:String):DebtMonthlyPayment?
    @Query("UPDATE debt_monthly_payments SET deleted=1, updatedAt=:at WHERE id=:id AND userId=:userId") suspend fun softDelete(id:String,userId:String,at:Long=System.currentTimeMillis())
    @Query("UPDATE debt_monthly_payments SET status=:status, amount=:amount, updatedAt=:at WHERE id=:id AND userId=:userId") suspend fun setStatus(id:String,userId:String,status:String,amount:Long,at:Long=System.currentTimeMillis())
}
@Dao interface AuditDao {
    @Query("SELECT * FROM audit_logs WHERE userId=:userId ORDER BY timestamp DESC") fun observe(userId:String): Flow<List<AuditLog>>
    @Insert suspend fun insert(value:AuditLog)
    @Query("SELECT * FROM audit_logs WHERE uploaded=0 LIMIT :limit") suspend fun pending(limit:Int):List<AuditLog>
    @Query("UPDATE audit_logs SET uploaded=1 WHERE id IN (:ids)") suspend fun markUploaded(ids:List<String>)
}
@Dao interface SyncDao {
    @Insert suspend fun enqueue(value:SyncQueue)
    @Query("SELECT * FROM sync_queue ORDER BY createdAt LIMIT :limit") suspend fun next(limit:Int):List<SyncQueue>
    @Delete suspend fun remove(value:SyncQueue)
    @Query("UPDATE sync_queue SET attempts=attempts+1,lastError=:error WHERE id=:id") suspend fun failed(id:String,error:String)
    @Query("SELECT COUNT(*) FROM sync_queue") suspend fun count():Int
}

@Database(entities=[Income::class,Expense::class,Debt::class,DebtMonthlyPayment::class,AuditLog::class,SyncQueue::class],version=2,exportSchema=false)
abstract class FinanceDatabase:RoomDatabase() {
    abstract fun income():IncomeDao; abstract fun expense():ExpenseDao; abstract fun debt():DebtDao
    abstract fun payment():PaymentDao; abstract fun audit():AuditDao; abstract fun sync():SyncDao
    companion object {
        @Volatile private var instance: FinanceDatabase? = null
        fun create(context:android.content.Context): FinanceDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "moneytrack.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
