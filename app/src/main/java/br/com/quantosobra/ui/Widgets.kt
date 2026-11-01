package br.com.quantosobra.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.quantosobra.domain.Money

@Composable
fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(25.dp),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(0.5.dp, Color(0xFFE8E0EF))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun FieldAmount(label: String, cents: Long, onChange: (Long) -> Unit, hint: String? = null,
    compact: Boolean = false, externalValueRevision: Int = 0) {
    var editor by remember { mutableStateOf(TextFieldValue(if (cents == 0L) "" else Money.inputGrouped(cents))) }
    var hasFocus by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    // Não substitui o texto enquanto o usuário digita; mantém os dados externos em sincronia.
    LaunchedEffect(cents) {
        if (!hasFocus && Money.parse(editor.text) != cents) {
            val shown = if (cents == 0L) "" else Money.inputGrouped(cents)
            editor = TextFieldValue(shown, TextRange(shown.length))
        }
    }
    // Ao tocar em "Usar" a edição pode continuar com foco (botões não necessariamente o removem).
    // Esse sinal externo atualiza o texto sem reformatar a cada tecla digitada normalmente.
    LaunchedEffect(externalValueRevision) {
        val shown = if (cents == 0L) "" else Money.inputGrouped(cents)
        editor = TextFieldValue(shown, TextRange(shown.length))
    }
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 1.dp else 5.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
                if (hasFocus != focus.isFocused) {
                    hasFocus = focus.isFocused
                    if (!focus.isFocused) {
                        val shown = if (cents == 0L) "" else Money.inputGrouped(cents)
                        editor = TextFieldValue(shown, TextRange(shown.length))
                    }
                }
            },
            value = editor,
            onValueChange = { newValue ->
                if (newValue.text.length <= 22 && Money.isValidMoneyDraft(newValue.text)) {
                    // Preserva a seleção, o toque no meio do número e a composição do IME.
                    // Pontos de milhar só aparecem ao sair do campo.
                    editor = newValue
                    Money.parse(newValue.text)?.let(onChange)
                }
            },
            label = { Text(label, fontSize = if (compact) 12.sp else 14.sp) },
            prefix = { Text("R$ ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
            supportingText = if (compact) null else ({ Text("Ex.: 12,00 ou 12.00", fontSize = 10.sp) }),
            isError = Money.parse(editor.text) == null,
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE6DDEA),
                focusedBorderColor = Purple
            )
        )
        if (hint != null) Hint(hint)
    }
}

/** Entrada monetária temporária: texto e cursor ficam sob controle do teclado durante a edição. */
@Composable
fun CurrencyTextEntry(label: String, value: String, onChange: (String) -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    var focused by remember { mutableStateOf(false) }
    var editor by remember { mutableStateOf(TextFieldValue(value)) }
    LaunchedEffect(value) {
        if (value != editor.text) editor = TextFieldValue(value, TextRange(value.length))
    }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().onFocusChanged { focus ->
            if (focused != focus.isFocused) {
                focused = focus.isFocused
                if (!focused) Money.parse(editor.text)?.let { cents ->
                    val shown = Money.inputGrouped(cents)
                    editor = TextFieldValue(shown, TextRange(shown.length))
                    onChange(shown)
                }
            }
        },
        value = editor,
        onValueChange = { new ->
            if (new.text.length <= 22 && Money.isValidMoneyDraft(new.text)) {
                editor = new
                onChange(new.text)
            }
        },
        label = { Text(label) },
        prefix = { Text("R$ ") },
        isError = Money.parse(editor.text) == null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
        shape = RoundedCornerShape(15.dp)
    )
}

@Composable
fun FieldCount(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    var input by remember { mutableStateOf(value.toString()) }
    LaunchedEffect(value) {
        if (input.toIntOrNull() != value) input = value.toString()
    }
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
        shape = RoundedCornerShape(15.dp)
    )
}

@Composable
fun ValueLine(label: String, amount: Long, emphasize: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f), color = if (emphasize) Ink else Muted,
            fontSize = 13.sp, fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal)
        Spacer(Modifier.width(10.dp))
        Text(Money.format(amount), color = Ink, fontSize = 14.sp,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium)
    }
}

/** Painel flutuante, em vez de uma página de navegação. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    ModalBottomSheet(
        onDismissRequest = onBack,
        sheetState = state,
        containerColor = Background,
        contentColor = Ink,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFCCBADB)) }
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = screenHeight * 0.80f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF8C29D6), DeepPurple)))) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 17.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("✦  SEU ORÇAMENTO", fontSize = 10.sp, color = Color(0xFFE8C9FF),
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(title, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(subtitle, fontSize = 12.sp, color = Color(0xFFE9D8F6))
                }
                Surface(
                    onClick = onBack, modifier = Modifier.size(42.dp).semantics { contentDescription = "Fechar painel" }, shape = CircleShape,
                    color = Color.White.copy(alpha = .18f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("×", fontSize = 25.sp, color = Color.White)
                    }
                }
                }
            }
            Spacer(Modifier.height(2.dp))
            content()
        }
    }
}

@Composable
fun SectionTotal(label: String, amount: Long) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(listOf(Purple, DeepPurple)))
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = Color(0xFFE8D5FA), fontSize = 12.sp)
            Text(Money.format(amount), fontSize = 27.sp, color = Color.White,
                fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun Hint(text: String) {
    Text(text, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
}

@Composable
fun AppButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth().height(49.dp),
        shape = RoundedCornerShape(16.dp)) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

/** Um mesmo padrão visual para edição, confirmação e cadastro de itens. */
@Composable
fun ModernDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.82f
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                .heightIn(max = maxHeight),
            shape = RoundedCornerShape(28.dp), color = Color.White, shadowElevation = 12.dp
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(title, color = Ink, fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold)
                    }
                    Surface(onClick = onDismiss, shape = CircleShape, color = Lilac,
                        modifier = Modifier.size(36.dp).semantics { contentDescription = "Fechar janela" }) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("×", fontSize = 22.sp, color = DeepPurple)
                        }
                    }
                }
                content()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(15.dp)) { Text("Cancelar") }
                    Button(onClick = onConfirm, enabled = confirmEnabled,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (destructive) Negative else Purple)
                    ) { Text(confirmText) }
                }
            }
        }
    }
}
