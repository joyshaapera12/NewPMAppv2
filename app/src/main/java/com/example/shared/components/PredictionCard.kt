package com.example.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.Prediction
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun PredictionCard(
    prediction: Prediction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = PitchMetricsTheme.colors.elevatedSurface
    val borderCol = PitchMetricsTheme.colors.border

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("prediction_card_${prediction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: League & Badges (Value Bet / Trending)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prediction.league,
                    style = MaterialTheme.typography.labelMedium,
                    color = PitchMetricsTheme.colors.textMuted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (prediction.isValueBet) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VALUE PICK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = PitchMetricsTheme.colors.pitchGreen
                            )
                        }
                    }
                    if (prediction.isTrending) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PitchMetricsTheme.colors.aiViolet.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TRENDING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = PitchMetricsTheme.colors.aiViolet
                            )
                        }
                    }
                }
            }

            // Teams vs Confidence Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TeamLogo(url = prediction.homeLogo, name = prediction.homeTeam, size = 22)
                        Text(
                            text = prediction.homeTeam,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TeamLogo(url = prediction.awayLogo, name = prediction.awayTeam, size = 22)
                        Text(
                            text = prediction.awayTeam,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Tip:",
                            style = MaterialTheme.typography.bodySmall,
                            color = PitchMetricsTheme.colors.textMuted
                        )
                        Text(
                            text = prediction.pick,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PitchMetricsTheme.colors.pitchGreen
                        )
                    }
                }

                ConfidenceRing(
                    confidence = prediction.confidence,
                    size = 58.dp,
                    strokeWidth = 5.dp
                )
            }

            // Tactical Reasoning
            Text(
                text = prediction.reasoning,
                style = MaterialTheme.typography.bodyMedium,
                color = PitchMetricsTheme.colors.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Probability Matrix Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PitchMetricsTheme.colors.glassBackground)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProbItem(label = "1", value = "${(prediction.homeWinProb * 100).toInt()}%")
                ProbItem(label = "X", value = "${(prediction.drawProb * 100).toInt()}%")
                ProbItem(label = "2", value = "${(prediction.awayWinProb * 100).toInt()}%")
                ProbItem(label = "O2.5", value = "${(prediction.over25Prob * 100).toInt()}%")
                ProbItem(label = "BTTS", value = "${(prediction.bttsProb * 100).toInt()}%")
            }
        }
    }
}

@Composable
private fun ProbItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PitchMetricsTheme.colors.textMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
