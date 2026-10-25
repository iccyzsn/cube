package com.cubetimerpro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubetimerpro.core.*
import kotlinx.coroutines.delay

private fun stickerColor(c: Char) = when (c) {
    'U' -> Color(0xFFF5F5F5); 'R' -> Color(0xFFC62828); 'F' -> Color(0xFF2E9E4F)
    'D' -> Color(0xFFFFD500); 'L' -> Orange; else -> Color(0xFF1E66D0)
}

/** Unfolded 2-D net of the cube state (a real 3-D renderer is a later phase; the state engine behind it is renderer-independent). */
@Composable
fun CubeNet(cube: CubieCube, modifier: Modifier = Modifier) {
    val f = remember(cube) { cube.toFacelets() }
    val outline = MaterialTheme.colorScheme.outline
    Canvas(modifier.fillMaxWidth().aspectRatio(12f / 9f)) {
        val cell = size.width / 12f
        val offs = mapOf('U' to (1 to 0), 'R' to (2 to 1), 'F' to (1 to 1), 'D' to (1 to 2), 'L' to (0 to 1), 'B' to (3 to 1))
        "URFDLB".forEachIndexed { fi, face ->
            val (ox, oy) = offs.getValue(face)
            for (i in 0..8) {
                val x = (ox * 3 + i % 3) * cell; val y = (oy * 3 + i / 3) * cell
                drawRoundRect(stickerColor(f[fi * 9 + i]), Offset(x + 2, y + 2), Size(cell - 4, cell - 4), CornerRadius(8f, 8f))
                drawRoundRect(outline, Offset(x + 2, y + 2), Size(cell - 4, cell - 4), CornerRadius(8f, 8f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
            }
        }
    }
}

@Composable
fun LearnScreen() {
    var selected by remember { mutableStateOf(AlgDb.cases.first()) }
    var step by remember(selected) { mutableIntStateOf(0) }
    var playing by remember(selected) { mutableStateOf(false) }
    val moves = remember(selected) { Alg.parse(selected.alg) }
    val start = remember(selected) { CubieCube.SOLVED.applyAlg(Alg.invert(selected.alg)) }
    val state = remember(selected, step) { start.applyMoves(moves.take(step)) }

    LaunchedEffect(playing) {
        if (playing) {
            while (step < moves.size) { delay(700); step++ }
            playing = false
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Learn", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Text("Methods & coverage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        val cfop = (AlgDb.ollCoverage.first + AlgDb.pllCoverage.first) / (AlgDb.ollCoverage.second + AlgDb.pllCoverage.second).toFloat()
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(14.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("CFOP — starter set only", fontWeight = FontWeight.Bold)
                Text("${AlgDb.ollCoverage.first}/${AlgDb.ollCoverage.second} OLL · ${AlgDb.pllCoverage.first}/${AlgDb.pllCoverage.second} PLL algorithms. No written lessons yet.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(progress = { cfop }, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                Text("Roux, ZB, ZZ, Petrus, Beginner LBL: not implemented yet.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Text("Algorithm viewer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AlgDb.cases, key = { it.id }) { c -> FilterChip(c.id == selected.id, { selected = c }, { Text(c.name) }) }
        }
        Text("${selected.group} · ${selected.note}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        CubeNet(state)
        val primary = MaterialTheme.colorScheme.primary
        val dim = MaterialTheme.colorScheme.onSurfaceVariant
        Text(buildAnnotatedString {
            moves.forEachIndexed { i, m ->
                val style = when {
                    i == step - 1 -> SpanStyle(color = primary, fontWeight = FontWeight.Bold)
                    i < step - 1 -> SpanStyle(color = MaterialTheme.colorScheme.onSurface)
                    else -> SpanStyle(color = dim)
                }
                withStyle(style) { append("$m ") }
            }
        }, fontFamily = FontFamily.Monospace, fontSize = 20.sp)
        Text("Step $step / ${moves.size}", style = MaterialTheme.typography.labelMedium, color = dim)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { playing = false; step = 0 }) { Icon(Icons.Filled.Replay, "Restart") }
            IconButton(onClick = { playing = false; if (step > 0) step-- }) { Icon(Icons.Filled.SkipPrevious, "Previous move") }
            FilledIconButton(onClick = {
                if (playing) playing = false else { if (step >= moves.size) step = 0; playing = true }
            }) { Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (playing) "Pause" else "Play") }
            IconButton(onClick = { playing = false; if (step < moves.size) step++ }) { Icon(Icons.Filled.SkipNext, "Next move") }
        }
        Text("The viewer starts from the state that this algorithm solves (computed by inverting it), then applies each move with the cube-state engine. Camera-rotatable 3-D rendering is planned for a later phase.",
            style = MaterialTheme.typography.bodySmall, color = dim)
    }
}
