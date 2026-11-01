package br.com.quantosobra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.quantosobra.domain.*

/** Editor único do wizard e da central, evitando divergências nas regras de renda. */
@Composable
fun IncomeEditor(budget: Budget, change: (Budget) -> Unit, compact: Boolean = false) {
    val c = budget.income
    val type = budget.workType
    val workName = when(type) {
        WorkType.MEI, WorkType.MEI_PJ, WorkType.PJ -> "Faturamento mensal do negócio"
        WorkType.PUBLIC_SERVANT -> "Remuneração bruta do contracheque"
        WorkType.SELF_EMPLOYED -> "Recebimentos mensais por serviços"
        WorkType.INFORMAL -> "Total recebido no mês"
        else -> "Renda principal recebida no mês"
    }
    fun save(ic: IncomeConfig) = change(budget.copy(income = ic))
    SurfaceCard {
        Text("Renda de ${type.label()}", color = Ink, fontWeight = FontWeight.Bold)
        Hint("Escolha se já conhece o valor disponível ou se deseja detalhar como ele é calculado.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { save(c.copy(detailed = false)) }, modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (!c.detailed) Purple else Lilac,
                    contentColor = if (!c.detailed) Color.White else DeepPurple)) { Text("Renda pronta") }
            Button(onClick = { save(c.copy(detailed = true)) }, modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (c.detailed) Purple else Lilac,
                    contentColor = if (c.detailed) Color.White else DeepPurple)) { Text("Detalhar") }
        }
        if (!c.detailed) {
            FieldAmount("Valor líquido mensal disponível", budget.netSalaryCents,
                { change(budget.copy(netSalaryCents = it)) }, compact = compact)
            Hint("Já descontou tributos e despesas do trabalho? Informe o valor que pode realmente usar. Abaixo, acrescente apenas outras fontes independentes.")
        } else {
            if (type == WorkType.INFORMAL || type == WorkType.SELF_EMPLOYED) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Calcular por dia trabalhado", modifier = Modifier.weight(1f))
                    Switch(checked = c.dailyBased, onCheckedChange = { save(c.copy(dailyBased = it)) })
                }
                if (c.dailyBased) {
                    FieldAmount("Recebimento por dia", c.dailyRateCents,
                        { save(c.copy(dailyRateCents = it)) }, compact = compact)
                    FieldCount("Dias remunerados no mês", c.workedDays, 0..31,
                        { save(c.copy(workedDays = it)) })
                    ValueLine("Receita estimada pelos dias", IncomeMath.revenue(c, type))
                    Hint("Informe dias efetivamente remunerados. Não são automaticamente os mesmos dias da escala de deslocamento.")
                }
            }
            if (!((type == WorkType.INFORMAL || type == WorkType.SELF_EMPLOYED) && c.dailyBased))
                FieldAmount(workName, c.receiptsCents, { save(c.copy(receiptsCents = it)) }, compact = compact)
            if (type == WorkType.PUBLIC_SERVANT) {
                Text("Esfera do órgão", color = Ink, fontWeight = FontWeight.Bold)
                PublicAdministration.entries.forEach { option -> Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = c.publicAdministration == option,
                        onClick = { save(c.copy(publicAdministration = option)) })
                    Text(option.title)
                } }
                Text("Regime previdenciário", color = Ink, fontWeight = FontWeight.Bold)
                PublicPension.entries.forEach { option -> Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = c.publicPension == option,
                        onClick = { save(c.copy(publicPension = option)) })
                    Text(option.title)
                } }
            }
            if (type == WorkType.PJ) {
                Text("Regime tributário informado", color = Ink, fontWeight = FontWeight.Bold)
                PjTaxRegime.entries.forEach { option -> Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = c.pjTaxRegime == option,
                        onClick = { save(c.copy(pjTaxRegime = option)) })
                    Text(option.title)
                } }
            }
            FieldAmount(if (type == WorkType.PUBLIC_SERVANT) "Outros descontos da folha" else "Custos da atividade / negócio",
                c.operatingCents, { save(c.copy(operatingCents = it)) }, compact = compact)
            FieldAmount(if (type == WorkType.PUBLIC_SERVANT) "Imposto de renda descontado" else "Tributos pagos (sem DAS/INSS abaixo)",
                c.taxesCents, { save(c.copy(taxesCents = it)) }, compact = compact)
            FieldAmount(if (type == WorkType.PUBLIC_SERVANT) "Previdência RPPS/RGPS efetivamente descontada"
                else "INSS / contribuição previdenciária informada", c.socialCents,
                { save(c.copy(socialCents = it)) }, compact = compact)
            if (type != WorkType.PUBLIC_SERVANT) {
                FieldAmount("Reserva do negócio / trabalho", c.businessReserveCents,
                    { save(c.copy(businessReserveCents = it)) }, compact = compact)
                FieldAmount("Retirada pessoal planejada (opcional)", c.personalWithdrawalCents,
                    { save(c.copy(personalWithdrawalCents = it)) }, compact = compact)
                Hint("Com retirada zero, considera-se todo o saldo após custos. Se informar uma retirada, o app limita a renda pessoal ao saldo disponível. Não confunda faturamento com salário.")
            }
            if (type.isMei()) {
                HorizontalDivider(color = Lilac)
                Text("MEI · DAS 2026", fontWeight = FontWeight.Bold, color = Ink)
                Hint("Valores oficiais de referência de 2026 para MEI comum. Não aplicável ao MEI caminhoneiro; confirme o valor no PGMEI.")
                MeiActivity.entries.forEach { activity ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = c.meiActivity == activity,
                            onClick = { save(c.copy(meiActivity = activity)) })
                        Text("${activity.title} · ${Money.format(activity.das2026Cents)}", modifier = Modifier.weight(1f))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Descontar DAS do saldo", modifier = Modifier.weight(1f))
                    Switch(checked = c.applyMeiDas,
                        onCheckedChange = { save(c.copy(applyMeiDas = it)) })
                }
                if (c.applyMeiDas) FieldAmount("DAS real (zero = referência 2026)", c.meiDasOverrideCents,
                    { save(c.copy(meiDasOverrideCents = it)) }, compact = compact)
                Text("Acompanhamento do teto MEI", fontWeight = FontWeight.Bold, color = Ink)
                FieldAmount("Faturamento acumulado antes deste mês", c.meiPreviousRevenueCents,
                    { save(c.copy(meiPreviousRevenueCents = it)) }, compact = compact)
                FieldCount("Mês de abertura no ano de 2026 (1 = antes/janeiro)", c.meiOpeningMonth,
                    1..12, { save(c.copy(meiOpeningMonth = it)) })
                val limit = IncomeMath.meiCap2026(c)
                val used = IncomeMath.meiRunningTotal(c)
                ValueLine("Total até este mês", used)
                ValueLine("Teto proporcional estimado", limit)
                if (used >= limit) Text("⚠ Faturamento igual ou superior à referência legal. Confira o enquadramento.", color = Negative)
                else if (used * 10L >= limit * 8L) Text("⚠ Próximo do limite anual do MEI. Confira suas receitas acumuladas.", color = Negative)
                Hint("Referência para 2026: teto geral de R$ 81 mil/ano ou R$ 6.750 por mês de atividade no ano de abertura. Não substitui controle fiscal.")
            } else if (type == WorkType.PJ) {
                Hint("PJ: informe tributos reais conforme o regime da empresa (Simples, Presumido etc.). O aplicativo não calcula automaticamente impostos nem autoriza distribuição de lucros.")
            } else if (type == WorkType.PUBLIC_SERVANT) {
                Hint("Servidor: use o valor real do contracheque; RPPS e RGPS têm regras distintas. O cálculo CLT de INSS/IRRF não é aplicado aqui.")
            } else if (type == WorkType.SELF_EMPLOYED) {
                Hint("Autônomo: informe a contribuição e o imposto efetivamente devidos. Carnê-Leão pode se aplicar a recebimentos de pessoas físicas ou do exterior, conforme a Receita Federal.")
            }
            HorizontalDivider(color = Lilac)
            ValueLine("Disponível da atividade", IncomeMath.workNet(c, type), emphasize = true)
        }
        if (type == WorkType.INFORMAL || type == WorkType.SELF_EMPLOYED || type == WorkType.PJ || type.isMei()) {
            var historyOpen by remember { mutableStateOf(false) }
            TextButton(onClick = { historyOpen = !historyOpen }) {
                Text(if (historyOpen) "Ocultar histórico de renda" else "Histórico e cenário conservador", color = Purple)
            }
            if (historyOpen) {
                Hint("Informe três meses anteriores, do mais recente ao mais antigo. Valores zero representam meses sem recebimentos; não são estimativas geradas pelo app.")
                repeat(3) { index ->
                    FieldAmount("Recebido há ${index+1} ${if (index == 0) "mês" else "meses"}", c.historyCents.getOrElse(index) { 0L }, { value ->
                        val vals = c.historyCents.toMutableList()
                        while (vals.size < 3) vals.add(0L)
                        vals[index] = value
                        save(c.copy(historyCents = vals))
                    }, compact = compact)
                }
                IncomeMath.averageHistory(c)?.let { ValueLine("Média registrada", it) }
                IncomeMath.conservativeHistory(c)?.let { low ->
                    ValueLine("Menor mês registrado", low)
                    ValueLine("Renda conservadora para orçamento",
                        (if (c.detailed) IncomeMath.workNet(c.copy(receiptsCents = low, dailyBased = false), type)
                         else low) + IncomeMath.sourcesMonthly(c))
                    Hint("Cenário apenas informativo: não substitui a renda usada no orçamento sem você alterar o campo principal.")
                }
            }
        }
    }
    IncomeSourcesEditor(budget, change)
    SurfaceCard {
        ValueLine("Total mensal para orçamento", IncomeMath.fullIncome(budget), emphasize = true)
        Hint("Fontes adicionais são somadas uma única vez. Benefícios VA/VR são separados do dinheiro.")
        if (IncomeMath.fullIncome(budget) < 0L) Text("⚠ Custos acima da receita. Revise antes de concluir.", color = Negative)
    }
}

