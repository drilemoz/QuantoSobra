package br.com.quantosobra.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.*

@Composable
fun FoodScreen(b: Budget, change: (Budget) -> Unit, back: () -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FoodProduct?>(null) }
    Page("Alimentação", "O custo real do seu supermercado", back) {
        SectionTotal("Pago com dinheiro no mês", BudgetMath.foodPaidFromCash(b))
        if (b.salary.foodBenefitCents > 0L) {
            SurfaceCard {
                ValueLine("Custo de alimentação", BudgetMath.food(b))
                ValueLine("VA/VR usado (até o custo)", minOf(b.salary.foodBenefitCents, BudgetMath.food(b)))
                Hint("O crédito VA/VR é configurado em Meu salário. Confira quais estabelecimentos e compras aceitam o benefício.")
            }
        }
        SurfaceCard {
            Text("Refeições fora de casa", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            FieldAmount("Restaurantes / lanches por mês", b.foodOutsideCents,
                { change(b.copy(foodOutsideCents = it)) })
        }
        SurfaceCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Lista de compras", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("${b.foodProducts.size} itens", color = Muted, fontSize = 12.sp)
            }
            Hint("Informe o preço por unidade e quanto consome no mês. Ex.: arroz R$ 8/kg × 5 kg.")
            if (b.foodProducts.isEmpty()) {
                Text("Nenhum produto cadastrado ainda.", color = Muted, fontSize = 13.sp)
            } else {
                b.foodProducts.forEach { p ->
                    HorizontalDivider(color = Lilac)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).clickable { editing = p }) {
                            Text(p.name, fontWeight = FontWeight.SemiBold)
                            Text("${Money.format(p.unitPriceCents)} × ${Money.quantityInput(p.quantityHundredths)}",
                                color = Muted, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(Money.format(BudgetMath.productCost(p)), fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { change(b.copy(foodProducts = b.foodProducts.filterNot { it.id == p.id })) }) {
                                Text("Excluir", fontSize = 12.sp, color = Negative)
                            }
                        }
                    }
                }
            }
            AppButton("+ Adicionar produto") { showAdd = true }
            HorizontalDivider(color = Lilac)
            ValueLine("Subtotal supermercado", b.foodProducts.sumOf(BudgetMath::productCost), emphasize = true)
        }
        Hint("Uma lista de planejamento mensal: não há compras automáticas nem acesso à internet.")
    }

    if (showAdd || editing != null) {
        ProductDialog(
            initial = editing,
            dismiss = { showAdd = false; editing = null },
            save = { name, price, qty ->
                val current = editing
                val items = if (current == null) {
                    b.foodProducts + FoodProduct((b.foodProducts.maxOfOrNull { it.id } ?: 0) + 1, name, price, qty)
                } else {
                    b.foodProducts.map { if (it.id == current.id) current.copy(name = name, unitPriceCents = price,
                        quantityHundredths = qty) else it }
                }
                change(b.copy(foodProducts = items))
                showAdd = false; editing = null
            }
        )
    }
}

@Composable
private fun ProductDialog(initial: FoodProduct?, dismiss: () -> Unit,
                          save: (String, Long, Int) -> Unit) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var price by remember(initial?.id) {
        mutableStateOf(if ((initial?.unitPriceCents ?: 0L) == 0L) "" else Money.input(initial!!.unitPriceCents))
    }
    var qty by remember(initial?.id) {
        mutableStateOf(Money.quantityInput(initial?.quantityHundredths ?: 100))
    }
    val cents = Money.parse(price)
    val quantity = Money.quantityHundredths(qty)
    ModernDialog(
        title = if (initial == null) "Novo produto" else "Editar produto",
        onDismiss = dismiss,
        confirmText = "Salvar",
        confirmEnabled = name.isNotBlank() && cents != null && quantity != null,
        onConfirm = { if (cents != null && quantity != null) save(name.trim(), cents, quantity) }
    ) {
        OutlinedTextField(name, { if (it.length <= 80) name = it },
            modifier = Modifier.fillMaxWidth(), label = { Text("Nome do produto") },
            singleLine = true, shape = RoundedCornerShape(15.dp))
        OutlinedTextField(price, { if (it.length <= 19) price = it },
            modifier = Modifier.fillMaxWidth(), label = { Text("Preço por unidade") },
            prefix = { Text("R$ ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
            shape = RoundedCornerShape(15.dp), isError = cents == null)
        OutlinedTextField(qty, { if (it.length <= 10) qty = it },
            modifier = Modifier.fillMaxWidth(), label = { Text("Quantidade por mês") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
            shape = RoundedCornerShape(15.dp), isError = quantity == null)
        if (cents != null && quantity != null) {
            Surface(color = Lilac, shape = RoundedCornerShape(14.dp)) {
                Text("Subtotal  ${Money.format((cents * quantity + 50L) / 100L)}",
                    modifier = Modifier.padding(14.dp), color = Purple, fontWeight = FontWeight.Bold)
            }
        }
    }
}
