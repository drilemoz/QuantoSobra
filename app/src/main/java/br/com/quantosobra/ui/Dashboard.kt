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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.*

@Composable
fun Dashboard(budget: Budget, onNavigate: (String) -> Unit) {
    val income = BudgetMath.salaryNet(budget)
    val expenses = BudgetMath.expenses(budget)
    val available = BudgetMath.availableAfterReserve(budget)
    val spentPercent = if (income > 0)
        (expenses.toDouble() / income).coerceIn(0.0, 1.0).toFloat() else 0f

    LazyColumn(Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF781BD5), Purple, DeepPurple)))
            ) {
                // Formas suaves, sem imagens externas, para dar profundidade ao cabeçalho.
                Box(Modifier.size(190.dp).offset(x = 255.dp, y = (-80).dp)
                    .background(Color.White.copy(alpha = 0.055f), CircleShape))
                Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 43.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center) {
                            Text("✦", color = Color.White, fontSize = 21.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Quanto Sobra?", color = Color.White, fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold)
                            Text("Sua vida financeira, mais clara.", color = Color(0xFFEADCF9), fontSize = 11.sp)
                        }
                        Surface(onClick = { onNavigate("settings") },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.size(42.dp).semantics { contentDescription = "Abrir configurações" }) {
                            Box(contentAlignment = Alignment.Center) {
                                SettingsGearIcon()
                            }
                        }
                    }
                    Spacer(Modifier.height(25.dp))
                    Text("SALÁRIO LÍQUIDO ESTIMADO / MÊS", color = Color(0xFFEDDBFB), fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(Money.format(income), color = Color.White,
                            fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f), maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                        Surface(onClick = { onNavigate("salary") }, color = Color.White.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(13.dp)) {
                            Text("Editar ✎", modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp).offset(y = (-23).dp)) {
                Surface(shape = RoundedCornerShape(25.dp), color = Color.White, shadowElevation = 7.dp,
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Disponível para você", fontSize = 12.sp, color = Muted)
                                Text(Money.format(available),
                                    color = if (available >= 0) Positive else Negative,
                                    fontSize = 29.sp, fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Surface(color = if (available >= 0) Color(0xFFE1F6EC) else Color(0xFFFBE9ED),
                                shape = RoundedCornerShape(12.dp)) {
                                Text(if (available >= 0) "✓ No limite" else "! Atenção",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    color = if (available >= 0) Positive else Negative,
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        LinearProgressIndicator(progress = { spentPercent },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = if (expenses > income) Negative else Purple,
                            trackColor = Lilac)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gastos ${Money.format(expenses)}", fontSize = 11.sp, color = Muted)
                            Text("Reserva ${Money.format(budget.reserveCents)}", fontSize = 11.sp, color = Muted)
                        }
                        if (income == 0L) Hint("Toque em Editar para começar o seu orçamento.")
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 1.dp, bottom = 11.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Organize seus gastos", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Ink)
                    Text("Toque em um cartão para editar", color = Muted, fontSize = 12.sp)
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    CategoryTile("⌂", "Moradia", "Aluguel e IPTU", BudgetMath.housing(budget), "housing", onNavigate, Modifier.weight(1f))
                    CategoryTile("✦", "Alimentação", "Mercado e refeições", BudgetMath.foodPaidFromCash(budget), "food", onNavigate, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    CategoryTile("ϟ", "Contas", "Água, luz e internet", BudgetMath.bills(budget), "bills", onNavigate, Modifier.weight(1f))
                    CategoryTile("↗", "Transporte", "Passagens e VT", BudgetMath.transportBudgetCost(budget), "transport", onNavigate, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    CategoryTile("＋", "Outros gastos", "Saúde, lazer e mais", BudgetMath.extras(budget), "extras", onNavigate, Modifier.weight(1f))
                    CategoryTile("◎", "Reserva", "Guardar por mês", budget.reserveCents, "reserve", onNavigate, Modifier.weight(1f))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.Center) {
                Text("●  Dados salvos apenas no seu aparelho", fontSize = 11.sp, color = Muted)
            }
        }
    }


}

@Composable
private fun CategoryTile(icon: String, title: String, subtitle: String, total: Long,
                         route: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = { onNavigate(route) }, modifier = modifier.height(139.dp),
        shape = RoundedCornerShape(22.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(Modifier.fillMaxSize().padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(35.dp).clip(RoundedCornerShape(11.dp))
                    .background(Lilac), contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 21.sp, color = Purple, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Text("↗", color = Color(0xFF9676AD), fontSize = 17.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = Muted, fontSize = 10.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text(Money.format(total), color = Purple, fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}


@Composable
private fun SettingsGearIcon() {
    androidx.compose.foundation.Canvas(Modifier.size(23.dp)) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val outer = size.minDimension * .39f
        val inner = size.minDimension * .23f
        for (i in 0 until 8) {
            val a = Math.PI * i / 4.0
            val start = androidx.compose.ui.geometry.Offset(
                center.x + kotlin.math.cos(a).toFloat() * outer * .80f,
                center.y + kotlin.math.sin(a).toFloat() * outer * .80f
            )
            val finish = androidx.compose.ui.geometry.Offset(
                center.x + kotlin.math.cos(a).toFloat() * outer * 1.12f,
                center.y + kotlin.math.sin(a).toFloat() * outer * 1.12f
            )
            drawLine(Color.White, start, finish, strokeWidth = 3.2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
        drawCircle(Color.White, outer)
        drawCircle(Color(0xFF713AA0), inner)
    }
}
