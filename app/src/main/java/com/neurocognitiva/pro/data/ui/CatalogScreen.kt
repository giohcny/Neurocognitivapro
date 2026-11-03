@file:OptIn(ExperimentalMaterial3Api::class)

package com.neurocognitiva.pro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neurocognitiva.pro.data.PatientEntity
import com.neurocognitiva.pro.data.SessionEntity
import com.neurocognitiva.pro.domain.ActivityDef
import com.neurocognitiva.pro.domain.Catalog
import com.neurocognitiva.pro.domain.Category
import com.neurocognitiva.pro.domain.Protocols
import com.neurocognitiva.pro.domain.Scoring
import com.neurocognitiva.pro.domain.Tiers
import com.neurocognitiva.pro.domain.Validity
import kotlin.math.roundToInt

/** Nivel recomendado: escalera adaptativa según la última sesión válida; si no hay, inicio del ámbito. */
fun recommendedLevel(act: ActivityDef, sessions: List<SessionEntity>, patient: PatientEntity?): Int {
    val protocolMax = patient?.let { Protocols.maxLevel(it.evalProtocol) } ?: Catalog.MAX_LEVEL
    val last = sessions.lastOrNull { it.activityId == act.id && !Validity.fromCode(it.validity).excludesFromIndex }
    val level = if (last != null) {
        Scoring.recommendNext(last.level, last.accuracy, Validity.fromCode(last.validity)).level
    } else {
        patient?.let { Tiers.range(it.appTier).first } ?: 1
    }
    return level.coerceIn(1, protocolMax)
}

@Composable
fun CatalogScreen(vm: MainViewModel, nav: NavController) {
    val patient by vm.activePatient.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf<Category?>(null) }

    fun start(activityId: String, level: Int) {
        if (patient == null) {
            vm.notify("Primero crea o selecciona un paciente")
            nav.navigate(Routes.profile(-1))
        } else {
            nav.navigate(Routes.game(activityId, level))
        }
    }

    val playedDomains = sessions.map { it.activityId }.distinct().size
    val avgAccuracy = if (sessions.isEmpty()) 0 else sessions.map { it.accuracy }.average().roundToInt()

    Column(Modifier.fillMaxSize()) {
        AppHeader(
            title = "NeuroCognitiva Pro",
            subtitle = patient?.let { "${it.name} · ${it.age} años" } ?: "Sin paciente activo",
            actions = {
                IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                }
            }
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (patient == null) {
                item {
                    SectionCard(title = "Comienza aquí") {
                        Text("Crea la ficha del evaluado para registrar sesiones y generar informes.")
                        Button(onClick = { nav.navigate(Routes.profile(-1)) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Crear ficha de paciente")
                        }
                    }
                }
            } else {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatBox("Sesiones", sessions.size.toString(), Modifier.weight(1f))
                        StatBox("Dominios", "$playedDomains/10", Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                        StatBox("Precisión media", "$avgAccuracy%", Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
                    }
                }
                item {
                    SectionCard(title = "Evaluación guiada") {
                        Text(
                            "Recorre las 10 áreas en orden con dificultad adaptativa: parte en el nivel sugerido para " +
                                "${patient?.appTier ?: "el ámbito"} y sube de nivel mientras la precisión sea ≥ 80 %, " +
                                "respetando el máximo del protocolo.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = {
                                val state = vm.startBattery()
                                if (state != null) nav.navigate(Routes.game(state.activityId, state.level))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null)
                            Text("  Iniciar evaluación guiada")
                        }
                    }
                }
            }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("Todas") })
                    Category.entries.forEach { c ->
                        FilterChip(selected = filter == c, onClick = { filter = c }, label = { Text(c.label) })
                    }
                }
            }
            items(Catalog.activities.filter { filter == null || it.category == filter }, key = { it.id }) { act ->
                ActivityCard(
                    act = act,
                    sessions = sessions.filter { it.activityId == act.id },
                    recommended = recommendedLevel(act, sessions, patient),
                    tierRange = patient?.let { Tiers.range(it.appTier) },
                    protocolMax = patient?.let { Protocols.maxLevel(it.evalProtocol) } ?: Catalog.MAX_LEVEL,
                    onStart = { level -> start(act.id, level) }
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(
    act: ActivityDef,
    sessions: List<SessionEntity>,
    recommended: Int,
    tierRange: IntRange?,
    protocolMax: Int,
    onStart: (Int) -> Unit
) {
    val color = Color(act.colorHex)
    val bestByLevel = sessions.groupBy { it.level }.mapValues { (_, list) -> list.maxOf { it.accuracy } }
    val mastered = sessions.filter { it.accuracy >= Scoring.MASTERY_ACCURACY }.maxOfOrNull { it.level }

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(46.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
                Icon(activityIcon(act.id), contentDescription = null, tint = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text(act.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(act.category.label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
        }
        Text(act.description, style = MaterialTheme.typography.bodyMedium)
        Text("Redes cerebrales: ${act.brainAreas}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        (1..Catalog.MAX_LEVEL).chunked(4).forEach { rowLevels ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowLevels.forEach { level ->
                    val best = bestByLevel[level]
                    val isRecommended = level == recommended
                    val inScope = tierRange != null && level in tierRange && level <= protocolMax
                    val shape = RoundedCornerShape(12.dp)
                    Surface(
                        modifier = Modifier.weight(1f).clip(shape).clickable { onStart(level) },
                        shape = shape,
                        color = if (best != null && best >= Scoring.MASTERY_ACCURACY) color.copy(alpha = 0.16f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            if (isRecommended) 2.dp else 1.dp,
                            if (isRecommended) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("N$level", fontWeight = FontWeight.ExtraBold)
                            Text(
                                text = when {
                                    best != null -> "$best%"
                                    inScope -> "sugerido"
                                    else -> " "
                                },
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        if (sessions.isNotEmpty()) {
            Text(
                "${sessions.size} sesión(es) · nivel dominado: ${mastered?.let { "N$it" } ?: "ninguno aún"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick = { onStart(recommended) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Text(if (sessions.isEmpty()) "  Iniciar en Nivel $recommended" else "  Continuar en Nivel $recommended (recomendado)")
        }
    }
}
