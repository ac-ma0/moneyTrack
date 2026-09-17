package ph.moneytrack;

import java.lang.System;

/**
 * The MVP deliberately uses platform Views rather than Material/Compose so it
 * remains buildable with the project's legacy Android Gradle plugin.
 */
@kotlin.Metadata(mv = {1, 4, 2}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u00d8\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\t\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\"\n\u0002\b\r\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u001c\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0014\u0010%\u001a\u00020&2\n\b\u0002\u0010\'\u001a\u0004\u0018\u00010(H\u0002J\b\u0010)\u001a\u00020&H\u0002J\u001c\u0010*\u001a\u00020&2\u0006\u0010+\u001a\u00020\u000b2\n\b\u0002\u0010\'\u001a\u0004\u0018\u00010,H\u0002J\b\u0010-\u001a\u00020.H\u0002J\b\u0010/\u001a\u00020&H\u0002J\u0010\u00100\u001a\u0002012\u0006\u00102\u001a\u00020\bH\u0002J \u00103\u001a\u00020\u00112\u0006\u00104\u001a\u00020\b2\u0006\u00102\u001a\u00020\b2\u0006\u00105\u001a\u00020\bH\u0002J\u0010\u00106\u001a\u00020\b2\u0006\u00107\u001a\u00020\u0004H\u0002J\u0018\u00108\u001a\n 9*\u0004\u0018\u00010\b0\b2\u0006\u00102\u001a\u00020:H\u0002J\u0010\u0010;\u001a\u00020&2\u0006\u0010<\u001a\u00020\bH\u0002J\u0010\u0010=\u001a\u00020&2\u0006\u0010<\u001a\u00020\bH\u0002J\u0010\u0010>\u001a\u00020&2\u0006\u0010<\u001a\u00020\bH\u0002J\u0010\u0010?\u001a\u00020\u00042\u0006\u00102\u001a\u00020\u0004H\u0002J\u0018\u0010@\u001a\u00020&2\u0006\u0010<\u001a\u00020\b2\u0006\u0010A\u001a\u00020\bH\u0002J\u0010\u0010B\u001a\u00020&2\u0006\u0010C\u001a\u00020(H\u0002J\u0012\u0010D\u001a\u00020&2\b\u0010\'\u001a\u0004\u0018\u00010\bH\u0002J\b\u0010E\u001a\u00020&H\u0002J\b\u0010F\u001a\u00020&H\u0002J\u0010\u0010G\u001a\u00020H2\u0006\u0010I\u001a\u00020\bH\u0002J\u0018\u0010J\u001a\u00020\u00042\u0006\u0010K\u001a\u00020\b2\u0006\u0010L\u001a\u00020\u0004H\u0002J\b\u0010M\u001a\u00020\u0011H\u0002J\u0018\u0010N\u001a\u00020\u00132\u0006\u00102\u001a\u00020\b2\u0006\u0010O\u001a\u00020PH\u0002J\u0010\u0010Q\u001a\u00020\b2\u0006\u0010R\u001a\u00020:H\u0002J\u0010\u0010S\u001a\u00020&2\u0006\u0010K\u001a\u00020\bH\u0002J\b\u0010T\u001a\u00020&H\u0002J\u0012\u0010U\u001a\u00020&2\b\u0010V\u001a\u0004\u0018\u00010WH\u0014J\b\u0010X\u001a\u00020&H\u0014J\b\u0010Y\u001a\u00020&H\u0002J\b\u0010Z\u001a\u00020&H\u0002J\b\u0010[\u001a\u00020&H\u0002J\b\u0010\\\u001a\u00020&H\u0002J\b\u0010]\u001a\u00020&H\u0002J\b\u0010^\u001a\u00020&H\u0002J\b\u0010_\u001a\u00020&H\u0002J\b\u0010`\u001a\u00020&H\u0002J\u0010\u0010a\u001a\u00020&2\u0006\u0010+\u001a\u00020\u000bH\u0002J\b\u0010b\u001a\u00020&H\u0002J,\u0010c\u001a\u00020\u00112\u0006\u0010K\u001a\u00020\b2\u0006\u0010d\u001a\u00020\u00042\u0012\u0010e\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020&0fH\u0002J0\u0010g\u001a\u00020\u00112\u0006\u00104\u001a\u00020\b2\u0006\u00105\u001a\u00020\b2\u0006\u0010h\u001a\u00020\u00042\u000e\u0010i\u001a\n\u0012\u0004\u0012\u00020&\u0018\u00010jH\u0002J4\u0010k\u001a\u0002Hl\"\u0004\b\u0000\u0010l2\u001c\u0010m\u001a\u0018\b\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u0002Hl0n\u0012\u0006\u0012\u0004\u0018\u00010,0fH\u0002\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010oJ\u0018\u0010p\u001a\u00020&2\u0006\u0010K\u001a\u00020\b2\u0006\u0010h\u001a\u00020\u0004H\u0002J&\u0010q\u001a\u00020&2\u0006\u0010<\u001a\u00020\b2\u0006\u0010A\u001a\u00020\b2\f\u0010r\u001a\b\u0012\u0004\u0012\u00020\b0sH\u0002J\b\u0010t\u001a\u00020&H\u0002J\u0010\u0010u\u001a\u00020&2\u0006\u0010C\u001a\u00020(H\u0002J\b\u0010v\u001a\u00020&H\u0002J\b\u0010w\u001a\u00020&H\u0002J\u0018\u0010w\u001a\u00020&2\u000e\u0010x\u001a\n\u0012\u0004\u0012\u00020&\u0018\u00010jH\u0002J\u0010\u0010y\u001a\n 9*\u0004\u0018\u00010\b0\bH\u0002JL\u0010z\u001a\u00020\u00112\u0006\u00104\u001a\u00020\b2\u0006\u00105\u001a\u00020\b2\u0006\u0010h\u001a\u00020\u00042\u0006\u0010{\u001a\u00020\u000b2\u0006\u0010|\u001a\u00020\u000b2\f\u0010}\u001a\b\u0012\u0004\u0012\u00020&0j2\f\u0010~\u001a\b\u0012\u0004\u0012\u00020&0jH\u0002J\b\u0010\u007f\u001a\u00020\u0011H\u0002J\n\u0010\u0080\u0001\u001a\u00030\u0081\u0001H\u0002J/\u0010\u0082\u0001\u001a\u00020:\"\u0004\b\u0000\u0010l*\t\u0012\u0004\u0012\u0002Hl0\u0083\u00012\u0013\u0010\u0084\u0001\u001a\u000e\u0012\u0004\u0012\u0002Hl\u0012\u0004\u0012\u00020:0fH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R \u0010\u0005\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\u00020\u00158BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0016\u0010\u0017R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u001fX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010 \u001a\u0004\u0018\u00010!X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\"\u001a\u00020\b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b#\u0010$\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0085\u0001"}, d2 = {"Lph/moneytrack/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "accent", "", "cloudUsers", "", "Lkotlin/Pair;", "", "current", "dark", "", "dashboardContainer", "drawer", "Landroidx/drawerlayout/widget/DrawerLayout;", "observing", "page", "Landroid/widget/LinearLayout;", "pageTitle", "Landroid/widget/TextView;", "permission", "Lph/moneytrack/data/LocalPermission;", "getPermission", "()Lph/moneytrack/data/LocalPermission;", "repository", "Lph/moneytrack/data/FinanceRepository;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "syncProcessor", "Lph/moneytrack/data/SyncProcessor;", "syncRepository", "Lph/moneytrack/data/SupabaseSyncRepository;", "syncTrigger", "Lph/moneytrack/data/ConnectivitySyncTrigger;", "userId", "getUserId", "()Ljava/lang/String;", "addDebt", "", "existing", "Lph/moneytrack/data/Debt;", "addSupabaseUser", "addTransaction", "income", "", "buildDrawer", "Landroid/view/View;", "buildShell", "button", "Landroid/widget/Button;", "value", "card", "title", "detail", "dateDaysAgo", "days", "dateTime", "kotlin.jvm.PlatformType", "", "deleteDebt", "id", "deleteExpense", "deleteIncome", "dp", "editCloudUser", "currentName", "editDebt", "debt", "editManagedUser", "editOwnProfileName", "enterApp", "field", "Landroid/widget/EditText;", "hint", "getThemeColor", "name", "fallback", "horizontal", "label", "size", "", "money", "cents", "navigate", "observeData", "onCreate", "state", "Landroid/os/Bundle;", "onDestroy", "reloadRepositoryScope", "render", "renderDashboard", "renderDebts", "renderHistory", "renderProfile", "renderReports", "renderSettings", "renderTransactions", "renderUsers", "rgbEditor", "initial", "changed", "Lkotlin/Function1;", "row", "color", "action", "Lkotlin/Function0;", "runBlockingValue", "T", "block", "Lkotlin/coroutines/Continuation;", "(Lkotlin/jvm/functions/Function1;)Ljava/lang/Object;", "saveThemeColor", "showCloudUserEditor", "currentPermissions", "", "showLogin", "showPaymentStatusDialog", "syncAfterAction", "syncNow", "onComplete", "today", "transactionRow", "canEdit", "canDelete", "edit", "delete", "vertical", "weight", "Landroid/widget/LinearLayout$LayoutParams;", "sumByLong", "", "selector", "app_debug"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private final kotlinx.coroutines.CoroutineScope scope = null;
    private ph.moneytrack.data.FinanceRepository repository;
    private androidx.drawerlayout.widget.DrawerLayout drawer;
    private android.widget.LinearLayout page;
    private android.widget.TextView pageTitle;
    private ph.moneytrack.data.ConnectivitySyncTrigger syncTrigger;
    private ph.moneytrack.data.SupabaseSyncRepository syncRepository;
    private ph.moneytrack.data.SyncProcessor syncProcessor;
    private boolean observing = false;
    private java.lang.String current = "Dashboard";
    private java.util.List<kotlin.Pair<java.lang.String, java.lang.String>> cloudUsers;
    private boolean dark = false;
    private int accent;
    private int dashboardContainer = android.graphics.Color.WHITE;
    
    private final ph.moneytrack.data.LocalPermission getPermission() {
        return null;
    }
    
    private final java.lang.String getUserId() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle state) {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    private final void showLogin() {
    }
    
    private final void enterApp() {
    }
    
    private final void buildShell() {
    }
    
    private final void syncNow() {
    }
    
    private final void syncNow(kotlin.jvm.functions.Function0<kotlin.Unit> onComplete) {
    }
    
    private final android.view.View buildDrawer() {
        return null;
    }
    
    private final void observeData() {
    }
    
    private final void navigate(java.lang.String name) {
    }
    
    private final void render() {
    }
    
    private final void renderDashboard() {
    }
    
    private final void renderTransactions(boolean income) {
    }
    
    private final void renderDebts() {
    }
    
    private final void showPaymentStatusDialog(ph.moneytrack.data.Debt debt) {
    }
    
    private final void renderHistory() {
    }
    
    private final void renderReports() {
    }
    
    private final void renderSettings() {
    }
    
    private final void renderProfile() {
    }
    
    private final void renderUsers() {
    }
    
    private final void editManagedUser(java.lang.String existing) {
    }
    
    private final void addSupabaseUser() {
    }
    
    private final void editOwnProfileName() {
    }
    
    private final void editCloudUser(java.lang.String id, java.lang.String currentName) {
    }
    
    private final void showCloudUserEditor(java.lang.String id, java.lang.String currentName, java.util.Set<java.lang.String> currentPermissions) {
    }
    
    private final void addTransaction(boolean income, java.lang.Object existing) {
    }
    
    private final void addDebt(ph.moneytrack.data.Debt existing) {
    }
    
    private final void editDebt(ph.moneytrack.data.Debt debt) {
    }
    
    private final void deleteIncome(java.lang.String id) {
    }
    
    private final void deleteExpense(java.lang.String id) {
    }
    
    private final void deleteDebt(java.lang.String id) {
    }
    
    private final void syncAfterAction() {
    }
    
    private final void reloadRepositoryScope() {
    }
    
    private final android.widget.LinearLayout vertical() {
        return null;
    }
    
    private final android.widget.LinearLayout horizontal() {
        return null;
    }
    
    private final android.widget.TextView label(java.lang.String value, float size) {
        return null;
    }
    
    private final android.widget.EditText field(java.lang.String hint) {
        return null;
    }
    
    private final android.widget.Button button(java.lang.String value) {
        return null;
    }
    
    private final android.widget.LinearLayout card(java.lang.String title, java.lang.String value, java.lang.String detail) {
        return null;
    }
    
    private final android.widget.LinearLayout row(java.lang.String title, java.lang.String detail, int color, kotlin.jvm.functions.Function0<kotlin.Unit> action) {
        return null;
    }
    
    private final android.widget.LinearLayout transactionRow(java.lang.String title, java.lang.String detail, int color, boolean canEdit, boolean canDelete, kotlin.jvm.functions.Function0<kotlin.Unit> edit, kotlin.jvm.functions.Function0<kotlin.Unit> delete) {
        return null;
    }
    
    private final android.widget.LinearLayout rgbEditor(java.lang.String name, int initial, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> changed) {
        return null;
    }
    
    private final int getThemeColor(java.lang.String name, int fallback) {
        return 0;
    }
    
    private final void saveThemeColor(java.lang.String name, int color) {
    }
    
    private final android.widget.LinearLayout.LayoutParams weight() {
        return null;
    }
    
    private final int dp(int value) {
        return 0;
    }
    
    private final java.lang.String money(long cents) {
        return null;
    }
    
    private final java.lang.String today() {
        return null;
    }
    
    private final java.lang.String dateDaysAgo(int days) {
        return null;
    }
    
    private final java.lang.String dateTime(long value) {
        return null;
    }
    
    private final <T extends java.lang.Object>T runBlockingValue(kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super T>, ? extends java.lang.Object> block) {
        return null;
    }
    
    private final <T extends java.lang.Object>long sumByLong(java.lang.Iterable<? extends T> $this$sumByLong, kotlin.jvm.functions.Function1<? super T, java.lang.Long> selector) {
        return 0L;
    }
    
    public MainActivity() {
        super();
    }
}