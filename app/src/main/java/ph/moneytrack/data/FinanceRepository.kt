package ph.moneytrack.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FinanceRepository(private val db: FinanceDatabase, private val userId: String) {
    val incomes: Flow<List<Income>> = db.income().observe(userId)
    val expenses: Flow<List<Expense>> = db.expense().observe(userId)
    val debts: Flow<List<Debt>> = db.debt().observe(userId)
    val audit: Flow<List<AuditLog>> = db.audit().observe(userId)

    suspend fun incomesValue(): List<Income> = db.income().observe(userId).first()
    suspend fun expensesValue(): List<Expense> = db.expense().observe(userId).first()
    suspend fun debtsValue(): List<Debt> = db.debt().observe(userId).first()

    suspend fun saveIncome(value: Income) = write("upsert","income",value.id,value) { db.income().upsert(value) }
    suspend fun saveExpense(value: Expense) = write("upsert","expense",value.id,value) { db.expense().upsert(value) }
    suspend fun saveDebt(value: Debt) = write("upsert","debt",value.id,value) { db.debt().upsert(value) }
    suspend fun deleteIncome(id:String) = write("delete","income",id,null) { db.income().softDelete(id,userId) }
    suspend fun deleteExpense(id:String) = write("delete","expense",id,null) { db.expense().softDelete(id,userId) }
    suspend fun deleteDebt(id:String) = write("delete","debt",id,null) { db.debt().softDelete(id,userId) }
    suspend fun setDebtStatus(id:String,status:String) = write("status","debt",id,status) { db.debt().setStatus(id,userId,status) }
    suspend fun savePayment(value:DebtMonthlyPayment) = write("upsert","debt_monthly_payment",value.id,value) { db.payment().upsert(value) }
    suspend fun setPaymentStatus(id:String,status:String,amount:Long) = write("status","debt_monthly_payment",id,status) { db.payment().setStatus(id,userId,status,amount) }

    private suspend fun write(operation:String,type:String,id:String,newValue:Any?, action:suspend()->Unit) {
        db.withTransaction {
            action()
            val payload = newValue?.toString()
            db.audit().insert(AuditLog(userId=userId,action=operation,recordType=type,recordId=id,oldValue=null,newValue=payload))
            db.sync().enqueue(SyncQueue(userId=userId,operation=operation,recordType=type,recordId=id,payload=payload ?: ""))
        }
    }
}
