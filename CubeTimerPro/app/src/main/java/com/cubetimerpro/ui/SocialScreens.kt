package com.cubetimerpro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubetimerpro.TimerViewModel
import com.cubetimerpro.core.CubeEvent
import com.cubetimerpro.core.TimeFormat

@Composable
fun LeaderboardScreen(vm: TimerViewModel) {
    val all by vm.allSolves.collectAsState()
    val s by vm.settings.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Leaderboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        InfoCard("Global rankings are not live yet",
            "Verified rankings need accounts, server-side submission checks, evidence review and moderation (README phases 4–6). Until that backend exists, no ranking data is shown and none is simulated.")
        Text("Your personal records (this device, unverified)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        val bests = CubeEvent.values().mapNotNull { e ->
            all.filter { it.event == e.name }.mapNotNull { it.finalMs() }.minOrNull()?.let { e to it }
        }
        if (bests.isEmpty()) Text("Solve something on the Timer tab and your records will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        bests.forEach { (e, ms) ->
            Surface(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.padding(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(e.label, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text(TimeFormat.format(ms, s.decimals), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun CommunityScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Community", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        InfoCard("Coming with the backend",
            "Friends, activity feed, clubs, live races, challenges and community review all require authenticated server-side state. They are not implemented in this build and no fake data is shown.")
        listOf("Friends & following", "Live race rooms", "Weekly / monthly challenges", "Clubs & group leaderboards", "Community verification voting")
            .forEach { Text("• $it — planned", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
