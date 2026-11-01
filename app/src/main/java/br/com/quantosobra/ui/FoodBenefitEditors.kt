package br.com.quantosobra.ui

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
import java.math.BigDecimal

/** Mesmo ajuste no assistente e em Meu salário. Não cria desconto por padrão. */
@Composable
fun FoodBenefitDeductionEditor(s: SalaryConfig, change: (SalaryConfig) -> Unit,
                               compact: Boolean = false) {
    val method = FoodBenefitMath.method(s)
    val entry = FoodBenefitMath.entry(s)
    val calculated = FoodBenefitMath.deductionCents(s)
    var rateText by remember(method) {
        mutableStateOf(if (method == FoodDiscountMethod.PERCENT)
            BigDecimal.valueOf((entry?.percentBasisPoints ?: 0).toLong(), 2)
                .stripTrailingZeros().toPlainString().replace('.', ',') else "")
    }
    Text("Desconto de VA/VR na folha", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    Hint("A empresa pode não descontar nada. No PAT a referência é 20% do custo direto do benefício, não 20% do salário.")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = method == FoodDiscountMethod.UNKNOWN,
            onClick = { change(FoodBenefitMath.change(s, FoodDiscountMethod.UNKNOWN)) },
            label = { Text("Não sei", fontSize = 12.sp) })
        FilterChip(selected = method == FoodDiscountMethod.NONE,
            onClick = { change(FoodBenefitMath.change(s, FoodDiscountMethod.NONE)) },
            label = { Text("Sem desconto", fontSize = 12.sp) })
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = method == FoodDiscountMethod.PERCENT,
            onClick = {
                change(FoodBenefitMath.change(s, FoodDiscountMethod.PERCENT,
                    percentageBP = entry?.percentBasisPoints ?: 0))
            }, label = { Text("Percentual (%)", fontSize = 12.sp) })
        FilterChip(selected = method == FoodDiscountMethod.FIXED,
            onClick = { change(FoodBenefitMath.change(s, FoodDiscountMethod.FIXED,
                fixedCents = calculated)) }, label = { Text("Valor (R$)", fontSize = 12.sp) })
    }
    if (method == FoodDiscountMethod.PERCENT) {
        OutlinedTextField(rateText, onValueChange = { raw ->
            if (raw.length <= 6 && raw.matches(Regex("[0-9]*([,.][0-9]{0,2})?"))) {
                rateText = raw
                Percentage.parseBasisPoints(raw)?.let {
                    change(FoodBenefitMath.change(s, FoodDiscountMethod.PERCENT, percentageBP = it))
                }
            }
        }, modifier = Modifier.fillMaxWidth(), label = { Text("Percentual sobre VA + VR") },
            suffix = { Text("%") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = Percentage.parseBasisPoints(rateText) == null,
            shape = RoundedCornerShape(15.dp))
        if (Percentage.parseBasisPoints(rateText) == null) Hint("Informe um percentual válido, de 0 a 100.")
    }
    if (method == FoodDiscountMethod.FIXED) {
        FieldAmount("Desconto mensal em R$", calculated, { amount ->
            change(FoodBenefitMath.change(s, FoodDiscountMethod.FIXED, fixedCents = amount))
        }, compact = compact)
    }
    when (method) {
        FoodDiscountMethod.UNKNOWN -> Hint("Nenhum desconto foi presumido. Confira se há cobrança no contracheque.")
        FoodDiscountMethod.NONE -> Hint("Você informou que não há participação do trabalhador.")
        else -> {
            ValueLine("Participação calculada / informada", calculated)
            if (FoodBenefitMath.exceedsPatReference(s)) {
                Text("⚠ Acima da referência de 20% sobre o crédito informado. " +
                    "No PAT o limite é apurado sobre o custo direto global, que pode ser diferente do saldo do cartão. " +
                    "Confira o holerite, as regras da empresa e a convenção coletiva.",
                    color = Negative, fontSize = 12.sp)
            } else if (s.totalFoodBenefitCents > 0L) {
                Hint("Referência do PAT: até ${Money.format((s.totalFoodBenefitCents + 2L) / 5L)} " +
                    "se o custo direto corresponder ao crédito. O desconto real pode ser menor ou zero.")
            } else {
                Hint("Informe VA/VR para comparar com a referência do PAT.")
            }
            if (s.mode == SalaryMode.NET) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Já está descontado do líquido?", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Hint(if (s.foodDeductionAlreadyInNet)
                            "Sim: o valor que cai na conta já considera este desconto. Não subtrair novamente."
                            else "Não: o valor informado ainda não considera este desconto. Subtrair uma vez.")
                    }
                    Switch(checked = s.foodDeductionAlreadyInNet,
                        onCheckedChange = { change(s.copy(foodDeductionAlreadyInNet = it)) })
                }
            } else Hint("No cálculo a partir do salário bruto, esse valor é abatido uma única vez na folha.")
        }
    }
}

/** Permite ajustar por categoria o uso esperado dos cartões. Não prevê aceitação universal. */
@Composable
fun FoodBenefitUsageEditor(b: Budget, change: (Budget) -> Unit) {
    val s = b.salary
    val covered = FoodBenefitMath.coverage(b)
    Text("Onde posso usar VA e VR?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    Hint("Marque apenas onde seus cartões são aceitos. O cálculo não gasta o mesmo benefício duas vezes.")
    if (s.foodBenefitCents > 0L) {
        BenefitUseOption("VA no supermercado", s.vaForMarket) {
            change(b.copy(salary = s.copy(vaForMarket = it)))
        }
        BenefitUseOption("VA em marmitas / restaurantes", s.vaForMeals) {
            change(b.copy(salary = s.copy(vaForMeals = it)))
        }
    }
    if (s.mealVoucherCents > 0L) {
        BenefitUseOption("VR em marmitas / restaurantes", s.vrForMeals) {
            change(b.copy(salary = s.copy(vrForMeals = it)))
        }
        BenefitUseOption("VR no supermercado", s.vrForMarket) {
            change(b.copy(salary = s.copy(vrForMarket = it)))
        }
    }
    ValueLine("VA previsto na alimentação", covered.vaMarketCents + covered.vaMealsCents)
    ValueLine("VR previsto na alimentação", covered.vrMarketCents + covered.vrMealsCents)
    ValueLine("Cobertura total estimada", covered.totalUsedCents, emphasize = true)
    Hint("Crédito não usado não entra como dinheiro. Confira saldos e estabelecimentos; o app não acompanha compras reais.")
}

@Composable
private fun BenefitUseOption(title: String, checked: Boolean, action: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontSize = 13.sp)
        Switch(checked = checked, onCheckedChange = action)
    }
}
