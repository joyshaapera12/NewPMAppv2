package com.example.shared.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.Lineup
import com.example.domain.models.TacticalPlayer
import com.example.ui.theme.PitchMetricsTheme

enum class TacticalViewMode {
    FORMATION,
    HEATMAP,
    ATTACK_CHANNELS
}

@Composable
fun TacticalPitchView(
    lineup: Lineup?,
    homeTeamName: String = "Home",
    awayTeamName: String = "Away",
    onPlayerClick: ((TacticalPlayer) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(TacticalViewMode.FORMATION) }
    val fieldBg = PitchMetricsTheme.colors.pitchFieldBg
    val fieldLines = PitchMetricsTheme.colors.pitchFieldLines
    val homeColor = PitchMetricsTheme.colors.pitchGreen
    val awayColor = PitchMetricsTheme.colors.secondaryBlue

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tactical_pitch_view"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fieldBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // View Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PitchMetricsTheme.colors.glassBackground)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TacticalModeChip(
                    label = "Formation",
                    selected = selectedMode == TacticalViewMode.FORMATION,
                    onClick = { selectedMode = TacticalViewMode.FORMATION },
                    modifier = Modifier.weight(1f)
                )
                TacticalModeChip(
                    label = "Heatmap",
                    selected = selectedMode == TacticalViewMode.HEATMAP,
                    onClick = { selectedMode = TacticalViewMode.HEATMAP },
                    modifier = Modifier.weight(1f)
                )
                TacticalModeChip(
                    label = "Channels",
                    selected = selectedMode == TacticalViewMode.ATTACK_CHANNELS,
                    onClick = { selectedMode = TacticalViewMode.ATTACK_CHANNELS },
                    modifier = Modifier.weight(1f)
                )
            }

            // Formations Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(homeColor))
                    Text(
                        text = "$homeTeamName (${lineup?.homeFormation ?: "4-3-3"})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "$awayTeamName (${lineup?.awayFormation ?: "4-2-3-1"})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(awayColor))
                }
            }

            // Pitch Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 1.5.dp.toPx())
                    val w = size.width
                    val h = size.height

                    // Field Outline
                    drawRect(color = fieldLines, topLeft = Offset(4f, 4f), size = Size(w - 8f, h - 8f), style = stroke)

                    // Halfway Line
                    drawLine(color = fieldLines, start = Offset(4f, h / 2f), end = Offset(w - 4f, h / 2f), strokeWidth = 1.5.dp.toPx())

                    // Center Circle
                    drawCircle(color = fieldLines, radius = w * 0.16f, center = Offset(w / 2f, h / 2f), style = stroke)
                    drawCircle(color = fieldLines, radius = 3.dp.toPx(), center = Offset(w / 2f, h / 2f))

                    // Top Penalty Box (Away side)
                    val penW = w * 0.52f
                    val penH = h * 0.18f
                    val penLeft = (w - penW) / 2f
                    drawRect(color = fieldLines, topLeft = Offset(penLeft, 4f), size = Size(penW, penH), style = stroke)
                    // Top Goal Box
                    val goalW = w * 0.26f
                    val goalH = h * 0.07f
                    drawRect(color = fieldLines, topLeft = Offset((w - goalW) / 2f, 4f), size = Size(goalW, goalH), style = stroke)

                    // Bottom Penalty Box (Home side)
                    drawRect(color = fieldLines, topLeft = Offset(penLeft, h - penH - 4f), size = Size(penW, penH), style = stroke)
                    // Bottom Goal Box
                    drawRect(color = fieldLines, topLeft = Offset((w - goalW) / 2f, h - goalH - 4f), size = Size(goalW, goalH), style = stroke)

                    // 1. Heatmap Mode Canvas Rendering
                    if (selectedMode == TacticalViewMode.HEATMAP) {
                        // Home Team Heat Circles (Bottom Half)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(homeColor.copy(alpha = 0.45f), Color.Transparent),
                                center = Offset(w * 0.5f, h * 0.68f),
                                radius = w * 0.35f
                            ),
                            radius = w * 0.35f,
                            center = Offset(w * 0.5f, h * 0.68f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(homeColor.copy(alpha = 0.55f), Color.Transparent),
                                center = Offset(w * 0.22f, h * 0.58f),
                                radius = w * 0.28f
                            ),
                            radius = w * 0.28f,
                            center = Offset(w * 0.22f, h * 0.58f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFEAB308).copy(alpha = 0.40f), Color.Transparent),
                                center = Offset(w * 0.75f, h * 0.56f),
                                radius = w * 0.26f
                            ),
                            radius = w * 0.26f,
                            center = Offset(w * 0.75f, h * 0.56f)
                        )

                        // Away Team Heat Circles (Top Half)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(awayColor.copy(alpha = 0.45f), Color.Transparent),
                                center = Offset(w * 0.5f, h * 0.35f),
                                radius = w * 0.32f
                            ),
                            radius = w * 0.32f,
                            center = Offset(w * 0.5f, h * 0.35f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(awayColor.copy(alpha = 0.5f), Color.Transparent),
                                center = Offset(w * 0.8f, h * 0.42f),
                                radius = w * 0.25f
                            ),
                            radius = w * 0.25f,
                            center = Offset(w * 0.8f, h * 0.42f)
                        )
                    }

                    // 2. Attack Channels Mode Canvas Rendering
                    if (selectedMode == TacticalViewMode.ATTACK_CHANNELS) {
                        val colW = (w - 8f) / 3f
                        // Left Channel Line
                        drawLine(
                            color = fieldLines.copy(alpha = 0.5f),
                            start = Offset(4f + colW, 4f),
                            end = Offset(4f + colW, h - 4f),
                            strokeWidth = 1.dp.toPx()
                        )
                        // Right Channel Line
                        drawLine(
                            color = fieldLines.copy(alpha = 0.5f),
                            start = Offset(4f + colW * 2, 4f),
                            end = Offset(4f + colW * 2, h - 4f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Overlay Content per Mode
                when (selectedMode) {
                    TacticalViewMode.FORMATION -> {
                        // Render Interactive Player Nodes
                        val homePlayers = lineup?.homeStarting ?: getFallbackStartingPlayers(isHome = true)
                        val awayPlayers = lineup?.awayStarting ?: getFallbackStartingPlayers(isHome = false)

                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val boxW = maxWidth
                            val boxH = maxHeight

                            // Home Team Nodes
                            homePlayers.take(11).forEachIndexed { idx, player ->
                                val (relX, relY) = getFormationCoords(idx, isHome = true)
                                Box(
                                    modifier = Modifier
                                        .absoluteOffset(
                                            x = boxW * relX - 22.dp,
                                            y = boxH * relY - 22.dp
                                        )
                                        .clickable { onPlayerClick?.invoke(player) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    PlayerPitchNode(player = player, nodeColor = homeColor)
                                }
                            }

                            // Away Team Nodes
                            awayPlayers.take(11).forEachIndexed { idx, player ->
                                val (relX, relY) = getFormationCoords(idx, isHome = false)
                                Box(
                                    modifier = Modifier
                                        .absoluteOffset(
                                            x = boxW * relX - 22.dp,
                                            y = boxH * relY - 22.dp
                                        )
                                        .clickable { onPlayerClick?.invoke(player) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    PlayerPitchNode(player = player, nodeColor = awayColor)
                                }
                            }
                        }
                    }
                    TacticalViewMode.HEATMAP -> {
                        // Heatmap Legend & Overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PitchMetricsTheme.colors.glassBackground)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "High Possession Intensity Zones (60-90')",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "● $homeTeamName Dominance", style = MaterialTheme.typography.labelSmall, color = homeColor)
                                Text(text = "● $awayTeamName Dominance", style = MaterialTheme.typography.labelSmall, color = awayColor)
                            }
                        }
                    }
                    TacticalViewMode.ATTACK_CHANNELS -> {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AttackChannelColumn(title = "LEFT FLANK", percentage = "38%", threat = "HIGH", color = PitchMetricsTheme.colors.pitchGreen)
                            AttackChannelColumn(title = "CENTRE", percentage = "34%", threat = "MODERATE", color = PitchMetricsTheme.colors.secondaryBlue)
                            AttackChannelColumn(title = "RIGHT FLANK", percentage = "28%", threat = "BALANCED", color = PitchMetricsTheme.colors.textMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TacticalModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) PitchMetricsTheme.colors.pitchGreen else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
            color = if (selected) Color.Black else PitchMetricsTheme.colors.textMuted
        )
    }
}

@Composable
private fun AttackChannelColumn(
    title: String,
    percentage: String,
    threat: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PitchMetricsTheme.colors.glassBackground.copy(alpha = 0.7f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = PitchMetricsTheme.colors.textMuted)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = percentage,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
            Text(text = threat, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = color)
        }
        Text(text = "▲ ATTACK", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = PitchMetricsTheme.colors.textMuted)
    }
}

