package com.example.feature.predictions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.PredictorLeaderboardUser
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun PredictorLeaderboardCard(
    leaderboard: List<PredictorLeaderboardUser>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("predictor_leaderboard_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Community Forecaster Leaderboard",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Top modelers & predictor accuracy rankings",
                            style = MaterialTheme.typography.bodySmall,
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    }
                }
            }

            Divider(color = PitchMetricsTheme.colors.border.copy(alpha = 0.5f))

            leaderboard.take(6).forEach { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (user.rank == 1) PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.08f)
                            else PitchMetricsTheme.colors.glassBackground
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Rank Badge
                    val rankBgColor = when (user.rank) {
                        1 -> Color(0xFFF59E0B)
                        2 -> Color(0xFF94A3B8)
                        3 -> Color(0xFFD97706)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(rankBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${user.rank}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (user.rank <= 3) Color.Black else MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    // User Info & Badge
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = user.badgeTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    }

                    // Streak Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PitchMetricsTheme.colors.elevatedSurface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = PitchMetricsTheme.colors.liveRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${user.streak}W",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PitchMetricsTheme.colors.liveRed
                            )
                        )
                    }

                    // Accuracy & Points
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${user.winRatePercent.toInt()}% ACC",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PitchMetricsTheme.colors.pitchGreen
                        )
                        Text(
                            text = "${user.points} pts",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    }
                }
            }
        }
    }
}
