package com.example.core.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_matches")
data class CachedMatchEntity(
    @PrimaryKey val id: String,
    val leagueId: String,
    val leagueName: String,
    val homeTeamName: String,
    val homeTeamLogo: String,
    val awayTeamName: String,
    val awayTeamLogo: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: String,
    val minute: String?,
    val startTime: String,
    val date: String,
    val aiConfidence: Float?,
    val aiPick: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_predictions")
data class CachedPredictionEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    val pick: String,
    val confidence: Float,
    val reasoning: String,
    val isValueBet: Boolean,
    val isTrending: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_leagues")
data class CachedLeagueEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val logoUrl: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Dao
interface MatchDao {
    @Query("SELECT * FROM cached_matches ORDER BY date DESC, startTime ASC")
    fun getAllMatches(): Flow<List<CachedMatchEntity>>

    @Query("SELECT * FROM cached_matches ORDER BY date DESC, startTime ASC")
    suspend fun getCachedMatchesList(): List<CachedMatchEntity>

    @Query("SELECT * FROM cached_matches WHERE id = :id LIMIT 1")
    suspend fun getMatchById(id: String): CachedMatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<CachedMatchEntity>)

    @Query("DELETE FROM cached_matches")
    suspend fun clearAll()
}

@Dao
interface PredictionDao {
    @Query("SELECT * FROM cached_predictions ORDER BY confidence DESC")
    fun getAllPredictions(): Flow<List<CachedPredictionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPredictions(predictions: List<CachedPredictionEntity>)

    @Query("DELETE FROM cached_predictions")
    suspend fun clearAll()
}

@Dao
interface LeagueDao {
    @Query("SELECT * FROM cached_leagues ORDER BY name ASC")
    fun getAllLeagues(): Flow<List<CachedLeagueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeagues(leagues: List<CachedLeagueEntity>)

    @Query("DELETE FROM cached_leagues")
    suspend fun clearAll()
}

@Database(
    entities = [
        CachedMatchEntity::class,
        CachedPredictionEntity::class,
        CachedLeagueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PitchMetricsDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun predictionDao(): PredictionDao
    abstract fun leagueDao(): LeagueDao

    companion object {
        @Volatile
        private var INSTANCE: PitchMetricsDatabase? = null

        fun getInstance(context: Context): PitchMetricsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PitchMetricsDatabase::class.java,
                    "pitchmetrics_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
