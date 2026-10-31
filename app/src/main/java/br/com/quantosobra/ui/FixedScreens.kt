package br.com.quantosobra.ui

import androidx.compose.runtime.Composable
import br.com.quantosobra.domain.Budget
import br.com.quantosobra.domain.BudgetMath

@Composable
fun HousingScreen(b: Budget, change: (Budget) -> Unit, back: () -> Unit) {
    Page("Moradia", "Seu custo para ter um lugar só seu", back) {
        SectionTotal("Total mensal de moradia", BudgetMath.housing(b))
        SurfaceCard {
            FieldAmount("Aluguel mensal", b.rentCents, { change(b.copy(rentCents = it)) })
            FieldAmount("Condomínio", b.condominiumCents, { change(b.copy(condominiumCents = it)) })
            FieldAmount("IPTU mensal", b.iptuCents, { change(b.copy(iptuCents = it)) },
                "Se o IPTU for anual, divida por 12 antes de cadastrar.")
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
            FieldAmount("Plano de celular / chip", b.mobileCents, { change(b.copy(mobileCents = it)) })
            FieldAmount("Gás (média por mês)", b.gasCents, { change(b.copy(gasCents = it)) },
                "Ex.: botijão de R$ 120 que dura 3 meses = R$ 40/mês.")
            FieldAmount("Outras contas fixas", b.otherBillsCents, { change(b.copy(otherBillsCents = it)) })
        }
        Hint("As despesas são calculadas automaticamente no painel inicial.")
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
