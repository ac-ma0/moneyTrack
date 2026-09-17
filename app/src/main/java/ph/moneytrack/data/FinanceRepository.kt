package ph.moneytrack.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FinanceRepository(private val db: FinanceDatabase, private val userId: String, private val viewAll: Boolean = false) {
    val incomes: Flow<List<Income>> = if (viewAll) db.income().observeAll() else db.income().observe(userId)
    val expenses: Flow<List<Expense>> = if (viewAll) db.expense().observeAll() else db.expense().observe(userId)
    val debts: Flow<List<Debt>> = if (viewAll) db.debt().observeAll() else db.debt().observe(userId)
    val audit: Flow<List<AuditLog>> = db.audit().observe(userId)

    suspend fun incomesValue(): List<Income> = incomes.first()
    suspend fun expensesValue(): List<Expense> = expenses.first()
    suspend fun debtsValue(): List<Debt> = debts.first()

    suspend fun saveIncome(value: Income) = write("upsert","income",value.id,value) { db.income().upsert(value) }
    suspend fun saveExpense(value: Expense) = write("upsert","expense",value.id,value) { db.expense().upsert(value) }
    suspend fun saveDebt(value: Debt) = write("upsert","debt",value.id,value) { db.debt().upsert(value) }
    suspend fun deleteIncome(id:String) = write("delete","income",id,null) { db.income().softDelete(id,userId) }
    suspend fun deleteExpense(id:String) = write("delete","expense",id,null) { db.expense().softDelete(id,userId) }
    suspend fun deleteDebt(id:String) = write("delete","debt",id,null) { db.debt().softDelete(id,userId) }
    suspend fun setDebtStatus(id:String,status:String) = write("status","debt",id,status) { db.debt().setStatus(id,userId,status) }
    suspend fun savePayment(value:DebtMonthlyPayment) = write("upsert","debt_monthly_payment",value.id,value) { db.payment().upsert(value) }
    suspend fun setPaymentStatus(id:String,status:String,amount:Long) = write("status","debt_monthly_payment",id,status) { db.payment().setStatus(id,userId,status,amount) }
    suspend fun deletePayment(id:String) = write("delete","debt_monthly_payment",id,null) { db.payment().softDelete(id,userId) }
    suspend fun payments(debtId: String): List<DebtMonthlyPayment> = db.payment().observe(debtId, userId).first()

    private suspend fun write(operation:String,type:String,id:String,newValue:Any?, action:suspend()->Unit) {
        db.withTransaction {
            val old = when (type) {
                "income" -> db.income().get(id,userId)?.toJson()
                "expense" -> db.expense().get(id,userId)?.toJson()
                "debt" -> db.debt().get(id,userId)?.toJson()
                "debt_monthly_payment" -> db.payment().get(id,userId)?.toJson()
                else -> null
            }
            action()
            val payload = if (operation == "status" || operation == "delete") {
                when (type) {
                    "debt" -> db.debt().get(id, userId)?.toJson()
                    "debt_monthly_payment" -> db.payment().get(id, userId)?.toJson()
                    "income" -> db.income().get(id, userId)?.toJson()
                    "expense" -> db.expense().get(id, userId)?.toJson()
                    else -> null
                }
            } else newValue?.toJson()
            val finalPayload = payload ?: JSONObject().apply {
                put("id", id)
                put("user_id", userId)
                put("deleted", true)
                put("updated_at", iso(System.currentTimeMillis()))
            }.toString()
            db.audit().insert(AuditLog(userId=userId,action=operation,recordType=type,recordId=id,oldValue=old,newValue=finalPayload))
            db.sync().enqueue(SyncQueue(userId=userId,operation=operation,recordType=type,recordId=id,payload=finalPayload))
        }
    }

    private fun Any.toJson(): String = JSONObject().apply {
        when (this@toJson) {
            is Income -> { put("id",id); put("user_id",userId); put("title",title); put("amount",amount/100.0); put("occurred_on",occurredOn); put("category",category); put("notes",notes); put("updated_at",iso(updatedAt)); put("deleted",deleted) }
            is Expense -> { put("id",id); put("user_id",userId); put("title",title); put("amount",amount/100.0); put("occurred_on",occurredOn); put("category",category); put("notes",notes); put("updated_at",iso(updatedAt)); put("deleted",deleted) }
            is Debt -> { put("id",id); put("user_id",userId); put("person",person); put("principal_amount",principalAmount/100.0); put("interest_rate",interestRate); put("interest_type",interestType); put("number_of_months",numberOfMonths); put("start_date",startDate); put("due_date",dueDate); put("monthly_expected_payment",monthlyExpectedPayment?.div(100.0)); put("total_payable",totalPayable?.div(100.0)); put("notes",notes); put("status",status); put("updated_at",iso(updatedAt)); put("deleted",deleted) }
            is DebtMonthlyPayment -> { put("id",id); put("debt_id",debtId); put("user_id",userId); put("payment_month",paymentMonth); put("amount",amount/100.0); put("status",status); put("notes",notes); put("updated_at",iso(updatedAt)); put("deleted",deleted) }
            else -> put("value",toString())
        }
    }.toString()

    private fun iso(time: Long): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(time))
}
