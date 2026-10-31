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
            var wizardActive by remember { mutableStateOf(!store.onboardingCompleted()) }
            var whatsNewActive by remember { mutableStateOf(
                !wizardActive && store.shouldShowWhatsNew(ReleaseCatalog.currentVersion)
            ) }
            val setBudget: (Budget) -> Unit = { updated ->
                budget = updated
                store.save(updated)
            }
            QuantoSobraTheme {
                when {
                    wizardActive -> OnboardingWizard(
                        if (budget == Budget()) budget.copy(
                            transport = budget.transport.copy(commuteMode = CommuteMode.UNSET)
                        ) else budget
                    ) { updated ->
                        setBudget(updated)
                        store.completeOnboarding()
                        store.markWhatsNewSeen(ReleaseCatalog.currentVersion)
                        wizardActive = false
                        whatsNewActive = false
                        panel = "home"
                    }
                    whatsNewActive || panel == "news" -> WhatsNewScreen {
                        store.markWhatsNewSeen(ReleaseCatalog.currentVersion)
                        whatsNewActive = false
                        panel = "home"
                    }
                    else -> {
                        Dashboard(budget, { panel = it })
                        when (panel) {
                            "salary" -> SalaryScreen(budget, setBudget) { panel = "home" }
                            "housing" -> HousingScreen(budget, setBudget) { panel = "home" }
                            "food" -> FoodScreen(budget, setBudget) { panel = "home" }
                            "bills" -> BillsScreen(budget, setBudget) { panel = "home" }
                            "transport" -> TransportScreen(budget, setBudget) { panel = "home" }
                            "extras" -> ExtrasScreen(budget, setBudget) { panel = "home" }
                            "reserve" -> ReserveScreen(budget, setBudget) { panel = "home" }
                            "settings" -> SettingsScreen(
                                onDismiss = { panel = "home" },
                                onRestartWizard = { panel = "home"; wizardActive = true },
                                onLoadExample = { example -> setBudget(example); panel = "home" },
                                onClearData = {
                                    store.clearAndRestartWizard()
                                    budget = Budget()
                                    panel = "home"
                                    whatsNewActive = false
                                    wizardActive = true
                                },
                                onOpenNews = { panel = "news" },
                                onOpenAbout = { panel = "about" }
                            )
                            "about" -> AboutScreen(
                                onBack = { panel = "settings" },
                                onOpenNews = { panel = "news" }
                            )
                        }
                    }
                }
            }
        }
    }
}
