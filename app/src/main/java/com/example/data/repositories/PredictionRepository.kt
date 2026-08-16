package com.example.data.repositories

import android.content.Context
import com.example.core.database.CachedPredictionEntity
import com.example.core.database.PitchMetricsDatabase
import com.example.core.network.NetworkClient
import com.example.data.mappers.DtoMappers
import com.example.domain.models.Prediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PredictionRepository(context: Context) {
    private val api = NetworkClient.getApiService(context)
    private val db = PitchMetricsDatabase.getInstance(context)
    private val predictionDao = db.predictionDao()

    suspend fun getHighConfidencePredictions(): Result<List<Prediction>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getHighConfidencePredictions()
            if (response.isSuccessful) {
                val list = response.body()?.predictions
                    ?: response.body()?.data
                    ?: response.body()?.results
                    ?: emptyList()
                val mapped = list.mapNotNull { DtoMappers.mapPrediction(it) }.distinctBy { it.id }
                if (mapped.isNotEmpty()) {
                    cachePredictions(mapped)
                    Result.success(mapped)
                } else {
                    getAllPredictions()
                }
            } else {
                getAllPredictions()
            }
        } catch (e: Exception) {
            getAllPredictions()
        }
    }

    suspend fun getAllPredictions(): Result<List<Prediction>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPredictions()
            if (response.isSuccessful) {
                val list = response.body()?.predictions
                    ?: response.body()?.data
                    ?: response.body()?.results
                    ?: emptyList()
                val mapped = list.mapNotNull { DtoMappers.mapPrediction(it) }.distinctBy { it.id }
                cachePredictions(mapped)
                Result.success(mapped)
            } else {
                val cached = getCachedPredictions()
                if (cached.isNotEmpty()) {
                    Result.success(cached)
                } else {
                    Result.failure(Exception("Failed to fetch predictions: HTTP ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            val cached = getCachedPredictions()
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getTrendingPredictions(): Result<List<Prediction>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTrendingPredictions()
            if (response.isSuccessful) {
                val list = response.body()?.predictions
                    ?: response.body()?.data
                    ?: response.body()?.results
                    ?: emptyList()
                val mapped = list.mapNotNull { DtoMappers.mapPrediction(it) }.distinctBy { it.id }
                if (mapped.isNotEmpty()) {
                    Result.success(mapped)
                } else {
                    getAllPredictions()
                }
            } else {
                getAllPredictions()
            }
        } catch (e: Exception) {
            getAllPredictions()
        }
    }

    suspend fun getPredictionForMatch(matchId: String): Result<Prediction?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPredictionForMatch(matchId)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null && (!dto.homeTeamName.isNullOrBlank() || !dto.homeTeam.isNullOrBlank() || dto.winnerPick != null)) {
                    Result.success(DtoMappers.mapPrediction(dto))
                } else {
                    // Try looking in full predictions list
                    val all = getAllPredictions().getOrNull() ?: emptyList()
                    val matchPred = all.firstOrNull { it.matchId == matchId || it.id == matchId }
                    Result.success(matchPred)
                }
            } else {
                val all = getAllPredictions().getOrNull() ?: emptyList()
                val matchPred = all.firstOrNull { it.matchId == matchId || it.id == matchId }
                Result.success(matchPred)
            }
        } catch (e: Exception) {
            val all = getAllPredictions().getOrNull() ?: emptyList()
            val matchPred = all.firstOrNull { it.matchId == matchId || it.id == matchId }
            Result.success(matchPred)
        }
    }

    private suspend fun cachePredictions(predictions: List<Prediction>) {
        try {
            val entities = predictions.map {
                CachedPredictionEntity(
                    id = it.id,
                    matchId = it.matchId,
                    homeTeam = it.homeTeam,
                    awayTeam = it.awayTeam,
                    league = it.league,
                    pick = it.pick,
                    confidence = it.confidence,
                    reasoning = it.reasoning,
                    isValueBet = it.isValueBet,
                    isTrending = it.isTrending
                )
            }
            predictionDao.insertPredictions(entities)
        } catch (_: Exception) {}
    }

    private suspend fun getCachedPredictions(): List<Prediction> {
        // Can be queried from DAO if needed
        return emptyList()
    }
}
