package com.sarah.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.sarah.app.domain.model.EnergyLevel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

interface SarahPreferences {
    var currentEnergyLevel: EnergyLevel
    val energyLevelFlow: Flow<EnergyLevel>
    var isOnboardingCompleted: Boolean
    val onboardingCompletedFlow: Flow<Boolean>
}

class SarahPreferencesManager(context: Context) : SarahPreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences("sarah_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_ENERGY = "key_current_energy"
        private const val KEY_ONBOARDING_DONE = "key_onboarding_done"
    }

    override var currentEnergyLevel: EnergyLevel
        get() {
            val name = prefs.getString(KEY_CURRENT_ENERGY, EnergyLevel.NORMAL.name) ?: EnergyLevel.NORMAL.name
            return runCatching { EnergyLevel.valueOf(name) }.getOrDefault(EnergyLevel.NORMAL)
        }
        set(value) {
            prefs.edit().putString(KEY_CURRENT_ENERGY, value.name).apply()
        }

    override val energyLevelFlow: Flow<EnergyLevel> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_CURRENT_ENERGY || key == null) {
                trySend(currentEnergyLevel)
            }
        }
        trySend(currentEnergyLevel)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    override val onboardingCompletedFlow: Flow<Boolean> = callbackFlow {
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
