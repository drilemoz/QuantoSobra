package br.com.quantosobra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.quantosobra.domain.EssentialProducts
import java.util.Locale

@Composable
fun EssentialsPicker(currentNames: Set<String>, dismiss: () -> Unit, apply: (Set<String>) -> Unit) {
    val existing = currentNames.map { it.trim().lowercase(Locale.ROOT) }.toSet()
    val available = EssentialProducts.items.keys.filter { it.lowercase(Locale.ROOT) !in existing }
    var selected by remember(currentNames) { mutableStateOf(available.toSet()) }
    ModernDialog("Montar lista essencial", dismiss, "Adicionar selecionados", { apply(selected) },
        confirmEnabled = selected.isNotEmpty()) {
        Text("Selecione os produtos. Os valores ficam vazios até você informar preço e quantidade.",
            color = Muted)
        if (available.isEmpty()) Text("Todos os itens essenciais já estão cadastrados.", color = Muted)
        available.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { name ->
                    FilterChip(selected = name in selected, onClick = {
                        selected = if (name in selected) selected - name else selected + name
                    }, label = { Text(name) }, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
