package br.com.quantosobra

import android.os.Bundle
import androidx.activity.ComponentActivity
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
        window.statusBarColor = android.graphics.Color.rgb(61, 11, 102)
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.rgb(248, 246, 250)
        setContent {
            val store = remember { BudgetStore(applicationContext) }
            var budget by remember { mutableStateOf(store.load()) }
            var panel by rememberSaveable { mutableStateOf("home") }
            val setBudget: (Budget) -> Unit = { updated ->
                budget = updated
                store.save(updated)
            }
            QuantoSobraTheme {
                // A home permanece ao fundo: as categorias são modais, não telas completas.
                Dashboard(budget, { panel = it }, setBudget)
                when (panel) {
                    "salary" -> SalaryScreen(budget, setBudget) { panel = "home" }
                    "housing" -> HousingScreen(budget, setBudget) { panel = "home" }
                    "food" -> FoodScreen(budget, setBudget) { panel = "home" }
                    "bills" -> BillsScreen(budget, setBudget) { panel = "home" }
                    "transport" -> TransportScreen(budget, setBudget) { panel = "home" }
                    "extras" -> ExtrasScreen(budget, setBudget) { panel = "home" }
                    "reserve" -> ReserveScreen(budget, setBudget) { panel = "home" }
                }
            }
        }
    }
}
