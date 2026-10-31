package br.com.quantosobra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import br.com.quantosobra.data.BudgetStore
import br.com.quantosobra.domain.*
import br.com.quantosobra.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.rgb(78, 20, 123)
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.rgb(248, 246, 250)
        setContent {
            val store = remember { BudgetStore(applicationContext) }
            var budget by remember { mutableStateOf(store.load()) }
            var screen by rememberSaveable { mutableStateOf("home") }
            val setBudget: (Budget) -> Unit = { updated ->
                budget = updated
                store.save(updated)
            }
            BackHandler(enabled = screen != "home") { screen = "home" }
            QuantoSobraTheme {
                when (screen) {
                    "home" -> Dashboard(
                        budget = budget,
                        onNavigate = { screen = it },
                        onBudgetChange = setBudget
                    )
                    "housing" -> HousingScreen(budget, setBudget) { screen = "home" }
                    "food" -> FoodScreen(budget, setBudget) { screen = "home" }
                    "bills" -> BillsScreen(budget, setBudget) { screen = "home" }
                    "transport" -> TransportScreen(budget, setBudget) { screen = "home" }
                    "extras" -> ExtrasScreen(budget, setBudget) { screen = "home" }
                    "reserve" -> ReserveScreen(budget, setBudget) { screen = "home" }
                    else -> Dashboard(budget, { screen = it }, setBudget)
                }
            }
        }
    }
}
