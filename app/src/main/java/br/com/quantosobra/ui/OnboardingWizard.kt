package br.com.quantosobra.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Configuração guiada sem rolagem: cada etapa ocupa a área útil e o usuário pode pular
 * informações não obrigatórias. O rascunho só é gravado ao concluir.
 */
@Composable
fun OnboardingWizard(initial: Budget, finish: (Budget) -> Unit) {
    var budget by remember { mutableStateOf(initial) }
    var step by remember { mutableIntStateOf(0) }
    val titles = listOf("Seu jeito de trabalhar", "Quanto você ganha?", "Benefícios e descontos",
        "Seu caminho", "Sua escala de trabalho", "Passagens e transporte", "Quem paga o transporte?",
        "Seu lugar no mundo", "Contas da casa", "Comida na mesa", "Seu novo panorama")
    val subtitles = listOf("Cada tipo de renda tem regras próprias.", "Vamos partir de um valor realista.",
        "O que entra e o que sai do contracheque.", "Como você chega ao trabalho?",
        "Um calendário. Sem contar dias na cabeça.", "Quanto custa chegar e voltar?",
        "O valor certo, sem desconto em dobro.", "Aluguel e taxas, tudo no mesmo lugar.",
        "Pequenas contas que fazem diferença.", "Seu mercado sem preços inventados.", "Seu orçamento começa aqui.")
    val count = titles.size
    val compact = LocalConfiguration.current.screenHeightDp < 760
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val spacing = if (compact) 6.dp else 10.dp
    val t = budget.transport
    val s = budget.salary
    val keyboard = LocalSoftwareKeyboardController.current
    val set: (Budget) -> Unit = { budget = it }
    fun deduction(id: Long, label: String): Long = s.deductions.find { it.id == id }?.amountCents ?: 0L
    fun writeDeduction(id: Long, label: String, amount: Long) {
        val original = s.deductions.filterNot { it.id == id }
        val updated = if (amount == 0L) original else original + PayrollDeduction(id, label, amount)
        set(budget.copy(salary = s.copy(deductions = updated)))
    }
    fun updateCalendar(t2: TransportConfig) {
        val month = WorkCalendar.month(t2.workCalendarMonth)
        val anchor = runCatching { LocalDate.parse(t2.workAnchorDate) }.getOrNull() ?: LocalDate.now()
        val dates = WorkCalendar.parseDates(t2.customWorkDates)
        val countDays = WorkCalendar.days(month, t2.workSchedule, t2.workWeekdaysMask, anchor, dates).size
        set(budget.copy(transport = t2.copy(daysPerMonth = if (t2.workSchedule == WorkSchedule.MANUAL)
            t2.daysPerMonth else countDays)))
    }
    Column(Modifier.fillMaxSize().background(Color(0xFFF8F4FC)).navigationBarsPadding().imePadding()) {
        if (!imeVisible) {
            Box(Modifier.fillMaxWidth().height(if (compact) 103.dp else 122.dp)
                .background(Brush.linearGradient(listOf(Color(0xFF962BEB), Color(0xFF521282), Color(0xFF2A0C50))))) {
                Box(Modifier.align(Alignment.TopEnd).offset(x = 40.dp, y = (-64).dp)
                    .size(177.dp).border(30.dp, Color.White.copy(alpha = .045f), CircleShape))
                Column(Modifier.align(Alignment.CenterStart).fillMaxWidth()
                    .padding(horizontal = 21.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(32.dp).clip(RoundedCornerShape(11.dp))
                            .background(Color.White.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
                            Text("✦", color = Color.White, fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(9.dp))
                        Text("Quanto Sobra?", color = Color.White, fontSize = 20.sp,
                            fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Surface(color = Color.White.copy(alpha = .13f), shape = RoundedCornerShape(14.dp)) {
                            Text("${step + 1} / $count", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    LinearProgressIndicator(progress = { (step + 1f) / count },
                        color = Color(0xFFE9CBFF), trackColor = Color.White.copy(alpha = .20f),
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(12.dp)))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp,
            top = if (imeVisible) 5.dp else 15.dp, bottom = if (compact) 9.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (!imeVisible) Text("ETAPA ${step + 1}  •  ${if (step == count - 1) "PRONTO" else "SEU PLANEJAMENTO"}",
                    color = Purple, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Text(titles[step], fontSize = if (imeVisible) 18.sp else if (compact) 23.sp else 28.sp,
                    color = Ink, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!imeVisible) Text(subtitles[step], color = Muted, fontSize = 12.sp, maxLines = 2)
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)) {
            when (step) {
                0 -> {
                    listOf(
                        Triple(WorkType.CLT, "Carteira assinada", "CLT • Descontos estimados"),
                        Triple(WorkType.MEI_PJ, "MEI / Pessoa jurídica", "Renda do negócio"),
                        Triple(WorkType.SELF_EMPLOYED, "Autônomo / Informal", "Rendimento próprio"),
                        Triple(WorkType.PUBLIC_SERVANT, "Servidor público", "Regras de folha específicas"),
                        Triple(WorkType.OTHER, "Outra renda", "Personalizar depois")
                    ).forEachIndexed { index, item ->
                        WizardOption(symbol = listOf("▣", "◈", "✳", "▥", "＋")[index],
                            label = item.second, detail = item.third,
                            selected = budget.workType == item.first, compact = compact) {
                            val clt = item.first == WorkType.CLT
                            set(budget.copy(workType = item.first,
                                salary = s.copy(mode = if (clt) SalaryMode.GROSS else SalaryMode.NET,
                                    autoInss = clt, autoIrrf = clt)))
                        }
                    }
                }
                1 -> {
                    val clt = budget.workType == WorkType.CLT
                    WizardPanel {
                        FieldAmount(if (clt) "Salário bruto mensal" else "Renda mensal disponível",
                            if (clt) s.grossCents else budget.netSalaryCents, {
                                set(if (clt) budget.copy(salary = s.copy(grossCents = it, mode = SalaryMode.GROSS))
                                else budget.copy(netSalaryCents = it, salary = s.copy(mode = SalaryMode.NET)))
                            }, compact = true)
                        if (!imeVisible) {
                            HorizontalDivider(color = Lilac)
                            WizardAmountLine(if (clt) "Líquido estimado" else "Renda informada",
                                BudgetMath.salaryNet(budget), true)
                        }
                    }
                    if (!imeVisible) WizardNote(if (clt) "INSS e IRRF calculados para um vínculo CLT. Você pode ajustar os descontos depois." else
                        "Para vínculos sem CLT, informe o valor disponível após tributos conhecidos. Não aplicamos descontos CLT automaticamente.")
                }
                2 -> {
                    WizardPanel {
                        FieldAmount("Vale-refeição / alimentação recebido", s.foodBenefitCents,
                            { set(budget.copy(salary = s.copy(foodBenefitCents = it))) }, compact = true)
                        if (budget.workType == WorkType.CLT) {
                            FieldAmount("Desconto de plano de saúde", deduction(900001L, ""),
                                { writeDeduction(900001L, "Plano de saúde", it) }, compact = true)
                            FieldAmount("Desconto de VA/VR na folha", deduction(900002L, ""),
                                { writeDeduction(900002L, "Desconto de VA/VR", it) }, compact = true)
                            FieldAmount("Outros descontos no salário", deduction(900003L, ""),
                                { writeDeduction(900003L, "Outros descontos", it) }, compact = true)
                        }
                    }
                    if (!imeVisible) WizardNote(if (budget.workType == WorkType.CLT)
                        "VA/VR recebido reduz o valor pago em comida. O desconto na folha é separado. Não repita o vale-transporte."
                        else "Para este vínculo, os impostos não são estimados como CLT. Ajuste a renda líquida ao editar o salário.")
                }
                3 -> {
                    listOf(
                        Triple(CommuteMode.PUBLIC, "Ônibus ou transporte público", "Passagem × trajetos × dias"),
                        Triple(CommuteMode.PRIVATE, "Carro ou moto", "Combustível e manutenção"),
                        Triple(CommuteMode.BIKE_WALK, "Bicicleta ou a pé", "Sem tarifa diária"),
                        Triple(CommuteMode.HOME, "Trabalho em casa", "Sem trajeto habitual")
                    ).forEachIndexed { index, item ->
                        WizardOption(listOf("▤", "▰", "➚", "⌂")[index], item.second, item.third,
                            t.commuteMode == item.first, compact) {
                            val chosen = t.copy(commuteMode = item.first,
                                mode = if (item.first == CommuteMode.PUBLIC) t.mode else TransportMode.NONE,
                                deductedFromNetSalary = false)
                            if (item.first == CommuteMode.PUBLIC && chosen.workSchedule == WorkSchedule.MANUAL) {
                                updateCalendar(chosen.copy(workSchedule = WorkSchedule.WEEKLY,
                                    workCalendarMonth = java.time.YearMonth.now().toString()))
                            } else set(budget.copy(transport = chosen))
                        }
                    }
                }
                4 -> {
                    WizardCalendar(t, compact, ::updateCalendar)
                }
                5 -> {
                    if (t.commuteMode == CommuteMode.PUBLIC) {
                        WizardPanel {
                            FieldAmount("Preço da passagem", t.fareCents,
                                { set(budget.copy(transport = t.copy(fareCents = it))) }, compact = true)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.weight(1f)) {
                                    FieldCount("Viagens por dia", t.ridesPerDay, 0..20,
                                        { set(budget.copy(transport = t.copy(ridesPerDay = it))) })
                                }
                                Box(Modifier.weight(1f)) {
                                    FieldCount("Dias no mês", t.daysPerMonth, 0..31,
                                        { set(budget.copy(transport = t.copy(daysPerMonth = it,
                                            workSchedule = WorkSchedule.MANUAL))) })
                                }
                            }
                            if (!imeVisible) WizardAmountLine("Passagens no mês", BudgetMath.transportGross(t), true)
                        }
                        if (!imeVisible) WizardNote("Os dias vêm do calendário anterior, mas você pode corrigir manualmente. Passagens extras podem ser incluídas nas viagens diárias.")
                    } else if (t.commuteMode == CommuteMode.PRIVATE) {
                        WizardPanel {
                            FieldAmount("Custo de transporte por mês", t.privateMonthlyCents,
                                { set(budget.copy(transport = t.copy(privateMonthlyCents = it))) }, compact = true)
                        }
                        if (!imeVisible) WizardNote("Inclua combustível, estacionamento e uma reserva para manutenção.")
                    } else {
                        WizardEmpty("✓", "Sem passagens diárias", "O transporte ficou zerado. Se houver manutenção ou outras despesas, você pode cadastrar depois.")
                    }
                }
                6 -> {
                    if (t.commuteMode == CommuteMode.PUBLIC && budget.workType == WorkType.CLT) {
                        listOf(
                            Triple(TransportMode.CLT, "Vale-transporte CLT", "Até 6% do salário básico"),
                            Triple(TransportMode.FULL, "Empresa paga tudo", "Você não paga a passagem"),
                            Triple(TransportMode.CUSTOM, "Empresa paga uma parte", "Informe a porcentagem"),
                            Triple(TransportMode.NONE, "Pago do meu bolso", "Sem ajuda da empresa")
                        ).forEachIndexed { index, item ->
                            WizardOption(listOf("▣", "✓", "%", "−")[index], item.second, item.third,
                                t.mode == item.first, compact) {
                                set(budget.copy(transport = t.copy(mode = item.first,
                                    basicSalaryCents = 0L, deductedFromNetSalary = false, customOnPayroll = false)))
                            }
                        }
                        if (t.mode == TransportMode.CUSTOM) {
                            FieldCount("Empresa paga (%)", t.companyPercent, 0..100,
                                { set(budget.copy(transport = t.copy(companyPercent = it))) })
                        }
                        if (!imeVisible) WizardAmountLine("Minha parte", BudgetMath.transportEmployeeCost(t, s.grossCents), true)
                    } else {
                        WizardEmpty("◈", "Benefício opcional", "O vale-transporte CLT automático só vale para o cenário com carteira assinada e transporte público. Ajustes ficam disponíveis na tela Transporte.")
                    }
                }
                7 -> {
                    WizardPanel {
                        FieldAmount(if (budget.rentPeriod == RentPeriod.YEARLY) "Aluguel anual" else "Aluguel mensal",
                            budget.rentCents, { set(budget.copy(rentCents = it)) }, compact = true)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TinyModeChip("Mensal", budget.rentPeriod == RentPeriod.MONTHLY, Modifier.weight(1f)) {
                                set(budget.copy(rentPeriod = RentPeriod.MONTHLY))
                            }
                            TinyModeChip("Anual ÷ 12", budget.rentPeriod == RentPeriod.YEARLY, Modifier.weight(1f)) {
                                set(budget.copy(rentPeriod = RentPeriod.YEARLY))
                            }
                        }
                        FieldAmount("Condomínio mensal", budget.condominiumCents,
                            { set(budget.copy(condominiumCents = it)) }, compact = true)
                        FieldAmount(if (budget.iptuPeriod == RentPeriod.YEARLY) "IPTU anual" else "IPTU mensal",
                            budget.iptuCents, { set(budget.copy(iptuCents = it)) }, compact = true)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TinyModeChip("IPTU mensal", budget.iptuPeriod == RentPeriod.MONTHLY, Modifier.weight(1f)) {
                                set(budget.copy(iptuPeriod = RentPeriod.MONTHLY))
                            }
                            TinyModeChip("IPTU anual ÷ 12", budget.iptuPeriod == RentPeriod.YEARLY, Modifier.weight(1f)) {
                                set(budget.copy(iptuPeriod = RentPeriod.YEARLY))
                            }
                        }
                    }
                    if (!imeVisible && !compact) WizardAmountLine("Moradia por mês", BudgetMath.housing(budget), true)
                }
                8 -> {
                    WizardPanel {
                        FieldAmount("Energia elétrica", budget.electricityCents,
                            { set(budget.copy(electricityCents = it)) }, compact = true)
                        FieldAmount("Água e esgoto", budget.waterCents,
                            { set(budget.copy(waterCents = it)) }, compact = true)
                        FieldAmount("Internet residencial", budget.internetCents,
                            { set(budget.copy(internetCents = it)) }, compact = true)
                        FieldAmount("Celular / chip", budget.mobileCents,
                            { set(budget.copy(mobileCents = it)) }, compact = true)
                    }
                    if (!imeVisible) WizardNote("Gás e outras contas podem ser adicionados depois. Luz e energia são a mesma despesa.")
                }
                9 -> {
                    WizardPanel {
                        FieldAmount("Supermercado mensal (estimativa)", budget.foodEstimateCents,
                            { set(budget.copy(foodEstimateCents = it)) }, compact = true)
                        FieldAmount("Restaurantes / lanches por mês", budget.foodOutsideCents,
                            { set(budget.copy(foodOutsideCents = it)) }, compact = true)
                        OutlinedButton(onClick = {
                            val base = budget.foodProducts
                            val existing = base.map { it.name.lowercase(Locale.ROOT) }.toSet()
                            var id = base.maxOfOrNull { it.id } ?: 0L
                            val added = listOf("Arroz", "Feijão", "Óleo", "Sal", "Ovos", "Macarrão", "Leite", "Café")
                                .filter { it.lowercase(Locale.ROOT) !in existing }
                                .take((500 - base.size).coerceAtLeast(0)).map { FoodProduct(++id, it, 0L, 100) }
                            set(budget.copy(foodProducts = base + added))
                        }, modifier = Modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(15.dp)) {
                            Text("＋ Adicionar alimentos essenciais", fontSize = 12.sp)
                        }
                        if (!imeVisible) WizardAmountLine("Alimentação paga em dinheiro", BudgetMath.foodPaidFromCash(budget), true)
                    }
                    if (!imeVisible) WizardNote("Os itens essenciais entram sem preço. Só serão contabilizados quando você informar valores reais; a estimativa continua enquanto a lista estiver incompleta.")
                }
                10 -> {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF8529D1), Color(0xFF36105E))))) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("SALDO MENSAL ESTIMADO", color = Color(0xFFE7C5FF),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(Money.format(BudgetMath.availableAfterReserve(budget)), color = Color.White,
                                fontWeight = FontWeight.Black, fontSize = if (compact) 32.sp else 37.sp)
                            LinearProgressIndicator(progress = {
                                val income = BudgetMath.salaryNet(budget)
                                if (income <= 0L) 0f else (BudgetMath.expenses(budget).toFloat() / income).coerceIn(0f, 1f)
                            }, color = Color(0xFFFFC889), trackColor = Color.White.copy(alpha = .22f),
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape))
                            Text("Suas despesas em relação à renda", color = Color.White.copy(alpha = .8f), fontSize = 11.sp)
                        }
                    }
                    WizardPanel {
                        WizardAmountLine("Renda após descontos", BudgetMath.salaryNet(budget))
                        WizardAmountLine("Moradia", BudgetMath.housing(budget))
                        WizardAmountLine("Alimentação do bolso", BudgetMath.foodPaidFromCash(budget))
                        WizardAmountLine("Transporte fora da folha", BudgetMath.transportBudgetCost(budget))
                        WizardAmountLine("Contas e outros gastos", BudgetMath.bills(budget) + BudgetMath.extras(budget))
                    }
                    if (!imeVisible) WizardNote("Simulação. Verifique itens pendentes e descontos no seu holerite. Você poderá editar tudo pelos painéis.")
                }
            }
        }
        if (!imeVisible) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = if (compact) 5.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedButton(onClick = { if (step > 0) step-- }, enabled = step > 0,
                        modifier = Modifier.weight(1f).height(49.dp), shape = RoundedCornerShape(18.dp)) { Text("Voltar") }
                    Button(onClick = { keyboard?.hide(); if (step == count - 1) finish(budget) else step++ },
                        modifier = Modifier.weight(1.65f).height(49.dp), shape = RoundedCornerShape(18.dp),
                        enabled = when (step) {
                            0 -> budget.workType != WorkType.UNKNOWN
                            1 -> if (budget.workType == WorkType.CLT) s.grossCents > 0L else budget.netSalaryCents > 0L
                            3 -> t.commuteMode != CommuteMode.UNSET
                            else -> true
                        }) { Text(if (step == count - 1) "Abrir meu orçamento  →" else "Continuar  →", fontWeight = FontWeight.Bold) }
                }
                if (step < count - 1) TextButton(onClick = { keyboard?.hide(); step++ },
                    modifier = Modifier.fillMaxWidth().height(31.dp)) { Text("Definir depois", fontSize = 12.sp, color = Muted) }
                else Text("Os dados serão guardados somente no seu aparelho.", modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center, color = Muted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun WizardPanel(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(24.dp), shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 15.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content)
    }
}

