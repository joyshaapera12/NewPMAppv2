package com.example.feature.live

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.models.LiveMatch
import com.example.shared.components.*
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun LiveScreen(
    viewModel: LiveViewModel,
    onNavigateToMatch: (LiveMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.startPolling()
        onDispose {
            viewModel.stopPolling()
        }
    }

    Scaffold(
        topBar = {
            PitchMetricsGlassHeader(
                title = "Live Centre",
                subtitle = "Real-Time Fast Polling (25s)",
                showSearchButton = false,
                showNotificationButton = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("live_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Live Status & Polling Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PitchMetricsTheme.colors.liveRed)
                    )
                    Text(
                        text = "${uiState.liveGamesCount} Games In Progress",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Sync: ${uiState.lastUpdatedSecondsAgo}s ago",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = PitchMetricsTheme.colors.textMuted
                    )
                    IconButton(
                        onClick = { viewModel.refreshNow() },
                        modifier = Modifier.size(28.dp).testTag("live_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh now",
                            tint = PitchMetricsTheme.colors.pitchGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = {
                    Text(
                        text = "Search live teams or leagues...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PitchMetricsTheme.colors.textMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = PitchMetricsTheme.colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PitchMetricsTheme.colors.elevatedSurface,
                    unfocusedContainerColor = PitchMetricsTheme.colors.elevatedSurface,
                    focusedBorderColor = PitchMetricsTheme.colors.pitchGreen,
                    unfocusedBorderColor = PitchMetricsTheme.colors.border
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("live_search_input")
            )

            // Period Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("ALL" to "All Live", "1H" to "1st Half", "HT" to "Half Time", "2H" to "2nd Half")
                items(filters) { (key, label) ->
                    val isSelected = uiState.selectedStatusFilter == key
                    val bg = if (isSelected) PitchMetricsTheme.colors.liveRed else PitchMetricsTheme.colors.elevatedSurface
                    val textCol = if (isSelected) Color.White else PitchMetricsTheme.colors.textMuted
                    val borderCol = if (isSelected) PitchMetricsTheme.colors.liveRed else PitchMetricsTheme.colors.border

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(bg)
                            .border(1.dp, borderCol, CircleShape)
                            .clickable { viewModel.onStatusFilterChanged(key) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = textCol
                        )
                    }
                }
            }

            // Live List Content
            if (uiState.isLoading && uiState.liveMatches.isEmpty()) {
                LoadingView(message = "Connecting to live match socket...", modifier = Modifier.fillMaxSize())
            } else if (uiState.filteredMatches.isEmpty()) {
                EmptyStateView(
                    title = "No Live Matches",
                    message = if (uiState.searchQuery.isNotBlank()) "No live fixtures match '${uiState.searchQuery}'" else "There are currently no live football matches in play.",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = PitchMetricsTheme.colors.liveRed,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    actionLabel = "Refresh Feed",
                    onAction = { viewModel.refreshNow() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(uiState.filteredMatches, key = { index, match -> "${match.eventKey}_$index" }) { _, match ->
                        LiveDetailedMatchCard(
                            match = match,
                            onClick = { onNavigateToMatch(match) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveDetailedMatchCard(
    match: LiveMatch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("live_match_card_${match.eventKey}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.liveRed.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // League and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${match.leagueName} • ${match.countryName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PitchMetricsTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                LiveIndicator(minute = match.minute)
            }

            // Teams and Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeamLogo(url = match.homeTeamLogo, name = match.homeTeamName, size = 26)
                    Text(
                        text = match.homeTeamName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Score Badge
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PitchMetricsTheme.colors.liveRedMuted)
                        .border(1.dp, PitchMetricsTheme.colors.liveRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (match.homeScore != null && match.awayScore != null) "${match.homeScore} - ${match.awayScore}" else match.score,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = PitchMetricsTheme.colors.liveRed
                    )
                }

                // Away
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = match.awayTeamName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TeamLogo(url = match.awayTeamLogo, name = match.awayTeamName, size = 26)
                }
            }
        }
    }
}