@Composable
fun IncomeSourcesEditor(b: Budget, change: (Budget) -> Unit) {
    var editing by remember { mutableStateOf<AdditionalIncome?>(null) }
    var open by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var cadence by remember { mutableStateOf(IncomeCadence.MONTHLY) }
    fun start(source: AdditionalIncome?) {
        editing = source; name = source?.name.orEmpty()
        amount = source?.let { Money.input(it.amountCents) }.orEmpty()
        cadence = source?.cadence ?: IncomeCadence.MONTHLY
        open = true
    }
    SurfaceCard {
        Text("Outras fontes de renda", fontWeight = FontWeight.Bold, color = Ink)
        Hint("Ex.: aluguel recebido, bolsa, pensão ou segundo trabalho. Não repita a receita principal acima.")
        b.income.sources.forEach { entry ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(entry.name, color = Ink)
                    Text("${Money.format(IncomeMath.sourceMonthly(entry))}/mês", color = Muted)
                }
                TextButton(onClick = { start(entry) }) { Text("Editar") }
                TextButton(onClick = { change(b.copy(income = b.income.copy(
                    sources = b.income.sources.filterNot { it.id == entry.id }))) }) { Text("Excluir", color = Negative) }
            }
        }
        OutlinedButton(onClick = { start(null) }, enabled = b.income.sources.size < 25,
            modifier = Modifier.fillMaxWidth()) { Text("+ Adicionar fonte de renda") }
        if (b.income.sources.size >= 25) Hint("Limite de 25 fontes atingido. Edite ou exclua uma fonte existente.")
    }
    if (open) AlertDialog(onDismissRequest = { open = false },
        title = { Text(if (editing == null) "Adicionar renda" else "Editar renda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it.take(70) },
                    label = { Text("Nome da fonte") }, singleLine = true)
                CurrencyTextEntry("Valor recebido", amount, { amount = it })
                Text("Frequência", color = Ink)
                IncomeCadence.entries.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = item == cadence, onClick = { cadence = item })
                        Text(when(item) { IncomeCadence.MONTHLY -> "Mensal"; IncomeCadence.WEEKLY -> "Semanal (52/12)";
                            IncomeCadence.ANNUAL -> "Anual (1/12)" })
                    }
                }
            }
        }, confirmButton = {
            TextButton(enabled = name.isNotBlank() && Money.parse(amount) != null,
                onClick = {
                    val item = AdditionalIncome(editing?.id ?: ((b.income.sources.maxOfOrNull { it.id } ?: 0L) + 1L),
                        name.trim(), Money.parse(amount) ?: 0L, cadence)
                    val sources = if (editing == null) b.income.sources + item else
                        b.income.sources.map { if (it.id == editing?.id) item else it }
                    change(b.copy(income = b.income.copy(sources = sources)))
                    open = false
                }) { Text("Salvar") }
        }, dismissButton = { TextButton(onClick = { open = false }) { Text("Cancelar") } })
}

