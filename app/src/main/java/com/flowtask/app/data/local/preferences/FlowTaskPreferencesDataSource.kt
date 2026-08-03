package com.flowtask.app.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flowtask.app.domain.model.EnergyPeriod
import com.flowtask.app.domain.model.ThemeMode
import com.flowtask.app.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.flowTaskDataStore by preferencesDataStore(name = "flowtask_preferences")

@Singleton
class FlowTaskPreferencesDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val preferences: Flow<UserPreferences> = context.flowTaskDataStore.data.map(Preferences::toDomain)

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        val updated = transform(preferences.first())
        context.flowTaskDataStore.edit { values -> values.write(updated) }
    }
}

private object PreferenceKeys {
    val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    val workdayStart = stringPreferencesKey("workday_start")
    val workdayEnd = stringPreferencesKey("workday_end")
    val peakEnergyPeriod = stringPreferencesKey("peak_energy_period")
    val focusDurationMinutes = intPreferencesKey("focus_duration_minutes")
    val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
    val themeMode = stringPreferencesKey("theme_mode")
    val dynamicColorEnabled = booleanPreferencesKey("dynamic_color_enabled")
    val sampleDataSeeded = booleanPreferencesKey("sample_data_seeded")
}

private fun Preferences.toDomain() = UserPreferences(
    onboardingCompleted = this[PreferenceKeys.onboardingCompleted] ?: false,
    workdayStart = this[PreferenceKeys.workdayStart]?.let(LocalTime::parse) ?: LocalTime.of(9, 0),
    workdayEnd = this[PreferenceKeys.workdayEnd]?.let(LocalTime::parse) ?: LocalTime.of(17, 0),
    peakEnergyPeriod = this[PreferenceKeys.peakEnergyPeriod]?.let(EnergyPeriod::valueOf) ?: EnergyPeriod.MORNING,
    focusDurationMinutes = this[PreferenceKeys.focusDurationMinutes] ?: 25,
    notificationsEnabled = this[PreferenceKeys.notificationsEnabled] ?: false,
    themeMode = this[PreferenceKeys.themeMode]?.let(ThemeMode::valueOf) ?: ThemeMode.SYSTEM,
    dynamicColorEnabled = this[PreferenceKeys.dynamicColorEnabled] ?: false,
    sampleDataSeeded = this[PreferenceKeys.sampleDataSeeded] ?: false,
)

private fun androidx.datastore.preferences.core.MutablePreferences.write(value: UserPreferences) {
    this[PreferenceKeys.onboardingCompleted] = value.onboardingCompleted
    this[PreferenceKeys.workdayStart] = value.workdayStart.toString()
    this[PreferenceKeys.workdayEnd] = value.workdayEnd.toString()
    this[PreferenceKeys.peakEnergyPeriod] = value.peakEnergyPeriod.name
    this[PreferenceKeys.focusDurationMinutes] = value.focusDurationMinutes
    this[PreferenceKeys.notificationsEnabled] = value.notificationsEnabled
    this[PreferenceKeys.themeMode] = value.themeMode.name
    this[PreferenceKeys.dynamicColorEnabled] = value.dynamicColorEnabled
    this[PreferenceKeys.sampleDataSeeded] = value.sampleDataSeeded
}
