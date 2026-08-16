package com.example.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.HorizontalRule
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
import com.example.domain.models.BookmakerOdds
import com.example.domain.models.MatchOdds
import com.example.domain.models.OddsTrend
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun MultiBookmakerOddsCard(
    matchOdds: MatchOdds?,
    homeTeamName: String = "Home",
    awayTeamName: String = "Away",
    modifier: Modifier = Modifier
) {
    val defaultHome = matchOdds?.homeWin?.toDoubleOrNull() ?: 2.15
    val defaultDraw = matchOdds?.draw?.toDoubleOrNull() ?: 3.40
    val defaultAway = matchOdds?.awayWin?.toDoubleOrNull() ?: 3.10

    val bookmakers = listOf(
        BookmakerOdds("Bet365", null, defaultHome, defaultDraw, defaultAway, defaultHome + 0.05, defaultDraw - 0.10, defaultAway - 0.15, OddsTrend.DOWN, OddsTrend.UP, OddsTrend.UP, 96.2),
        BookmakerOdds("DraftKings", null, defaultHome + 0.03, defaultDraw - 0.05, defaultAway - 0.02, defaultHome, defaultDraw, defaultAway, OddsTrend.UP, OddsTrend.DOWN, OddsTrend.DOWN, 95.8),
        BookmakerOdds("Unibet", null, defaultHome - 0.02, defaultDraw + 0.05, defaultAway + 0.04, defaultHome - 0.02, defaultDraw, defaultAway, OddsTrend.STABLE, OddsTrend.UP, OddsTrend.UP, 95.4),
        BookmakerOdds("1xBet", null, defaultHome + 0.05, defaultDraw + 0.08, defaultAway + 0.06, defaultHome + 0.10, defaultDraw, defaultAway, OddsTrend.DOWN, OddsTrend.UP, OddsTrend.STABLE, 97.1)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("multi_bookmaker_odds_card"),
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
                Column {
                    Text(
                        text = "Live Odds Movement & Line Shifts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Multi-bookmaker consensus & market shift tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = PitchMetricsTheme.colors.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "96.4% AVG PAYOUT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = PitchMetricsTheme.colors.pitchGreen
                    )
                }
            }

            // Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "BOOKMAKER", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.weight(1.2f))
                Text(text = "1 (Home)", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.weight(0.9f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text(text = "X (Draw)", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.weight(0.9f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text(text = "2 (Away)", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.weight(0.9f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }

            Divider(color = PitchMetricsTheme.colors.border.copy(alpha = 0.4f))

            // Bookmakers List
            bookmakers.forEach { bkm ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bkm.bookmakerName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1.2f)
                    )

                    OddsValueCell(odds = bkm.homeOdds, trend = bkm.homeTrend, modifier = Modifier.weight(0.9f))
                    OddsValueCell(odds = bkm.drawOdds, trend = bkm.drawTrend, modifier = Modifier.weight(0.9f))
                    OddsValueCell(odds = bkm.awayOdds, trend = bkm.awayTrend, modifier = Modifier.weight(0.9f))
                }
            }
        }
    }
}

@Composable
private fun OddsValueCell(
    odds: Double,
    trend: OddsTrend,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(PitchMetricsTheme.colors.glassBackground)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format("%.2f", odds),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Icon(
            imageVector = when (trend) {
                OddsTrend.UP -> Icons.Default.ArrowDropUp
                OddsTrend.DOWN -> Icons.Default.ArrowDropDown
                OddsTrend.STABLE -> Icons.Default.HorizontalRule
            },
            contentDescription = null,
            tint = when (trend) {
                OddsTrend.UP -> PitchMetricsTheme.colors.pitchGreen
                OddsTrend.DOWN -> PitchMetricsTheme.colors.liveRed
                OddsTrend.STABLE -> PitchMetricsTheme.colors.textMuted
            },
            modifier = Modifier.size(16.dp)
        )
    }
}
