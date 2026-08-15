package com.sarah.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.sarah.app.domain.model.EnergyLevel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class SarahPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sarah_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_ENERGY = "key_current_energy"
        private const val KEY_ONBOARDING_DONE = "key_onboarding_done"
    }

    var currentEnergyLevel: EnergyLevel
        get() {
            val name = prefs.getString(KEY_CURRENT_ENERGY, EnergyLevel.NORMAL.name) ?: EnergyLevel.NORMAL.name
            return runCatching { EnergyLevel.valueOf(name) }.getOrDefault(EnergyLevel.NORMAL)
        }
        set(value) {
            prefs.edit().putString(KEY_CURRENT_ENERGY, value.name).apply()
        }

    val energyLevelFlow: Flow<EnergyLevel> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_CURRENT_ENERGY || key == null) {
                trySend(currentEnergyLevel)
            }
        }
        trySend(currentEnergyLevel)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    val onboardingCompletedFlow: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_ONBOARDING_DONE || key == null) {
                trySend(isOnboardingCompleted)
            }
        }
        trySend(isOnboardingCompleted)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()
}
