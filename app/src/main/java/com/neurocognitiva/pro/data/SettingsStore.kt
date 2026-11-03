package com.neurocognitiva.pro.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Preferencias livianas: paciente activo y consentimiento para usar IA externa. */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _activePatientId = MutableStateFlow(prefs.getLong(KEY_ACTIVE, -1L))
    val activePatientId: StateFlow<Long> = _activePatientId.asStateFlow()

    private val _aiConsent = MutableStateFlow(prefs.getBoolean(KEY_AI, false))
    val aiConsent: StateFlow<Boolean> = _aiConsent.asStateFlow()

    fun setActivePatient(id: Long) {
        prefs.edit().putLong(KEY_ACTIVE, id).apply()
        _activePatientId.value = id
    }

    fun setAiConsent(value: Boolean) {
        prefs.edit().putBoolean(KEY_AI, value).apply()
        _aiConsent.value = value
    }

    private companion object {
        const val KEY_ACTIVE = "active_patient"
        const val KEY_AI = "ai_consent"
    }
}