@Composable
private fun WizardOption(symbol: String, label: String, detail: String, selected: Boolean,
                         compact: Boolean, click: () -> Unit) {
    Surface(onClick = click, modifier = Modifier.fillMaxWidth().height(if (compact) 60.dp else 70.dp),
        color = if (selected) Color(0xFFF0E1FF) else Color.White,
        border = BorderStroke(if (selected) 1.5.dp else .5.dp,
            if (selected) Purple else Color(0xFFEBE5EF)), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(if (compact) 38.dp else 43.dp).clip(RoundedCornerShape(13.dp))
                .background(if (selected) Purple else Lilac), contentAlignment = Alignment.Center) {
                Text(symbol, color = if (selected) Color.White else Purple, fontSize = 19.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = Ink, fontWeight = FontWeight.Bold, fontSize = if (compact) 13.sp else 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(detail, color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(Modifier.size(22.dp).border(1.5.dp, if (selected) Purple else Muted, CircleShape)
                .background(if (selected) Purple else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                if (selected) Text("✓", fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun WizardAmountLine(label: String, cents: Long, strong: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = if (strong) Ink else Muted,
            fontSize = 12.sp, maxLines = 2)
        Text(Money.format(cents), color = if (strong) Purple else Ink,
            fontSize = if (strong) 16.sp else 13.sp, fontWeight = FontWeight.ExtraBold,
            maxLines = 1)
    }
}

@Composable
private fun WizardNote(message: String) {
    Text(message, fontSize = 11.sp, lineHeight = 15.sp, color = Muted, maxLines = 3,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 3.dp))
}

@Composable
private fun WizardEmpty(symbol: String, title: String, detail: String) {
    WizardPanel {
        Text(symbol, color = Purple, fontSize = 37.sp)
        Text(title, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(detail, color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun TinyModeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, click: () -> Unit) {
    Surface(onClick = click, modifier = modifier.height(33.dp),
        color = if (selected) Lilac else Color(0xFFF6F3F8),
        shape = RoundedCornerShape(12.dp), border = if (selected) BorderStroke(1.dp, Purple) else null) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) DeepPurple else Muted, fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun WizardCalendar(t: TransportConfig, compact: Boolean, changed: (TransportConfig) -> Unit) {
    val month = WorkCalendar.month(t.workCalendarMonth)
    val anchor = runCatching { LocalDate.parse(t.workAnchorDate) }.getOrNull() ?: LocalDate.now()
    val customDates = WorkCalendar.parseDates(t.customWorkDates)
    val activeDays = WorkCalendar.days(month, t.workSchedule, t.workWeekdaysMask, anchor, customDates)
    val dateLabel = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("pt-BR"))
    WizardPanel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TinyModeChip("Seg–Sex", t.workSchedule == WorkSchedule.WEEKLY, Modifier.weight(1f)) {
                changed(t.copy(workSchedule = WorkSchedule.WEEKLY, workWeekdaysMask = 31))
            }
            TinyModeChip("12 × 36", t.workSchedule == WorkSchedule.TWELVE_BY_THIRTY_SIX, Modifier.weight(1f)) {
                changed(t.copy(workSchedule = WorkSchedule.TWELVE_BY_THIRTY_SIX,
                    workAnchorDate = if (t.workAnchorDate.isBlank()) LocalDate.now().toString() else t.workAnchorDate))
            }
            TinyModeChip("Marcar dias", t.workSchedule == WorkSchedule.CUSTOM, Modifier.weight(1f)) {
                changed(t.copy(workSchedule = WorkSchedule.CUSTOM))
            }
        }
        Row(Modifier.fillMaxWidth().height(36.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { changed(t.copy(workCalendarMonth = month.minusMonths(1).toString())) },
                modifier = Modifier.width(45.dp)) { Text("‹", fontSize = 24.sp) }
            Text(month.format(dateLabel).replaceFirstChar { it.titlecase(Locale.forLanguageTag("pt-BR")) },
                modifier = Modifier.weight(1f), color = Ink, textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold, fontSize = 13.sp)
            TextButton(onClick = { changed(t.copy(workCalendarMonth = month.plusMonths(1).toString())) },
                modifier = Modifier.width(45.dp)) { Text("›", fontSize = 24.sp) }
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("S", "T", "Q", "Q", "S", "S", "D").forEach { label ->
                Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
            }
        }
        val offset = month.atDay(1).dayOfWeek.value - 1
        repeat(6) { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(7) { dayOfWeek ->
                    val day = week * 7 + dayOfWeek - offset + 1
                    val date = if (day in 1..month.lengthOfMonth()) month.atDay(day) else null
                    val selected = date != null && date in activeDays
                    Box(Modifier.weight(1f).height(if (compact) 30.dp else 36.dp)) {
                        if (date != null) Surface(onClick = {
                            val changedTransport = when (t.workSchedule) {
                                WorkSchedule.WEEKLY -> t.copy(workWeekdaysMask = t.workWeekdaysMask xor
                                    (1 shl (date.dayOfWeek.value - 1)))
                                WorkSchedule.TWELVE_BY_THIRTY_SIX -> t.copy(workAnchorDate = date.toString())
                                WorkSchedule.CUSTOM -> t.copy(customWorkDates = WorkCalendar.encodeDates(
                                    if (date in customDates) customDates - date else customDates + date))
                                WorkSchedule.MANUAL -> t.copy(workSchedule = WorkSchedule.CUSTOM,
                                    customWorkDates = WorkCalendar.encodeDates(customDates + date))
                            }
                            changed(changedTransport)
                        }, modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(10.dp),
                            color = if (selected) Purple else Color(0xFFF7F3FA)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("$day", color = if (selected) Color.White else Ink, fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (t.workSchedule == WorkSchedule.MANUAL) "Selecione sua escala" else "Dias de trabalho neste mês",
                    fontSize = 11.sp, color = Muted)
                Text(if (t.workSchedule == WorkSchedule.MANUAL) "Pendente" else "${activeDays.size} dias",
                    fontSize = 18.sp, color = Purple, fontWeight = FontWeight.ExtraBold)
            }
            Text("Toque nas datas para ajustar", color = Muted, fontSize = 10.sp)
        }
    }
    WizardNote("Seg–Sex: toque num dia para alterar o dia da semana. 12×36: toque no dia de trabalho inicial. Marcar dias: escolha cada data. Feriados e folgas devem ser ajustados.")
}
