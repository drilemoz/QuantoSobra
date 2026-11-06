package br.com.quantosobra.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.Budget
import br.com.quantosobra.domain.TransportConfig
import br.com.quantosobra.domain.TransportMode

private const val DEVELOPER_EMAIL = "drilemoz@gmail.com"

@Composable
private fun SettingsItem(symbol: String, title: String, subtitle: String, dangerous: Boolean = false, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.White, shape = RoundedCornerShape(19.dp),
        modifier = Modifier.fillMaxWidth(), shadowElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                .background(if (dangerous) Color(0xFFFCE8ED) else Lilac), contentAlignment = Alignment.Center) {
                Text(symbol, color = if (dangerous) Negative else Purple, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = if (dangerous) Negative else Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text("›", color = Muted, fontSize = 24.sp)
        }
    }
}

@Composable
fun SettingsScreen(
    onDismiss: () -> Unit,
    onRestartWizard: () -> Unit,
    onLoadExample: (Budget) -> Unit,
    onClearData: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenAbout: () -> Unit
) {
    var exampleDialog by remember { mutableStateOf(false) }
    var clearDialog by remember { mutableStateOf(false) }
    Page("Configurações", "Seu espaço, suas escolhas", onDismiss) {
        SurfaceCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(49.dp).clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(listOf(Purple, DeepPurple))),
                    contentAlignment = Alignment.Center) {
                    Text("✦", color = Color.White, fontSize = 26.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Quanto Sobra?", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Seu dinheiro, sem surpresas.", color = Muted, fontSize = 12.sp)
                }
            }
        }
        Text("MEU ORÇAMENTO", color = Purple, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        SettingsItem("↻", "Refazer configuração", "Abra novamente o assistente sem perder os dados até concluir") {
            onRestartWizard()
        }
        SettingsItem("▣", "Carregar exemplo", "Experimente uma simulação de R$ 3.000") {
            exampleDialog = true
        }
        SettingsItem("×", "Limpar meus dados", "Apaga o orçamento e inicia a configuração novamente", dangerous = true) { clearDialog = true }
        Text("O APLICATIVO", color = Purple, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        SettingsItem("✧", "Novidades", "Veja o que mudou nas atualizações") { onOpenNews() }
        SettingsItem("ⓘ", "Sobre o aplicativo", "Versão, histórico e contato do desenvolvedor") { onOpenAbout() }
        Hint("Seus dados financeiros continuam somente neste dispositivo. Não é necessário login.")
    }
    if (exampleDialog) ModernDialog("Carregar simulação?", { exampleDialog = false }, "Substituir", {
        onLoadExample(Budget(
            netSalaryCents = 300_000, rentCents = 135_000, foodOutsideCents = 80_000,
            electricityCents = 8_000, waterCents = 6_000, internetCents = 10_000,
            mobileCents = 3_000,
            transport = TransportConfig(fareCents = 570, ridesPerDay = 2, daysPerMonth = 22,
                mode = TransportMode.CLT, basicSalaryCents = 300_000)
        ))
        exampleDialog = false
    }) {
        Hint("O exemplo substituirá todos os valores, produtos e despesas atuais. Esta ação não poderá ser desfeita.")
    }
    if (clearDialog) ModernDialog("Limpar dados e recomeçar?", { clearDialog = false }, "Limpar e reiniciar", {
        clearDialog = false
        onClearData()
    }, destructive = true) {
        Hint("Seu orçamento será apagado deste aparelho e o assistente de primeiro acesso será aberto. Esta ação não poderá ser desfeita.")
    }
}

@Composable
fun ReleaseItems(limit: Int = 3) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        ReleaseCatalog.recent.take(limit).forEachIndexed { index, item ->
            Surface(color = Color.White, shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, Color(0xFFF0E9F4))) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(37.dp).clip(RoundedCornerShape(12.dp)).background(Lilac),
                        contentAlignment = Alignment.Center) {
                        Text(if (index == 0) "✦" else "✓", color = Purple, fontSize = 20.sp)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.headline, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${item.version}  •  ${item.date}", color = Purple, fontSize = 11.sp)
                        Text(item.features.joinToString("  •  ") { it.detail }, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
        }
    }
}

/** Tela de novidades compartilhada: automaticamente após update ou pelo menu Configurações. */
@Composable
fun WhatsNewScreen(onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Background)) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF8025DB), Purple, DeepPurple)))) {
            Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 30.dp, bottom = 26.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("✦", color = Color(0xFFE7C6FF), fontSize = 28.sp, modifier = Modifier.weight(1f))
                    Text(ReleaseCatalog.currentVersion.uppercase(), color = Color(0xFFEAD7F8), fontSize = 10.sp)
                }
                Text("O Quanto Sobra? evoluiu.", color = Color.White,
                    fontWeight = FontWeight.ExtraBold, fontSize = 29.sp, lineHeight = 33.sp)
                Text("Uma experiência mais completa para organizar seu dinheiro.",
                    color = Color(0xFFEAD7F8), fontSize = 13.sp)
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())
            .padding(horizontal = 19.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("NOVIDADES DA ATUALIZAÇÃO", fontWeight = FontWeight.ExtraBold, color = Purple,
                fontSize = 11.sp, letterSpacing = 1.sp)
            val current = ReleaseCatalog.recent.first()
            current.features.forEachIndexed { index, feature ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(Lilac),
                        contentAlignment = Alignment.Center) {
                        Text(ReleaseCatalog.featureIcon(index), color = Purple, fontSize = 23.sp)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(feature.title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(feature.detail, color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
            Text("${current.version}  •  ${current.date}", color = Muted, fontSize = 11.sp)
        }
        Box(Modifier.fillMaxWidth().background(Background).padding(horizontal = 19.dp, vertical = 15.dp)) {
            AppButton("Começar a usar   →", onClick = onContinue)
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit, onOpenNews: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    Page("Sobre", "Informações e últimas atualizações", onBack) {
        SurfaceCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(67.dp).clip(RoundedCornerShape(21.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF852BD5), DeepPurple))),
                    contentAlignment = Alignment.Center) {
                    Text("✦", color = Color.White, fontSize = 33.sp)
                }
                Spacer(Modifier.width(13.dp))
                Column {
                    Text("Quanto Sobra?", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    Text(ReleaseCatalog.currentVersion, color = Purple, fontSize = 12.sp)
                    Text("Seu dinheiro, sem surpresas.", color = Muted, fontSize = 12.sp)
                }
            }
        }
        Text("ÚLTIMAS ATUALIZAÇÕES", color = Purple, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        ReleaseItems(limit = 3)
        OutlinedButton(onClick = onOpenNews, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)) { Text("Rever tela de novidades") }
        Text("DESENVOLVEDOR", color = Purple, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        SurfaceCard {
            Text("Adriedson Lemos", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(DEVELOPER_EMAIL, color = Muted, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(DEVELOPER_EMAIL)) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Copiar e-mail") }
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$DEVELOPER_EMAIL?subject=Quanto%20Sobra"))
                    try { context.startActivity(intent) } catch (_: ActivityNotFoundException) { /* Sem app de e-mail */ }
                }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("Contato") }
            }
        }
        Hint("Aplicativo offline. Nenhum dado financeiro é enviado para o desenvolvedor.")
    }
}
