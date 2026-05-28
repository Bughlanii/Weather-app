package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NaturalLightColorScheme = lightColorScheme(
  primary = NaturalOlive,
  onPrimary = White,
  primaryContainer = NaturalSageLight,
  onPrimaryContainer = NaturalTextDark,
  secondary = NaturalSandDark,
  onSecondary = White,
  secondaryContainer = NaturalSandLight,
  onSecondaryContainer = NaturalTextDark,
  tertiary = NaturalSandDark,
  onTertiary = White,
  tertiaryContainer = NaturalCreamLight,
  onTertiaryContainer = NaturalSandDark,
  background = NaturalBackground,
  onBackground = NaturalTextDark,
  surface = White,
  onSurface = NaturalTextDark,
  surfaceVariant = NaturalCreamLight,
  onSurfaceVariant = NaturalOlive,
  outline = NaturalSageBorder,
  outlineVariant = NaturalSandBorder
)

private val NaturalDarkColorScheme = darkColorScheme(
  primary = NaturalSageLight,
  onPrimary = NaturalTextDark,
  primaryContainer = NaturalOlive,
  onPrimaryContainer = NaturalBackground,
  secondary = NaturalSandLight,
  onSecondary = NaturalTextDark,
  secondaryContainer = NaturalSandDark,
  onSecondaryContainer = NaturalBackground,
  background = Color(0xFF1D1B16), // NaturalTextDark as dark background
  onBackground = NaturalBackground,
  surface = Color(0xFF2C2A24),
  onSurface = NaturalBackground,
  surfaceVariant = Color(0xFF3C3A34),
  onSurfaceVariant = NaturalSageLight,
  outline = NaturalSageBorder,
  outlineVariant = NaturalSandBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Disable dynamic colors by default to strictly enforce Natural Tones vibe!
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) NaturalDarkColorScheme else NaturalLightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
