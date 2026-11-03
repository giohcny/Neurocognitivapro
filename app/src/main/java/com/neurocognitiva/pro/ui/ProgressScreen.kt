@file:OptIn(ExperimentalMaterial3Api::class)

package com.neurocognitiva.pro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neurocognitiva.pro.data.SessionEntity
import com.neurocognitiva.pro.domain.Catalog
import com.neurocognitiva.pro.domain.Scoring
import com.neurocognitiva.pro.domain.Validity

@Composable
fun ProgressScreen(vm: MainViewModel, nav: NavController) {
    val patient by vm.activePatient.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<String?>(null) }

    val filtered = sessions.filter { selected == null || it.activityId == selected }
    val chartData = filtered.takeLast(30)

    Column(Modifier.fillMaxSize()) {
        AppHeader("Evolución", patient?.name ?: "Sin paciente activo")
        if (patient == null || sessions.isEmpty()) {
            EmptyState(
                title = "Sin sesiones todavía",
                message = "Completa actividades para ver la evolución del rendimiento a lo largo del tiempo."
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = selected == null, onClick = { selected = null }, label = { Text("Todas") })
                        Catalog.activities.forEach { act ->
                            FilterChip(
                                selected = selected == act.id,
                                onClick = { selected = act.id },
                                label = { Text(act.shortTitle) }
                            )
                        }
                    }
                }
                item {
                    SectionCard(title = "Rendimiento por sesión (0-100)") {
                        LineChart(
                            values = chartData.map { Scoring.sessionPerformance(it).toFloat() },
                            labels = chartData.mapIndexed { i, s -> if (chartData.size <= 10 || i % 3 == 0) "N${s.level}" else "" },
                            modifier = Modifier.fillMaxWidth().height(210.dp)
                        )
                        val first = chartData.firstOrNull()?.let { Scoring.sessionPerformance(it) }
                        val last = chartData.lastOrNull()?.let { Scoring.sessionPerformance(it) }
                        if (first != null && last != null && chartData.size >= 2) {
                            val delta = last - first
                            val text = when {
                                delta >= 3 -> "Tendencia favorable: +$delta puntos entre la primera y la última sesión."
                                delta <= -3 -> "Descenso de $delta puntos: considera fatiga, variabilidad o ajustar el nivel."
                                else -> "Rendimiento estable entre sesiones."
                            }
                            Text(text, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "El rendimiento combina precisión (55 %) y nivel superado (45 %). Las etiquetas indican el nivel jugado.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(filtered.reversed(), key = { it.id }) { s -> SessionRow(s) }
            }
        }
    }
}

@Composable
private fun SessionRow(s: SessionEntity) {
    val act = Catalog.byId(s.activityId)
    val validity = Validity.fromCode(s.validity)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).background(Color(act.colorHex), CircleShape))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(act.shortTitle, fontWeight = FontWeight.Bold)
                Text(
                    "Nivel ${s.level} · ${formatDateTime(s.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (s.simulated) Pill("SIMULADA", Color(0xFFD97706))
                    if (validity != Validity.OK) Pill(validity.code.lowercase().replaceFirstChar { it.uppercase() }, Color(0xFF64748B))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${s.accuracy}%", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (s.avgReactionMs > 0) "${s.avgReactionMs} ms" else "—",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
