package com.neurocognitiva.pro.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neurocognitiva.pro.NeuroApp
import com.neurocognitiva.pro.data.PatientEntity
import com.neurocognitiva.pro.data.SessionEntity
import com.neurocognitiva.pro.domain.Battery
import com.neurocognitiva.pro.domain.BatteryState
import com.neurocognitiva.pro.domain.DemoData
import com.neurocognitiva.pro.domain.ReportBuilder
import com.neurocognitiva.pro.domain.ReportData
import com.neurocognitiva.pro.report.AiClient
import com.neurocognitiva.pro.report.CsvExporter
import com.neurocognitiva.pro.report.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as NeuroApp
    private val repo = app.repository
    private val settings = app.settings

    val patients: StateFlow<List<PatientEntity>> =
        repo.patients.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activePatientId: StateFlow<Long> = settings.activePatientId
    val aiConsent: StateFlow<Boolean> = settings.aiConsent

    val activePatient: StateFlow<PatientEntity?> =
        combine(patients, activePatientId) { list, id -> list.firstOrNull { it.id == id } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sessions: StateFlow<List<SessionEntity>> =
        activePatientId.flatMapLatest { repo.sessions(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val report: StateFlow<ReportData?> =
        combine(activePatient, sessions) { patient, list -> patient?.let { ReportBuilder.build(it, list) } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _battery = MutableStateFlow<BatteryState?>(null)
    val battery: StateFlow<BatteryState?> = _battery.asStateFlow()

    private val _aiText = MutableStateFlow<String?>(null)
    val aiText: StateFlow<String?> = _aiText.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        // Mantiene siempre un paciente activo válido (se lee el Flow de Room directamente para
        // no reaccionar al valor inicial vacío del StateFlow).
        viewModelScope.launch {
            repo.patients.collect { list ->
                val id = settings.activePatientId.value
                when {
                    list.isEmpty() -> if (id != -1L) settings.setActivePatient(-1L)
                    list.none { it.id == id } -> settings.setActivePatient(list.first().id)
                }
            }
        }
    }

    fun notify(message: String) { _events.tryEmit(message) }

    fun selectPatient(id: Long) {
        settings.setActivePatient(id)
        _aiText.value = null
        _battery.value = null
    }

    fun savePatient(patient: PatientEntity, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.savePatient(patient)
            settings.setActivePatient(id)
            _aiText.value = null
            notify("Ficha guardada")
            onDone(id)
        }
    }

    fun deletePatient(id: Long) {
        viewModelScope.launch {
            repo.deletePatient(id)
            notify("Paciente y sesiones eliminados")
        }
    }

    fun saveSession(session: SessionEntity) {
        viewModelScope.launch { repo.saveSession(session) }
    }

    fun addDemoSessions() {
        val patient = activePatient.value ?: return notify("Primero crea o selecciona un paciente")
        viewModelScope.launch {
            repo.saveSessions(DemoData.sessions(patient.id))
            _aiText.value = null
            notify("Se agregaron sesiones SIMULADAS de demostración")
        }
    }

    fun clearSimulated() {
        val patient = activePatient.value ?: return
        viewModelScope.launch {
            repo.deleteSimulated(patient.id)
            notify("Sesiones simuladas eliminadas")
        }
    }

    fun clearAllSessions() {
        val patient = activePatient.value ?: return
        viewModelScope.launch {
            repo.deleteSessions(patient.id)
            _aiText.value = null
            notify("Historial de sesiones borrado")
        }
    }

    fun setAiConsent(value: Boolean) = settings.setAiConsent(value)

    // --- Evaluación guiada (batería adaptativa) ---
    fun startBattery(): BatteryState? {
        val patient = activePatient.value ?: return null
        val state = Battery.start(patient.appTier, patient.evalProtocol)
        _battery.value = state
        return state
    }

    fun setBattery(state: BatteryState?) { _battery.value = state }

    // --- IA opcional ---
    fun requestAi() {
        val r = report.value ?: return notify("Selecciona un paciente")
        if (r.playedCount == 0) return notify("Completa al menos una actividad")
        if (!aiConsent.value) return notify("Activa el consentimiento de IA en Ajustes")
        if (!AiClient.isConfigured()) return notify("Falta GEMINI_API_KEY en local.properties")
        viewModelScope.launch {
            _aiLoading.value = true
            val (system, user) = ReportBuilder.aiPrompt(r)
            AiClient.generate(system, user)
                .onSuccess { _aiText.value = it }
                .onFailure { notify("No se pudo contactar a la IA: ${it.message}") }
            _aiLoading.value = false
        }
    }

    // --- Exportaciones ---
    fun exportPdf(onReady: (File) -> Unit) {
        val r = report.value ?: return notify("Selecciona un paciente")
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                runCatching { PdfExporter(getApplication<Application>()).export(r, _aiText.value) }.getOrNull()
            }
            if (file != null) onReady(file) else notify("No se pudo generar el PDF")
        }
    }

    fun exportCsv(onReady: (File) -> Unit) {
        val patient = activePatient.value ?: return notify("Selecciona un paciente")
        val list = sessions.value
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                runCatching { CsvExporter.export(getApplication<Application>(), patient, list) }.getOrNull()
            }
            if (file != null) onReady(file) else notify("No se pudo generar el CSV")
        }
    }
}
