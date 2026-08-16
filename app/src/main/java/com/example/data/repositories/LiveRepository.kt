package com.example.data.repositories

import android.content.Context
import com.example.core.network.NetworkClient
import com.example.core.network.SafeApiLogger
import com.example.data.mappers.DtoMappers
import com.example.domain.models.LiveMatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LiveRepository(context: Context) {
    private val api = NetworkClient.getApiService(context)
    private var lastCachedLiveMatches: List<LiveMatch> = emptyList()

    suspend fun getLiveMatches(): Result<List<LiveMatch>> = withContext(Dispatchers.IO) {
        val endpoint = "allsports-live-all"
        SafeApiLogger.logRequest("https://pitchmetrics.online/api/", endpoint)
        try {
            val response = api.getAllSportsLive()
            val httpStatus = response.code()
            val contentType = response.raw().header("Content-Type")

            if (response.isSuccessful) {
                val envelope = response.body()
                val topLevelKeys = mutableListOf<String>()
                if (envelope?.matches != null) topLevelKeys.add("matches")
                if (envelope?.data != null) topLevelKeys.add("data")
                if (envelope?.results != null) topLevelKeys.add("results")

                val list = envelope?.matches ?: envelope?.results ?: envelope?.data ?: emptyList()
                val liveMatches = list.mapNotNull { DtoMappers.mapAllSportsLive(it) }.distinctBy { it.eventKey }

                val first = liveMatches.firstOrNull()
                SafeApiLogger.logResponse(
                    endpoint = endpoint,
                    httpStatus = httpStatus,
                    contentType = contentType,
                    topLevelKeys = topLevelKeys,
                    matchesCount = liveMatches.size,
                    firstMatchId = first?.eventKey,
                    firstMatchStatus = first?.status,
                    firstHomeNameNonEmpty = !first?.homeTeamName.isNullOrBlank(),
                    firstAwayNameNonEmpty = !first?.awayTeamName.isNullOrBlank()
                )

                if (liveMatches.isNotEmpty()) {
                    lastCachedLiveMatches = liveMatches
                }
                Result.success(liveMatches)
            } else {
                val errorMsg = "HTTP $httpStatus: ${response.message()}"
                SafeApiLogger.logError(endpoint, httpStatus, errorMsg, null)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            SafeApiLogger.logError(endpoint, null, e.localizedMessage, e.javaClass.simpleName)
            Result.failure(e)
        }
    }

    fun getLastCached(): List<LiveMatch> = lastCachedLiveMatches
}
