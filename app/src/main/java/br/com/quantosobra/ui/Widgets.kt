package br.com.quantosobra.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.Money

@Composable
fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) { Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content) }
}

@Composable
fun FieldAmount(label: String, cents: Long, onChange: (Long) -> Unit, hint: String? = null) {
    var input by remember { mutableStateOf(if (cents == 0L) "" else Money.input(cents)) }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = input,
            onValueChange = { newValue ->
                if (newValue.length <= 19) {
                    input = newValue
                    Money.parse(newValue)?.let(onChange)
                }
            },
            label = { Text(label) },
            prefix = { Text("R$ ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = Money.parse(input) == null,
            shape = RoundedCornerShape(16.dp)
        )
        if (hint != null) Text(hint, color = Muted, fontSize = 12.sp)
    }
}

@Composable
fun FieldCount(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    var input by remember { mutableStateOf(value.toString()) }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = input,
        onValueChange = { v ->
            if (v.length <= 4 && v.all(Char::isDigit)) {
                input = v
                v.toIntOrNull()?.let { if (it in range) onChange(it) }
            }
        },
        label = { Text(label) },
        singleLine = true,
        isError = input.toIntOrNull()?.let { it in range } != true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ValueLine(label: String, amount: Long, emphasize: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (emphasize) Ink else Muted,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal)
        Text(Money.format(amount), color = Ink,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun Page(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(Background)) {
        Row(
            Modifier.fillMaxWidth().background(DeepPurple).padding(start = 8.dp, top = 18.dp, end = 18.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack, modifier = Modifier.sizeIn(minWidth = 55.dp, minHeight = 52.dp)) {
                Text("‹", fontSize = 38.sp, color = Color.White)
            }
            Column {
                Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 12.sp, color = Color(0xFFE9D8F7))
            }
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun SectionTotal(label: String, amount: Long) {
    SurfaceCard {
        Text(label, color = Muted, fontSize = 12.sp)
        Text(Money.format(amount), fontSize = 28.sp, color = Purple, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Hint(text: String) {
    Text(text, color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
}

@Composable
fun AppButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}
