package com.example.moneytrackph
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.*
import com.example.moneytrackph.data.Repositories
import com.example.moneytrackph.ui.*
import kotlinx.coroutines.launch
class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{MoneyTrackPHApp()}}}
@Composable fun MoneyTrackPHApp(){
    var dark by remember{mutableStateOf(false)}
    val nav=rememberNavController()
    val scope=rememberCoroutineScope()
    val repositories=remember{Repositories(androidx.compose.ui.platform.LocalContext.current.applicationContext)}
    MaterialTheme(if(dark) darkColorScheme() else lightColorScheme()){
        NavHost(nav,"login"){
            composable("login"){LoginScreen{email,password->
                scope.launch {
                    runCatching { repositories.signIn(email,password) }
                        .recoverCatching { repositories.signUp(email,password) }
                        .onSuccess { nav.navigate("dashboard"){popUpTo("login"){inclusive=true}} }
                }
            }}
            composable("dashboard"){DashboardScreen(nav){dark=!dark}}
            composable("income"){TransactionScreen(nav,"Income")}
            composable("expenses"){TransactionScreen(nav,"Expenses")}
            composable("debts"){DebtScreen(nav)}
            composable("history"){HistoryScreen(nav)}
        }
    }
}

