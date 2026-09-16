package ph.moneytrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ph.moneytrack.data.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity:ComponentActivity() {
    override fun onCreate(state:Bundle?) { super.onCreate(state); setContent { MoneyTrackApp() } }
}

@Composable fun MoneyTrackApp() {
    val context=androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { context.getSharedPreferences("moneytrack", 0) }
    var signedIn by remember { mutableStateOf(preferences.getBoolean("signed_in", false)) }
    if (!signedIn) {
        LoginScreen(
            onOffline = { preferences.edit().putBoolean("signed_in", true).apply(); signedIn = true },
            onSignedIn = { preferences.edit().putBoolean("signed_in", true).apply(); signedIn = true }
        )
        return
    }
    val userId = remember { preferences.getString("user_id", null) ?: UUID.randomUUID().toString().also { preferences.edit().putString("user_id", it).apply() } }
    val repo=remember { FinanceRepository(FinanceDatabase.create(context),userId) }
    val incomes by repo.incomes.collectAsState(emptyList()); val expenses by repo.expenses.collectAsState(emptyList())
    val debts by repo.debts.collectAsState(emptyList()); val scope=rememberCoroutineScope()
    MaterialTheme { Scaffold(topBar={TopAppBar(title={Text("MoneyTrack PH")})}) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp)) {
            item { Text("Monthly overview",style=MaterialTheme.typography.h6); Text("Income: ₱${incomes.fold(0L) { total, item -> total + item.amount }/100.0}"); Text("Expenses: ₱${expenses.fold(0L) { total, item -> total + item.amount }/100.0}"); Spacer(Modifier.height(16.dp)) }
            item { Button(onClick={scope.launch { repo.saveIncome(Income(userId=userId,title="Sample income",amount=100000,occurredOn="2026-01-01")) }}) { Text("Add income") }; Spacer(Modifier.height(8.dp)); Button(onClick={scope.launch { repo.saveExpense(Expense(userId=userId,title="Sample expense",amount=25000,occurredOn="2026-01-01")) }}) { Text("Add expense") }; Spacer(Modifier.height(8.dp)); Button(onClick={scope.launch { repo.saveDebt(Debt(userId=userId,person="Sample debt",principalAmount=500000,startDate="2026-01-01",monthlyExpectedPayment=50000,totalPayable=500000)) }}) { Text("Add debt") } }
            items(incomes.size) { i -> val item=incomes[i]; Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text("${item.title}: ₱${item.amount/100.0}"); TextButton(onClick={scope.launch { repo.deleteIncome(item.id) }}) { Text("Delete") } } }
            items(expenses.size) { i -> val item=expenses[i]; Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text("${item.title}: ₱${item.amount/100.0}"); TextButton(onClick={scope.launch { repo.deleteExpense(item.id) }}) { Text("Delete") } } }
            item { Spacer(Modifier.height(16.dp)); Text("Debts",style=MaterialTheme.typography.h6) }
            items(debts.size) { i -> val debt=debts[i]; Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text("${debt.person}: ${debt.status}"); Button(onClick={scope.launch { repo.setDebtStatus(debt.id,if(debt.status=="paid")"active" else "paid") }}) { Text(if(debt.status=="paid")"Reopen" else "Paid") } } }
        }
    } }
}

@Composable
private fun LoginScreen(onOffline: () -> Unit, onSignedIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val auth = remember { SupabaseAuth(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("MoneyTrack PH", style = MaterialTheme.typography.h4)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            scope.launch {
                auth.signIn(email, password).fold({ onSignedIn() }, { message = it.message ?: "Sign in failed" })
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Sign in") }
        TextButton(onClick = {
            scope.launch {
                auth.signUp(email, password).fold({ message = "Account created. Check email confirmation." }, { message = "Account created. You can sign in." })
            }
        }) { Text("Create account") }
        OutlinedButton(onClick = onOffline, modifier = Modifier.fillMaxWidth()) { Text("Continue offline") }
        if (message.isNotEmpty()) Text(message, style = MaterialTheme.typography.caption)
    }
}
