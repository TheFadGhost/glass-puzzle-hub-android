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
import androidx.room.Room
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

    @Query("SELECT * FROM game_sessions WHERE gameId = :gameId AND completedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun activeSessionForGame(gameId: String): Flow<GameSessionEntity?>

    @Query("SELECT * FROM game_sessions WHERE gameId = :gameId AND completedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun latestActiveSessionForGame(gameId: String): GameSessionEntity?

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

class SessionRepository private constructor(private val dao: HubDao) {
    val activeSessions: Flow<List<GameSessionEntity>> = dao.activeSessions()

    fun activeSessionForGame(gameId: String): Flow<GameSessionEntity?> =
        dao.activeSessionForGame(gameId)

    suspend fun latestActiveSessionForGame(gameId: String): GameSessionEntity? =
        dao.latestActiveSessionForGame(gameId)

    suspend fun upsertSession(session: GameSessionEntity) {
        dao.upsertSession(session)
    }

    suspend fun completeSession(
        session: GameSessionEntity,
        statePayload: String,
        elapsedMs: Long,
        completedAt: Long = System.currentTimeMillis(),
    ) {
        dao.upsertSession(
            session.copy(
                statePayload = statePayload,
                elapsedMs = elapsedMs,
                completedAt = completedAt,
            ),
        )
    }

    companion object {
        @Volatile
        private var instance: SessionRepository? = null

        fun get(context: Context): SessionRepository =
            instance ?: synchronized(this) {
                instance ?: SessionRepository(
                    Room.databaseBuilder(
                        context.applicationContext,
                        HubDatabase::class.java,
                        "glass-puzzle-hub.db",
                    ).build().hubDao(),
                ).also { instance = it }
            }
    }
}

data class HubSettings(
    val themeId: String = "solar",
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
            themeId = prefs[themeKey] ?: "solar",
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
