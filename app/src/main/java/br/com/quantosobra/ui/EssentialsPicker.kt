package br.com.quantosobra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.*
import java.util.Locale

private data class BasicDraft(
    val name: String, val unit: String, val selected: Boolean,
    val qty: String, val price: String
)

/** Mesmo editor para o wizard e o painel de alimentação; não sobrescreve dados existentes. */
@Composable
fun EssentialsPicker(current: List<FoodProduct>, dismiss: () -> Unit,
                     apply: (List<FoodProduct>) -> Unit) {
    val existing = current.map { it.name.trim().lowercase(Locale.ROOT) }.toSet()
    val available = EssentialProducts.basics.filter { it.name.lowercase(Locale.ROOT) !in existing }
    var drafts by remember(existing) {
        mutableStateOf(available.map { BasicDraft(it.name, it.unit, true,
            it.quantity.toString(), Money.input(it.priceCents)) })
    }
    val valid = drafts.filter { it.selected }.all {
        Money.quantityHundredths(it.qty) != null && Money.parse(it.price) != null && it.unit.isNotBlank()
    }
    val chosen = drafts.filter { it.selected }
    val subtotal = chosen.sumOf { draft ->
        val price = Money.parse(draft.price) ?: 0L
        val qty = Money.quantityHundredths(draft.qty) ?: 0
        BudgetMath.productCost(FoodProduct(0L, draft.name, price, qty, draft.unit))
    }
    ModernDialog("Carregar produtos básicos", dismiss, "Adicionar ${chosen.size} produtos", {
        apply(chosen.mapNotNull { draft ->
            val quantity = Money.quantityHundredths(draft.qty) ?: return@mapNotNull null
            val price = Money.parse(draft.price) ?: return@mapNotNull null
            FoodProduct(0L, draft.name, price, quantity, draft.unit,
                if (price > 0L) "REFERENCIA_ATACADAO" else "MANUAL")
        })
    }, confirmEnabled = chosen.isNotEmpty() && valid) {
        Text("Preços indicativos do catálogo Atacadão (${EssentialProducts.referenceDate}). " +
            "Não são uma consulta ao vivo nem oferta garantida na sua loja. Edite preço e quantidade antes de carregar.",
            color = Muted, fontSize = 12.sp)
        if (existing.isNotEmpty()) Text("Itens já cadastrados não serão duplicados nem substituídos. " +
            "Edite-os na lista de compras.", color = Muted, fontSize = 12.sp)
        if (available.isEmpty()) {
            Text("Os oito produtos básicos já estão na sua lista. Use Editar para personalizá-los.", color = Muted)
        } else {
            TextButton(onClick = {
                val target = !drafts.all { it.selected }
                drafts = drafts.map { it.copy(selected = target) }
            }) { Text(if (drafts.all { it.selected }) "Desmarcar todos" else "Selecionar todos") }
            drafts.forEachIndexed { index, p ->
                HorizontalDivider(color = Lilac)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = p.selected, onCheckedChange = { checked ->
                        drafts = drafts.toMutableList().also { it[index] = p.copy(selected = checked) }
                    })
                    Column(Modifier.weight(1f)) {
                        Text(p.name, fontWeight = FontWeight.SemiBold)
                        Text(p.unit, fontSize = 11.sp, color = Muted)
                    }
                }
                if (p.selected) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = p.qty, onValueChange = { value ->
                        if (value.length <= 8) drafts = drafts.toMutableList().also {
                            it[index] = p.copy(qty = value)
                        }
                    }, label = { Text("Quantidade") }, isError = Money.quantityHundredths(p.qty) == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = p.price, onValueChange = { value ->
                        if (value.length <= 15) drafts = drafts.toMutableList().also {
                            it[index] = p.copy(price = value)
                        }
                    }, label = { Text("R$ / embalagem") }, isError = Money.parse(p.price) == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, modifier = Modifier.weight(1.3f), shape = RoundedCornerShape(12.dp))
                }
            }
            HorizontalDivider(color = Lilac)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal selecionado", fontWeight = FontWeight.Medium)
                Text(Money.format(subtotal), color = Purple, fontWeight = FontWeight.Bold)
            }
            Text("Óleo: quatro garrafas de 900 ml = 3,6 litros. Arroz: três pacotes de 5 kg. " +
                "Farinha considerada: trigo.", fontSize = 11.sp, color = Muted)
        }
    }
}
