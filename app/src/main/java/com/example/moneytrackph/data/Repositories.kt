package com.example.moneytrackph.data

import android.content.Context
import androidx.room.Room
import com.example.moneytrackph.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class Repositories(context: Context) {
    val db = Room.databaseBuilder(context, AppDatabase::class.java, "moneytrack.db")
        .fallbackToDestructiveMigration()
        .build()
    val supabase = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) {
        install(Auth)
        install(Postgrest)
    }
    val transactions = TransactionRepository(db.transactions())
    val debts = DebtRepository(db.debts())

    suspend fun signIn(email: String, secret: String) = supabase.auth.signInWith(Email) {
        this.email = email
        password = secret
    }

    suspend fun signUp(email: String, secret: String) = supabase.auth.signUpWith(Email) {
        this.email = email
        password = secret
    }
}

class TransactionRepository(private val dao: TransactionDao) {
    val items: Flow<List<TransactionEntity>> = dao.observe()
    suspend fun save(item: TransactionEntity) = withContext(Dispatchers.IO) { dao.upsert(item) }
    suspend fun sync() = dao.pending().forEach { dao.synced(it.id) }
}

class DebtRepository(private val dao: DebtDao) {
    val items: Flow<List<DebtEntity>> = dao.observe()
    suspend fun save(item: DebtEntity) = withContext(Dispatchers.IO) { dao.upsert(item) }
    suspend fun pay(debt: DebtEntity, amount: Double) {
        dao.upsert(debt.copy(remaining = (debt.remaining - amount).coerceAtLeast(0.0), synced = false))
        dao.payment(DebtPaymentEntity(java.util.UUID.randomUUID().toString(), debt.id, amount))
    }
    suspend fun sync() = dao.pending().forEach { dao.synced(it.id) }
}
