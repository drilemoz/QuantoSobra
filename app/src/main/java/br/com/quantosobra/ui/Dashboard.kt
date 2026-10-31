package br.com.quantosobra.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.*

@Composable
fun Dashboard(budget: Budget, onNavigate: (String) -> Unit, onBudgetChange: (Budget) -> Unit) {
    var salaryDialog by remember { mutableStateOf(false) }
    var exampleDialog by remember { mutableStateOf(false) }
    var clearDialog by remember { mutableStateOf(false) }
    val expenses = BudgetMath.expenses(budget)
    val available = BudgetMath.availableAfterReserve(budget)
    val spentPercent = if (budget.netSalaryCents > 0)
        (expenses.toDouble() / budget.netSalaryCents).coerceIn(0.0, 1.0).toFloat() else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Column(
                Modifier.fillMaxWidth().background(
                    Brush.verticalGradient(listOf(Purple, DeepPurple))
                ).padding(start = 23.dp, end = 23.dp, top = 28.dp, bottom = 30.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(Color.White.copy(alpha = 0.16f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Text("$", fontSize = 27.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Quanto Sobra?", color = Color.White, fontSize = 25.sp,
                            fontWeight = FontWeight.ExtraBold)
                        Text("O seu dinheiro, sem surpresas.", color = Color(0xFFE9D7F3), fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(28.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SALÁRIO LÍQUIDO MENSAL", color = Color(0xFFEAD9F7),
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(Money.format(budget.netSalaryCents), color = Color.White,
                            fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    TextButton(onClick = { salaryDialog = true }) {
                        Text("Editar ✎", color = Color.White)
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp).offset(y = (-14).dp)) {
                SurfaceCard {
                    Text("Saldo disponível no mês", fontSize = 13.sp, color = Muted)
                    Text(Money.format(available), color = if (available >= 0) Positive else Negative,
                        fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    if (budget.netSalaryCents == 0L) {
                        Hint("Comece informando o salário. Depois toque em cada categoria para montar seu orçamento.")
                    } else {
                        Text(if (available >= 0) "Depois das despesas e da reserva planejada"
                            else "Seu orçamento está acima da renda informada",
                            color = Muted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { spentPercent }, modifier = Modifier.fillMaxWidth().height(10.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        color = if (expenses > budget.netSalaryCents) Negative else Purple,
                        trackColor = Lilac
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Despesas: ${Money.format(expenses)}", fontSize = 12.sp, color = Muted)
                        Text("Reserva: ${Money.format(budget.reserveCents)}", fontSize = 12.sp, color = Muted)
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 10.dp)) {
                Text("Seu orçamento", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("Toque em uma categoria para detalhar", fontSize = 13.sp, color = Muted)
            }
        }
        item { CategoryCard("⌂", "Moradia", "Aluguel, condomínio e IPTU", BudgetMath.housing(budget), "housing", onNavigate) }
        item { CategoryCard("✦", "Alimentação", "Produtos, quantidades e refeições", BudgetMath.food(budget), "food", onNavigate) }
        item { CategoryCard("⚡", "Contas da casa", "Luz, água, internet, gás e celular", BudgetMath.bills(budget), "bills", onNavigate) }
        item { CategoryCard("↗", "Transporte", "Passagens, dias de trabalho e VT", BudgetMath.transportFromAvailable(budget.transport), "transport", onNavigate) }
        item { CategoryCard("＋", "Outras despesas", "Saúde, higiene, roupas, lazer e parcelas", BudgetMath.extras(budget), "extras", onNavigate) }
        item { CategoryCard("◎", "Reserva mensal", "Emergência e objetivos", budget.reserveCents, "reserve", onNavigate) }
        item {
            Column(Modifier.padding(horizontal = 22.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                HorizontalDivider(color = Color(0xFFE8E0ED))
                Text("Simulações ficam salvas somente neste aparelho.", color = Muted, fontSize = 12.sp)
                TextButton(onClick = { exampleDialog = true }) { Text("Carregar exemplo de R$ 3.000") }
                TextButton(onClick = { clearDialog = true }) { Text("Limpar meus dados", color = Negative) }
                Text("Quanto Sobra?  •  0.1.0-alpha.3", color = Muted, fontSize = 11.sp)
            }
        }
    }

    if (salaryDialog) {
        var value by remember { mutableStateOf(if (budget.netSalaryCents == 0L) "" else Money.input(budget.netSalaryCents)) }
        AlertDialog(
            onDismissRequest = { salaryDialog = false },
            title = { Text("Seu salário líquido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = value, onValueChange = { if (it.length <= 19) value = it },
                        label = { Text("Valor mensal em R$") }, singleLine = true,
                        isError = Money.parse(value) == null)
                    Hint("Informe quanto realmente cai na sua conta, após descontos. Se o vale-transporte já foi descontado, marque isso na tela Transporte.")
                }
            },
            confirmButton = {
                TextButton(enabled = Money.parse(value) != null,
                    onClick = { onBudgetChange(budget.copy(netSalaryCents = Money.parse(value) ?: 0)); salaryDialog = false }) {
                    Text("Salvar")
                }
            },
            dismissButton = { TextButton(onClick = { salaryDialog = false }) { Text("Cancelar") } }
        )
    }
    if (exampleDialog) {
        AlertDialog(
            onDismissRequest = { exampleDialog = false },
            title = { Text("Substituir pelos dados de exemplo?") },
            text = { Text("A simulação substituirá todos os valores salvos neste aparelho.") },
            confirmButton = { TextButton(onClick = {
                onBudgetChange(Budget(
                    netSalaryCents = 300_000, rentCents = 135_000, foodOutsideCents = 80_000,
                    electricityCents = 8_000, waterCents = 6_000,
                    internetCents = 10_000, mobileCents = 3_000,
                    transport = TransportConfig(570, 2, 22, TransportMode.CLT, 300_000)
                ))
                exampleDialog = false
            }) { Text("Substituir") } },
            dismissButton = { TextButton(onClick = { exampleDialog = false }) { Text("Cancelar") } }
        )
    }
    if (clearDialog) {
        AlertDialog(
            onDismissRequest = { clearDialog = false },
            title = { Text("Apagar todo o orçamento?") },
            text = { Text("Esta ação remove os valores e produtos cadastrados. Não pode ser desfeita.") },
            confirmButton = { TextButton(onClick = { onBudgetChange(Budget()); clearDialog = false }) {
                Text("Apagar", color = Negative)
            } },
            dismissButton = { TextButton(onClick = { clearDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CategoryCard(icon: String, title: String, detail: String, total: Long,
                         route: String, onNavigate: (String) -> Unit) {
    Card(
        onClick = { onNavigate(route) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).background(Lilac, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 23.sp, color = Purple, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(detail, color = Muted, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(Money.format(total), color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("›", color = Purple, fontSize = 24.sp)
            }
        }
    }
}
