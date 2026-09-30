package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
  lightColorScheme(
    primary = NixtaTerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = NixtaTerracottaContainer,
    onPrimaryContainer = NixtaOnTerracottaContainer,
    secondary = NixtaTerracottaPressed,
    background = NixtaBackgroundCream,
    onBackground = NixtaTextPrimary,
    surface = NixtaSurfaceCream,
    onSurface = NixtaTextPrimary,
    surfaceVariant = NixtaSurfaceCream,
    outline = NixtaSurfaceBorder,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = NixtaTerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = NixtaDarkGradientStart,
    onPrimaryContainer = NixtaDarkTextPrimary,
    secondary = NixtaTerracottaPressed,
    background = NixtaDarkBackground,
    onBackground = NixtaDarkTextPrimary,
    surface = NixtaDarkSurface,
    onSurface = NixtaDarkTextPrimary,
    surfaceVariant = NixtaDarkSurface,
    outline = NixtaDarkSurfaceBorder,
  )

@Composable
fun NixtaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colors = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colors,
    typography = Typography,
    content = content
  )
}
