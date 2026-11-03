package com.neurocognitiva.pro.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neurocognitiva.pro.data.PatientEntity

@Composable
fun PatientsScreen(vm: MainViewModel, nav: NavController) {
    val patients by vm.patients.collectAsStateWithLifecycle()
    val activeId by vm.activePatientId.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<PatientEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        AppHeader(
            title = "Pacientes",
            subtitle = "${patients.size} registrado(s) · los datos se guardan solo en este dispositivo",
            actions = {
                IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                }
            }
        )
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (patients.isEmpty()) {
                EmptyState(
                    title = "Aún no hay pacientes",
                    message = "Crea la ficha del primer evaluado para comenzar. Cada paciente conserva su propio historial de sesiones."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(patients, key = { it.id }) { patient ->
                        PatientCard(
                            patient = patient,
                            selected = patient.id == activeId,
                            onSelect = { vm.selectPatient(patient.id) },
                            onEdit = { nav.navigate(Routes.profile(patient.id)) },
                            onDelete = { toDelete = patient }
                        )
                    }
                }
            }
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.profile(-1)) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo paciente") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }

    toDelete?.let { patient ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar paciente") },
            text = { Text("Se eliminarán la ficha de ${patient.name} y TODAS sus sesiones. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deletePatient(patient.id)
                    toDelete = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun PatientCard(
    patient: PatientEntity,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (selected) "Paciente activo" else "Seleccionar",
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(patient.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("${patient.doc} · ${patient.age} años", style = MaterialTheme.typography.bodySmall)
                Text(patient.appTier, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
