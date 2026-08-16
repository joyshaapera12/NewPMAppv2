package com.example.core.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.core.notifications.PitchMetricsNotificationManager
import com.example.core.storage.UserPreferencesRepository
import com.example.data.repositories.LiveRepository
import com.example.data.repositories.PredictionRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class MatchSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val liveRepo = LiveRepository(appContext)
    private val predictionRepo = PredictionRepository(appContext)
    private val userPrefs = UserPreferencesRepository(appContext)

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting periodic MatchSyncWorker execution")
        try {
            val goalAlertsEnabled = userPrefs.notifyLiveGoalsFlow.first()
            val aiAlertsEnabled = userPrefs.notifyAiPicksFlow.first()
            val sharedPrefs = appContext.getSharedPreferences("pitchmetrics_sync_cache", Context.MODE_PRIVATE)

            // 1. Live Match Score Monitoring
            if (goalAlertsEnabled) {
                val liveRes = liveRepo.getLiveMatches()
                val liveMatches = liveRes.getOrDefault(emptyList())

                for (match in liveMatches) {
                    val cacheKey = "score_${match.eventKey}"
                    val previousScore = sharedPrefs.getString(cacheKey, null)
                    val currentScore = match.score.ifBlank { "${match.homeScore ?: 0} - ${match.awayScore ?: 0}" }

                    if (previousScore != null && previousScore != currentScore && match.isLive) {
                        Log.d(TAG, "Score change detected for ${match.homeTeamName} vs ${match.awayTeamName}: $previousScore -> $currentScore")
                        PitchMetricsNotificationManager.showGoalNotification(
                            context = appContext,
                            matchId = match.eventKey,
                            homeTeam = match.homeTeamName,
                            awayTeam = match.awayTeamName,
                            score = currentScore,
                            minute = match.minute.ifBlank { "Live" },
                            scorer = null
                        )
                    }
                    sharedPrefs.edit().putString(cacheKey, currentScore).apply()
                }
            }

            // 2. High Confidence AI Prediction Check
            if (aiAlertsEnabled) {
                val minConfidence = userPrefs.confidenceThresholdFlow.first().toFloat() / 100f
                val predsRes = predictionRepo.getHighConfidencePredictions()
                val predictions = predsRes.getOrDefault(emptyList())

                val topPick = predictions.firstOrNull { it.confidence >= minConfidence }
                if (topPick != null) {
                    val alertKey = "ai_alert_sent_${topPick.id}"
                    val alreadySent = sharedPrefs.getBoolean(alertKey, false)
                    if (!alreadySent) {
                        PitchMetricsNotificationManager.showHighConfidencePickNotification(
                            context = appContext,
                            prediction = topPick
                        )
                        sharedPrefs.edit().putBoolean(alertKey, true).apply()
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "MatchSyncWorker failed with exception", e)
            return Result.retry()
        }
    }

    companion object {
        private const val TAG = "MatchSyncWorker"
        const val WORK_NAME = "pitchmetrics_periodic_sync"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<MatchSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            Log.d(TAG, "Enqueued periodic sync worker (15-minute interval)")
        }
    }
}
