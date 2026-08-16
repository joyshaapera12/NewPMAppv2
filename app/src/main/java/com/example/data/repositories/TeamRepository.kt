package com.example.data.repositories

import android.content.Context
import com.example.core.network.NetworkClient
import com.example.data.mappers.DtoMappers
import com.example.domain.models.Match
import com.example.domain.models.TeamDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeamRepository(context: Context) {
    private val api = NetworkClient.getApiService(context)

    suspend fun getTeamDetail(teamId: String): Result<TeamDetail> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTeamDetail(teamId)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    val squad = (dto.players ?: emptyList()).map { DtoMappers.mapPlayer(it) }
                    val matches = (dto.recentMatches ?: emptyList()).mapNotNull { DtoMappers.mapMatch(it) }
                    val teamDetail = TeamDetail(
                        id = dto.id ?: dto.teamKey ?: teamId,
                        name = dto.name ?: dto.teamName ?: "Team",
                        logoUrl = dto.logo ?: dto.teamBadge ?: "",
                        country = dto.country ?: dto.countryName ?: dto.leagueCountry ?: "",
                        founded = dto.founded,
                        stadium = dto.stadium,
                        coach = dto.coach,
                        squad = squad,
                        recentMatches = matches
                    )
                    Result.success(teamDetail)
                } else {
                    Result.failure(Exception("Team data empty"))
                }
            } else {
                Result.failure(Exception("Failed to load team: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTeamMatches(teamId: String): Result<List<Match>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTeamMatches(teamId)
            if (response.isSuccessful) {
                val list = response.body()?.matches ?: response.body()?.data ?: emptyList()
                Result.success(list.mapNotNull { DtoMappers.mapMatch(it) })
            } else {
                Result.failure(Exception("Failed to load team matches: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
