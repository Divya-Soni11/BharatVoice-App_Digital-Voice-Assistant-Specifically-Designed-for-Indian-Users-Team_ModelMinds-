package com.runanywhere.startup_hackathon20.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// VentureVault Color Palette
object VVColors {
    // Primary Colors
    val Primary = Color(0xFF2563EB) // Blue
    val PrimaryDark = Color(0xFF1E40AF)
    val PrimaryLight = Color(0xFF60A5FA)
    val PrimaryContainer = Color(0xFFDCEBFF)

    // Secondary Colors
    val Secondary = Color(0xFFF59E0B) // Amber
    val SecondaryDark = Color(0xFFD97706)
    val SecondaryLight = Color(0xFFFBBF24)
    val SecondaryContainer = Color(0xFFFEF3C7)

    // Success, Error, Warning, Info
    val Success = Color(0xFF10B981)
    val SuccessLight = Color(0xFFD1FAE5)
    val Error = Color(0xFFEF4444)
    val ErrorLight = Color(0xFFFEE2E2)
    val Warning = Color(0xFFF59E0B)
    val WarningLight = Color(0xFFFEF3C7)
    val Info = Color(0xFF3B82F6)
    val InfoLight = Color(0xFFDBEAFE)

    // Neutral Colors
    val Gray50 = Color(0xFFF9FAFB)
    val Gray100 = Color(0xFFF3F4F6)
    val Gray200 = Color(0xFFE5E7EB)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray500 = Color(0xFF6B7280)
    val Gray600 = Color(0xFF4B5563)
    val Gray700 = Color(0xFF374151)
    val Gray800 = Color(0xFF1F2937)
    val Gray900 = Color(0xFF111827)

    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    // Surface Colors
    val Surface = Color(0xFFFFFFFF)
    val SurfaceDim = Color(0xFFF9FAFB)
    val Background = Color(0xFFFAFAFA)
}

// Gradient Presets
object VVGradients {
    val PrimaryGradient = Brush.verticalGradient(
        colors = listOf(
            VVColors.Primary,
            VVColors.PrimaryDark
        )
    )

    val PrimaryHorizontal = Brush.horizontalGradient(
        colors = listOf(
            VVColors.Primary,
            VVColors.PrimaryLight
        )
    )

    val AccentGradient = Brush.verticalGradient(
        colors = listOf(
            VVColors.Secondary,
            VVColors.SecondaryDark
        )
    )

    val SoftGradient = Brush.verticalGradient(
        colors = listOf(
            VVColors.PrimaryContainer,
            VVColors.White
        )
    )

    val CardGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF667EEA),
            Color(0xFF764BA2)
        )
    )
}

// Spacing System
object VVSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

// Border Radius
object VVRadius {
    val sm = 4.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val xxl = 24.dp
    val full = 9999.dp
}

// Elevation
object VVElevation {
    val none = 0.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
    val xl = 16.dp
}
