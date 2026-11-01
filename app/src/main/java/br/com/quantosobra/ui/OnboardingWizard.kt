package br.com.quantosobra.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
        "Pequenas contas que fazem diferença.", "Produtos, embalagens e preços ajustáveis.", "Seu orçamento começa aqui.")
    val count = titles.size
    val compact = LocalConfiguration.current.screenHeightDp < 760
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val spacing = if (compact) 6.dp else 10.dp
    val t = budget.transport
    val s = budget.salary
    val keyboard = LocalSoftwareKeyboardController.current
    var salaryExternalRevision by remember { mutableIntStateOf(0) }
    var showSalaryRules by remember { mutableStateOf(false) }
    var chooseEssential by remember { mutableStateOf(false) }
    var wizardAdding by remember { mutableStateOf(false) }
    var wizardEditing by remember { mutableStateOf<FoodProduct?>(null) }
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
        set(budget.copy(transport = t2.copy(workCalendarMonth = month.toString(),
            daysPerMonth = if (t2.workSchedule == WorkSchedule.MANUAL) t2.daysPerMonth else countDays)))
    }
    Column(Modifier.fillMaxSize().background(Color(0xFFF8F4FC)).navigationBarsPadding().imePadding()) {
        if (!imeVisible) {
            Box(Modifier.fillMaxWidth().height(if (compact) 90.dp else 122.dp)
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
        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)
            .verticalScroll(remember(step) { androidx.compose.foundation.ScrollState(0) }),
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
                            }, compact = true, externalValueRevision = salaryExternalRevision)
                        if (!imeVisible) {
                            HorizontalDivider(color = Lilac)
                            if (clt) {
                                WizardAmountLine("Bruto informado", s.grossCents)
                                WizardAmountLine("INSS estimado", SalaryMath.breakdown(budget).inssCents)
                                WizardAmountLine("IRRF estimado", SalaryMath.breakdown(budget).irrfCents)
                            }
                            WizardAmountLine(if (clt) "Líquido após descontos" else "Renda informada",
                                BudgetMath.salaryNet(budget), true)
                        }
                    }
                    if (clt && !imeVisible) WizardPanel {
                        MinimumWageBox(compact = true, applyToSalary = { minimum ->
                            // Atualiza o orçamento e força a exibição do valor no campo,
                            // mesmo se o campo ainda conservava foco após fechar o teclado.
                            set(budget.copy(salary = s.copy(grossCents = minimum, mode = SalaryMode.GROSS)))
                            salaryExternalRevision++
                        })
                        Text("Mínimo nacional BRUTO. O líquido é menor devido aos descontos indicados acima.",
                            color = Muted, fontSize = 10.sp)
                    }
                    if (!imeVisible) {
                        OutlinedButton(onClick = { showSalaryRules = true }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp)) { Text("ⓘ  Como são calculados os descontos?", fontSize = 12.sp) }
                        WizardNote(if (clt) "Estimativa de INSS/IRRF 2026. Outros descontos e benefícios são configuráveis." else
                            "Outros vínculos não recebem descontos CLT automáticos. Confira seu rendimento líquido.")
                    }
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
                    if (!imeVisible) {
                        OutlinedButton(onClick = { showSalaryRules = true }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)) { Text("ⓘ  Percentuais e benefícios", fontSize = 12.sp) }
                        WizardNote("VA/VR é um crédito de alimentação, não renda em dinheiro. Não repita o VT nos descontos.")
                    }
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
                            Triple(TransportMode.FIXED, "Empresa paga valor fixo", "Informe a ajuda mensal"),
                            Triple(TransportMode.NONE, "Pago do meu bolso", "Sem ajuda da empresa")
                        ).forEachIndexed { index, item ->
                            WizardOption(listOf("▣", "✓", "%", "R$", "−")[index], item.second, item.third,
                                t.mode == item.first, compact) {
                                set(budget.copy(transport = t.copy(mode = item.first,
                                    basicSalaryCents = 0L, deductedFromNetSalary = false, customOnPayroll = false)))
                            }
                        }
                        if (t.mode == TransportMode.CUSTOM) {
                            FieldCount("Empresa paga (%)", t.companyPercent, 0..100,
                                { set(budget.copy(transport = t.copy(companyPercent = it))) })
                        }
                        if (t.mode == TransportMode.FIXED) {
                            FieldAmount("Ajuda mensal da empresa", t.companyFixedCents,
                                { set(budget.copy(transport = t.copy(companyFixedCents = it))) }, compact = true)
                        }
                        if (!imeVisible) {
                            WizardAmountLine("Minha parte", BudgetMath.transportEmployeeCost(t, s.grossCents), true)
                            TextButton(onClick = { showSalaryRules = true }) {
                                Text("ⓘ Regras legais do transporte", color = Purple, fontSize = 12.sp)
                            }
                        }
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
                    }
                    if (!imeVisible) WizardNote("Chip, gás e outros custos agora são cadastrados em Outros gastos. Luz e energia são a mesma despesa.")
                }
                9 -> {
                    WizardPanel {
                        FieldAmount("Supermercado mensal (estimativa)", budget.foodEstimateCents,
                            { set(budget.copy(foodEstimateCents = it)) }, compact = true)
                        FieldAmount("Restaurantes / lanches por mês", budget.foodOutsideCents,
                            { set(budget.copy(foodOutsideCents = it)) }, compact = true)
                        OutlinedButton(onClick = { chooseEssential = true },
                            modifier = Modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(15.dp)) {
                            Text("＋ Carregar produtos básicos", fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = { wizardAdding = true }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp)) { Text("＋ Adicionar produto personalizado", fontSize = 12.sp) }
                        if (budget.foodProducts.isNotEmpty()) {
                            val pending = budget.foodProducts.count { it.unitPriceCents <= 0L }
                            Text("${budget.foodProducts.size} produtos • $pending preço(s) pendente(s)",
                                color = Muted, fontSize = 11.sp)
                            budget.foodProducts.forEach { item ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(item.name, color = Ink, fontSize = 13.sp)
                                        Text("${Money.quantityInput(item.quantityHundredths)} × ${item.unit} • " +
                                            if (item.unitPriceCents > 0L) Money.format(BudgetMath.productCost(item))
                                            else "Preço pendente", color = Muted, fontSize = 10.sp)
                                    }
                                    TextButton(onClick = { wizardEditing = item }) { Text("Editar", fontSize = 11.sp) }
                                }
                            }
                        }
                        if (!imeVisible) WizardAmountLine("Alimentação paga em dinheiro", BudgetMath.foodPaidFromCash(budget), true)
                    }
                    if (!imeVisible) WizardNote("Referências de preços são indicativas, não ofertas ao vivo. Você pode editar os itens; enquanto houver preço pendente, a estimativa protege o orçamento.")
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
    if (showSalaryRules) PayrollRulesDialog(budget) { showSalaryRules = false }
    if (chooseEssential) EssentialsPicker(
        current = budget.foodProducts,
        dismiss = { chooseEssential = false },
        apply = { chosen ->
            budget = budget.copy(foodProducts = EssentialProducts.merge(budget.foodProducts, chosen))
            chooseEssential = false
        }
    )
    if (wizardAdding || wizardEditing != null) ProductDialog(
        initial = wizardEditing,
        dismiss = { wizardAdding = false; wizardEditing = null },
        save = { name, price, qty, unit ->
            val target = wizardEditing
            val list = if (target == null) budget.foodProducts + FoodProduct(
                (budget.foodProducts.maxOfOrNull { it.id } ?: 0L) + 1L,
                name, price, qty, unit
            ) else budget.foodProducts.map { if (it.id == target.id) it.copy(
                name = name, unitPriceCents = price, quantityHundredths = qty,
                unit = unit, priceOrigin = "MANUAL") else it }
            budget = budget.copy(foodProducts = list)
            wizardAdding = false
            wizardEditing = null
        }
    )
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

