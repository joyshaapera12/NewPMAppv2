package com.example.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.models.Prediction

object PitchMetricsNotificationManager {

    const val CHANNEL_LIVE_ALERTS = "pitchmetrics_live_alerts"
    const val CHANNEL_AI_PICKS = "pitchmetrics_ai_picks"
    const val CHANNEL_TEAM_ALERTS = "pitchmetrics_team_alerts"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val liveChannel = NotificationChannel(
                CHANNEL_LIVE_ALERTS,
                "Live Match Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time goal updates, red cards, and match status events"
                enableVibration(true)
            }

            val aiChannel = NotificationChannel(
                CHANNEL_AI_PICKS,
                "AI Predictions & Match IQ",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "High confidence algorithmic picks and tactical insights"
            }

            val teamChannel = NotificationChannel(
                CHANNEL_TEAM_ALERTS,
                "Followed Teams & Competitions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Kickoff reminders and lineup announcements for your tracked teams"
            }

            notificationManager.createNotificationChannels(listOf(liveChannel, aiChannel, teamChannel))
        }
    }

    fun showGoalNotification(
        context: Context,
        matchId: String,
        homeTeam: String,
        awayTeam: String,
        score: String,
        minute: String,
        scorer: String?
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_match_id", matchId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            matchId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚽ GOAL! $score ($minute)"
        val body = if (!scorer.isNullOrBlank()) {
            "$scorer scored in $homeTeam vs $awayTeam"
        } else {
            "$homeTeam $score $awayTeam"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_LIVE_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\nTap to open Match Centre and tactical metrics."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(matchId.hashCode() + 100, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showHighConfidencePickNotification(
        context: Context,
        prediction: Prediction
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_match_id", prediction.matchId)
            putExtra("target_tab", "predictions")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            prediction.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🔥 High Confidence Pick (${prediction.confidencePercent}%)"
        val body = "${prediction.homeTeam} vs ${prediction.awayTeam}: ${prediction.pick}"

        val builder = NotificationCompat.Builder(context, CHANNEL_AI_PICKS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n${prediction.reasoning}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(prediction.id.hashCode() + 200, builder.build())
        } catch (_: SecurityException) {}
    }
}
