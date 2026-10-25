package com.cubetimerpro.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Orange = Color(0xFFFF7900)
val BrightOrange = Color(0xFFFF941F)
val DeepOrange = Color(0xFFE56700)
val SoftOrange = Color(0xFFFFF1E5)
val Ink = Color(0xFF171717)
val Gray = Color(0xFF737373)
val Border = Color(0xFFE9E9EB)
val SuccessGreen = Color(0xFF16834A)
val ErrorRed = Color(0xFFD93636)

private val Light = lightColorScheme(
    primary = Orange, onPrimary = Color.White, primaryContainer = SoftOrange, onPrimaryContainer = DeepOrange,
    secondary = BrightOrange, background = Color.White, onBackground = Ink, surface = Color.White, onSurface = Ink,
    surfaceVariant = Color(0xFFF7F7F8), onSurfaceVariant = Gray, outline = Border, error = ErrorRed
)
private val Dark = darkColorScheme(
    primary = BrightOrange, onPrimary = Color(0xFF1A1A1A), primaryContainer = Color(0xFF3A2410), onPrimaryContainer = Color(0xFFFFB878),
    secondary = Orange, background = Color(0xFF121212), onBackground = Color(0xFFF2F2F2), surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFF2F2F2), surfaceVariant = Color(0xFF26262A), onSurfaceVariant = Color(0xFFA0A0A8),
    outline = Color(0xFF3A3A3F), error = Color(0xFFFF6B6B)
)

@Composable
fun AppTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