@Composable
fun NonCltIncomeScreen(b: Budget, change: (Budget) -> Unit, back: () -> Unit) {
    Page("Minha renda", "Recebimentos, custos e renda realmente disponível", back) {
        SurfaceCard {
            Text("Tipo de atividade", color = Ink, fontWeight = FontWeight.Bold)
            listOf(WorkType.MEI, WorkType.PJ, WorkType.SELF_EMPLOYED, WorkType.INFORMAL,
                WorkType.PUBLIC_SERVANT, WorkType.OTHER, WorkType.CLT).forEach { type ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = b.workType == type || (type == WorkType.MEI && b.workType == WorkType.MEI_PJ),
                        onClick = { change(b.withWorkType(type)) })
                    Text(type.label())
                }
            }
            Hint("Trocar vínculo preserva os valores já digitados, mas desativa descontos CLT incompatíveis.")
        }
        IncomeEditor(b, change)
        SurfaceCard {
            Text("Benefícios recebidos", color = Ink, fontWeight = FontWeight.Bold)
            FieldAmount("VA", b.salary.foodBenefitCents,
                { change(b.copy(salary = b.salary.copy(foodBenefitCents = it))) })
            FieldAmount("VR", b.salary.mealVoucherCents,
                { change(b.copy(salary = b.salary.copy(mealVoucherCents = it))) })
            FoodBenefitDeductionEditor(b.salary, { change(b.copy(salary = it)) })
        }
    }
}
