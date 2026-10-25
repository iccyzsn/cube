package com.cubetimerpro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubetimerpro.TimerViewModel
import com.cubetimerpro.core.CubeEvent

private data class Tab(val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("Timer", Icons.Filled.Timer), Tab("Leaderboard", Icons.Filled.EmojiEvents), Tab("Learn", Icons.Filled.School),
    Tab("Community", Icons.Filled.Groups), Tab("Profile", Icons.Filled.Person)
)

@Composable
fun CubeTimerApp(vm: TimerViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(bottomBar = {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            tabs.forEachIndexed { i, t ->
                NavigationBarItem(
                    selected = tab == i, onClick = { tab = i },
                    icon = { Icon(t.icon, contentDescription = t.label) }, label = { Text(t.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }
    }) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> TimerScreen(vm)
                1 -> LeaderboardScreen(vm)
                2 -> LearnScreen()
                3 -> CommunityScreen()
                else -> ProfileScreen(vm)
            }
        }
    }
}

@Composable
fun EventPicker(current: CubeEvent, onSelect: (CubeEvent) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) {
            Text(current.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Change event")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            CubeEvent.values().forEach { e ->
                DropdownMenuItem(text = { Text(e.label) }, onClick = { onSelect(e); open = false })
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    Surface(modifier, shape = RoundedCornerShape(16.dp),
        color = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun InfoCard(title: String, body: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
