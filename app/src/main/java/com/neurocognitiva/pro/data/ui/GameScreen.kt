@file:OptIn(ExperimentalMaterial3Api::class)

package com.neurocognitiva.pro.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neurocognitiva.pro.data.SessionEntity
import com.neurocognitiva.pro.domain.Battery
import com.neurocognitiva.pro.domain.BatteryState
import com.neurocognitiva.pro.domain.Catalog
import com.neurocognitiva.pro.domain.Scoring
import com.neurocognitiva.pro.domain.Tiers
import com.neurocognitiva.pro.domain.Validity
import com.neurocognitiva.pro.game.GameStage
import com.neurocognitiva.pro.game.GameTracker
import kotlinx.coroutines.delay

private enum class GamePhase { Instructions, Countdown, Playing, Result }

@Composable
fun GameScreen(vm: MainViewModel, nav: NavController, activityId: String, startLevel: Int) {
    val activity = remember(activityId) { Catalog.byId(activityId) }
    val patient by vm.activePatient.collectAsStateWithLifecycle()
    val battery by vm.battery.collectAsStateWithLifecycle()
    val view = LocalView.current

    var levelNumber by remember { mutableIntStateOf(startLevel.coerceIn(1, Catalog.MAX_LEVEL)) }
    var phase by remember { mutableStateOf(GamePhase.Instructions) }
    var tracker by remember { mutableStateOf<GameTracker?>(null) }
    var result by remember { mutableStateOf<SessionEntity?>(null) }
    var confirmExit by remember { mutableStateOf(false) }

    val levelDef = activity.level(levelNumber)
    val inTask = phase == GamePhase.Playing || phase == GamePhase.Countdown

    // La pantalla no se apaga durante la tarea
    DisposableEffect(inTask) {
        view.keepScreenOn = inTask
        onDispose { view.keepScreenOn = false }
    }

    fun exit() {
        vm.setBattery(null)
        nav.popBackStack()
    }

    BackHandler {
        if (inTask) confirmExit = true else exit()
    }

    // La sesión se registra una sola vez (finishOnce) aunque llegue por tiempo y por último ítem a la vez
    val finish: () -> Unit = remember(tracker) {
        {
            val t = tracker
            if (t != null && t.finishOnce()) {
                val session = t.buildSession()
                vm.saveSession(session)
                result = session
                phase = GamePhase.Result
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        AppHeader(
            title = activity.shortTitle,
            subtitle = levelDef.name + (battery?.let { "  ·  Evaluación guiada ${it.index + 1}/${it.total}" } ?: ""),
            onBack = { if (inTask) confirmExit = true else exit() }
        )

        when (phase) {
            GamePhase.Instructions -> InstructionsPane(
                activityId = activityId,
                levelNumber = levelNumber,
                onLevel = { levelNumber = it },
                lockedLevel = battery != null,
                suggested = patient?.let { Tiers.range(it.appTier) },
                onStart = { phase = GamePhase.Countdown }
            )

            GamePhase.Countdown -> {
                var count by remember { mutableIntStateOf(3) }
                LaunchedEffect(Unit) {
                    for (i in 3 downTo 1) {
                        count = i
                        delay(800)
                    }
                    tracker = GameTracker(activity, levelDef, patient?.id ?: -1L, battery != null)
                    phase = GamePhase.Playing
                }
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Prepárate", style = MaterialTheme.typography.titleMedium)
                        Text(count.toString(), fontSize = 96.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            GamePhase.Playing -> {
                val t = tracker
                if (t != null) {
                    GameHud(t)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        key(t) { GameStage(t, finish) }
                    }
                }
            }

            GamePhase.Result -> {
                val s = result
                if (s != null) {
                    ResultPane(
                        session = s,
                        battery = battery,
                        onRetry = { level ->
                            levelNumber = level
                            tracker = null
                            result = null
                            phase = GamePhase.Countdown
                        },
                        onNextBattery = { next ->
                            vm.setBattery(next)
                            nav.navigate(Routes.game(next.activityId, next.level)) {
                                popUpTo(Routes.GAME) { inclusive = true }
                            }
                        },
                        onFinishBattery = {
                            vm.setBattery(null)
                            nav.goToTab(Routes.REPORT)
                        },
                        onExit = { exit() },
                        onReport = {
                            vm.setBattery(null)
                            nav.goToTab(Routes.REPORT)
                        }
                    )
                }
            }
        }
    }

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("¿Abandonar la tarea?") },
            text = { Text("La sesión en curso no se guardará.") },
            confirmButton = { TextButton(onClick = { confirmExit = false; exit() }) { Text("Abandonar") } },
            dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Continuar") } }
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.InstructionsPane(
    activityId: String,
    levelNumber: Int,
    onLevel: (Int) -> Unit,
    lockedLevel: Boolean,
    suggested: IntRange?,
    onStart: () -> Unit
) {
    val activity = Catalog.byId(activityId)
    val level = activity.level(levelNumber)
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionCard(title = activity.title) {
            Text(activity.domain, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(activity.description, style = MaterialTheme.typography.bodyMedium)
            Text("Redes: ${activity.brainAreas}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SectionCard(title = "Nivel") {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..Catalog.MAX_LEVEL).forEach { l ->
                    FilterChip(
                        selected = l == levelNumber,
                        onClick = { onLevel(l) },
                        enabled = !lockedLevel,
                        label = { Text(if (suggested != null && l in suggested) "N$l ★" else "N$l") }
                    )
                }
            }
            Text(level.name, fontWeight = FontWeight.Bold)
            Text(level.desc, style = MaterialTheme.typography.bodyMedium)
            if (suggested != null) {
                Text("★ = rango sugerido para el ámbito de aplicación del paciente", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        SectionCard(title = "Instrucciones") {
            Text(activity.instructions, style = MaterialTheme.typography.bodyLarge)
            Text(
                "Antes de comenzar, confirma que la persona comprendió la tarea y está descansada. Los resultados se guardan automáticamente.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Comenzar") }
    }
}

@Composable
private fun GameHud(t: GameTracker) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HudItem("Ronda", "${minOf(t.round, t.totalRounds)}/${t.totalRounds}")
            HudItem("Puntaje", t.score.toString())
            HudItem("Aciertos/Err.", "${t.hits}/${t.totalErrors}")
            HudItem("T. reacción", if (t.avgRtMs > 0) "${t.avgRtMs} ms" else "—")
            if (t.timeRemaining >= 0) {
                HudItem("Tiempo", "${t.timeRemaining}s", highlight = t.timeRemaining <= 10)
            }
        }
    }
}

@Composable
private fun HudItem(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontWeight = FontWeight.ExtraBold,
            color = if (highlight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.ResultPane(
    session: SessionEntity,
    battery: BatteryState?,
    onRetry: (Int) -> Unit,
    onNextBattery: (BatteryState) -> Unit,
    onFinishBattery: () -> Unit,
    onExit: () -> Unit,
    onReport: () -> Unit
) {
    val validity = Validity.fromCode(session.validity)
    val recommendation = Scoring.recommendNext(session.level, session.accuracy, validity)
    val nextBattery = remember(session) { battery?.let { Battery.advance(it, session.accuracy, validity) } }

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionCard(title = "Resultado de la sesión") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("Precisión", "${session.accuracy}%", Modifier.weight(1f))
                StatBox(
                    "T. reacción",
                    if (session.avgReactionMs > 0) "${session.avgReactionMs} ms" else "—",
                    Modifier.weight(1f), MaterialTheme.colorScheme.secondary
                )
                StatBox("Puntaje", session.score.toString(), Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KeyValue("Aciertos", session.hits.toString(), Modifier.weight(1f))
                KeyValue("Errores", session.wrong.toString(), Modifier.weight(1f))
                KeyValue("Omisiones", session.omissions.toString(), Modifier.weight(1f))
                KeyValue("Comisiones", session.commissions.toString(), Modifier.weight(1f))
            }
            session.dPrime?.let {
                Text("Sensibilidad de detección (d′): ${"%.2f".format(it)}", style = MaterialTheme.typography.bodyMedium)
            }
            if (validity != Validity.OK) {
                Pill(validity.label, if (validity.excludesFromIndex) Color(0xFFD97706) else Color(0xFF64748B))
            }
        }
        SectionCard(title = "Recomendación adaptativa") {
            Text(recommendation.text, style = MaterialTheme.typography.bodyLarge)
        }

        if (battery != null) {
            if (nextBattery != null) {
                val nextAct = Catalog.byId(nextBattery.activityId)
                val sameActivity = nextBattery.activityId == battery.activityId
                Button(onClick = { onNextBattery(nextBattery) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (sameActivity) "Subir al Nivel ${nextBattery.level}" else "Siguiente: ${nextAct.shortTitle} (N${nextBattery.level})")
                }
            } else {
                Button(onClick = onFinishBattery, modifier = Modifier.fillMaxWidth()) { Text("Finalizar y ver informe") }
            }
            OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("Terminar evaluación guiada") }
        } else {
            if (recommendation.level != session.level) {
                Button(onClick = { onRetry(recommendation.level) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Jugar Nivel ${recommendation.level}")
                }
                OutlinedButton(onClick = { onRetry(session.level) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Repetir Nivel ${session.level}")
                }
            } else {
                Button(onClick = { onRetry(session.level) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Repetir Nivel ${session.level}")
                }
            }
            OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text("Elegir otra actividad") }
            TextButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) { Text("Ver informe") }
        }
        Text(
            "Cada sesión queda registrada en el historial del paciente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
