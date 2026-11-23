package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.TextFieldColors
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

@Composable
fun greenThemeTextFieldColors(highlighted: Boolean = false): TextFieldColors {
  val primary = MaterialTheme.colorScheme.primary
  val highlightBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
  val restingBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f)
  return OutlinedTextFieldDefaults.colors(
    focusedContainerColor = highlightBg,
    unfocusedContainerColor = if (highlighted) highlightBg else restingBg,
    focusedBorderColor = primary,
    unfocusedBorderColor = if (highlighted) primary else primary.copy(alpha = 0.62f),
    focusedLabelColor = primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLeadingIconColor = primary,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTrailingIconColor = primary,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
  )
}

@Composable
fun blueLoginTextFieldColors(): TextFieldColors =
  OutlinedTextFieldDefaults.colors(
    focusedContainerColor = AuthLoginBlueHighlight,
    unfocusedContainerColor = AuthLoginBlueBg,
    focusedBorderColor = AuthLoginBlue,
    unfocusedBorderColor = AuthLoginBlue.copy(alpha = 0.65f),
    focusedLabelColor = AuthLoginBlue,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLeadingIconColor = AuthLoginBlue,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTrailingIconColor = AuthLoginBlue,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = AuthLoginBlue,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
  )
