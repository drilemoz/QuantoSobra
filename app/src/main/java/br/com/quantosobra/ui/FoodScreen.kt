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
    var showEssentials by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FoodProduct?>(null) }
    Page("Alimentação", "Planeje e ajuste suas compras", back) {
        SectionTotal("Pago com dinheiro no mês", BudgetMath.foodPaidFromCash(b))
        if (b.salary.foodBenefitCents > 0L) {
            SurfaceCard {
                ValueLine("Custo de alimentação", BudgetMath.food(b))
                ValueLine("VA/VR usado (até o custo)", minOf(b.salary.foodBenefitCents, BudgetMath.food(b)))
                Hint("O crédito VA/VR é configurado em Meu salário. Confira quais estabelecimentos e compras aceitam o benefício.")
            }
        }
        SurfaceCard {
            FieldAmount("Estimativa mensal do supermercado", b.foodEstimateCents,
                { change(b.copy(foodEstimateCents = it)) },
                "Enquanto os produtos não têm todos os preços, a estimativa evita subestimar o gasto. Quando estiverem completos, será substituída pelo total real.")
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
            Hint("Preço por embalagem × quantidade mensal. Preços de referência são editáveis e não são ofertas em tempo real.")
            OutlinedButton(onClick = { showEssentials = true }, shape = RoundedCornerShape(16.dp)) {
                Text("＋ Carregar produtos básicos", fontSize = 12.sp)
            }
            val pending = b.foodProducts.count { it.unitPriceCents <= 0L }
            if (pending > 0) Hint("$pending produto(s) sem preço. O orçamento usa a maior referência entre a estimativa manual e o subtotal conhecido.")
            if (b.foodProducts.isEmpty()) {
                Text("Nenhum produto cadastrado ainda.", color = Muted, fontSize = 13.sp)
            } else {
                b.foodProducts.forEach { p ->
                    HorizontalDivider(color = Lilac)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.SemiBold)
                            Text("${Money.quantityInput(p.quantityHundredths)} × ${p.unit}",
                                color = Muted, fontSize = 12.sp)
                            Text(if (p.unitPriceCents > 0L) "${Money.format(p.unitPriceCents)} por ${p.unit}"
                                else "Preço não informado", color = Muted, fontSize = 12.sp)
                            if (p.priceOrigin == "REFERENCIA_ATACADAO")
                                Text("Referência indicativa Atacadão • 01/11/2026", color = Purple, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (p.unitPriceCents > 0L) Money.format(BudgetMath.productCost(p))
                                else "Pendente", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            TextButton(onClick = { editing = p }, contentPadding = PaddingValues(0.dp)) {
                                Text("Editar", fontSize = 12.sp)
                            }
                            TextButton(onClick = { change(b.copy(foodProducts = b.foodProducts.filterNot { it.id == p.id })) },
                                contentPadding = PaddingValues(0.dp)) { Text("Excluir", color = Negative, fontSize = 12.sp) }
                        }
                    }
                }
            }
            AppButton("+ Adicionar produto") { showAdd = true }
            HorizontalDivider(color = Lilac)
            ValueLine("Subtotal com preços informados", b.foodProducts.sumOf(BudgetMath::productCost))
            if (b.foodProducts.isEmpty() || b.foodProducts.any { it.unitPriceCents <= 0L })
                ValueLine("Estimativa considerada no orçamento", BudgetMath.foodMarket(b), emphasize = true)
            else ValueLine("Total dos produtos no orçamento", BudgetMath.foodMarket(b), emphasize = true)
        }
        Hint("Uma lista de planejamento mensal: não há compras automáticas nem acesso à internet.")
    }

    if (showEssentials) EssentialsPicker(
        current = b.foodProducts,
        dismiss = { showEssentials = false },
        apply = { selected ->
            val newItems = EssentialProducts.merge(b.foodProducts, selected)
            change(b.copy(foodProducts = newItems))
            showEssentials = false
        }
    )

    if (showAdd || editing != null) {
        ProductDialog(
            initial = editing,
            dismiss = { showAdd = false; editing = null },
            save = { name, price, qty, unit ->
                val current = editing
                val items = if (current == null) {
                    b.foodProducts + FoodProduct((b.foodProducts.maxOfOrNull { it.id } ?: 0) + 1, name, price, qty, unit)
                } else {
                    b.foodProducts.map { if (it.id == current.id) current.copy(name = name, unitPriceCents = price,
                        quantityHundredths = qty, unit = unit, priceOrigin = "MANUAL") else it }
                }
                change(b.copy(foodProducts = items))
                showAdd = false; editing = null
            }
        )
    }
}

@Composable
fun ProductDialog(initial: FoodProduct?, dismiss: () -> Unit,
                          save: (String, Long, Int, String) -> Unit) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var price by remember(initial?.id) {
        mutableStateOf(if ((initial?.unitPriceCents ?: 0L) == 0L) "" else Money.input(initial!!.unitPriceCents))
    }
    var qty by remember(initial?.id) {
        mutableStateOf(Money.quantityInput(initial?.quantityHundredths ?: 100))
    }
    var unit by remember(initial?.id) { mutableStateOf(initial?.unit ?: "un") }
    var unitDropdown by remember { mutableStateOf(false) }
    val cents = Money.parse(price)
    val quantity = Money.quantityHundredths(qty)
    ModernDialog(
        title = if (initial == null) "Novo produto" else "Editar produto",
        onDismiss = dismiss,
        confirmText = "Salvar",
        confirmEnabled = name.isNotBlank() && unit.isNotBlank() && cents != null && quantity != null,
        onConfirm = { if (cents != null && quantity != null) save(name.trim(), cents, quantity, unit) }
    ) {
        OutlinedTextField(name, { if (it.length <= 80) name = it },
            modifier = Modifier.fillMaxWidth(), label = { Text("Nome do produto") },
            singleLine = true, shape = RoundedCornerShape(15.dp))
        CurrencyTextEntry("Preço por unidade", price, { price = it })
        Box {
            OutlinedButton(onClick = { unitDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Unidade: $unit  ▾")
            }
            DropdownMenu(expanded = unitDropdown, onDismissRequest = { unitDropdown = false }) {
                listOf("un", "kg", "g", "L", "pacote", "garrafa", "dúzia", "pacote 5 kg", "pacote 1 kg", "pacote 500 g", "garrafa 900 ml", "cartela 30 un", "caixa 1 L").forEach { item ->
                    DropdownMenuItem(text = { Text(item) }, onClick = { unit = item; unitDropdown = false })
                }
            }
        }
        OutlinedTextField(unit, { if (it.length <= 32) unit = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Embalagem / unidade (editável)") }, singleLine = true,
            shape = RoundedCornerShape(15.dp))
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
