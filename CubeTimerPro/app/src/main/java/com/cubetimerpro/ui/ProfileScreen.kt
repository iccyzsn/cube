package com.cubetimerpro.ui

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubetimerpro.TimerViewModel
import com.cubetimerpro.core.Stats
import com.cubetimerpro.core.TimeFormat
import com.cubetimerpro.data.toCsv

@Composable
fun LineChart(series: List<Pair<List<Float?>, Color>>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val all = series.flatMap { it.first }.filterNotNull()
        if (all.size < 2) return@Canvas
        val lo = all.min(); val hi = all.max(); val span = (hi - lo).coerceAtLeast(1f)
        val n = series.maxOf { it.first.size }
        val pad = 8f
        fun x(i: Int) = if (n <= 1) 0f else i.toFloat() / (n - 1) * size.width
        fun y(v: Float) = size.height - pad - (v - lo) / span * (size.height - 2 * pad)
        series.forEach { (pts, color) ->
            val path = Path(); var pen = false
            pts.forEachIndexed { i, v ->
                if (v == null) pen = false
                else if (!pen) { path.moveTo(x(i), y(v)); pen = true } else path.lineTo(x(i), y(v))
            }
            drawPath(path, color, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

@Composable
fun ProfileScreen(vm: TimerViewModel) {
    val solves by vm.solves.collectAsState()
    val all by vm.allSolves.collectAsState()
    val s by vm.settings.collectAsState()
    val ctx = LocalContext.current
    val dec = s.decimals
    val results = remember(solves) { solves.asReversed().map { it.finalMs() } }
    var confirmClear by remember { mutableStateOf(false) }
    fun fmt(v: Long?) = if (v == null) "–" else TimeFormat.format(v, dec)
    fun cur(n: Int) = if (results.size < n) "–" else TimeFormat.format(Stats.currentAverage(results, n), dec)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        InfoCard("Guest mode",
            "Your solves are stored on this device only. Accounts, sync, public profiles and verified rankings need the backend described in the README (phases 4–7); nothing here is faked.")

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            EventPicker(vm.event) { vm.selectEvent(it) }
        }
        if (results.isEmpty()) {
            Text("No solves yet for this event. Record some on the Timer tab.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Best single", fmt(Stats.best(results)), Modifier.weight(1f), highlight = true)
                StatCard("Session mean", fmt(Stats.sessionMean(results)), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Ao5", cur(5), Modifier.weight(1f)); StatCard("Best Ao5", fmt(Stats.bestAverage(results, 5)), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Ao12", cur(12), Modifier.weight(1f)); StatCard("Best Ao12", fmt(Stats.bestAverage(results, 12)), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Ao100", cur(100), Modifier.weight(1f)); StatCard("Solves", "${solves.size}", Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("+2 count", "${solves.count { it.penalty == 1 }}", Modifier.weight(1f))
                StatCard("DNF count", "${solves.count { it.penalty == 2 }}", Modifier.weight(1f))
            }
            Text("Progress (last 50 · grey = single, orange = Ao5)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LineChart(listOf(
                results.takeLast(50).map { it?.toFloat() } to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                Stats.rolling(results, 5).takeLast(50).map { it?.toFloat() } to Orange
            ), Modifier.fillMaxWidth().height(160.dp))
        }

        HorizontalDivider()
        Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Theme", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("System", "Light", "Dark").forEachIndexed { i, l -> FilterChip(s.theme == i, { vm.setTheme(i) }, { Text(l) }) }
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("WCA-style 15 s inspection", Modifier.weight(1f))
            Switch(s.inspection, { vm.setInspection(it) })
        }
        Text("Decimal places", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(dec == 2, { vm.setDecimals(2) }, { Text("2") }); FilterChip(dec == 3, { vm.setDecimals(3) }, { Text("3") })
        }
        Text("Hold-to-ready: ${s.holdMs} ms", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(s.holdMs.toFloat(), { vm.setHold((it / 50).toInt() * 50) }, valueRange = 0f..1000f, steps = 19)

        HorizontalDivider()
        Text("Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Button(onClick = {
            val i = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, "CubeTimer Pro solves (CSV)"); putExtra(Intent.EXTRA_TEXT, toCsv(all))
            }
            ctx.startActivity(Intent.createChooser(i, "Export solves (CSV)"))
        }, enabled = all.isNotEmpty()) { Text("Export all solves as CSV") }
        OutlinedButton(onClick = { confirmClear = true }, enabled = solves.isNotEmpty()) { Text("Delete all ${vm.event.label} solves") }

        HorizontalDivider()
        Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("CubeTimer Pro 0.1.0 · Time. Train. Compete. Connect.\nNot affiliated with or endorsed by the World Cube Association. Inspection and penalty handling follow WCA-style practice rules; results here are personal practice records, not official or verified.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    if (confirmClear) AlertDialog(
        onDismissRequest = { confirmClear = false },
        title = { Text("Delete ${vm.event.label} solves?") },
        text = { Text("This permanently removes ${solves.size} solves from this device.") },
        confirmButton = { TextButton({ vm.clearEvent(); confirmClear = false }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton({ confirmClear = false }) { Text("Cancel") } }
    )
}
