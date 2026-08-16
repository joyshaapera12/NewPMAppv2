package com.example.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.models.Match
import com.example.domain.models.MatchStatus
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit,
    onTogglePin: (() -> Unit)? = null,
    isPinned: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isLive = match.isLive
    val cardBg = PitchMetricsTheme.colors.elevatedSurface
    val borderColor = if (isLive) PitchMetricsTheme.colors.liveRed.copy(alpha = 0.5f) else PitchMetricsTheme.colors.border

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: League & Status / Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (match.leagueLogoUrl.isNotBlank()) {
                        AsyncImage(
                            model = match.leagueLogoUrl,
                            contentDescription = match.leagueName,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = match.leagueName,
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchMetricsTheme.colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLive) {
                        LiveIndicator(minute = match.minute)
                    } else if (match.isFinished) {
                        Text(
                            text = "FT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    } else {
                        Text(
                            text = if (match.startTime.isNotBlank()) match.startTime else "Upcoming",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = PitchMetricsTheme.colors.pitchGreen
                        )
                    }

                    if (onTogglePin != null) {
                        IconButton(
                            onClick = onTogglePin,
                            modifier = Modifier.size(28.dp).testTag("pin_match_button_${match.id}")
                        ) {
                            Icon(
                                imageVector = if (isPinned) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Pin match",
                                tint = if (isPinned) PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Teams and Score Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeamLogo(url = match.homeTeam.logoUrl, name = match.homeTeam.name)
                    Text(
                        text = match.homeTeam.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Score / VS
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLive) PitchMetricsTheme.colors.liveRedMuted else PitchMetricsTheme.colors.glassBackground)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.homeScore != null && match.awayScore != null) {
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = if (isLive) PitchMetricsTheme.colors.liveRed else MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    }
                }

                // Away Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.awayTeam.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TeamLogo(url = match.awayTeam.logoUrl, name = match.awayTeam.name)
                }
            }

            // Footer: AI Prediction Insight if present
            if (match.aiPick != null || match.aiConfidence != null) {
                Divider(
                    color = PitchMetricsTheme.colors.border.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PitchMetricsTheme.colors.aiViolet.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI PICK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = PitchMetricsTheme.colors.aiViolet
                            )
                        }
                        if (match.aiPick != null) {
                            Text(
                                text = match.aiPick,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    if (match.aiConfidence != null) {
                        Text(
                            text = "${(match.aiConfidence * 100).toInt()}% Conf",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PitchMetricsTheme.colors.pitchGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeamLogo(
    url: String,
    name: String,
    size: Int = 28
) {
    if (url.isNotBlank()) {
        AsyncImage(
            model = url,
            contentDescription = name,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(PitchMetricsTheme.colors.border),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = name,
                tint = PitchMetricsTheme.colors.textMuted,
                modifier = Modifier.size((size * 0.65).dp)
            )
        }
    }
}
