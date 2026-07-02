package com.thefadghost.glasspuzzlehub.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore("hub_settings")

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey val sessionId: String,
    val gameId: String,
    val puzzleId: String,
    val statePayload: String,
    val elapsedMs: Long,
    val moveCount: Int,
    val hintsUsed: Int,
    val mistakes: Int,
    val startedAt: Long,
    val completedAt: Long?,
)

@Entity(tableName = "daily_records")
data class DailyRecordEntity(
    @PrimaryKey val recordId: String,
    val gameId: String,
    val puzzleDate: String,
    val difficulty: String,
    val completedAt: Long?,
    val elapsedMs: Long,
    val hintsUsed: Int,
    val mistakes: Int,
)

@Entity(tableName = "stats")
data class StatsEntity(
    @PrimaryKey val id: String,
    val solves: Int,
    val bestTimeMs: Long?,
    val totalTimeMs: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalHints: Int,
    val totalMistakes: Int,
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val unlockedAt: Long,
)

@Entity(tableName = "generated_puzzle_cache")
data class GeneratedPuzzleEntity(
    @PrimaryKey val cacheId: String,
    val gameId: String,
    val difficulty: String,
    val seed: Long,
    val generatorVersion: Int,
    val puzzlePayload: String,
    val createdAt: Long,
)

@Dao
interface HubDao {
    @Query("SELECT * FROM game_sessions WHERE completedAt IS NULL ORDER BY startedAt DESC")
    fun activeSessions(): Flow<List<GameSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: GameSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyRecord(record: DailyRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: StatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGeneratedPuzzle(puzzle: GeneratedPuzzleEntity)
}

@Database(
    entities = [
        GameSessionEntity::class,
        DailyRecordEntity::class,
        StatsEntity::class,
        AchievementEntity::class,
        GeneratedPuzzleEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class HubDatabase : RoomDatabase() {
    abstract fun hubDao(): HubDao
}

data class HubSettings(
    val themeId: String = "noir",
    val haptics: Boolean = true,
    val sound: Boolean = false,
    val reducedMotion: Boolean = false,
    val highContrast: Boolean = false,
)

class SettingsStore(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_id")
    private val hapticsKey = booleanPreferencesKey("haptics")
    private val soundKey = booleanPreferencesKey("sound")
    private val reducedMotionKey = booleanPreferencesKey("reduced_motion")
    private val highContrastKey = booleanPreferencesKey("high_contrast")

    val settings: Flow<HubSettings> = context.settingsDataStore.data.map { prefs ->
        HubSettings(
            themeId = prefs[themeKey] ?: "noir",
            haptics = prefs[hapticsKey] ?: true,
            sound = prefs[soundKey] ?: false,
            reducedMotion = prefs[reducedMotionKey] ?: false,
            highContrast = prefs[highContrastKey] ?: false,
        )
    }

    suspend fun setTheme(themeId: String) {
        context.settingsDataStore.edit { it[themeKey] = themeId }
    }

    suspend fun setBoolean(key: String, value: Boolean) {
        val preferenceKey = when (key) {
            "haptics" -> hapticsKey
            "sound" -> soundKey
            "reduced_motion" -> reducedMotionKey
            "high_contrast" -> highContrastKey
            else -> error("Unknown setting key: $key")
        }
        context.settingsDataStore.edit { it[preferenceKey] = value }
    }
}
