package ph.moneytrack

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ph.moneytrack.data.*
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val screenScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: FinanceRepository
    private lateinit var content: LinearLayout
    private lateinit var summary: TextView
    private val userId by lazy {
        val preferences = getSharedPreferences("moneytrack", 0)
        preferences.getString("user_id", null)
            ?: UUID.randomUUID().toString().also { preferences.edit().putString("user_id", it).apply() }
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        if (getSharedPreferences("moneytrack", 0).getBoolean("signed_in", false)) showDashboard()
        else showLogin()
    }

    override fun onDestroy() {
        screenScope.cancel()
        super.onDestroy()
    }

    private fun showLogin() {
        val root = verticalLayout()
        root.gravity = Gravity.CENTER
        val title = text("MoneyTrack PH", 26f)
        root.addView(title)
        val email = field("Email")
        val password = field("Password")
        password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        root.addView(email); root.addView(password)
        val message = text("", 13f)
        val auth = SupabaseAuth(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY)
        val signIn = button("Sign in")
        signIn.setOnClickListener {
            signIn.isEnabled = false
            screenScope.launch {
                when (val result = auth.signIn(email.text.toString(), password.text.toString())) {
                    is AuthResult.Success -> enterApp()
                    is AuthResult.Failure -> {
                        message.text = result.error.message ?: "Sign in failed"
                        signIn.isEnabled = true
                    }
                }
            }
        }
        val signUp = button("Create account")
        signUp.setOnClickListener {
            screenScope.launch {
                when (val result = auth.signUp(email.text.toString(), password.text.toString())) {
                    is AuthResult.Success -> message.text = "Account created. Check your email, then sign in."
                    is AuthResult.Failure -> message.text = result.error.message ?: "Sign up failed"
                }
            }
        }
        val offline = button("Continue offline")
        offline.setOnClickListener { enterApp() }
        root.addView(signIn); root.addView(signUp); root.addView(offline); root.addView(message)
        setContentView(root)
    }

    private fun enterApp() {
        getSharedPreferences("moneytrack", 0).edit().putBoolean("signed_in", true).apply()
        showDashboard()
    }

    private fun showDashboard() {
        repository = FinanceRepository(FinanceDatabase.create(this), userId)
        val root = verticalLayout()
        val header = horizontalLayout()
        header.addView(text("MoneyTrack PH", 24f), weightParams())
        val logout = button("Logout")
        logout.setOnClickListener {
            getSharedPreferences("moneytrack", 0).edit().putBoolean("signed_in", false).apply()
            showLogin()
        }
        header.addView(logout)
        root.addView(header)
        summary = text("Loading...", 16f)
        root.addView(summary)
        val actions = horizontalLayout()
        actions.addView(button("+ Income").also { it.setOnClickListener { addTransaction(true) } }, weightParams())
        actions.addView(button("+ Expense").also { it.setOnClickListener { addTransaction(false) } }, weightParams())
        root.addView(actions)
        root.addView(button("+ Debt").also { it.setOnClickListener { addDebt() } })
        content = verticalLayout()
        val scroll = ScrollView(this)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        screenScope.launch {
            repository.incomes.collect { refresh() }
        }
        screenScope.launch {
            repository.expenses.collect { refresh() }
        }
        screenScope.launch {
            repository.debts.collect { refresh() }
        }
    }

    private fun refresh() {
        screenScope.launch {
            val incomes = repository.incomesValue()
            val expenses = repository.expensesValue()
            val incomeTotal = incomes.fold(0L) { total, item -> total + item.amount }
            val expenseTotal = expenses.fold(0L) { total, item -> total + item.amount }
            summary.text = "Income: PHP ${money(incomeTotal)}    Expenses: PHP ${money(expenseTotal)}\nNet: PHP ${money(incomeTotal - expenseTotal)}"
            content.removeAllViews()
            incomes.forEach { addRow("${it.title}  PHP ${money(it.amount)}", "Delete") { repository.deleteIncome(it.id) } }
            expenses.forEach { addRow("${it.title}  PHP ${money(it.amount)}", "Delete") { repository.deleteExpense(it.id) } }
            repository.debtsValue().forEach { debt ->
                addRow("Debt: ${debt.person} (${debt.status})", if (debt.status == "paid") "Reopen" else "Paid") {
                    repository.setDebtStatus(debt.id, if (debt.status == "paid") "active" else "paid")
                }
            }
        }
    }

    private fun addTransaction(income: Boolean) {
        val title = field("Description")
        val amount = field("Amount in PHP")
        amount.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val box = verticalLayout(); box.addView(title); box.addView(amount)
        AlertDialog.Builder(this).setTitle(if (income) "Add income" else "Add expense").setView(box)
            .setPositiveButton("Save") { _, _ ->
                val cents = ((amount.text.toString().toDoubleOrNull() ?: 0.0) * 100).toLong()
                screenScope.launch {
                    if (income) repository.saveIncome(Income(userId = userId, title = title.text.toString(), amount = cents, occurredOn = today()))
                    else repository.saveExpense(Expense(userId = userId, title = title.text.toString(), amount = cents, occurredOn = today()))
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun addDebt() {
        val person = field("Borrower/debtor")
        val principal = field("Principal amount")
        principal.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val box = verticalLayout(); box.addView(person); box.addView(principal)
        AlertDialog.Builder(this).setTitle("Add debt").setView(box).setPositiveButton("Save") { _, _ ->
            val cents = ((principal.text.toString().toDoubleOrNull() ?: 0.0) * 100).toLong()
            screenScope.launch { repository.saveDebt(Debt(userId = userId, person = person.text.toString(), principalAmount = cents, startDate = today())) }
        }.setNegativeButton("Cancel", null).show()
    }

    private fun addRow(label: String, action: String, callback: suspend () -> Unit) {
        val row = horizontalLayout()
        row.addView(text(label, 15f), weightParams())
        row.addView(button(action).also { it.setOnClickListener { screenScope.launch { callback() } } })
        content.addView(row)
    }

    private fun verticalLayout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 24, 24, 24)
    }
    private fun horizontalLayout() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
    private fun text(value: String, size: Float) = TextView(this).apply { text = value; textSize = size; setPadding(8, 8, 8, 8) }
    private fun field(hint: String) = EditText(this).apply { this.hint = hint; setSingleLine(true); layoutParams = LinearLayout.LayoutParams(-1, -2) }
    private fun button(label: String) = Button(this).apply { text = label }
    private fun weightParams() = LinearLayout.LayoutParams(0, -2, 1f)
    private fun money(cents: Long) = "%.2f".format(cents / 100.0)
    private fun today() = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
}
