package ph.moneytrack

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import ph.moneytrack.data.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * The MVP deliberately uses platform Views rather than Material/Compose so it
 * remains buildable with the project's legacy Android Gradle plugin.
 */
class MainActivity : AppCompatActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: FinanceRepository
    private lateinit var drawer: DrawerLayout
    private lateinit var page: LinearLayout
    private lateinit var pageTitle: TextView
    private var syncTrigger: ConnectivitySyncTrigger? = null
    private lateinit var syncRepository: SupabaseSyncRepository
    private lateinit var syncProcessor: SyncProcessor
    private var observing = false
    private var current = "Dashboard"
    private var cloudUsers: List<Pair<String, String>> = emptyList()
    private var dark = false
    private var accent = Color.rgb(35, 105, 175)
    private var dashboardContainer = Color.WHITE
    private val permission: LocalPermission
        get() {
            val prefs = getSharedPreferences("moneytrack", 0)
            val session = SessionStore(this)
            val storedRole = if (session.accessToken != null) session.role else prefs.getString("role", "ADMIN") ?: "ADMIN"
            val role = runCatching { LocalRole.valueOf(storedRole.toUpperCase(Locale.US)) }.getOrDefault(LocalRole.ADMIN)
            val cloudPermissions = session.permissions()
            val cloudPolicyLoaded = session.accessToken != null
            fun allowed(name: String, fallback: Boolean) = if (role == LocalRole.ADMIN) true else if (cloudPolicyLoaded) cloudPermissions.contains(name) else prefs.getBoolean("permission_$name", fallback)
            return LocalPermission(
                role = role,
                canAddIncome = allowed("add_income", role != LocalRole.VIEWER),
                canEditIncome = allowed("edit_income", role != LocalRole.VIEWER),
                canDeleteIncome = allowed("delete_income", role == LocalRole.ADMIN),
                canAddExpenses = allowed("add_expenses", role != LocalRole.VIEWER),
                canEditExpenses = allowed("edit_expenses", role != LocalRole.VIEWER),
                canDeleteExpenses = allowed("delete_expenses", role == LocalRole.ADMIN),
                canManageDebts = allowed("manage_debts", role != LocalRole.VIEWER),
                canViewAllRecords = allowed("view_all_records", role == LocalRole.ADMIN),
                canViewReports = allowed("view_reports", true),
                canViewHistory = allowed("view_history", role == LocalRole.ADMIN),
                canManageUsers = allowed("manage_users", role == LocalRole.ADMIN)
            )
        }
    private val userId: String
        get() {
        val p = getSharedPreferences("moneytrack", 0)
        return p.getString("user_id", null) ?: UUID.randomUUID().toString().also {
            p.edit().putString("user_id", it).apply()
        }
        }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        dark = getSharedPreferences("moneytrack", 0).getBoolean("dark", false)
        accent = getSharedPreferences("moneytrack", 0).getInt("theme_accent",
            getSharedPreferences("moneytrack", 0).getInt("accent", accent))
        dashboardContainer = getSharedPreferences("moneytrack", 0).getInt("theme_container", if (dark) Color.rgb(43,54,67) else Color.WHITE)
        if (getSharedPreferences("moneytrack", 0).getBoolean("signed_in", false)) enterApp() else showLogin()
    }

    override fun onDestroy() {
        syncTrigger?.stop()
        scope.cancel()
        super.onDestroy()
    }

    private fun showLogin() {
        val root = vertical().apply { gravity = Gravity.CENTER; setPadding(dp(28), dp(28), dp(28), dp(28)) }
        root.addView(label("MoneyTrack PH", 30f))
        root.addView(label("A simple, private view of your money", 14f))
        val email = field("Email")
        val password = field("Password").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        root.addView(email); root.addView(password)
        val message = label("", 13f)
        val auth = SupabaseAuth(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY)
        val signIn = button("Sign in")
        signIn.setOnClickListener {
            signIn.isEnabled = false
            scope.launch {
                when (val result = auth.signIn(email.text.toString(), password.text.toString())) {
                    is AuthResult.Success -> {
                        val session = SessionStore(this@MainActivity)
                        SupabaseAuth.accessToken(result.body)?.let { session.accessToken = it }
                        SupabaseAuth.refreshToken(result.body)?.let { session.refreshToken = it }
                        SupabaseAuth.userId(result.body)?.let {
                            session.userId = it
                            getSharedPreferences("moneytrack", 0).edit().putString("user_id", it).apply()
                        }
                        session.email = email.text.toString().trim()
                        if (session.accessToken != null) enterApp() else {
                            message.text = "Sign in did not return a session"; signIn.isEnabled = true
                        }
                    }
                    is AuthResult.Failure -> { message.text = result.error.message ?: "Sign in failed"; signIn.isEnabled = true }
                }
            }
        }
        val signUp = button("Create account")
        signUp.setOnClickListener {
            scope.launch {
                when (val result = auth.signUp(email.text.toString(), password.text.toString())) {
                    is AuthResult.Success -> {
                        val session = SessionStore(this@MainActivity)
                        SupabaseAuth.accessToken(result.body)?.let { session.accessToken = it }
                        SupabaseAuth.refreshToken(result.body)?.let { session.refreshToken = it }
                        SupabaseAuth.userId(result.body)?.let { session.userId = it; getSharedPreferences("moneytrack", 0).edit().putString("user_id", it).apply() }
                        session.email = email.text.toString().trim()
                        if (session.accessToken != null) enterApp() else message.text = "Account created. Check your email, then sign in."
                    }
                    is AuthResult.Failure -> message.text = result.error.message ?: "Sign up failed"
                }
            }
        }
        root.addView(signIn); root.addView(signUp)
        root.addView(button("Continue offline").also { it.setOnClickListener { enterApp() } })
        root.addView(message)
        setContentView(root)
    }

    private fun enterApp() {
        getSharedPreferences("moneytrack", 0).edit().putBoolean("signed_in", true).apply()
        repository = FinanceRepository(
            FinanceDatabase.create(this),
            userId,
            SessionStore(this).role == "admin" || SessionStore(this).permissions().contains("view_all_records")
        )
        syncRepository = SupabaseSyncRepository(
            BuildConfig.SUPABASE_URL,
            BuildConfig.SUPABASE_ANON_KEY,
            SessionStore(this)
        )
        syncProcessor = SyncProcessor(FinanceDatabase.create(this))
        syncTrigger = ConnectivitySyncTrigger(this) {
            syncRepository.refreshSession()
            syncRepository.refreshAccessPolicy()
            syncProcessor.synchronize(
                uploader = { item -> syncRepository.upload(item) },
                puller = { syncRepository.pullAll(FinanceDatabase.create(this@MainActivity), userId) }
            )
        }
        try {
            syncTrigger?.start()
            scope.launch(Dispatchers.IO) {
                syncRepository.refreshSession()
                syncRepository.refreshAccessPolicy()
                withContext(Dispatchers.Main) { reloadRepositoryScope() }
                syncProcessor.synchronize(
                    uploader = { item -> syncRepository.upload(item) },
                    puller = { syncRepository.pullAll(FinanceDatabase.create(this@MainActivity), userId) }
                )
            }
        } catch (_: RuntimeException) {
            syncTrigger = null
        }
        buildShell()
        observeData()
        navigate("Dashboard")
    }

    private fun buildShell() {
        drawer = DrawerLayout(this)
        val main = vertical().apply { setBackgroundColor(getThemeColor("background", if (dark) Color.rgb(20, 25, 31) else Color.rgb(247, 249, 252))) }
        val toolbar = horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setBackgroundColor(accent)
        }
        toolbar.addView(button("☰").apply {
            minWidth = dp(48); setOnClickListener { drawer.openDrawer(Gravity.LEFT) }
        })
        pageTitle = label("Dashboard", 21f).apply { setTextColor(if (dark) Color.WHITE else Color.rgb(25, 42, 65)) }
        toolbar.addView(pageTitle, LinearLayout.LayoutParams(0, -2, 1f))
        toolbar.addView(label("₱", 24f))
        main.addView(toolbar)
        page = vertical()
        val refreshLayout = SwipeRefreshLayout(this)
        refreshLayout.setOnRefreshListener {
            syncNow { refreshLayout.isRefreshing = false }
        }
        refreshLayout.addView(ScrollView(this).apply { addView(page) })
        main.addView(refreshLayout, LinearLayout.LayoutParams(-1, 0, 1f))
        drawer.addView(main, DrawerLayout.LayoutParams(-1, -1))
        drawer.addView(buildDrawer(), DrawerLayout.LayoutParams(dp(300), -1).apply { gravity = Gravity.LEFT })
        setContentView(drawer)
    }

    private fun syncNow() {
        syncNow(null)
    }

    private fun syncNow(onComplete: (() -> Unit)?) {
        if (!::syncRepository.isInitialized || !::syncProcessor.isInitialized) {
            Toast.makeText(this, "Sync is not available in offline mode.", Toast.LENGTH_SHORT).show()
            onComplete?.invoke()
            return
        }
        Toast.makeText(this, "Syncing local changes and fetching cloud data...", Toast.LENGTH_SHORT).show()
        scope.launch {
            val success = withContext(Dispatchers.IO) {
                syncRepository.refreshSession()
                syncRepository.refreshAccessPolicy()
                withContext(Dispatchers.Main) { reloadRepositoryScope() }
                syncProcessor.synchronize(
                    uploader = { item -> syncRepository.upload(item) },
                    puller = { syncRepository.pullAll(FinanceDatabase.create(this@MainActivity), userId) }
                )
            }
            render()
            Toast.makeText(
                this@MainActivity,
                if (success) "Sync complete." else "Sync incomplete: ${syncRepository.lastError ?: "check Supabase schema, RLS, and session."}",
                Toast.LENGTH_LONG
            ).show()
            onComplete?.invoke()
        }
    }

    private fun buildDrawer(): View {
        val menu = vertical().apply {
            setPadding(dp(18), dp(34), dp(12), dp(18))
            setBackgroundColor(if (dark) Color.rgb(31, 40, 50) else Color.WHITE)
        }
        menu.addView(label("MONEYTRACK PH", 13f))
        menu.addView(label("Your personal finance workspace", 12f))
        val items = arrayOf("Dashboard", "Income", "Expenses", "Debt Tracker", "History", "Reports",
            "Users", "Settings", "My Profile", "Logout").filter {
            (it != "Users" || permission.canManageUsers) &&
            (it != "History" || permission.canViewHistory) &&
            (it != "Reports" || permission.canViewReports)
        }
        items.forEach { name ->
            val item = button(if (name == current) "●  $name" else "   $name").apply {
                gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                setOnClickListener {
                    drawer.closeDrawer(Gravity.LEFT)
                    if (name == "Logout") {
                        getSharedPreferences("moneytrack", 0).edit().putBoolean("signed_in", false).apply()
                        SessionStore(this@MainActivity).clear()
                        syncTrigger?.stop()
                        syncTrigger = null
                        observing = false
                        showLogin()
                    } else navigate(name)
                }
            }
            menu.addView(item)
        }
        return ScrollView(this).apply { addView(menu) }
    }

    private fun observeData() {
        if (observing) return
        observing = true
        scope.launch { repository.incomes.collect { if (current == "Dashboard" || current == "Income" || current == "Reports") render() } }
        scope.launch { repository.expenses.collect { if (current == "Dashboard" || current == "Expenses" || current == "Reports") render() } }
        scope.launch { repository.debts.collect { if (current == "Dashboard" || current == "Debt Tracker") render() } }
        scope.launch { repository.audit.collect { if (current == "History") render() } }
    }

    private fun navigate(name: String) {
        current = name
        pageTitle.text = name
        render()
    }

    private fun render() {
        if (!::page.isInitialized) return
        page.removeAllViews()
        when (current) {
            "Dashboard" -> renderDashboard()
            "Income" -> renderTransactions(true)
            "Expenses" -> renderTransactions(false)
            "Debt Tracker" -> renderDebts()
            "History" -> renderHistory()
            "Reports" -> renderReports()
            "Settings" -> renderSettings()
            "My Profile" -> renderProfile()
            "Users" -> renderUsers()
        }
    }

    private fun renderDashboard() {
        page.addView(label("Good day 👋", 25f))
        page.addView(label("Here's your financial snapshot", 14f))
        val inc = runBlockingValue { repository.incomesValue() }
        val exp = runBlockingValue { repository.expensesValue() }
        val income = inc.sumByLong { it.amount }; val expense = exp.sumByLong { it.amount }
        val month = today().substring(0, 7)
        val monthlyIncome = inc.filter { it.occurredOn.startsWith(month) }.sumByLong { it.amount }
        val monthlyExpense = exp.filter { it.occurredOn.startsWith(month) }.sumByLong { it.amount }
        val weekStart = dateDaysAgo(6)
        val weeklyIncome = inc.filter { it.occurredOn >= weekStart }.sumByLong { it.amount }
        val weeklyExpense = exp.filter { it.occurredOn >= weekStart }.sumByLong { it.amount }
        val cards = horizontal()
        cards.addView(card("THIS MONTH", "₱${money(monthlyIncome - monthlyExpense)}", "Net balance"), weight())
        cards.addView(card("ALL TIME", "₱${money(income - expense)}", "Available net"), weight())
        page.addView(cards)
        page.addView(card("LAST 7 DAYS", "₱${money(weeklyIncome - weeklyExpense)}",
            "Income ₱${money(weeklyIncome)}  •  Expenses ₱${money(weeklyExpense)}"))
        page.addView(label("Quick actions", 18f))
        val actions = horizontal()
        if (permission.canAddIncome) actions.addView(button("+ Income").also { it.setOnClickListener { addTransaction(true) } }, weight())
        if (permission.canAddExpenses) actions.addView(button("+ Expense").also { it.setOnClickListener { addTransaction(false) } }, weight())
        page.addView(actions)
        if (permission.canManageDebts) page.addView(button("+ Track a debt").also { it.setOnClickListener { addDebt() } })
        page.addView(label("Recent activity", 18f))
        inc.take(3).forEach { page.addView(row("↑ ${it.title}", "₱${money(it.amount)}", Color.rgb(34, 145, 92)) { deleteIncome(it.id) }) }
        exp.take(3).forEach { page.addView(row("↓ ${it.title}", "₱${money(it.amount)}", Color.rgb(205, 71, 71)) { deleteExpense(it.id) }) }
    }

    private fun renderTransactions(income: Boolean) {
        page.addView(label(if (income) "Income" else "Expenses", 26f))
        page.addView(label(if (income) "Keep every inflow in one place." else "See where your money goes.", 14f))
        if (if (income) permission.canAddIncome else permission.canAddExpenses) {
            page.addView(button(if (income) "+ Add income" else "+ Add expense").also { it.setOnClickListener { addTransaction(income) } })
        }
        val search = field("Search ${if (income) "income" else "expenses"}…")
        page.addView(search)
        val list = vertical(); page.addView(list)
        fun update() {
            list.removeAllViews()
            val query = search.text.toString()
            if (income) {
                runBlockingValue { repository.incomesValue() }
                    .filter { it.title.contains(query, true) }
                    .forEach { item ->
                    list.addView(transactionRow(item.title, "₱${money(item.amount)}  •  ${item.occurredOn}${item.category?.let { " • $it" } ?: ""}", Color.rgb(34, 145, 92),
                        permission.canEditIncome, permission.canDeleteIncome,
                        { if (permission.canEditIncome) addTransaction(true, item) },
                        { if (permission.canDeleteIncome) deleteIncome(item.id) }))
                    }
            } else {
                runBlockingValue { repository.expensesValue() }
                    .filter { it.title.contains(query, true) }
                    .forEach { item ->
                    list.addView(transactionRow(item.title, "₱${money(item.amount)}  •  ${item.occurredOn}${item.category?.let { " • $it" } ?: ""}", Color.rgb(205, 71, 71),
                        permission.canEditExpenses, permission.canDeleteExpenses,
                        { if (permission.canEditExpenses) addTransaction(false, item) },
                        { if (permission.canDeleteExpenses) deleteExpense(item.id) }))
                    }
            }
            if (list.childCount == 0) list.addView(label("No matching records yet.", 14f))
        }
        search.addTextChangedListener(SimpleTextWatcher { update() }); update()
    }

    private fun renderDebts() {
        page.addView(label("Debt Tracker", 26f))
        page.addView(label("Track borrowers, due dates, and monthly commitments.", 14f))
        if (permission.canManageDebts) page.addView(button("+ Add debt").also { it.setOnClickListener { addDebt() } })
        runBlockingValue { repository.debtsValue() }.forEach { debt ->
            val box = card(debt.person, "₱${money(debt.principalAmount)}  •  ${debt.status.toUpperCase(Locale.US)}",
                "Started ${debt.startDate}${debt.dueDate?.let { "  •  Due $it" } ?: ""}")
            val actions = horizontal()
            actions.addView(button("Edit").also { it.setOnClickListener { editDebt(debt) } }, weight())
            actions.addView(button(if (debt.status == "paid") "Mark active" else "Mark paid").also {
                it.setOnClickListener { scope.launch { repository.setDebtStatus(debt.id, if (debt.status == "paid") "active" else "paid"); syncAfterAction() } }
            }, weight())
            actions.addView(button("Delete").also { it.setOnClickListener { deleteDebt(debt.id) } }, weight())
            box.addView(actions); page.addView(box)
            page.addView(label("Monthly payment status: ${debt.monthlyExpectedPayment?.let { "₱${money(it)} expected" } ?: "Not set"}", 13f))
            page.addView(button("Record this month's payment").also { it.setOnClickListener {
                showPaymentStatusDialog(debt)
            } })
        }
    }

    private fun showPaymentStatusDialog(debt: Debt) {
        val statuses = arrayOf("Paid", "Partially Paid", "Unpaid")
        AlertDialog.Builder(this)
            .setTitle("Payment status for ${today().substring(0, 7)}")
            .setItems(statuses) { _, index ->
                val status = when (index) {
                    0 -> "paid"
                    1 -> "partially_paid"
                    else -> "unpaid"
                }
                val amount = if (status == "unpaid") 0L else debt.monthlyExpectedPayment ?: 0L
                scope.launch {
                    repository.savePayment(
                        DebtMonthlyPayment(
                            debtId = debt.id,
                            userId = userId,
                            paymentMonth = today().substring(0, 7),
                            amount = amount,
                            status = status
                        )
                    )
                    syncAfterAction()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun renderHistory() {
        page.addView(label("History", 26f)); page.addView(label("An audit trail of changes made in this account.", 14f))
        val logs = runBlockingValue { repository.audit.first() }
        if (logs.isEmpty()) page.addView(label("No activity recorded yet.", 15f))
        logs.forEach { page.addView(row(it.action.toUpperCase(Locale.US), "${it.recordType ?: "record"}  •  ${dateTime(it.timestamp)}", Color.GRAY, null)) }
    }

    private fun renderReports() {
        page.addView(label("Reports", 26f)); page.addView(label("Plain-language summaries for quick decisions.", 14f))
        val inc = runBlockingValue { repository.incomesValue() }; val exp = runBlockingValue { repository.expensesValue() }
        val i = inc.sumByLong { it.amount }; val e = exp.sumByLong { it.amount }
        page.addView(card("CASH FLOW", "₱${money(i-e)}", "Income ₱${money(i)} minus expenses ₱${money(e)}"))
        page.addView(card("RECORDS", "${inc.size + exp.size}", "${inc.size} income entries • ${exp.size} expense entries"))
        page.addView(ChartView(this, i, e), LinearLayout.LayoutParams(-1, dp(190)))
        page.addView(label(if (i >= e) "You're spending within recorded income." else "Recorded expenses exceed income—review your expense list.", 15f))
    }

    private fun renderSettings() {
        page.addView(label("Settings", 26f)); page.addView(label("Personalize your MoneyTrack workspace.", 14f))
        val theme = Switch(this).apply { text = "Dark theme"; isChecked = dark; setPadding(dp(8), dp(18), dp(8), dp(18)) }
        theme.setOnCheckedChangeListener { _, checked ->
            dark = checked; getSharedPreferences("moneytrack", 0).edit().putBoolean("dark", dark).apply(); buildShell(); observeData(); navigate(current)
        }
        page.addView(theme)
        page.addView(label("Background color (RGB)", 14f))
        page.addView(rgbEditor("background", getThemeColor("background", if (dark) Color.rgb(20,25,31) else Color.rgb(247,249,252))) { color ->
            saveThemeColor("background", color); buildShell(); observeData(); navigate(current)
        })
        page.addView(label("Font / text color (RGB)", 14f))
        page.addView(rgbEditor("text", getThemeColor("text", if (dark) Color.LTGRAY else Color.rgb(35,45,58))) { color ->
            saveThemeColor("text", color); buildShell(); observeData(); navigate(current)
        })
        page.addView(label("Accent color", 14f))
        val accents = horizontal()
        listOf(Color.rgb(35,105,175), Color.rgb(34,145,92), Color.rgb(155,75,170), Color.rgb(205,71,71)).forEach { color ->
            accents.addView(button("●").apply {
                setTextColor(color)
                setOnClickListener { accent = color; getSharedPreferences("moneytrack", 0).edit().putInt("accent", color).apply(); buildShell(); observeData(); navigate(current) }
            }, weight())
        }
        page.addView(accents)
        page.addView(label("Accent color (RGB)", 14f))
        page.addView(rgbEditor("accent", accent) { color ->
            accent = color
            saveThemeColor("accent", color)
            buildShell(); observeData(); navigate(current)
        })
        page.addView(label("Dashboard container color (RGB)", 14f))
        page.addView(rgbEditor("container", dashboardContainer) { color ->
            dashboardContainer = color
            saveThemeColor("container", color)
            render()
        })
        page.addView(button("Reset local preferences").also { it.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Reset preferences?").setMessage("Your finance records are kept; only sign-in and theme preferences reset.")
                .setPositiveButton("Reset") { _, _ -> getSharedPreferences("moneytrack", 0).edit().clear().apply(); showLogin() }.setNegativeButton("Cancel", null).show()
        } })
        page.addView(button("Reset theme only").also { it.setOnClickListener {
            getSharedPreferences("moneytrack", 0).edit()
                .remove("dark").remove("accent").remove("theme_background").remove("theme_text").remove("theme_accent").apply()
            dark = false
            accent = Color.rgb(35, 105, 175)
            dashboardContainer = Color.WHITE
            buildShell(); observeData(); navigate(current)
        } })
    }

    private fun renderProfile() {
        page.addView(label("My Profile", 26f)); page.addView(label("Personal account", 14f))
        val session = SessionStore(this)
        page.addView(card("NAME", session.fullName ?: "Name not set", "Your Supabase profile name"))
        page.addView(card("EMAIL", session.email ?: "Email unavailable", "Signed-in Supabase account"))
        page.addView(card("ROLE", session.role, "Cloud role"))
        page.addView(label("Permissions: ${if (permission.role == LocalRole.ADMIN) "All permissions" else session.permissions().sorted().joinToString().ifBlank { "No additional permissions" }}", 15f))
        page.addView(button("Set or change my name").also { it.setOnClickListener { editOwnProfileName() } })
    }

    private fun renderUsers() {
        page.addView(label("Users", 26f)); page.addView(label("Roles and permissions for this AndroidIDE MVP.", 14f))
        val prefs = getSharedPreferences("moneytrack", 0)
        if (permission.canManageUsers && cloudUsers.isEmpty()) {
            page.addView(label("Loading Supabase users...", 14f))
            scope.launch {
                try {
                    cloudUsers = withContext(Dispatchers.IO) { syncRepository.fetchProfiles() }
                    render()
                } catch (error: Exception) {
                    Toast.makeText(this@MainActivity, error.message ?: "User fetch failed", Toast.LENGTH_LONG).show()
                }
            }
        }
        if (cloudUsers.isNotEmpty()) {
            page.addView(label("Supabase users", 16f))
            cloudUsers.forEach { user ->
                page.addView(transactionRow(user.first, user.second, accent, permission.canManageUsers, permission.canManageUsers,
                    {
                        val id = user.first.substringBefore(" •")
                        editCloudUser(id, user.first.substringAfter(" • ", ""))
                    }, {
                        val id = user.first.substringBefore(" •")
                        scope.launch {
                            if (syncRepository.clearProfileName(id)) {
                                cloudUsers = withContext(Dispatchers.IO) { syncRepository.fetchProfiles() }
                                render()
                            }
                        }
                    }))
            }
            if (cloudUsers.none { it.second == "admin" }) {
                page.addView(label("No admin is assigned. Verify your Supabase password to claim the first admin role.", 14f))
                page.addView(button("Set me as first admin").also { it.setOnClickListener { claimFirstAdmin() } })
            }
        }
        if (permission.canManageUsers) page.addView(button("Add user").also { it.setOnClickListener { addSupabaseUser() } })
        page.addView(label("Admins can rename profiles, clear a display name, and assign admin or user roles. Authentication accounts are not deleted by the publishable-key client.", 13f))
    }

    private fun editManagedUser(existing: String?) {
        val parts = existing?.split("|")
        val name = field("User name or email").apply { setText(parts?.getOrElse(0) { "" } ?: "") }
        val role = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, LocalRole.values().map { it.name }.toTypedArray())
            setSelection(LocalRole.values().indexOf(runCatching { LocalRole.valueOf(parts?.getOrElse(1) { "VIEWER" } ?: "VIEWER") }.getOrDefault(LocalRole.VIEWER)))
        }

        val box = vertical(); box.addView(name); box.addView(role)
        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add user" else "Edit user")
            .setView(box)
            .setPositiveButton("Save") { _, _ ->
                val value = name.text.toString().trim()
                if (value.isEmpty()) return@setPositiveButton
                val users = HashSet(getSharedPreferences("moneytrack", 0).getStringSet("managed_users", emptySet()) ?: emptySet())
                if (existing != null) users.remove(existing)
                users.add("$value|${LocalRole.values()[role.selectedItemPosition].name}")
                getSharedPreferences("moneytrack", 0).edit().putStringSet("managed_users", users).apply()
                render()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addSupabaseUser() {
        val email = field("Email")
        val name = field("Full name")
        val password = field("Temporary password").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val box = vertical(); box.addView(name); box.addView(email); box.addView(password)
        AlertDialog.Builder(this).setTitle("Add Supabase user").setView(box)
            .setPositiveButton("Create") { _, _ ->
                scope.launch {
                    if (name.text.toString().trim().isEmpty()) {
                        Toast.makeText(this@MainActivity, "A user name is required.", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    val created = syncRepository.createUser(email.text.toString().trim(), password.text.toString(), name.text.toString().trim())
                    Toast.makeText(this@MainActivity,
                        if (created) "User created. Assign role and permissions after refreshing users." else syncRepository.lastError ?: "User creation failed.",
                        Toast.LENGTH_LONG).show()
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun editOwnProfileName() {
        val name = field("Full name").apply { setText(SessionStore(this@MainActivity).fullName ?: "") }
        AlertDialog.Builder(this).setTitle("My profile name").setView(name)
            .setPositiveButton("Save") { _, _ ->
                val value = name.text.toString().trim()
                if (value.isNotEmpty()) scope.launch {
                    if (syncRepository.updateProfileName(userId, value)) {
                        SessionStore(this@MainActivity).fullName = value
                        render()
                    }
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun claimFirstAdmin() {
        val password = field("Supabase password").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        AlertDialog.Builder(this).setTitle("Verify first admin")
            .setMessage(SessionStore(this).email ?: "Signed-in email")
            .setView(password)
            .setPositiveButton("Verify") { _, _ ->
                scope.launch {
                    val session = SessionStore(this@MainActivity)
                    val result = SupabaseAuth(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY)
                        .signIn(session.email.orEmpty(), password.text.toString())
                    if (result is AuthResult.Success && syncRepository.updateProfile(userId, session.fullName ?: session.email.orEmpty(), "admin")) {
                        session.role = "admin"
                        cloudUsers = withContext(Dispatchers.IO) { syncRepository.fetchProfiles() }
                        render()
                    } else {
                        Toast.makeText(this@MainActivity, "Password verification failed.", Toast.LENGTH_LONG).show()
                    }
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun editCloudUser(id: String, currentName: String) {
        scope.launch {
            val currentPermissions = withContext(Dispatchers.IO) { syncRepository.fetchPermissions(id) }
            showCloudUserEditor(id, currentName, currentPermissions)
        }
    }

    private fun showCloudUserEditor(id: String, currentName: String, currentPermissions: Set<String>) {
        val name = field("Full name").apply { setText(if (currentName == "null") "" else currentName) }
        val role = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("user", "admin"))
            setSelection(if (cloudUsers.firstOrNull { it.first.startsWith(id) }?.second == "admin") 1 else 0)
        }
        val permissionKeys = arrayOf(
            "view_all_records" to "View all records", "add_income" to "Add income", "edit_income" to "Edit income",
            "delete_income" to "Delete income", "add_expenses" to "Add expenses", "edit_expenses" to "Edit expenses",
            "delete_expenses" to "Delete expenses", "manage_debts" to "Manage debts", "view_reports" to "View reports",
            "view_history" to "View history", "manage_users" to "Manage users"
        )
        val checks = mutableMapOf<String, CheckBox>()
        val box = vertical(); box.addView(name); box.addView(role)
        box.addView(label("Permissions", 15f))
        permissionKeys.forEach { (key, title) ->
            checks[key] = CheckBox(this).apply {
                text = title
                isChecked = currentPermissions.contains(key)
                box.addView(this)
            }
        }
        AlertDialog.Builder(this).setTitle("Edit user").setView(box)
            .setPositiveButton("Save") { _, _ ->
                val value = name.text.toString().trim()
                scope.launch {
                    if (value.isEmpty()) {
                        Toast.makeText(this@MainActivity, "A user name is required.", Toast.LENGTH_LONG).show()
                    } else if (syncRepository.updateProfile(id, value, role.selectedItem.toString())) {
                        checks.forEach { (key, check) -> syncRepository.setPermission(id, key, check.isChecked) }
                        cloudUsers = withContext(Dispatchers.IO) { syncRepository.fetchProfiles() }
                        render()
                    }
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun addTransaction(income: Boolean, existing: Any? = null) {
        val title = field("Description"); val amount = field("Amount in PHP").apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val date = field("Date (YYYY-MM-DD)").apply { setText(if (existing is Income) existing.occurredOn else if (existing is Expense) existing.occurredOn else today()) }
        val category = field("Category"); val notes = field("Notes")
        if (existing is Income) { title.setText(existing.title); amount.setText((existing.amount / 100.0).toString()); category.setText(existing.category ?: ""); notes.setText(existing.notes ?: "") }
        if (existing is Expense) { title.setText(existing.title); amount.setText((existing.amount / 100.0).toString()); category.setText(existing.category ?: ""); notes.setText(existing.notes ?: "") }
        val box = vertical(); box.addView(title); box.addView(amount); box.addView(date); box.addView(category); box.addView(notes)
        AlertDialog.Builder(this).setTitle(if (income) "Add income" else "Add expense").setView(box)
            .setPositiveButton("Save") { _, _ ->
                val cents = ((amount.text.toString().toDoubleOrNull() ?: 0.0) * 100).toLong()
                if (title.text.toString().trim().isEmpty() || cents <= 0) return@setPositiveButton
                scope.launch { if (income) repository.saveIncome(Income(id = (existing as? Income)?.id ?: UUID.randomUUID().toString(), userId=userId,title=title.text.toString(),amount=cents,occurredOn=date.text.toString(),category=category.text.toString().trim().takeIf { it.isNotEmpty() },notes=notes.text.toString().takeIf { it.isNotEmpty() }))
                else repository.saveExpense(Expense(id = (existing as? Expense)?.id ?: UUID.randomUUID().toString(), userId=userId,title=title.text.toString(),amount=cents,occurredOn=date.text.toString(),category=category.text.toString().trim().takeIf { it.isNotEmpty() },notes=notes.text.toString().takeIf { it.isNotEmpty() })); syncAfterAction() }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun addDebt(existing: Debt? = null) {
        val person = field("Borrower / debtor"); val principal = field("Principal amount").apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }; val monthly = field("Monthly payment (optional)").apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val rate = field("Interest rate %"); val months = field("Number of months").apply { inputType = InputType.TYPE_CLASS_NUMBER }; val type = field("Interest type: flat, simple, reducing")
        val due = field("Due date (YYYY-MM-DD)"); val notes = field("Notes")
        existing?.let {
            person.setText(it.person); principal.setText((it.principalAmount / 100.0).toString())
            rate.setText(it.interestRate.toString()); type.setText(it.interestType); months.setText(it.numberOfMonths.toString())
            monthly.setText(it.monthlyExpectedPayment?.let { amount -> (amount / 100.0).toString() } ?: "")
            due.setText(it.dueDate ?: ""); notes.setText(it.notes ?: "")
        }
        val box = vertical(); box.addView(person); box.addView(principal); box.addView(rate); box.addView(type); box.addView(months); box.addView(monthly); box.addView(due); box.addView(notes)
        AlertDialog.Builder(this).setTitle("Add debt").setView(box).setPositiveButton("Save") { _, _ ->
            val cents = ((principal.text.toString().toDoubleOrNull() ?: 0.0) * 100).toLong()
            val monthlyCents = ((monthly.text.toString().toDoubleOrNull() ?: 0.0) * 100).toLong().takeIf { it > 0 }
            if (person.text.toString().trim().isNotEmpty() && cents > 0) scope.launch {
                val n = (months.text.toString().toIntOrNull() ?: 1).coerceAtLeast(1)
                val interest = rate.text.toString().toDoubleOrNull() ?: 0.0
                val kind = type.text.toString().trim().ifEmpty { "flat" }
                val total = DebtCalculator.total(cents, interest, kind, n)
                repository.saveDebt(Debt(id=existing?.id ?: UUID.randomUUID().toString(),userId=userId,person=person.text.toString(),principalAmount=cents,interestRate=interest,interestType=kind,numberOfMonths=n,startDate=existing?.startDate ?: today(),dueDate=due.text.toString().takeIf { it.isNotBlank() },monthlyExpectedPayment=monthlyCents ?: DebtCalculator.monthly(total,n),totalPayable=total,notes=notes.text.toString().takeIf { it.isNotBlank() })); syncAfterAction()
            }
        }.setNegativeButton("Cancel", null).show()
    }

    private fun editDebt(debt: Debt) { addDebt(debt) }

    private fun deleteIncome(id: String) { scope.launch { repository.deleteIncome(id); syncAfterAction() } }
    private fun deleteExpense(id: String) { scope.launch { repository.deleteExpense(id); syncAfterAction() } }
    private fun deleteDebt(id: String) { scope.launch { repository.deleteDebt(id); syncAfterAction() } }

    private fun syncAfterAction() {
        if (!::syncRepository.isInitialized || !::syncProcessor.isInitialized) return
        scope.launch(Dispatchers.IO) {
            syncRepository.refreshSession()
            syncRepository.refreshAccessPolicy()
            withContext(Dispatchers.Main) { reloadRepositoryScope() }
            syncProcessor.synchronize(
                uploader = { item -> syncRepository.upload(item) },
                puller = { syncRepository.pullAll(FinanceDatabase.create(this@MainActivity), userId) }
            )
        }
    }

    private fun reloadRepositoryScope() {
        val session = SessionStore(this)
        val viewAll = session.role == "admin" || session.permissions().contains("view_all_records")
        repository = FinanceRepository(FinanceDatabase.create(this), userId, viewAll)
        observing = false
        observeData()
        render()
    }

    private fun vertical() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(18), dp(20), dp(18)) }
    private fun horizontal() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    private fun label(value: String, size: Float) = TextView(this).apply { text=value; textSize=size; setTextColor(getThemeColor("text", if (dark) Color.LTGRAY else Color.rgb(35,45,58))); setPadding(dp(6), dp(7), dp(6), dp(7)) }
    private fun field(hint: String) = EditText(this).apply { this.hint=hint; setSingleLine(true); setPadding(dp(10), dp(10), dp(10), dp(10)) }
    private fun button(value: String) = Button(this).apply { text=value; isAllCaps=false }
    private fun card(title: String, value: String, detail: String): LinearLayout {
        val box = vertical().apply { setPadding(dp(16), dp(14), dp(16), dp(14)); setBackgroundColor(dashboardContainer) }
        box.addView(label(title, 11f)); box.addView(label(value, 22f)); box.addView(label(detail, 12f)); return box
    }
    private fun row(title: String, detail: String, color: Int, action: (() -> Unit)?): LinearLayout {
        val r = horizontal().apply { setPadding(dp(8), dp(6), dp(8), dp(6)) }
        val t = label(title, 15f).apply { setTextColor(color) }; r.addView(vertical().apply { addView(t); addView(label(detail, 12f)) }, LinearLayout.LayoutParams(0,-2,1f))
        if (action != null) r.addView(button("Delete").also { it.setOnClickListener { action() } }); return r
    }
    private fun transactionRow(title: String, detail: String, color: Int, canEdit: Boolean, canDelete: Boolean, edit: () -> Unit, delete: () -> Unit): LinearLayout {
        val r = horizontal().apply { setPadding(dp(8), dp(6), dp(8), dp(6)) }
        val t = label(title, 15f).apply { setTextColor(color) }
        r.addView(vertical().apply { addView(t); addView(label(detail, 12f)) }, LinearLayout.LayoutParams(0, -2, 1f))
        if (canEdit) r.addView(button("Edit").also { it.setOnClickListener { edit() } })
        if (canDelete) r.addView(button("Delete").also { it.setOnClickListener { delete() } })
        return r
    }
    private fun rgbEditor(name: String, initial: Int, changed: (Int) -> Unit): LinearLayout {
        val box = horizontal()
        val preview = TextView(this).apply {
            text = "Preview"
            gravity = Gravity.CENTER
            setTextColor(if (name == "text") initial else Color.WHITE)
            setBackgroundColor(if (name == "text") getThemeColor("background", Color.DKGRAY) else initial)
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        val red = field("R").apply { inputType = InputType.TYPE_CLASS_NUMBER; setText(Color.red(initial).toString()) }
        val green = field("G").apply { inputType = InputType.TYPE_CLASS_NUMBER; setText(Color.green(initial).toString()) }
        val blue = field("B").apply { inputType = InputType.TYPE_CLASS_NUMBER; setText(Color.blue(initial).toString()) }
        fun previewColor() {
            val color = Color.rgb(red.text.toString().toIntOrNull()?.coerceIn(0, 255) ?: 0,
                green.text.toString().toIntOrNull()?.coerceIn(0, 255) ?: 0,
                blue.text.toString().toIntOrNull()?.coerceIn(0, 255) ?: 0)
            if (name == "text") {
                preview.setTextColor(color)
                preview.setBackgroundColor(getThemeColor("background", Color.DKGRAY))
            } else {
                preview.setTextColor(Color.WHITE)
                preview.setBackgroundColor(color)
            }
        }
        val watcher = SimpleTextWatcher { previewColor() }
        red.addTextChangedListener(watcher); green.addTextChangedListener(watcher); blue.addTextChangedListener(watcher)
        val apply = button("Apply")
        apply.setOnClickListener {
            val color = Color.rgb(red.text.toString().toIntOrNull()?.coerceIn(0, 255) ?: 0,
                green.text.toString().toIntOrNull()?.coerceIn(0, 255) ?: 0,
                blue.text.toString().toIntOrNull()?.coerceIn(0, 255) ?: 0)
            changed(color)
        }
        box.addView(preview, weight()); box.addView(red, weight()); box.addView(green, weight()); box.addView(blue, weight()); box.addView(apply)
        return box
    }
    private fun getThemeColor(name: String, fallback: Int): Int =
        getSharedPreferences("moneytrack", 0).getInt("theme_$name", fallback)
    private fun saveThemeColor(name: String, color: Int) {
        getSharedPreferences("moneytrack", 0).edit().putInt("theme_$name", color).apply()
    }
    private fun weight() = LinearLayout.LayoutParams(0, -2, 1f)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun money(cents: Long) = String.format(Locale.US, "%.2f", cents / 100.0)
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun dateDaysAgo(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }
    private fun dateTime(value: Long) = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US).format(Date(value))
    private fun <T> runBlockingValue(block: suspend () -> T): T = kotlinx.coroutines.runBlocking { block() }
    private fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long = fold(0L) { total, item -> total + selector(item) }
}

private class SimpleTextWatcher(val changed: () -> Unit) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { changed() }
    override fun afterTextChanged(s: android.text.Editable?) {}
}
