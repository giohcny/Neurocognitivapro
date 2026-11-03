@file:OptIn(ExperimentalMaterial3Api::class)

package com.neurocognitiva.pro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neurocognitiva.pro.data.PatientEntity
import com.neurocognitiva.pro.domain.DemoPatients
import com.neurocognitiva.pro.domain.Education
import com.neurocognitiva.pro.domain.Protocols
import com.neurocognitiva.pro.domain.Rut
import com.neurocognitiva.pro.domain.Tiers
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset

private val GENDERS = listOf("Masculino", "Femenino", "No binario / Otro")
private val HANDS = listOf("Diestro/a", "Zurdo/a", "Ambidiestro/a")

@Composable
fun ProfileScreen(vm: MainViewModel, nav: NavController, patientId: Long) {
    val patients by vm.patients.collectAsStateWithLifecycle()
    val existing = patients.firstOrNull { it.id == patientId }

    var name by rememberSaveable(patientId) { mutableStateOf(existing?.name ?: "") }
    var doc by rememberSaveable(patientId) { mutableStateOf(existing?.doc ?: "") }
    var birthdate by rememberSaveable(patientId) { mutableStateOf(existing?.birthdate ?: "") }
    var age by rememberSaveable(patientId) { mutableStateOf(existing?.age?.toString() ?: "") }
    var gender by rememberSaveable(patientId) { mutableStateOf(existing?.gender ?: GENDERS[0]) }
    var education by rememberSaveable(patientId) { mutableStateOf(existing?.education ?: Education.options[3]) }
    var handedness by rememberSaveable(patientId) { mutableStateOf(existing?.handedness ?: HANDS[0]) }
    var occupation by rememberSaveable(patientId) { mutableStateOf(existing?.occupation ?: "") }
    var tier by rememberSaveable(patientId) { mutableStateOf(existing?.appTier ?: Tiers.DEFAULT) }
    var protocol by rememberSaveable(patientId) { mutableStateOf(existing?.evalProtocol ?: Protocols.DEFAULT) }
    var evaluator by rememberSaveable(patientId) { mutableStateOf(existing?.evaluator ?: "") }
    var evalDate by rememberSaveable(patientId) { mutableStateOf(existing?.evalDate ?: LocalDate.now().toString()) }
    var notes by rememberSaveable(patientId) { mutableStateOf(existing?.notes ?: "") }
    var showPicker by remember { mutableStateOf(false) }
    // Si la ficha llega después (carga asíncrona), se completan los campos una sola vez
    var loaded by rememberSaveable(patientId) { mutableStateOf(existing != null || patientId == -1L) }
    LaunchedEffect(existing?.id) {
        val e = existing
        if (!loaded && e != null) {
            name = e.name; doc = e.doc; birthdate = e.birthdate; age = e.age.toString()
            gender = e.gender; education = e.education; handedness = e.handedness
            occupation = e.occupation; tier = e.appTier; protocol = e.evalProtocol
            evaluator = e.evaluator; evalDate = e.evalDate; notes = e.notes
            loaded = true
        }
    }

    val rutState = Rut.validate(doc)
    val ageInt = age.toIntOrNull()
    val ageWarning = if (ageInt != null) Tiers.ageWarning(ageInt, tier) else null

    Column(Modifier.fillMaxSize()) {
        AppHeader(
            title = if (existing == null) "Nueva ficha" else "Editar ficha",
            subtitle = "Identificación y ámbito de aplicación",
            onBack = { nav.popBackStack() }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (existing == null) {
                OutlinedButton(
                    onClick = {
                        val d = DemoPatients.random()
                        name = d.name; doc = d.doc; birthdate = d.birthdate; age = d.age.toString()
                        gender = d.gender; education = d.education; handedness = d.handedness
                        occupation = d.occupation; tier = d.appTier; protocol = d.evalProtocol
                        evaluator = d.evaluator; evalDate = d.evalDate; notes = d.notes
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cargar ficha de ejemplo (demostración)") }
            }

            OutlinedTextField(
                value = name, onValueChange = { name = it }, label = { Text("Nombre completo *") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = doc, onValueChange = { doc = it }, label = { Text("RUT / documento *") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                isError = rutState == false,
                supportingText = {
                    when (rutState) {
                        false -> Text("El dígito verificador del RUT no coincide (puedes guardar igualmente).")
                        true -> Text("RUT válido")
                        null -> Text("Formato libre (ej. 12.345.678-5)")
                    }
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = birthdate, onValueChange = {}, readOnly = true,
                    label = { Text("Nacimiento") }, singleLine = true, modifier = Modifier.weight(1.4f),
                    trailingIcon = {
                        IconButton(onClick = { showPicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Elegir fecha")
                        }
                    }
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { v -> age = v.filter { it.isDigit() }.take(3) },
                    label = { Text("Edad *") }, singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DropdownField("Género", GENDERS, gender, { gender = it }, Modifier.weight(1f))
                DropdownField("Lateralidad", HANDS, handedness, { handedness = it }, Modifier.weight(1f))
            }
            DropdownField("Escolaridad", Education.options, education, { education = it })
            OutlinedTextField(
                value = occupation, onValueChange = { occupation = it }, label = { Text("Ocupación") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                label = "Nivel y ámbito de aplicación",
                options = Tiers.options.map { it.value },
                selected = tier,
                onSelected = { tier = it },
                display = { Tiers.info(it).label }
            )
            if (ageWarning != null) {
                Text(ageWarning, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            DropdownField("Protocolo de aplicación", Protocols.options, protocol, { protocol = it })
            OutlinedTextField(
                value = evaluator, onValueChange = { evaluator = it }, label = { Text("Profesional evaluador") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = evalDate, onValueChange = { evalDate = it }, label = { Text("Fecha de evaluación (AAAA-MM-DD)") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes, onValueChange = { notes = it }, label = { Text("Motivo de evaluación / observaciones") },
                minLines = 3, modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Los datos de salud son sensibles: se almacenan solo en este dispositivo y no se envían a ningún servidor.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    val a = age.toIntOrNull()
                    when {
                        name.isBlank() || doc.isBlank() -> vm.notify("Completa nombre y documento")
                        a == null || a !in 1..120 -> vm.notify("Ingresa una edad válida (1-120)")
                        else -> {
                            val entity = PatientEntity(
                                id = existing?.id ?: 0L,
                                name = name.trim(), doc = doc.trim(), age = a, birthdate = birthdate,
                                gender = gender, education = education, handedness = handedness,
                                occupation = occupation.trim(), appTier = tier, evalProtocol = protocol,
                                evaluator = evaluator.trim(), evalDate = evalDate.trim(), notes = notes.trim(),
                                createdAt = existing?.createdAt ?: System.currentTimeMillis()
                            )
                            vm.savePatient(entity) { nav.goToTab(Routes.CATALOG) }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar ficha y continuar") }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        val date = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                        birthdate = date.toString()
                        age = Period.between(date, LocalDate.now()).years.toString()
                        if (tier == Tiers.DEFAULT) tier = Tiers.suggestForAge(age.toInt())
                    }
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = pickerState) }
    }
}
