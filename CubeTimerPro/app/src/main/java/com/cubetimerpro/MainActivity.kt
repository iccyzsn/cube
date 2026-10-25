package com.cubetimerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cubetimerpro.ui.AppTheme
import com.cubetimerpro.ui.CubeTimerApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: TimerViewModel = viewModel()
            val s by vm.settings.collectAsState()
            val dark = when (s.theme) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
            AppTheme(dark) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { CubeTimerApp(vm) }
            }
        }
    }
}
