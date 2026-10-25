package com.cubetimerpro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubetimerpro.TimerViewModel
import com.cubetimerpro.core.*
import com.cubetimerpro.core.TimerEngine.State
import com.cubetimerpro.data.SolveEntity
import kotlin.math.ceil

fun solveLabel(s: SolveEntity, dec: Int): String = when (Penalty.fromCode(s.penalty)) {
    Penalty.NONE -> TimeFormat.format(s.rawMs, dec)
    Penalty.PLUS2 -> TimeFormat.format(s.rawMs + 2000, dec) + "+"
    Penalty.DNF -> "DNF (${TimeFormat.format(s.rawMs, dec)})"
}

private fun inspectionText(ms: Long): String {
    val rem = 15000 - ms
    return when {
        rem > 0 -> ceil(rem / 1000.0).toInt().toString()
        ms <= 17000 -> "+2"
        else -> "DNF"
    }
}

@Composable
fun TimerScreen(vm: TimerViewModel) {
    val ui = vm.ui
    val solves by vm.solves.collectAsState()
    val settings by vm.settings.collectAsState()
    val dec = settings.decimals
    val results = remember(solves) { solves.asReversed().map { it.finalMs() } }
    val clipboard = LocalClipboardManager.current
    val focus = remember { FocusRequester() }
    var spaceDown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    val inInspection = ui.state == State.INSPECTING || (ui.state == State.HOLDING && ui.inspectionMs != null)
    val last = solves.firstOrNull()
    val timeText = when {
        inInspection -> inspectionText(ui.inspectionMs ?: 0)
        ui.state == State.STOPPED && last != null -> solveLabel(last, dec)
        ui.state == State.RUNNING || ui.state == State.STOPPED -> TimeFormat.format(ui.ms, dec)
        else -> TimeFormat.format(0, dec)
    }
    val timeColor = when {
        ui.state == State.HOLDING && ui.ready -> SuccessGreen
        ui.state == State.HOLDING -> ErrorRed
        inInspection -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onBackground
    }
    val status = when (ui.state) {
        State.IDLE -> if (settings.inspection) "Tap to start inspection" else "Hold to get ready"
        State.INSPECTING -> "Hold to get ready"
        State.HOLDING -> if (ui.ready) "Release to start" else "Keep holding…"
        State.RUNNING -> "Tap to stop"
        State.STOPPED -> "Tap for next solve"
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("CubeTimer ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text("Pro", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.weight(1f))
            EventPicker(vm.event) { vm.selectEvent(it) }
        }
        Surface(Modifier.fillMaxWidth().heightIn(max = 150.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(vm.scramble, Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(vertical = 10.dp),
                    fontFamily = FontFamily.Monospace, fontSize = 16.sp, textAlign = TextAlign.Center)
                Column {
                    IconButton(onClick = { clipboard.setText(AnnotatedString(vm.scramble)) }) { Icon(Icons.Filled.ContentCopy, "Copy scramble") }
                    IconButton(onClick = { vm.newScramble() }, enabled = ui.state != State.RUNNING) { Icon(Icons.Filled.Refresh, "New scramble") }
                }
            }
        }
        Text("Practice scramble (random-move, not an official random-state scramble)", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))

        Box(
            Modifier.weight(1f).fillMaxWidth().padding(vertical = 10.dp).clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .focusRequester(focus).focusable()
                .onKeyEvent { e ->
                    if (e.key == Key.Spacebar) {
                        if (e.type == KeyEventType.KeyDown) { if (!spaceDown) { spaceDown = true; vm.onPress() } }
                        else if (e.type == KeyEventType.KeyUp) { spaceDown = false; vm.onRelease() }
                        true
                    } else false
                }
                .pointerInput(Unit) { detectTapGestures(onPress = { vm.onPress(); tryAwaitRelease(); vm.onRelease() }) },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(timeText, fontSize = 72.sp, fontWeight = FontWeight.Bold, color = timeColor,
                    fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (last != null) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Last", color = MaterialTheme.colorScheme.onSurfaceVariant)
                FilterChip(last.penalty == 0, { vm.setPenalty(last, Penalty.NONE) }, { Text("OK") })
                FilterChip(last.penalty == 1, { vm.setPenalty(last, Penalty.PLUS2) }, { Text("+2") })
                FilterChip(last.penalty == 2, { vm.setPenalty(last, Penalty.DNF) }, { Text("DNF") })
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { vm.delete(last) }) { Icon(Icons.Filled.Delete, "Delete last solve") }
            }
        }

        fun avg(n: Int) = if (results.size < n) "–" else TimeFormat.format(Stats.currentAverage(results, n), dec)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Ao5", avg(5), Modifier.weight(1f), highlight = true)
            StatCard("Ao12", avg(12), Modifier.weight(1f))
            StatCard("Best", Stats.best(results)?.let { TimeFormat.format(it, dec) } ?: "–", Modifier.weight(1f))
        }
        LazyRow(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(solves.take(30), key = { it.id }) { s ->
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(solveLabel(s, dec), Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