/** Cartões da escala + calendário diretamente ajustável, seguindo o layout do wizard. */
@Composable
fun WizardCalendar(t: TransportConfig, compact: Boolean, changed: (TransportConfig) -> Unit) {
    var month by remember { mutableStateOf(WorkCalendar.month(t.workCalendarMonth)) }
    var choosingAnchor by remember { mutableStateOf(false) }
    val budgetMonth = WorkCalendar.month(t.workCalendarMonth)
    val anchor = runCatching { LocalDate.parse(t.workAnchorDate) }.getOrNull() ?: month.atDay(1)
    val customDates = WorkCalendar.parseDates(t.customWorkDates)
    val extra = WorkCalendar.parseDates(t.workExtraDates)
    val off = WorkCalendar.parseDates(t.workOffDates)
    val baseDays = WorkCalendar.days(month, t.workSchedule, t.workWeekdaysMask, anchor, customDates)
    val activeDays = WorkCalendar.days(month, t.workSchedule, t.workWeekdaysMask, anchor, customDates, extra, off)
    val dateLabel = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
    val scaleCards = listOf(Triple("5×2", "5 dias", 31), Triple("6×1", "6 dias", 63),
        Triple("12×36", "Plantão", -1), Triple("4×3", "4 dias", 15), Triple("Outra", "Personalizar", -2))
    val selectedScale = when {
        t.workSchedule == WorkSchedule.TWELVE_BY_THIRTY_SIX -> 2
        t.workSchedule == WorkSchedule.FOUR_BY_THREE -> 3
        t.workSchedule == WorkSchedule.CUSTOM -> 4
        t.workSchedule == WorkSchedule.WEEKLY && t.workWeekdaysMask == 31 -> 0
        t.workSchedule == WorkSchedule.WEEKLY && t.workWeekdaysMask == 63 -> 1
        t.workSchedule == WorkSchedule.WEEKLY && t.workWeekdaysMask == 15 -> 3
        else -> 4
    }
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 9.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            scaleCards.take(3).forEachIndexed { index, item ->
                ScaleCard(item.first, item.second, selectedScale == index, Modifier.weight(1f), compact) {
                    val config = if (item.third == -1) t.copy(workSchedule = WorkSchedule.TWELVE_BY_THIRTY_SIX,
                        workAnchorDate = if (t.workAnchorDate.isBlank()) month.atDay(1).toString() else t.workAnchorDate,
                        workExtraDates = "", workOffDates = "")
                    else t.copy(workSchedule = WorkSchedule.WEEKLY, workWeekdaysMask = item.third,
                        workExtraDates = "", workOffDates = "")
                    changed(config)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            scaleCards.drop(3).forEachIndexed { ix, item ->
                ScaleCard(item.first, item.second, selectedScale == ix + 3, Modifier.weight(1f), compact) {
                    changed(if (item.third == -2) t.copy(workSchedule = WorkSchedule.CUSTOM,
                        customWorkDates = if (t.workSchedule == WorkSchedule.CUSTOM) t.customWorkDates
                            else WorkCalendar.encodeDates(activeDays),
                        workExtraDates = "", workOffDates = "")
                    else t.copy(workSchedule = WorkSchedule.FOUR_BY_THREE,
                        workAnchorDate = month.atDay(1).with(java.time.DayOfWeek.MONDAY).toString(),
                        workExtraDates = "", workOffDates = ""))
                }
            }
            Spacer(Modifier.weight(1f))
        }
        if (t.workSchedule == WorkSchedule.WEEKLY) {
            Text("Selecione seus dias de trabalho", color = Muted, fontSize = 11.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom").forEachIndexed { index, label ->
                    val selected = t.workWeekdaysMask and (1 shl index) != 0
                    Surface(onClick = { changed(t.copy(workWeekdaysMask = t.workWeekdaysMask xor (1 shl index))) },
                        modifier = Modifier.weight(1f).height(35.dp),
                        color = if (selected) Color(0xFFEAD7F9) else Color.White,
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(.5.dp, Lilac)) {
                        Box(contentAlignment = Alignment.Center) { Text(label, color = if (selected) DeepPurple else Muted,
                            fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
                    }
                }
            }
        } else if (t.workSchedule == WorkSchedule.TWELVE_BY_THIRTY_SIX || t.workSchedule == WorkSchedule.FOUR_BY_THREE) {
            Text("Ciclo contínuo: toque em um dia para registrar uma exceção individual.", color = Muted, fontSize = 11.sp)
            OutlinedButton(onClick = { choosingAnchor = !choosingAnchor }) {
                Text(if (choosingAnchor) "Toque na data inicial do ciclo" else "Alterar data inicial do ciclo", fontSize = 11.sp)
            }
        } else if (t.workSchedule == WorkSchedule.CUSTOM) {
            Text("Toque nos dias em que realmente vai trabalhar.", color = Muted, fontSize = 11.sp)
        }
        Surface(color = Color(0xFFF0EEF2), shape = RoundedCornerShape(23.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = if (compact) 8.dp else 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { month = month.minusMonths(1) },
                        modifier = Modifier.width(37.dp)) { Text("‹", fontSize = 23.sp) }
                    Text(month.format(dateLabel).replaceFirstChar { it.titlecase(Locale.forLanguageTag("pt-BR")) },
                        modifier = Modifier.weight(1f), color = Ink, textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    TextButton(onClick = { month = month.plusMonths(1) },
                        modifier = Modifier.width(37.dp)) { Text("›", fontSize = 23.sp) }
                }
                Row(Modifier.fillMaxWidth()) {
                    listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom").forEach { label ->
                        Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                            fontSize = 10.sp, color = Muted)
                    }
                }
                val offset = month.atDay(1).dayOfWeek.value - 1
                val rows = (offset + month.lengthOfMonth() + 6) / 7
                repeat(rows) { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(7) { weekDay ->
                            val day = week * 7 + weekDay - offset + 1
                            val date = if (day in 1..month.lengthOfMonth()) month.atDay(day) else null
                            val selected = date != null && date in activeDays
                            Box(Modifier.weight(1f).height(if (compact) 25.dp else 32.dp)) {
                                if (date != null) Surface(onClick = {
                                    val updated = when {
                                        choosingAnchor && t.workSchedule in listOf(
                                            WorkSchedule.TWELVE_BY_THIRTY_SIX, WorkSchedule.FOUR_BY_THREE) -> {
                                            choosingAnchor = false
                                            t.copy(workAnchorDate = date.toString(), workExtraDates = "", workOffDates = "")
                                        }
                                        t.workSchedule == WorkSchedule.MANUAL -> t.copy(workSchedule = WorkSchedule.CUSTOM,
                                            customWorkDates = WorkCalendar.encodeDates(setOf(date)))
                                        t.workSchedule == WorkSchedule.CUSTOM -> t.copy(customWorkDates =
                                            WorkCalendar.encodeDates(if (date in customDates) customDates - date else customDates + date))
                                        else -> {
                                            val (newExtra, newOff) = WorkCalendar.toggleDate(
                                                date, activeDays, extra, off, date in baseDays)
                                            t.copy(workExtraDates = WorkCalendar.encodeDates(newExtra),
                                                workOffDates = WorkCalendar.encodeDates(newOff))
                                        }
                                    }
                                    changed(updated)
                                }, modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(11.dp),
                                    color = if (selected) Color(0xFFDECBEB) else Color(0xFFF6F5F7)) {
                                    Box(contentAlignment = Alignment.Center) { Text("$day", color = Ink, fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFD9CBDD))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Dias de trabalho", color = Muted, fontSize = 10.sp)
                        Text("${activeDays.size} dias", color = DeepPurple, fontWeight = FontWeight.Black,
                            fontSize = 20.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Folgas", color = Muted, fontSize = 10.sp)
                        Text("${month.lengthOfMonth() - activeDays.size}", color = Ink, fontWeight = FontWeight.Bold,
                            fontSize = 18.sp)
                    }
                }
            }
        }
        Text("Mês usado no orçamento do transporte: ${budgetMonth.format(dateLabel)} " +
            "(${t.daysPerMonth} dias). Trocar o mês da prévia não altera seu custo.",
            color = Muted, fontSize = 10.sp)
        if (month != budgetMonth) OutlinedButton(onClick = {
            changed(t.copy(workCalendarMonth = month.toString()))
        }, modifier = Modifier.fillMaxWidth()) { Text("Usar este mês no orçamento", fontSize = 12.sp) }
        Text("Toque num dia para marcar folga ou trabalho excepcional. Feriados e férias não são presumidos.",
            color = Muted, fontSize = 10.sp, lineHeight = 12.sp)
    }
}

@Composable
private fun ScaleCard(title: String, subtitle: String, selected: Boolean,
                      modifier: Modifier, compact: Boolean, click: () -> Unit) {
    Surface(onClick = click, modifier = modifier.height(if (compact) 51.dp else 69.dp),
        color = if (selected) Color(0xFFEADFF4) else Color(0xFFF5F4F6),
        border = BorderStroke(if (selected) 2.dp else .6.dp, if (selected) Purple else Color(0xFFE0DCE3)),
        shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(title, color = if (selected) Purple else Ink, fontSize = if (compact) 19.sp else 22.sp,
                fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = Muted, fontSize = 10.sp)
        }
    }
}
