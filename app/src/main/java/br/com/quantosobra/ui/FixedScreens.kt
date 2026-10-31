package br.com.quantosobra.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.quantosobra.domain.RentPeriod
import br.com.quantosobra.domain.Money
import br.com.quantosobra.domain.Budget
import br.com.quantosobra.domain.BudgetMath

@Composable
fun HousingScreen(b: Budget, change: (Budget) -> Unit, back: () -> Unit) {
    Page("Moradia", "Seu custo para ter um lugar só seu", back) {
        SectionTotal("Total mensal de moradia", BudgetMath.housing(b))
        SurfaceCard {
            FieldAmount(if (b.rentPeriod == RentPeriod.YEARLY) "Aluguel anual" else "Aluguel mensal",
                b.rentCents, { change(b.copy(rentCents = it)) })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = b.rentPeriod == RentPeriod.MONTHLY,
                    onClick = { change(b.copy(rentPeriod = RentPeriod.MONTHLY)) }, label = { Text("Mensal") })
                FilterChip(selected = b.rentPeriod == RentPeriod.YEARLY,
                    onClick = { change(b.copy(rentPeriod = RentPeriod.YEARLY)) }, label = { Text("Anual") })
            }
            if (b.rentPeriod == RentPeriod.YEARLY)
                Hint("Equivalente mensal: ${Money.format(BudgetMath.monthlyRent(b))}. O pagamento anual real continua ocorrendo na data da cobrança.")
            FieldAmount("Condomínio", b.condominiumCents, { change(b.copy(condominiumCents = it)) })
            FieldAmount(if (b.iptuPeriod == RentPeriod.YEARLY) "IPTU anual" else "IPTU mensal",
                b.iptuCents, { change(b.copy(iptuCents = it)) })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = b.iptuPeriod == RentPeriod.MONTHLY,
                    onClick = { change(b.copy(iptuPeriod = RentPeriod.MONTHLY)) }, label = { Text("IPTU mensal") })
                FilterChip(selected = b.iptuPeriod == RentPeriod.YEARLY,
                    onClick = { change(b.copy(iptuPeriod = RentPeriod.YEARLY)) }, label = { Text("IPTU anual") })
            }
            if (b.iptuPeriod == RentPeriod.YEARLY)
                Hint("IPTU mensal equivalente: ${Money.format(BudgetMath.monthlyIptu(b))}.")
        }
        Hint("Caução, primeiro aluguel e mudança são custos iniciais. Esta versão calcula principalmente as despesas recorrentes.")
    }
}

@Composable
fun BillsScreen(b: Budget, change: (Budget) -> Unit, back: () -> Unit) {
    Page("Contas da casa", "Sem duplicar luz e energia", back) {
        SectionTotal("Total mensal de contas", BudgetMath.bills(b))
        SurfaceCard {
            FieldAmount("Energia elétrica (luz)", b.electricityCents, { change(b.copy(electricityCents = it)) })
            FieldAmount("Água e esgoto", b.waterCents, { change(b.copy(waterCents = it)) })
            FieldAmount("Internet residencial", b.internetCents, { change(b.copy(internetCents = it)) })
        }
        Hint("Celular, gás e outras contas ficam em Outros gastos. Seus valores antigos foram preservados.")
    }
}

@Composable
fun ReserveScreen(b: Budget, change: (Budget) -> Unit, back: () -> Unit) {
    Page("Sua reserva", "Planeje uma folga no orçamento", back) {
        SectionTotal("Dinheiro separado por mês", b.reserveCents)
        SurfaceCard {
            FieldAmount("Valor que deseja guardar", b.reserveCents, { change(b.copy(reserveCents = it)) })
            Hint("A reserva não é uma conta paga. Ela é subtraída do saldo disponível para mostrar quanto sobra depois de guardar dinheiro.")
        }
        Hint("Não é aconselhamento financeiro. Ajuste o valor à sua realidade e às despesas essenciais.")
    }
}
