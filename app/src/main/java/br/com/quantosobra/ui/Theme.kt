package br.com.quantosobra.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple = Color(0xFF7219B8)
val DeepPurple = Color(0xFF3D0B66)
val Lilac = Color(0xFFF0E6FA)
val Background = Color(0xFFF8F6FA)
val Ink = Color(0xFF21182A)
val Muted = Color(0xFF796F82)
val Positive = Color(0xFF14785E)
val Negative = Color(0xFFB43651)

private val palette = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = Lilac,
    onPrimaryContainer = DeepPurple,
    background = Background,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF1EDF4),
    onSurfaceVariant = Muted
)

@Composable
fun QuantoSobraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = palette, content = content)
}
