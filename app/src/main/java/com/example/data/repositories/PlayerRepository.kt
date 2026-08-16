package com.example.data.repositories

import android.content.Context
import com.example.core.network.NetworkClient
import com.example.domain.models.PlayerProfile
import com.example.domain.models.RefereeAnalytics
import com.example.domain.models.StadiumAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class PlayerRepository(context: Context) {
    private val api = NetworkClient.getApiService(context)
    private val playerCache = mutableMapOf<String, PlayerProfile>()

    suspend fun getPlayerProfile(
        playerId: String,
        playerName: String = "Player",
        teamName: String = "Club",
        position: String = "MF",
        number: String = "10"
    ): Result<PlayerProfile> = withContext(Dispatchers.IO) {
        val cached = playerCache[playerId]
        if (cached != null) return@withContext Result.success(cached)

        // Deterministic realistic stats generation based on player identity & position
        val hash = abs(playerId.hashCode() + playerName.hashCode())
        val isFw = position.contains("FW", ignoreCase = true) || position.contains("ST", ignoreCase = true) || position.contains("W", ignoreCase = true)
        val isMf = position.contains("MF", ignoreCase = true) || position.contains("CM", ignoreCase = true) || position.contains("DM", ignoreCase = true) || position.contains("AM", ignoreCase = true)
        val isDf = position.contains("DF", ignoreCase = true) || position.contains("CB", ignoreCase = true) || position.contains("LB", ignoreCase = true) || position.contains("RB", ignoreCase = true)
        val isGk = position.contains("GK", ignoreCase = true)

        val appearances = 18 + (hash % 14)
        val minutesPlayed = appearances * (70 + (hash % 20))
        val goals = when {
            isFw -> 6 + (hash % 16)
            isMf -> 2 + (hash % 8)
            isDf -> hash % 3
            else -> 0
        }
        val assists = when {
            isFw -> 3 + (hash % 8)
            isMf -> 4 + (hash % 10)
            isDf -> 1 + (hash % 4)
            else -> 0
        }
        val shotsTotal = when {
            isFw -> (goals * 4) + (hash % 15)
            isMf -> (goals * 5) + (hash % 20)
            else -> goals * 6 + (hash % 5)
        }.coerceAtLeast(goals)

        val shotsOnTarget = (shotsTotal * (0.42f + (hash % 20) / 100f)).toInt().coerceAtLeast(goals)
        val conversion = if (shotsTotal > 0) ((goals.toFloat() / shotsTotal.toFloat()) * 100f) else 0f
        val xg = (goals * 0.92f) + ((hash % 10) / 10f)
        val xa = (assists * 0.88f) + ((hash % 8) / 10f)
        val rating = 6.6f + ((hash % 25) / 15f)

        val profile = PlayerProfile(
            id = playerId,
            name = playerName,
            number = number,
            position = position,
            teamName = teamName,
            teamLogo = null,
            photoUrl = null,
            age = 20 + (hash % 14),
            nationality = getNationalityForHash(hash),
            height = "${174 + (hash % 20)} cm",
            weight = "${70 + (hash % 18)} kg",
            preferredFoot = if (hash % 3 == 0) "Left" else "Right",
            marketValue = "€${15 + (hash % 70)}.5M",
            appearances = appearances,
            minutesPlayed = minutesPlayed,
            goals = goals,
            assists = assists,
            shotsTotal = shotsTotal,
            shotsOnTarget = shotsOnTarget,
            shotConversionRate = conversion,
            xg = xg,
            xa = xa,
            keyPassesPer90 = if (isMf || isFw) 1.4f + ((hash % 15) / 10f) else 0.5f,
            passAccuracy = if (isMf || isDf) 86.0f + (hash % 10) else 78.0f + (hash % 12),
            yellowCards = (hash % 6),
            redCards = if (hash % 8 == 0) 1 else 0,
            rating = rating,
            tacticalRole = getTacticalRole(position)
        )

        playerCache[playerId] = profile
        Result.success(profile)
    }

    fun getRefereeAnalytics(refereeName: String?): RefereeAnalytics {
        val name = refereeName?.ifBlank { "Michael Oliver" } ?: "Michael Oliver"
        val hash = abs(name.hashCode())
        val fouls = 18.0f + (hash % 8) + ((hash % 10) / 10f)
        val yellows = 3.5f + ((hash % 20) / 10f)
        val reds = 0.15f + ((hash % 15) / 100f)
        val pens = 0.25f + ((hash % 20) / 100f)
        val strictness = if (yellows > 4.5f) "Strict" else if (yellows < 3.8f) "Lenient" else "Moderate"

        return RefereeAnalytics(
            name = name,
            matchesCount = 16 + (hash % 12),
            foulsPerGame = fouls,
            yellowCardsPerGame = yellows,
            redCardsPerGame = reds,
            penaltiesPerGame = pens,
            strictnessLevel = strictness
        )
    }

    fun getStadiumAnalytics(stadiumName: String?, homeTeam: String = "Home Team"): StadiumAnalytics {
        val name = stadiumName?.ifBlank { "$homeTeam Stadium" } ?: "$homeTeam Stadium"
        val hash = abs(name.hashCode())
        val capacity = 35000 + ((hash % 45) * 1000)
        val homeWin = 52.0f + (hash % 20)
        val avgGoals = 2.4f + ((hash % 15) / 10f)
        val rating = 8.0f + ((hash % 18) / 10f)

        return StadiumAnalytics(
            name = name,
            city = if (hash % 2 == 0) "London" else "Madrid",
            capacity = capacity,
            surface = "Hybrid Grass",
            homeWinRatePercent = homeWin,
            avgGoalsPerGame = avgGoals,
            atmosphereRating = rating.coerceAtMost(10.0f)
        )
    }

    private fun getNationalityForHash(hash: Int): String {
        val nations = listOf("England", "Spain", "France", "Germany", "Brazil", "Argentina", "Portugal", "Netherlands", "Italy", "Belgium", "Nigeria", "Japan")
        return nations[hash % nations.size]
    }

    private fun getTacticalRole(position: String): String {
        return when {
            position.contains("GK", true) -> "Sweeper Keeper"
            position.contains("CB", true) -> "Ball-Playing Centre Back"
            position.contains("LB", true) || position.contains("RB", true) -> "Attacking Wing-Back"
            position.contains("DM", true) -> "Deep-Lying Playmaker"
            position.contains("CM", true) -> "Box-to-Box Engine"
            position.contains("AM", true) -> "Advanced Playmaker / Shadow Striker"
            position.contains("W", true) -> "Inverted Winger"
            position.contains("ST", true) || position.contains("FW", true) -> "Complete Forward"
            else -> "Tactical Utility"
        }
    }
}
