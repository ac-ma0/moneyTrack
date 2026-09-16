package ph.moneytrack.data;

import java.lang.System;

@androidx.room.Database(entities = {ph.moneytrack.data.Income.class, ph.moneytrack.data.Expense.class, ph.moneytrack.data.Debt.class, ph.moneytrack.data.DebtMonthlyPayment.class, ph.moneytrack.data.AuditLog.class, ph.moneytrack.data.SyncQueue.class}, version = 1, exportSchema = false)
@kotlin.Metadata(mv = {1, 4, 2}, bv = {1, 0, 3}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u000f2\u00020\u0001:\u0001\u000fB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&J\b\u0010\u000b\u001a\u00020\fH&J\b\u0010\r\u001a\u00020\u000eH&\u00a8\u0006\u0010"}, d2 = {"Lph/moneytrack/data/FinanceDatabase;", "Landroidx/room/RoomDatabase;", "()V", "audit", "Lph/moneytrack/data/AuditDao;", "debt", "Lph/moneytrack/data/DebtDao;", "expense", "Lph/moneytrack/data/ExpenseDao;", "income", "Lph/moneytrack/data/IncomeDao;", "payment", "Lph/moneytrack/data/PaymentDao;", "sync", "Lph/moneytrack/data/SyncDao;", "Companion", "app_debug"})
public abstract class FinanceDatabase extends androidx.room.RoomDatabase {
    @org.jetbrains.annotations.NotNull()
    public static final ph.moneytrack.data.FinanceDatabase.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    public abstract ph.moneytrack.data.IncomeDao income();
    
    @org.jetbrains.annotations.NotNull()
    public abstract ph.moneytrack.data.ExpenseDao expense();
    
    @org.jetbrains.annotations.NotNull()
    public abstract ph.moneytrack.data.DebtDao debt();
    
    @org.jetbrains.annotations.NotNull()
    public abstract ph.moneytrack.data.PaymentDao payment();
    
    @org.jetbrains.annotations.NotNull()
    public abstract ph.moneytrack.data.AuditDao audit();
    
    @org.jetbrains.annotations.NotNull()
    public abstract ph.moneytrack.data.SyncDao sync();
    
    public FinanceDatabase() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 4, 2}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0013\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lph/moneytrack/data/FinanceDatabase$Companion;", "", "()V", "create", "error/NonExistentClass", "context", "Landroid/content/Context;", "(Landroid/content/Context;)Lerror/NonExistentClass;", "app_debug"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final error.NonExistentClass create(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}