package com.saif.nativeandroidlab.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = NightTraceBlue,
    onPrimary = Graphite,
    primaryContainer = NightTraceBlueContainer,
    onPrimaryContainer = NightInk,
    secondary = NightVerifiedGreen,
    onSecondary = Graphite,
    secondaryContainer = NightVerifiedGreenContainer,
    onSecondaryContainer = NightInk,
    tertiary = NightProbeOrange,
    onTertiary = Graphite,
    tertiaryContainer = NightProbeOrangeContainer,
    onTertiaryContainer = NightInk,
    background = NightLab,
    onBackground = NightInk,
    surface = NightPaper,
    onSurface = NightInk,
    surfaceVariant = NightSurfaceSubtle,
    onSurfaceVariant = NightInk,
    outline = NightGridLine,
    outlineVariant = NightSurfaceSubtle,
    error = NightCriticalRed,
    onError = Graphite,
    errorContainer = NightCriticalRedContainer,
    onErrorContainer = NightInk,
    inverseSurface = LabMist,
    inverseOnSurface = Graphite,
    inversePrimary = TraceBlue,
    scrim = Color.Black,
)

private val LightColorScheme = lightColorScheme(
    primary = TraceBlue,
    onPrimary = Paper,
    primaryContainer = TraceBlueContainer,
    onPrimaryContainer = Graphite,
    secondary = VerifiedGreen,
    onSecondary = Paper,
    secondaryContainer = VerifiedGreenContainer,
    onSecondaryContainer = Graphite,
    tertiary = ProbeOrange,
    onTertiary = Paper,
    tertiaryContainer = ProbeOrangeContainer,
    onTertiaryContainer = Graphite,
    background = LabMist,
    onBackground = Graphite,
    surface = Paper,
    onSurface = Graphite,
    surfaceVariant = SurfaceSubtle,
    onSurfaceVariant = Graphite,
    outline = GridLine,
    outlineVariant = SurfaceSubtle,
    error = CriticalRed,
    onError = Paper,
    errorContainer = CriticalRedContainer,
    onErrorContainer = Graphite,
    inverseSurface = Graphite,
    inverseOnSurface = Paper,
    inversePrimary = NightTraceBlue,
    scrim = Color.Black,
)

@Composable
fun NativeAndroidLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
