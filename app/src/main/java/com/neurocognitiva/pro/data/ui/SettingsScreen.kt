package com.neurocognitiva.pro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neurocognitiva.pro.BuildConfig
import com.neurocognitiva.pro.report.AiClient
import com.neurocognitiva.pro.report.ShareUtils

@Composable
fun SettingsScreen(vm: MainViewModel, nav: NavController) {
    val context = LocalContext.current
    val patient by vm.activePatient.collectAsStateWithLifecycle()
    val consent by vm.aiConsent.collectAsStateWithLifecycle()
    var confirmClear by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        AppHeader("Ajustes", "Privacidad, exportación e IA", onBack = { nav.popBackStack() })
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard(title = "Datos del paciente activo") {
                Text(patient?.name ?: "Sin paciente activo", fontWeight = FontWeight.Bold)
                OutlinedButton(
                    enabled = patient != null,
                    onClick = { vm.exportCsv { file -> ShareUtils.shareFile(context, file, "text/csv", "Sesiones CSV") } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Exportar sesiones (CSV anonimizado)") }
                OutlinedButton(
                    enabled = patient != null,
                    onClick = { vm.clearSimulated() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Eliminar sesiones simuladas (demo)") }
                OutlinedButton(
                    enabled = patient != null,
                    onClick = { confirmClear = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Borrar todo el historial de sesiones", color = MaterialTheme.colorScheme.error) }
                OutlinedButton(
                    onClick = {
                        ShareUtils.clearExports(context)
                        vm.notify("Archivos temporales de exportación eliminados")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Limpiar archivos exportados temporales") }
                Text(
                    "El CSV no incluye nombre ni documento: solo un identificador interno, la edad y el ámbito.",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionCard(title = "Ampliación del informe con IA (opcional)") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Permitir enviar resultados anonimizados", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Se envían edad, escolaridad, lateralidad, ocupación, ámbito y métricas por prueba. " +
                                "Nunca se envían nombre, documento ni notas clínicas.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = consent, onCheckedChange = { vm.setAiConsent(it) })
                }
                Text(
                    if (AiClient.isConfigured()) "Clave de API: configurada · modelo ${BuildConfig.GEMINI_MODEL}"
                    else "Clave de API: no configurada (agrega GEMINI_API_KEY en local.properties y recompila).",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            SectionCard(title = "Acerca de") {
                Text("NeuroCognitiva Pro ${BuildConfig.VERSION_NAME}", fontWeight = FontWeight.Bold)
                Text(
                    "Herramienta de tamizaje y estimulación cognitiva. Los índices usan referencias heurísticas y no baremos " +
                        "poblacionales validados; no constituye diagnóstico. Los datos se almacenan únicamente en este dispositivo.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("¿Borrar el historial?") },
            text = { Text("Se eliminarán todas las sesiones de ${patient?.name ?: "este paciente"}. La ficha se conserva.") },
            confirmButton = {
                TextButton(onClick = { vm.clearAllSessions(); confirmClear = false }) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancelar") } }
        )
    }
}
