package com.cubetimerpro

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cubetimerpro.core.*
import com.cubetimerpro.core.TimerEngine.State
import com.cubetimerpro.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TimerUi(
    val state: State = State.IDLE,
    val ms: Long = 0,
    val inspectionMs: Long? = null,
    val ready: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).solveDao()
    private val store = SettingsStore(app)
    private val engine = TimerEngine { SystemClock.elapsedRealtimeNanos() }
    private var ticker: Job? = null
    private val eventFlow = MutableStateFlow(CubeEvent.THREE)

    var event by mutableStateOf(CubeEvent.THREE); private set
    var scramble by mutableStateOf(ScrambleGenerator.generate(CubeEvent.THREE)); private set
    var ui by mutableStateOf(TimerUi()); private set

    val settings: StateFlow<AppSettings> = store.flow.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    val solves: StateFlow<List<SolveEntity>> = eventFlow.flatMapLatest { dao.observe(it.name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSolves: StateFlow<List<SolveEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            settings.collect {
                engine.inspectionEnabled = it.inspection
                engine.holdMs = it.holdMs.toLong()
            }
        }
    }

    fun selectEvent(e: CubeEvent) {
        if (engine.state == State.RUNNING) return
        event = e; eventFlow.value = e; newScramble()
    }

    fun newScramble() { scramble = ScrambleGenerator.generate(event) }

    fun onPress() {
        val r = engine.press()
        startTicker()
        if (r != null) finish(r)
        refresh()
    }

    fun onRelease() { engine.release(); refresh() }

    private fun finish(r: TimerResult) {
        val sc = scramble; val ev = event
        viewModelScope.launch {
            dao.insert(SolveEntity(event = ev.name, scramble = sc, rawMs = r.rawMs, penalty = r.inspectionPenalty.code,
                inspectionMs = r.inspectionMs, timestamp = System.currentTimeMillis()))
        }
        newScramble()
    }

    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = viewModelScope.launch {
            while (engine.state == State.INSPECTING || engine.state == State.HOLDING || engine.state == State.RUNNING) {
                refresh(); delay(16)
            }
            refresh()
        }
    }

    private fun refresh() {
        ui = TimerUi(engine.state, engine.elapsedMs(), engine.inspectionElapsedMs(), engine.isReady())
    }

    fun setPenalty(s: SolveEntity, p: Penalty) { viewModelScope.launch { dao.update(s.copy(penalty = p.code)) } }
    fun delete(s: SolveEntity) { viewModelScope.launch { dao.delete(s) } }
    fun clearEvent() { viewModelScope.launch { dao.deleteEvent(event.name) } }

    fun setTheme(v: Int) { viewModelScope.launch { store.setTheme(v) } }
    fun setInspection(v: Boolean) { viewModelScope.launch { store.setInspection(v) } }
    fun setDecimals(v: Int) { viewModelScope.launch { store.setDecimals(v) } }
    fun setHold(v: Int) { viewModelScope.launch { store.setHold(v) } }
}
