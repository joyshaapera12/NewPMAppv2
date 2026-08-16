package com.example.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material.icons.filled.Sports
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
import com.example.domain.models.RefereeAnalytics
import com.example.domain.models.StadiumAnalytics
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun RefereeAnalyticsCard(
    referee: RefereeAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("referee_analytics_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Sports,
                        contentDescription = null,
                        tint = PitchMetricsTheme.colors.pitchGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = referee.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Match Official • ${referee.matchesCount} Matches Scored",
                            style = MaterialTheme.typography.bodySmall,
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (referee.strictnessLevel) {
                                "Strict" -> PitchMetricsTheme.colors.liveRed.copy(alpha = 0.15f)
                                "Lenient" -> PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.15f)
                                else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${referee.strictnessLevel.uppercase()} STRICTNESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = when (referee.strictnessLevel) {
                            "Strict" -> PitchMetricsTheme.colors.liveRed
                            "Lenient" -> PitchMetricsTheme.colors.pitchGreen
                            else -> Color(0xFFF59E0B)
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(label = "Fouls/90", value = String.format("%.1f", referee.foulsPerGame), color = MaterialTheme.colorScheme.onBackground)
                StatColumn(label = "Yellows/90", value = String.format("%.1f", referee.yellowCardsPerGame), color = Color(0xFFF59E0B))
                StatColumn(label = "Reds/90", value = String.format("%.2f", referee.redCardsPerGame), color = PitchMetricsTheme.colors.liveRed)
                StatColumn(label = "Pens/90", value = String.format("%.2f", referee.penaltiesPerGame), color = PitchMetricsTheme.colors.secondaryBlue)
            }
        }
    }
}

@Composable
fun StadiumAnalyticsCard(
    stadium: StadiumAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stadium_analytics_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Stadium,
                        contentDescription = null,
                        tint = PitchMetricsTheme.colors.pitchGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = stadium.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${stadium.city} • ${stadium.capacity.toString().replace(Regex("(\\d)(?=(\\d{3})+$)"), "$1,")} Capacity",
                            style = MaterialTheme.typography.bodySmall,
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${stadium.atmosphereRating} ATMOSPHERE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PitchMetricsTheme.colors.pitchGreen
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(label = "Home Win %", value = "${stadium.homeWinRatePercent.toInt()}%", color = PitchMetricsTheme.colors.pitchGreen)
                StatColumn(label = "Avg Goals/Game", value = String.format("%.2f", stadium.avgGoalsPerGame), color = MaterialTheme.colorScheme.onBackground)
                StatColumn(label = "Surface", value = stadium.surface.split(" ").firstOrNull() ?: "Grass", color = PitchMetricsTheme.colors.textMuted)
            }
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}