@Composable
private fun PlayerPitchNode(
    player: TacticalPlayer,
    nodeColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(44.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(nodeColor)
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.number,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
        }
        Text(
            text = player.name.split(" ").lastOrNull() ?: player.name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

private fun getFormationCoords(index: Int, isHome: Boolean): Pair<Float, Float> {
    return if (isHome) {
        when (index) {
            0 -> Pair(0.50f, 0.90f) // GK
            1 -> Pair(0.15f, 0.77f) // LB
            2 -> Pair(0.38f, 0.79f) // CB
            3 -> Pair(0.62f, 0.79f) // CB
            4 -> Pair(0.85f, 0.77f) // RB
            5 -> Pair(0.30f, 0.65f) // CM
            6 -> Pair(0.50f, 0.68f) // DM
            7 -> Pair(0.70f, 0.65f) // CM
            8 -> Pair(0.18f, 0.54f) // LW
            9 -> Pair(0.50f, 0.53f) // ST
            10 -> Pair(0.82f, 0.54f) // RW
            else -> Pair(0.50f, 0.70f)
        }
    } else {
        when (index) {
            0 -> Pair(0.50f, 0.10f) // GK
            1 -> Pair(0.15f, 0.23f) // RB
            2 -> Pair(0.38f, 0.21f) // CB
            3 -> Pair(0.62f, 0.21f) // CB
            4 -> Pair(0.85f, 0.23f) // LB
            5 -> Pair(0.35f, 0.35f) // DM
            6 -> Pair(0.65f, 0.35f) // DM
            7 -> Pair(0.50f, 0.40f) // AM
            8 -> Pair(0.18f, 0.46f) // RW
            9 -> Pair(0.50f, 0.47f) // ST
            10 -> Pair(0.82f, 0.46f) // LW
            else -> Pair(0.50f, 0.30f)
        }
    }
}

private fun getFallbackStartingPlayers(isHome: Boolean): List<TacticalPlayer> {
    val prefix = if (isHome) "H" else "A"
    return listOf(
        TacticalPlayer("1", "$prefix. Keeper", "1", "GK", 0.5f, 0.9f, "7.2", null),
        TacticalPlayer("2", "$prefix. Defender 1", "2", "DF", 0.15f, 0.8f, "6.8", null),
        TacticalPlayer("3", "$prefix. Defender 2", "4", "DF", 0.38f, 0.8f, "7.0", null),
        TacticalPlayer("4", "$prefix. Defender 3", "5", "DF", 0.62f, 0.8f, "7.1", null),
        TacticalPlayer("5", "$prefix. Defender 4", "3", "DF", 0.85f, 0.8f, "6.9", null),
        TacticalPlayer("6", "$prefix. Midfielder 1", "6", "MF", 0.3f, 0.65f, "7.4", null),
        TacticalPlayer("7", "$prefix. Midfielder 2", "8", "MF", 0.5f, 0.68f, "7.6", null),
        TacticalPlayer("8", "$prefix. Midfielder 3", "10", "MF", 0.7f, 0.65f, "8.0", null),
        TacticalPlayer("9", "$prefix. Forward 1", "7", "FW", 0.18f, 0.54f, "7.5", null),
        TacticalPlayer("10", "$prefix. Striker", "9", "FW", 0.5f, 0.53f, "8.3", null),
        TacticalPlayer("11", "$prefix. Forward 2", "11", "FW", 0.82f, 0.54f, "7.8", null)
    )
}
