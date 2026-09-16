package com.example.moneytrackph.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Entity(tableName="transactions") data class TransactionEntity(@PrimaryKey val id:String,val type:String,val amount:Double,val category:String="",val note:String="",val occurredAt:Long=System.currentTimeMillis(),val synced:Boolean=false)
@Entity(tableName="debts") data class DebtEntity(@PrimaryKey val id:String,val name:String,val principal:Double,val remaining:Double,val monthlyPayment:Double,val dueDay:Int=1,val synced:Boolean=false)
@Entity(tableName="debt_payments") data class DebtPaymentEntity(@PrimaryKey val id:String,val debtId:String,val amount:Double,val paidAt:Long=System.currentTimeMillis(),val synced:Boolean=false)
@Dao interface TransactionDao { @Query("SELECT * FROM transactions ORDER BY occurredAt DESC") fun observe():Flow<List<TransactionEntity>>; @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(x:TransactionEntity); @Query("SELECT * FROM transactions WHERE synced=0") suspend fun pending():List<TransactionEntity>; @Query("UPDATE transactions SET synced=1 WHERE id=:id") suspend fun synced(id:String) }
@Dao interface DebtDao { @Query("SELECT * FROM debts ORDER BY name") fun observe():Flow<List<DebtEntity>>; @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(x:DebtEntity); @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun payment(x:DebtPaymentEntity); @Query("SELECT * FROM debts WHERE synced=0") suspend fun pending():List<DebtEntity>; @Query("UPDATE debts SET synced=1 WHERE id=:id") suspend fun synced(id:String) }
@Database(entities=[TransactionEntity::class,DebtEntity::class,DebtPaymentEntity::class],version=1,exportSchema=false) abstract class AppDatabase:RoomDatabase(){abstract fun transactions():TransactionDao;abstract fun debts():DebtDao}

