package com.cubetimerpro.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import com.cubetimerpro.core.Penalty
import com.cubetimerpro.core.finalResultMs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "solves", indices = [Index("event")])
data class SolveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val event: String,
    val scramble: String,
    val rawMs: Long,
    val penalty: Int = 0,
    val inspectionMs: Long? = null,
    val timestamp: Long,
    val sessionId: Int = 1,
    val note: String? = null
) {
    fun finalMs(): Long? = finalResultMs(rawMs, Penalty.fromCode(penalty))
}

@Dao
interface SolveDao {
    @Query("SELECT * FROM solves WHERE event = :event ORDER BY id DESC")
    fun observe(event: String): Flow<List<SolveEntity>>

    @Query("SELECT * FROM solves ORDER BY id DESC")
    fun observeAll(): Flow<List<SolveEntity>>

    @Insert suspend fun insert(s: SolveEntity): Long
    @Update suspend fun update(s: SolveEntity)
    @Delete suspend fun delete(s: SolveEntity)

    @Query("DELETE FROM solves WHERE event = :event")
    suspend fun deleteEvent(event: String)
}

@Database(entities = [SolveEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun solveDao(): SolveDao

    companion object {
        @Volatile private var inst: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "cubetimer.db").build().also { inst = it }
        }
    }
}

private val Context.dataStore by preferencesDataStore("settings")

data class AppSettings(val theme: Int = 0, val inspection: Boolean = true, val decimals: Int = 2, val holdMs: Int = 300)

class SettingsStore(private val ctx: Context) {
    private val THEME = intPreferencesKey("theme")
    private val INSP = booleanPreferencesKey("inspection")
    private val DEC = intPreferencesKey("decimals")
    private val HOLD = intPreferencesKey("hold_ms")

    val flow: Flow<AppSettings> = ctx.dataStore.data.map {
        AppSettings(it[THEME] ?: 0, it[INSP] ?: true, it[DEC] ?: 2, it[HOLD] ?: 300)
    }

    suspend fun setTheme(v: Int) { ctx.dataStore.edit { it[THEME] = v } }
    suspend fun setInspection(v: Boolean) { ctx.dataStore.edit { it[INSP] = v } }
    suspend fun setDecimals(v: Int) { ctx.dataStore.edit { it[DEC] = v } }
    suspend fun setHold(v: Int) { ctx.dataStore.edit { it[HOLD] = v } }
}

fun toCsv(solves: List<SolveEntity>): String = buildString {
    appendLine("id,event,scramble,raw_ms,penalty,final_ms,inspection_ms,timestamp_ms,session,note")
    for (s in solves.asReversed()) {
        val note = (s.note ?: "").replace("\"", "\"\"")
        appendLine("${s.id},${s.event},\"${s.scramble.replace("\n", " ")}\",${s.rawMs},${Penalty.fromCode(s.penalty)},${s.finalMs() ?: "DNF"},${s.inspectionMs ?: ""},${s.timestamp},${s.sessionId},\"$note\"")
    }
}
