package ph.moneytrack.data;

import java.lang.System;

/**
 * Safe sync foundation: nothing is sent unless the host supplies an authenticated uploader.
 * Failed items stay queued with an error and can be retried when connectivity/auth is restored.
 */
@kotlin.Metadata(mv = {1, 4, 2}, bv = {1, 0, 3}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J?\u0010\u0005\u001a\u00020\u00062\"\u0010\u0007\u001a\u001e\b\u0001\u0012\u0004\u0012\u00020\t\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\b2\b\b\u0002\u0010\f\u001a\u00020\rH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u000f"}, d2 = {"Lph/moneytrack/data/SyncProcessor;", "", "db", "Lph/moneytrack/data/FinanceDatabase;", "(Lph/moneytrack/data/FinanceDatabase;)V", "drain", "", "uploader", "Lkotlin/Function2;", "Lph/moneytrack/data/SyncQueue;", "Lkotlin/coroutines/Continuation;", "", "limit", "", "(Lkotlin/jvm/functions/Function2;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class SyncProcessor {
    private final ph.moneytrack.data.FinanceDatabase db = null;
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object drain(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super ph.moneytrack.data.SyncQueue, ? super kotlin.coroutines.Continuation<? super java.lang.Boolean>, ? extends java.lang.Object> uploader, int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> p2) {
        return null;
    }
    
    public SyncProcessor(@org.jetbrains.annotations.NotNull()
    ph.moneytrack.data.FinanceDatabase db) {
        super();
    }
}