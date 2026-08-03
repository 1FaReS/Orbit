package com.flowtask.app.domain.model

import java.time.LocalTime

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val workdayStart: LocalTime = LocalTime.of(9, 0),
    val workdayEnd: LocalTime = LocalTime.of(17, 0),
    val peakEnergyPeriod: EnergyPeriod = EnergyPeriod.MORNING,
    val focusDurationMinutes: Int = 25,
    val notificationsEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = false,
    val sampleDataSeeded: Boolean = false,
)

enum class EnergyPeriod { MORNING, AFTERNOON, EVENING }
enum class ThemeMode { LIGHT, DARK, SYSTEM }
