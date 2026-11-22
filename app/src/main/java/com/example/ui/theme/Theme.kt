package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val FixNearDarkColorScheme =
  darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DispatchAmberLight,
    onSecondary = Color(0xFF2B1000),
    secondaryContainer = Color(0xFF522408),
    onSecondaryContainer = Color(0xFFFFDBCC),
    tertiary = EmeraldLive,
    onTertiary = Color(0xFF002116),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = EmeraldContainer,
    background = DarkBackground,
    onBackground = LightInk,
    surface = DarkSurface,
    onSurface = LightInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkMutedSlate,
    outline = DarkOutline,
    outlineVariant = Color(0xFF1D332D),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
  )

private val FixNearLightColorScheme =
  lightColorScheme(
    primary = DeepTeal,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = DispatchAmber,
    onSecondary = Color.White,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    tertiary = EmeraldLive,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldContainer,
    onTertiaryContainer = OnEmeraldContainer,
    background = LightBackground,
    onBackground = DarkInk,
    surface = LightSurface,
    onSurface = DarkInk,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = MutedSlate,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = ErrorCrimson,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = Color(0xFF7F1D1D),
  )

val FixNearShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun FixNearTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) FixNearDarkColorScheme else FixNearLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = FixNearTypography,
    shapes = FixNearShapes,
    content = content,
  )
}
