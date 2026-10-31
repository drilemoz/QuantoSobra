package br.com.quantosobra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.data.MinimumWageRepository
import br.com.quantosobra.domain.Money
import kotlinx.coroutines.launch

/** Referência consultável e editável: nunca sobrescreve o salário pessoal sem toque em Usar. */
@Composable
fun MinimumWageBox(applyToSalary: (Long) -> Unit, compact: Boolean = false) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val repo = remember(context) { MinimumWageRepository(context) }
    val scope = rememberCoroutineScope()
    var reference by remember { mutableStateOf(repo.current()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var edit by remember { mutableStateOf(false) }
    var manualInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp)) {
        Text("SALÁRIO MÍNIMO NACIONAL • BRUTO", fontSize = 10.sp, color = Muted,
            fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(Money.format(reference.cents), color = Purple, fontWeight = FontWeight.Bold,
                fontSize = if (compact) 17.sp else 22.sp)
            Text(if (reference.checkedAt.isNotEmpty()) "Consulta: ${reference.checkedAt}"
                else "Referência local", color = Muted, fontSize = 10.sp)
        }
        if (!compact) Text("${reference.source} • Vigência: ${reference.effectiveDate}", color = Muted,
            fontSize = 11.sp, maxLines = 2)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            OutlinedButton(
                onClick = {
                    if (!loading) {
                        loading = true
                        message = null
                        scope.launch {
                            val result = repo.consultOnline()
                            result.onSuccess { reference = it; message = "Referência atualizada. Seu salário não foi alterado." }
                                .onFailure { err ->
                                    val error = err.message.orEmpty()
                                    val detail = when {
                                        error.startsWith("HTTP ") -> " (${error.take(8)})"
                                        err is java.net.SocketTimeoutException -> " (tempo esgotado)"
                                        err is java.net.UnknownHostException -> " (sem conexão)"
                                        else -> ""
                                    }
                                    message = "Consulta indisponível$detail. Referência offline mantida."
                                }
                            loading = false
                        }
                    }
                }, enabled = !loading,
                modifier = Modifier.weight(1.2f).height(38.dp),
                contentPadding = PaddingValues(horizontal = 3.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (loading) "Consultando" else "Consultar online", fontSize = 10.sp, maxLines = 1) }
            OutlinedButton(onClick = { manualInput = Money.input(reference.cents); edit = true },
                modifier = Modifier.weight(.8f).height(38.dp),
                contentPadding = PaddingValues(horizontal = 3.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Alterar", fontSize = 10.sp)
            }
            Button(onClick = {
                focusManager.clearFocus(force = true)
                applyToSalary(reference.cents)
                message = "${Money.format(reference.cents)} aplicado ao campo de salário bruto."
            }, modifier = Modifier.weight(.65f).height(38.dp),
                contentPadding = PaddingValues(horizontal = 3.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Usar", fontSize = 10.sp)
            }
        }
        if (message != null) Text(message!!, fontSize = 10.sp, color = Muted, maxLines = 2)
    }

    if (edit) {
        ModernDialog(title = "Editar mínimo de referência", onDismiss = { edit = false },
            confirmText = "Salvar referência", confirmEnabled = (Money.parse(manualInput) ?: 0L) in 100L..100_000_000L,
            onConfirm = {
                val cents = Money.parse(manualInput)
                if (cents != null && cents in 100L..100_000_000L) {
                    reference = repo.setManual(cents)
                    message = "Referência manual salva. O salário pessoal só muda ao tocar em Usar."
                    edit = false
                }
            }
        ) {
            CurrencyTextEntry("Salário mínimo bruto (R$)", manualInput, { manualInput = it })
            Hint("Altera somente a referência local. Não confirma um valor previsto em lei e não altera automaticamente seu salário.")
        }
    }
}
