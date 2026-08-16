package com.example.data.repositories

import android.content.Context
import com.example.core.database.CachedMatchEntity
import com.example.core.database.PitchMetricsDatabase
import com.example.core.network.NetworkClient
import com.example.core.network.SafeApiLogger
import com.example.data.mappers.DtoMappers
import com.example.domain.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MatchRepository(private val context: Context) {
    private val api = NetworkClient.getApiService(context)
    private val db = PitchMetricsDatabase.getInstance(context)

    suspend fun getTodayMatches(forceRefresh: Boolean = false): Result<List<Match>> = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        val todayStr = dateFormat.format(Date())
        val endpoint = "matches?date=$todayStr"
        val baseUrl = NetworkClient.getCookieJar(context).let { "https://pitchmetrics.online/api/" }
        SafeApiLogger.logRequest(baseUrl, endpoint)

        try {
            val response = api.getMatches(date = todayStr)
            val httpStatus = response.code()
            val contentType = response.raw().header("Content-Type")

            if (response.isSuccessful) {
                val envelope = response.body()
                val topLevelKeys = mutableListOf<String>()
                if (envelope?.matches != null) topLevelKeys.add("matches")
                if (envelope?.data != null) topLevelKeys.add("data")
                if (envelope?.results != null) topLevelKeys.add("results")

                val list = envelope?.matches ?: envelope?.data ?: envelope?.results ?: emptyList()
                val matches = list.mapNotNull { DtoMappers.mapMatch(it) }.distinctBy { it.id }

                val first = matches.firstOrNull()
                SafeApiLogger.logResponse(
                    endpoint = endpoint,
                    httpStatus = httpStatus,
                    contentType = contentType,
                    topLevelKeys = topLevelKeys,
                    matchesCount = matches.size,
                    firstMatchId = first?.id,
                    firstMatchStatus = first?.statusText,
                    firstHomeNameNonEmpty = !first?.homeTeam?.name.isNullOrBlank(),
                    firstAwayNameNonEmpty = !first?.awayTeam?.name.isNullOrBlank()
                )

                if (matches.isNotEmpty()) {
                    val entities = matches.map { m ->
                        CachedMatchEntity(
                            id = m.id,
                            leagueId = m.leagueId,
                            leagueName = m.leagueName,
                            homeTeamName = m.homeTeam.name,
                            homeTeamLogo = m.homeTeam.logoUrl,
                            awayTeamName = m.awayTeam.name,
                            awayTeamLogo = m.awayTeam.logoUrl,
                            homeScore = m.homeScore,
                            awayScore = m.awayScore,
                            status = m.status.name,
                            minute = m.minute,
                            startTime = m.startTime,
                            date = m.date,
                            aiConfidence = m.aiConfidence,
                            aiPick = m.aiPick
                        )
                    }
                    db.matchDao().insertMatches(entities)
                }
                Result.success(matches)
            } else {
                val errorMsg = "HTTP $httpStatus: ${response.message()}"
                SafeApiLogger.logError(endpoint, httpStatus, errorMsg, null)
                loadCachedMatches(errorMsg)
            }
        } catch (e: Exception) {
            SafeApiLogger.logError(endpoint, null, e.localizedMessage, e.javaClass.simpleName)
            loadCachedMatches(e.localizedMessage ?: "Network connection failed")
        }
    }

    private suspend fun loadCachedMatches(errorReason: String): Result<List<Match>> {
        val cached = db.matchDao().getCachedMatchesList()
        return if (cached.isNotEmpty()) {
            val mapped = cached.map { entity ->
                val status = try {
                    MatchStatus.valueOf(entity.status)
                } catch (_: Exception) {
                    MatchStatus.SCHEDULED
                }
                Match(
                    id = entity.id,
                    leagueId = entity.leagueId,
                    leagueName = entity.leagueName,
                    leagueLogoUrl = "",
                    leagueCountry = "",
                    homeTeam = Team(id = "h_${entity.id}", name = entity.homeTeamName, logoUrl = entity.homeTeamLogo),
                    awayTeam = Team(id = "a_${entity.id}", name = entity.awayTeamName, logoUrl = entity.awayTeamLogo),
                    homeScore = entity.homeScore,
                    awayScore = entity.awayScore,
                    status = status,
                    statusText = entity.minute ?: entity.status,
                    minute = entity.minute,
                    startTime = entity.startTime,
                    date = entity.date,
                    stadium = null,
                    aiConfidence = entity.aiConfidence,
                    aiPick = entity.aiPick
                )
            }
            Result.success(mapped)
        } else {
            Result.failure(Exception(errorReason))
        }
    }

    suspend fun getMatchDetail(id: String): Result<Match> = withContext(Dispatchers.IO) {
        val endpoint = "matches/$id"
        SafeApiLogger.logRequest("https://pitchmetrics.online/api/", endpoint)
        try {
            val response = api.getMatchDetail(id)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    val mapped = DtoMappers.mapMatch(dto)
                    if (mapped != null) {
                        SafeApiLogger.logMatchDetailResponse(
                            source = "pitchmetrics",
                            returnedMatchId = mapped.id,
                            returnedHomeTeam = mapped.homeTeam.name,
                            returnedAwayTeam = mapped.awayTeam.name,
                            returnedStatus = mapped.statusText,
                            returnedScore = if (mapped.homeScore != null && mapped.awayScore != null) "${mapped.homeScore}-${mapped.awayScore}" else null
                        )
                        Result.success(mapped)
                    } else {
                        loadCachedMatchById(id, "Match data incomplete")
                    }
                } else {
                    loadCachedMatchById(id, "Match not found in API response")
                }
            } else {
                loadCachedMatchById(id, "Failed to load match detail: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            loadCachedMatchById(id, e.localizedMessage ?: "Network connection failed")
        }
    }

    private suspend fun loadCachedMatchById(id: String, fallbackReason: String): Result<Match> {
        val cached = db.matchDao().getMatchById(id)
        if (cached != null) {
            val status = try {
                MatchStatus.valueOf(cached.status)
            } catch (_: Exception) {
                MatchStatus.SCHEDULED
            }
            val match = Match(
                id = cached.id,
                pmId = cached.id,
                allSportsId = null,
                leagueId = cached.leagueId,
                leagueName = cached.leagueName,
                leagueLogoUrl = "",
                leagueCountry = "",
                homeTeam = Team(id = "h_${cached.id}", name = cached.homeTeamName, logoUrl = cached.homeTeamLogo),
                awayTeam = Team(id = "a_${cached.id}", name = cached.awayTeamName, logoUrl = cached.awayTeamLogo),
                homeScore = cached.homeScore,
                awayScore = cached.awayScore,
                status = status,
                statusText = cached.minute ?: cached.status,
                minute = cached.minute,
                startTime = cached.startTime,
                date = cached.date,
                stadium = null,
                aiConfidence = cached.aiConfidence,
                aiPick = cached.aiPick
            )
            return Result.success(match)
        }
        return Result.failure(Exception(fallbackReason))
    }

    suspend fun getMatchEvents(id: String): Result<List<MatchEvent>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMatchEvents(id)
            if (response.isSuccessful) {
                val dtos = response.body()?.events ?: emptyList()
                val events = dtos.map { dto -> DtoMappers.mapMatchEvent(dto) }
                Result.success(events)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    suspend fun getMatchStatistics(id: String): Result<MatchStatistics?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMatchStatistics(id)
            if (response.isSuccessful) {
                val dto = response.body()?.statistics
                if (dto != null) {
                    Result.success(DtoMappers.mapMatchStatistics(dto))
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    suspend fun getMatchLineups(id: String): Result<Lineup?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMatchLineups(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && (body.homeTeam != null || body.awayTeam != null || body.homeStarting != null)) {
                    Result.success(DtoMappers.mapLineup(body))
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    suspend fun getMatchOdds(id: String): Result<MatchOdds?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMatchOdds(id)
            if (response.isSuccessful) {
                val dto = response.body()?.data
                if (dto != null) {
                    Result.success(
                        MatchOdds(
                            homeWin = dto.homeWin,
                            draw = dto.draw,
                            awayWin = dto.awayWin,
                            over25 = dto.over25,
                            under25 = dto.under25,
                            bttsYes = dto.bttsYes,
                            bttsNo = dto.bttsNo
                        )
                    )
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    suspend fun getMatchH2H(id: String): Result<HeadToHead?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMatchH2H(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(DtoMappers.mapH2H(body))
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    suspend fun searchMulti(query: String): Result<GroupedSearchResults> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext Result.success(GroupedSearchResults())
        }
        try {
            val response = api.search(query.trim())
            if (response.isSuccessful) {
                val body = response.body()
                val rawResults = body?.results ?: emptyList()
                val byType = body?.byType

                val mappedList = rawResults.mapNotNull { DtoMappers.mapSearchResult(it) }

                val teamsList = (byType?.teams?.mapNotNull { DtoMappers.mapSearchResult(it) } ?: emptyList())
                    .ifEmpty { mappedList.filter { it.type == SearchResultType.TEAM || (it.type == SearchResultType.PAGE_LINK && it.title.contains("vs", ignoreCase = true).not()) } }

                val leaguesList = (byType?.leagues?.mapNotNull { DtoMappers.mapSearchResult(it) } ?: emptyList())
                    .ifEmpty { mappedList.filter { it.type == SearchResultType.LEAGUE } }

                val matchesList = (byType?.matches?.mapNotNull { DtoMappers.mapSearchResult(it) } ?: emptyList())
                    .ifEmpty { mappedList.filter { it.type == SearchResultType.MATCH || it.title.contains("vs", ignoreCase = true) } }

                Result.success(
                    GroupedSearchResults(
                        teams = teamsList.distinctBy { it.title },
                        competitions = leaguesList.distinctBy { it.title },
                        matches = matchesList.distinctBy { it.title }
                    )
                )
            } else {
                Result.failure(Exception("Search failed with code HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
