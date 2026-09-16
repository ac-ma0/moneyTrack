package com.example.moneytrackph.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("MoneyTrack PH", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Button({ onLogin(email, password) }, Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("Sign in / Create account")
        }
    }
}

@Composable
fun DashboardScreen(nav: NavController, toggleTheme: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar({ Text("Dashboard") }, actions = {
                IconButton(toggleTheme) { Icon(Icons.Default.Settings, "Theme settings") }
            })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(true, {}, { Icon(Icons.Default.Home, "Home") }, label = { Text("Home") })
                NavigationBarItem(false, { nav.navigate("history") }, { Icon(Icons.Default.History, "History") }, label = { Text("History") })
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Your finances at a glance", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(20.dp))
            listOf("income" to "Add income", "expenses" to "Add expense", "debts" to "Debt tracker").forEach { (route, label) ->
                Button({ nav.navigate(route) }, Modifier.fillMaxWidth().padding(vertical = 6.dp)) { Text(label) }
            }
        }
    }
}

@Composable
fun TransactionScreen(nav: NavController, title: String) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(amount, { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(note, { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
        Button({ nav.popBackStack() }, Modifier.padding(top = 16.dp)) { Text("Save offline") }
    }
}

@Composable
fun DebtScreen(nav: NavController) {
    var name by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf("") }
    var monthly by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Debt tracker", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(name, { name = it }, label = { Text("Debt name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(principal, { principal = it }, label = { Text("Principal") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(monthly, { monthly = it }, label = { Text("Monthly payment") }, modifier = Modifier.fillMaxWidth())
        Button({ nav.popBackStack() }, Modifier.padding(top = 16.dp)) { Text("Add debt") }
        Text("Track monthly payments; pending changes sync when online.", Modifier.padding(top = 24.dp))
    }
}

@Composable
fun HistoryScreen(nav: NavController) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("History", style = MaterialTheme.typography.headlineMedium)
        Text("Income, expenses and debt payments appear here.", Modifier.padding(top = 16.dp))
        Button({ nav.popBackStack() }, Modifier.padding(top = 16.dp)) { Text("Back") }
    }
}
