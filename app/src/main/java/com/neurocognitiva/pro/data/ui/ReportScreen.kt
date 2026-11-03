@file:OptIn(ExperimentalMaterial3Api::class)

package com.neurocognitiva.pro.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neurocognitiva.pro.domain.DomainMetric
import com.neurocognitiva.pro.domain.ReportBuilder
import com.neurocognitiva.pro.domain.ReportData
import com.neurocognitiva.pro.report.ShareUtils

private val BUTTON_PADDING = PaddingValues(horizontal = 14.dp, vertical = 6.dp)

@Composable
fun ReportScreen(vm: MainViewModel, nav: NavController) {
    val report by vm.report.collectAsStateWithLifecycle()
    val aiText by vm.aiText.collectAsStateWithLifecycle()
    val aiLoading by vm.aiLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val r = report

    Column(Modifier.fillMaxSize()) {
        AppHeader("Informe clínico", r?.let { "${it.code} · ${it.dateLabel}" } ?: "Sin paciente activo")
        if (r == null) {
            EmptyState(
                title = "Sin paciente activo",
                message = "Crea o selecciona un paciente para generar el informe.",
                action = { Button(onClick = { nav.navigate(Routes.profile(-1)) }) { Text("Crear ficha") } }
            )
            return@Column
        }

        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { vm.exportPdf { file -> ShareUtils.shareFile(context, file, "application/pdf", "Informe ${r.code}") } },
                contentPadding = BUTTON_PADDING
            ) { Icon(Icons.Default.Share, null, Modifier.size(18.dp)); Text("  PDF") }
            FilledTonalButton(
                onClick = {
                    ShareUtils.copyText(context, "Informe ${r.code}", ReportBuilder.toPlainText(r, aiText))
                    vm.notify("Informe copiado al portapapeles")
                },
                contentPadding = BUTTON_PADDING
            ) { Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp)); Text("  Copiar") }
            FilledTonalButton(onClick = { vm.requestAi() }, enabled = !aiLoading, contentPadding = BUTTON_PADDING) {
                if (aiLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp))
                Text("  Ampliar con IA")
            }
            OutlinedButton(onClick = { vm.addDemoSessions() }, contentPadding = BUTTON_PADDING) {
                Icon(Icons.Default.Science, null, Modifier.size(18.dp)); Text("  Datos demo")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (r.hasSimulated) {
                item {
                    SectionCard {
                        Text(
                            "Este informe contiene datos SIMULADOS (demostración). No lo uses como registro clínico.",
                            color = Color(0xFFB45309), fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(onClick = { vm.clearSimulated() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Eliminar sesiones simuladas")
                        }
                    }
                }
            }
            item { IdentificationCard(r) }
            item { GlobalCard(r) }
            item {
                SectionCard(title = "III. Detalle psicométrico por función") {
                    r.metrics.forEach { MetricRow(it) }
                }
            }
            item { SectionCard(title = "IV. Análisis cualitativo por función") {
                val items = r.qualitative.filter { it.metric.played }
                if (items.isEmpty()) {
                    Text("Aún no hay funciones evaluadas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items.forEach { q ->
                    Text(q.metric.activity.title, fontWeight = FontWeight.Bold, color = Color(q.metric.activity.colorHex))
                    Text(markupToAnnotated(q.text), style = MaterialTheme.typography.bodyMedium)
                }
            } }
            item { SectionCard(title = "V. Conclusión integrada") {
                r.synthesis.forEach { Text(markupToAnnotated(it), style = MaterialTheme.typography.bodyMedium) }
            } }
            if (!aiText.isNullOrBlank()) {
                item {
                    SectionCard(title = "Interpretación avanzada (IA)") {
                        Text(aiText.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Texto generado por un modelo de IA a partir de resultados anonimizados. Requiere revisión y validación profesional.",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Text(
                    "Este informe resume el desempeño en tareas computarizadas de tamizaje y estimulación cognitiva. " +
                        "No constituye diagnóstico clínico y debe interpretarse junto con la entrevista y pruebas estandarizadas.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IdentificationCard(r: ReportData) {
    val p = r.patient
    SectionCard(title = "I. Identificación del evaluado") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KeyValue("Nombre", p.name, Modifier.weight(1f))
            KeyValue("Documento", p.doc, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KeyValue("Edad", "${p.age} años", Modifier.weight(1f))
            KeyValue("Género / lateralidad", "${p.gender} / ${p.handedness}", Modifier.weight(1f))
        }
        KeyValue("Escolaridad", p.education)
        KeyValue("Ocupación", p.occupation)
        KeyValue("Ámbito de aplicación", p.appTier)
        KeyValue("Protocolo", p.evalProtocol)
        KeyValue("Evaluador", p.evaluator)
        KeyValue("Motivo / observaciones", p.notes)
    }
}

@Composable
private fun GlobalCard(r: ReportData) {
    SectionCard(title = "II. Perfil global") {
        val g = r.global
        if (g == null) {
            Text("Sin datos suficientes. Completa actividades o usa la evaluación guiada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@SectionCard
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox("ICG", "${g.icg}/100", Modifier.weight(1f))
            StatBox("Precisión", "${g.accuracy}%", Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
            StatBox("Latencia", if (g.rtMs > 0) "${g.rtMs} ms" else "—", Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
            StatBox("Nivel máx.", "${g.maxLevel}/8", Modifier.weight(1f), Color(0xFFD97706))
        }
        Text(g.category, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(
            "${r.sessionCount} sesiones en ${r.playedCount} de 10 dominios.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        RadarChart(
            labels = r.metrics.map { it.activity.shortTitle.take(13) },
            values = r.metrics.map { if (it.played) it.indexScore.toFloat() else 0f },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MetricRow(m: DomainMetric) {
    val color = classificationColor(m.classification)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(m.activity.shortTitle, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Pill(if (m.played) m.classification.label else "No evaluado", color)
        }
        if (m.played) {
            ScoreBar(m.indexScore, color)
            Text(
                "Índice ${m.indexScore} · Nivel máx. N${m.maxLevel} (dominado N${m.masteredLevel}) · " +
                    "Precisión ${m.accuracy}% · " + (if (m.avgRtMs > 0) "${m.avgRtMs} ms" else "sin TR"),
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
