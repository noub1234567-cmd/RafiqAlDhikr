package com.rafiqaldhikr.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rafiqaldhikr.app.R

/**
 * Amiri (SIL OFL, see docs/licenses) for every piece of text in the app: a classical Naskh face that reads well for
 * adhkar and Quranic text. Amiri has tall ascenders and a small x-height, so line heights are generous.
 */
val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_regular, FontWeight.Medium),
    Font(R.font.amiri_bold, FontWeight.SemiBold),
    Font(R.font.amiri_bold, FontWeight.Bold)
)

private fun amiri(weight: FontWeight, size: Int, line: Int) = TextStyle(
    fontFamily = AmiriFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp
)

val RafiqTypography = Typography(
    displayLarge = amiri(FontWeight.Bold, 40, 56),
    displayMedium = amiri(FontWeight.Bold, 34, 48),
    displaySmall = amiri(FontWeight.Bold, 29, 42),
    headlineLarge = amiri(FontWeight.Bold, 28, 40),
    headlineMedium = amiri(FontWeight.Bold, 24, 36),
    headlineSmall = amiri(FontWeight.Bold, 21, 32),
    titleLarge = amiri(FontWeight.Bold, 21, 32),
    titleMedium = amiri(FontWeight.Bold, 18, 28),
    titleSmall = amiri(FontWeight.Bold, 16, 26),
    bodyLarge = amiri(FontWeight.Normal, 20, 36),
    bodyMedium = amiri(FontWeight.Normal, 16, 28),
    bodySmall = amiri(FontWeight.Normal, 14, 24),
    labelLarge = amiri(FontWeight.Bold, 16, 24),
    labelMedium = amiri(FontWeight.Normal, 14, 20),
    labelSmall = amiri(FontWeight.Normal, 12, 18)
)
