package com.example.data.repositories

import android.content.Context
import com.example.core.network.NetworkClient
import com.example.data.mappers.DtoMappers
import com.example.domain.models.League
import com.example.domain.models.Match
import com.example.domain.models.Standing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LeagueRepository(context: Context) {
    private val api = NetworkClient.getApiService(context)

    suspend fun getLeagues(): Result<List<League>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLeagues()
            if (response.isSuccessful) {
                val list = response.body()?.leagues
                    ?: response.body()?.data
                    ?: response.body()?.results
                    ?: emptyList()
                Result.success(list.mapNotNull { DtoMappers.mapLeague(it) }.distinctBy { it.name })
            } else {
                Result.failure(Exception("Failed to fetch leagues: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStandings(leagueId: String): Result<List<Standing>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLeagueStandings(leagueId)
            if (response.isSuccessful) {
                val list = response.body()?.standings
                    ?: response.body()?.data
                    ?: response.body()?.results
                    ?: emptyList()
                Result.success(list.mapNotNull { DtoMappers.mapStanding(it) }.distinctBy { it.teamId })
            } else {
                Result.failure(Exception("Failed to fetch standings: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeagueMatches(leagueId: String): Result<List<Match>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLeagueMatches(leagueId)
            if (response.isSuccessful) {
                val list = response.body()?.matches
                    ?: response.body()?.data
                    ?: response.body()?.results
                    ?: emptyList()
                Result.success(list.mapNotNull { DtoMappers.mapMatch(it) }.distinctBy { it.id })
            } else {
                Result.failure(Exception("Failed to fetch league matches: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
