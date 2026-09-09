package com.afomsteam.enlistedplanner.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF071A2B)
val Navy2 = Color(0xFF0B2944)
val CardBlue = Color(0xFF123A5A)
val AirBlue = Color(0xFF63B3ED)
val SoftBlue = Color(0xFFB9DCF5)
val TextMuted = Color(0xFFADC4D5)
val Good = Color(0xFF65D18C)
val Warn = Color(0xFFFFC857)
val Critical = Color(0xFFFF6B6B)

private val PlannerColors = darkColorScheme(
    primary = AirBlue,
    onPrimary = Navy,
    secondary = SoftBlue,
    background = Navy,
    surface = Navy2,
    surfaceVariant = CardBlue,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = SoftBlue,
    error = Critical
)

@Composable
fun EnlistedPlannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PlannerColors, content = content)
}
