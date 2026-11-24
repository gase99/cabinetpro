package com.cabinetpro.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = GoldLight,
    secondary = Gold,
    background = Background,
    surface = Surface,
    onBackground = Navy,
    onSurface = Navy
)

@Composable
fun CabinetProTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
