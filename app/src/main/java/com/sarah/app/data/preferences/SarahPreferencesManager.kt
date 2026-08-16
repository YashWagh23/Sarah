package com.sarah.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.preferences.SarahPreferences as DomainSarahPreferences
import com.sarah.app.preferences.AndroidSarahPreferences
import kotlinx.coroutines.flow.Flow

typealias SarahPreferences = DomainSarahPreferences

class SarahPreferencesManager(
    private val delegate: AndroidSarahPreferences
) : DomainSarahPreferences by delegate {

    constructor(context: Context) : this(
        AndroidSarahPreferences(context)
    )

    constructor(prefs: SharedPreferences) : this(
        AndroidSarahPreferences(prefs)
    )
}
