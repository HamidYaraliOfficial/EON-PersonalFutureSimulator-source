package com.eon.futuresimulator.core.theme

import androidx.compose.ui.graphics.Color

/**
 * EON palette.
 *
 * Four selectable palettes are provided (Windows-11 inspired "Fluent" look):
 *   - Light   : default light Windows-11 style (Mica-like soft neutrals + accent blue)
 *   - Dark    : Windows-11 dark mode
 *   - AmoledBlue : pure-black AMOLED variant of the blue accent
 *   - Red     : alternate accent theme
 *   - Blue    : alternate high-contrast accent theme (deep royal blue)
 */

// Neutral / surface tones (Fluent-like)
val NeutralLight50 = Color(0xFFFAFAFC)
val NeutralLight100 = Color(0xFFF3F3F7)
val NeutralLight200 = Color(0xFFE7E7EE)
val NeutralLight300 = Color(0xFFD6D6E0)
val NeutralDark900 = Color(0xFF0B0B10)
val NeutralDark800 = Color(0xFF16161D)
val NeutralDark700 = Color(0xFF202028)
val NeutralDark600 = Color(0xFF2B2B35)

// Accent — Windows blue
val AccentBluePrimary = Color(0xFF0067C0)
val AccentBlueLight = Color(0xFF3A96DD)
val AccentBlueDark = Color(0xFF00396E)

// Accent — Red theme
val AccentRedPrimary = Color(0xFFC42B1C)
val AccentRedLight = Color(0xFFE74856)
val AccentRedDark = Color(0xFF7A0000)

// Accent — deep royal Blue theme (distinct from default Windows blue)
val AccentRoyalPrimary = Color(0xFF2B4CD1)
val AccentRoyalLight = Color(0xFF5C7CFA)
val AccentRoyalDark = Color(0xFF16267A)

// Semantic colors — used across Feasibility / Risk / Confidence widgets
val FeasibleGreen = Color(0xFF2E9E5B)
val ChallengingAmber = Color(0xFFC98C00)
val UnrealisticRed = Color(0xFFD1342E)

val RiskLow = Color(0xFF2E9E5B)
val RiskMedium = Color(0xFFC98C00)
val RiskHigh = Color(0xFFE0562C)
val RiskCritical = Color(0xFFD1342E)

// Dual Reality Mode
val RealDataColor = Color(0xFF2E9E5B)
val SimulatedDataColor = Color(0xFF7C4DFF)

// Confidence band fill (semi transparent accent)
val ConfidenceBandLight = Color(0x330067C0)
val ConfidenceBandDark = Color(0x333A96DD)
