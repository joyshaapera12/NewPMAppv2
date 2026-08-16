package com.example.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.domain.models.PlayerProfile
import com.example.ui.theme.PitchMetricsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailBottomSheet(
    player: PlayerProfile,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = PitchMetricsTheme.colors.border)
        },
        modifier = modifier.testTag("player_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.2f))
                            .border(2.dp, PitchMetricsTheme.colors.pitchGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${player.number}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            ),
                            color = PitchMetricsTheme.colors.pitchGreen
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PitchMetricsTheme.colors.glassBackground)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = player.position,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PitchMetricsTheme.colors.secondaryBlue
                                )
                            }
                            Text(
                                text = player.teamName,
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchMetricsTheme.colors.textMuted
                            )
                        }

                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = player.tacticalRole,
                            style = MaterialTheme.typography.bodySmall,
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    }

                    // Rating Pill
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.15f))
                            .border(1.dp, PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = String.format("%.2f", player.rating),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = PitchMetricsTheme.colors.pitchGreen
                        )
                        Text(
                            text = "RATING",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    }
                }
            }

            // Bio / Physical Stats Chips
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BioStatItem(label = "Age", value = "${player.age}")
                    BioStatItem(label = "Nation", value = player.nationality)
                    BioStatItem(label = "Height", value = player.height)
                    BioStatItem(label = "Foot", value = player.preferredFoot)
                    BioStatItem(label = "Value", value = player.marketValue)
                }
            }

            // Season Attacking & Playmaking Metrics
            Text(
                text = "Season Performance (${player.appearances} Apps • ${player.minutesPlayed} mins)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = "Goals",
                    value = "${player.goals}",
                    subtitle = "xG: ${String.format("%.1f", player.xg)}",
                    accentColor = PitchMetricsTheme.colors.pitchGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "Assists",
                    value = "${player.assists}",
                    subtitle = "xA: ${String.format("%.1f", player.xa)}",
                    accentColor = PitchMetricsTheme.colors.secondaryBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "Shot Conv.",
                    value = "${player.shotConversionRate.toInt()}%",
                    subtitle = "${player.shotsOnTarget}/${player.shotsTotal} SOT",
                    accentColor = PitchMetricsTheme.colors.aiViolet,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = "Pass Accuracy",
                    value = "${player.passAccuracy.toInt()}%",
                    subtitle = "${player.keyPassesPer90} Key/90",
                    accentColor = PitchMetricsTheme.colors.pitchGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "Yellow Cards",
                    value = "${player.yellowCards}",
                    subtitle = "Fouls discipline",
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "Red Cards",
                    value = "${player.redCards}",
                    subtitle = "Expulsions",
                    accentColor = PitchMetricsTheme.colors.liveRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BioStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = accentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = PitchMetricsTheme.colors.textMuted
            )
        }
    }
}
