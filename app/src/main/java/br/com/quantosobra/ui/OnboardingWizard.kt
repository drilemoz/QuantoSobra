package br.com.quantosobra.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.*

/**
 * Primeiro acesso: uma única pergunta curta por página e rodapé sempre acessível.
 * Não há lista rolável, nem tela inteira de formulário.
 * Somente "Concluir" persiste o rascunho: "Definir depois" mantém o valor em aberto.
 */
@Composable
fun OnboardingWizard(initial: Budget, finish: (Budget) -> Unit) {
    var budget by remember { mutableStateOf(initial) }
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 8
    val compact = LocalConfiguration.current.screenHeightDp < 700
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val spacing = if (compact) 7.dp else 12.dp
    val titles = listOf("Seu trabalho", "Sua renda", "Seu caminho", "Custo do transporte",
        "Benefício da empresa", "Sua moradia", "Sua alimentação", "Seu orçamento")
    val descriptions = listOf("Como você recebe seu dinheiro?", "Informe o valor para começar.",
        "Como você costuma ir ao trabalho?", "Vamos calcular o custo mensal.",
        "Quem paga as passagens?", "Quanto custa seu aluguel?",
        "Pode ser só uma estimativa inicial.", "Você pode ajustar tudo depois.")
    val update: (Budget) -> Unit = { budget = it }
    val transport = budget.transport
    val salary = budget.salary
    val commute = transport.commuteMode

    Column(
        Modifier.fillMaxSize().background(Background).navigationBarsPadding().imePadding()
    ) {
        if (!imeVisible) Box(
            Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF7C2CE0), DeepPurple)))
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp,
                vertical = if (compact) 14.dp else 22.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✦  Quanto Sobra?", color = Color.White,
                        fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
                        modifier = Modifier.weight(1f))
                    Text("${step + 1}/$totalSteps", color = Color.White, fontSize = 13.sp)
                }
                LinearProgressIndicator(
                    progress = { (step + 1) / totalSteps.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(5.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 19.dp, vertical = spacing),
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(titles[step], color = Ink, fontWeight = FontWeight.ExtraBold,
                fontSize = if (compact) 23.sp else 28.sp)
            Text(descriptions[step], color = Muted,
                fontSize = if (compact) 12.sp else 14.sp, maxLines = 2)
        }
        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 19.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)) {
            when (step) {
                0 -> {
                    WizardChoice("Carteira assinada", "CLT", budget.workType == WorkType.CLT, compact) {
                        update(budget.copy(workType = WorkType.CLT, salary = salary.copy(mode = SalaryMode.GROSS)))
                    }
                    WizardChoice("MEI / Pessoa jurídica", "Trabalho por conta ou empresa", budget.workType == WorkType.MEI_PJ, compact) {
                        update(budget.copy(workType = WorkType.MEI_PJ, salary = salary.copy(mode = SalaryMode.NET)))
                    }
                    WizardChoice("Autônomo / Informal", "Renda variável", budget.workType == WorkType.SELF_EMPLOYED, compact) {
                        update(budget.copy(workType = WorkType.SELF_EMPLOYED, salary = salary.copy(mode = SalaryMode.NET)))
                    }
                    WizardChoice("Servidor público", "Regras da folha variam", budget.workType == WorkType.PUBLIC_SERVANT, compact) {
                        update(budget.copy(workType = WorkType.PUBLIC_SERVANT, salary = salary.copy(mode = SalaryMode.NET)))
                    }
                    WizardChoice("Outra renda", "Sem desconto automático", budget.workType == WorkType.OTHER, compact) {
                        update(budget.copy(workType = WorkType.OTHER, salary = salary.copy(mode = SalaryMode.NET)))
                    }
                }
                1 -> {
                    val clt = budget.workType == WorkType.CLT
                    if (clt) {
                        FieldAmount("Salário bruto por mês", salary.grossCents,
                            { update(budget.copy(salary = salary.copy(mode = SalaryMode.GROSS, grossCents = it))) })
                        val net = BudgetMath.salaryNet(budget)
                        if (!imeVisible) WizardSummary("Líquido estimado, antes do transporte", Money.format(net))
                        Text("INSS e IRRF: tabelas CLT 2026. Outras deduções podem ser incluídas depois.",
                            color = Muted, fontSize = 12.sp)
                    } else {
                        FieldAmount("Renda mensal informada", budget.netSalaryCents,
                            { update(budget.copy(salary = salary.copy(mode = SalaryMode.NET), netSalaryCents = it)) })
                        Text("Ainda sem cálculo de tributos. Para MEI, PJ, autônomos e servidores, ajuste o valor após descontos conhecidos.",
                            color = Muted, fontSize = 12.sp)
                    }
                }
                2 -> {
                    WizardChoice("Ônibus ou transporte público", "Tarifa por viagem", commute == CommuteMode.PUBLIC, compact) {
                        update(budget.copy(transport = transport.copy(commuteMode = CommuteMode.PUBLIC)))
                    }
                    WizardChoice("Carro ou moto", "Custo mensal próprio", commute == CommuteMode.PRIVATE, compact) {
                        update(budget.copy(transport = transport.copy(commuteMode = CommuteMode.PRIVATE,
                            mode = TransportMode.NONE, deductedFromNetSalary = false)))
                    }
                    WizardChoice("A pé ou bicicleta", "Sem tarifa por viagem", commute == CommuteMode.BIKE_WALK, compact) {
                        update(budget.copy(transport = transport.copy(commuteMode = CommuteMode.BIKE_WALK,
                            mode = TransportMode.NONE, deductedFromNetSalary = false)))
                    }
                    WizardChoice("Trabalho em casa", "Sem deslocamento", commute == CommuteMode.HOME, compact) {
                        update(budget.copy(transport = transport.copy(commuteMode = CommuteMode.HOME,
                            mode = TransportMode.NONE, deductedFromNetSalary = false)))
                    }
                }
                3 -> when (commute) {
                    CommuteMode.PUBLIC -> {
                        FieldAmount("Valor de cada passagem", transport.fareCents,
                            { update(budget.copy(transport = transport.copy(fareCents = it))) })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) {
                                FieldCount("Viagens por dia", transport.ridesPerDay, 0..20,
                                    { update(budget.copy(transport = transport.copy(ridesPerDay = it))) })
                            }
                            Box(Modifier.weight(1f)) {
                                FieldCount("Dias por mês", transport.daysPerMonth, 0..31,
                                    { update(budget.copy(transport = transport.copy(daysPerMonth = it))) })
                            }
                        }
                        if (!imeVisible) WizardSummary("Total mensal das passagens", Money.format(BudgetMath.transportGross(transport)))
                    }
                    CommuteMode.PRIVATE -> {
                        FieldAmount("Combustível e custos por mês", transport.privateMonthlyCents,
                            { update(budget.copy(transport = transport.copy(privateMonthlyCents = it))) })
                        Text("Considere também estacionamento e manutenção. Informe uma estimativa mensal.",
                            color = Muted, fontSize = 12.sp)
                    }
                    else -> Text("Nenhuma passagem será incluída por enquanto. Você poderá cadastrar outros custos de deslocamento mais tarde.",
                        color = Muted, fontSize = 14.sp)
                }
                4 -> {
                    if (commute == CommuteMode.PUBLIC && budget.workType == WorkType.CLT) {
                        WizardChoice("Vale-transporte CLT", "Até 6% do salário básico", transport.mode == TransportMode.CLT, compact) {
                            update(budget.copy(transport = transport.copy(mode = TransportMode.CLT,
                                deductedFromNetSalary = false, basicSalaryCents = 0)))
                        }
                        WizardChoice("Empresa paga tudo", "Sem desconto", transport.mode == TransportMode.FULL, compact) {
                            update(budget.copy(transport = transport.copy(mode = TransportMode.FULL,
                                deductedFromNetSalary = false)))
                        }
                        WizardChoice("Eu pago tudo", "Sem benefício", transport.mode == TransportMode.NONE, compact) {
                            update(budget.copy(transport = transport.copy(mode = TransportMode.NONE,
                                deductedFromNetSalary = false)))
                        }
                        WizardChoice("Empresa paga uma parte", "Definir percentual depois", transport.mode == TransportMode.CUSTOM, compact) {
                            update(budget.copy(transport = transport.copy(mode = TransportMode.CUSTOM,
                                deductedFromNetSalary = false, customOnPayroll = false)))
                        }
                        if (transport.mode == TransportMode.CUSTOM) {
                            FieldCount("Parte paga pela empresa (%)", transport.companyPercent, 0..100,
                                { update(budget.copy(transport = transport.copy(companyPercent = it))) })
                        }
                        if (!imeVisible) WizardSummary("Parte da empresa", Money.format(
                            BudgetMath.transportGross(transport) - BudgetMath.transportEmployeeCost(
                                transport, salary.grossCents)))
                    } else {
                        Text("Sem desconto CLT automático para este transporte ou vínculo. O custo informado ficará no orçamento, podendo ser editado depois.",
                            color = Muted, fontSize = 14.sp)
                    }
                }
                5 -> {
                    FieldAmount(if (budget.rentPeriod == RentPeriod.YEARLY) "Aluguel anual" else "Aluguel mensal",
                        budget.rentCents, { update(budget.copy(rentCents = it)) })
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(selected = budget.rentPeriod == RentPeriod.MONTHLY,
                            onClick = { update(budget.copy(rentPeriod = RentPeriod.MONTHLY)) },
                            label = { Text("Mensal") })
                        FilterChip(selected = budget.rentPeriod == RentPeriod.YEARLY,
                            onClick = { update(budget.copy(rentPeriod = RentPeriod.YEARLY)) },
                            label = { Text("Anual") })
                    }
                    if (!imeVisible) WizardSummary("Equivalente mensal", Money.format(BudgetMath.monthlyRent(budget)))
                    Text("Condomínio, IPTU, água e luz podem ser informados depois, nas categorias da tela principal.",
                        color = Muted, fontSize = 12.sp)
                }
                6 -> {
                    FieldAmount("Supermercado por mês (estimado)", budget.foodEstimateCents,
                        { update(budget.copy(foodEstimateCents = it)) })
                    Text("Depois você pode substituir essa estimativa por uma lista de produtos com preços reais.",
                        color = Muted, fontSize = 12.sp)
                    if (!imeVisible) WizardSummary("Alimentação no orçamento", Money.format(BudgetMath.food(budget)))
                }
                7 -> {
                    if (!imeVisible) WizardSummary("Dinheiro após descontos estimados", Money.format(BudgetMath.salaryNet(budget)))
                    if (!imeVisible) WizardSummary("Moradia por mês", Money.format(BudgetMath.housing(budget)))
                    if (!imeVisible) WizardSummary("Transporte do seu bolso", Money.format(BudgetMath.transportEmployeeCost(
                        transport, if (budget.salary.mode == SalaryMode.GROSS) salary.grossCents else 0L)))
                    if (!imeVisible) WizardSummary("Alimentação", Money.format(BudgetMath.foodPaidFromCash(budget)))
                    val balance = BudgetMath.availableAfterReserve(budget)
                    if (!imeVisible) WizardSummary("Quanto sobra no mês", Money.format(balance), highlight = true)
                    Text("Resultado parcial: contas, benefícios e gastos não informados ainda precisam ser configurados.",
                        color = Muted, fontSize = 11.sp, maxLines = 2)
                }
            }
        }
        if (!imeVisible) Column(Modifier.fillMaxWidth().padding(horizontal = 19.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f).height(48.dp),
                    onClick = { if (step > 0) step-- }, enabled = step > 0,
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Voltar") }
                Button(
                    modifier = Modifier.weight(1.35f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { if (step == totalSteps - 1) finish(budget) else step++ },
                    enabled = when (step) {
                        0 -> budget.workType != WorkType.UNKNOWN
                        1 -> if (budget.workType == WorkType.CLT) salary.grossCents > 0L
                            else budget.netSalaryCents > 0L
                        2 -> commute != CommuteMode.UNSET
                        else -> true
                    }
                ) { Text(if (step == totalSteps - 1) "Concluir" else "Continuar") }
            }
            if (step < totalSteps - 1) {
                TextButton(onClick = {
                    if (step == 2) budget = budget.copy(transport = budget.transport.copy(
                        commuteMode = CommuteMode.UNSET, mode = TransportMode.NONE))
                    step++
                }, modifier = Modifier.fillMaxWidth().height(36.dp)) {
                    Text("Definir depois", fontSize = 12.sp, color = Muted)
                }
            } else {
                Text("Dados armazenados somente no aparelho ao concluir.",
                    modifier = Modifier.fillMaxWidth(), color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun WizardChoice(title: String, detail: String, selected: Boolean, compact: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick, modifier = Modifier.fillMaxWidth().height(if (compact) 49.dp else 59.dp),
        shape = RoundedCornerShape(17.dp),
        color = if (selected) Lilac else Color.White,
        border = BorderStroke(1.dp, if (selected) Purple else Color(0xFFE8DFED))
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontWeight = FontWeight.SemiBold,
                    fontSize = if (compact) 12.sp else 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(detail, color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            RadioButton(selected = selected, onClick = onClick, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun WizardSummary(label: String, value: String, highlight: Boolean = false) {
    Surface(color = if (highlight) DeepPurple else Color.White, shape = RoundedCornerShape(17.dp),
        modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = if (highlight) Color.White else Muted,
                fontSize = 12.sp, modifier = Modifier.weight(1f), maxLines = 2)
            Spacer(Modifier.width(8.dp))
            Text(value, color = if (highlight) Color.White else Purple,
                fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
        }
    }
}
