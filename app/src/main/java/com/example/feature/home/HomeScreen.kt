package com.example.feature.home

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.models.LiveMatch
import com.example.domain.models.Match
import com.example.domain.models.Prediction
import com.example.shared.components.*
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToMatch: (Match) -> Unit,
    onNavigateToLiveMatch: (LiveMatch) -> Unit,
    onNavigateToPrediction: (Prediction) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PitchMetricsGlassHeader(
                title = "PitchMetrics",
                subtitle = "Football Intelligence & AI Analytics",
                onSearchClick = onNavigateToSearch,
                onNotificationClick = onNavigateToNotifications
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("home_screen")
    ) { paddingValues ->
        if (uiState.isLoading && uiState.todayMatches.isEmpty()) {
            LoadingView(
                message = "Analyzing football data & predictions...",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else if (uiState.errorMessage != null && uiState.todayMatches.isEmpty() && uiState.liveMatches.isEmpty()) {
            ErrorStateView(
                message = uiState.errorMessage ?: "Network error",
                onRetry = { viewModel.loadHomeData(isRefresh = true) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Offline Notice
                if (uiState.isOffline) {
                    item {
                        OfflineBanner(
                            lastUpdatedText = uiState.lastUpdatedText,
                            onRefresh = { viewModel.loadHomeData(isRefresh = true) }
                        )
                    }
                }

                // AI Pick of the Day Hero Banner
                uiState.featuredPick?.let { featured ->
                    item {
                        FeaturedPickHero(
                            prediction = featured,
                            onClick = { onNavigateToPrediction(featured) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Live Now Section
                if (uiState.liveMatches.isNotEmpty()) {
                    item {
                        LiveNowSection(
                            liveMatches = uiState.liveMatches,
                            onMatchClick = { liveMatch -> onNavigateToLiveMatch(liveMatch) }
                        )
                    }
                }

                // League Quick Filters
                item {
                    LeagueFilterBar(
                        selectedFilter = uiState.selectedLeagueFilter,
                        onSelectFilter = { viewModel.setLeagueFilter(it) }
                    )
                }

                // Today's Fixtures Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Matches",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${uiState.todayMatches.size} Fixtures",
                            style = MaterialTheme.typography.labelSmall,
                            color = PitchMetricsTheme.colors.textMuted
                        )
                    }
                }

                // Filtered Matches List
                val filteredMatches = if (uiState.selectedLeagueFilter == "ALL") {
                    uiState.todayMatches
                } else {
                    uiState.todayMatches.filter {
                        it.leagueName.contains(uiState.selectedLeagueFilter, ignoreCase = true) ||
                        it.leagueId == uiState.selectedLeagueFilter
                    }
                }

                if (filteredMatches.isEmpty() && uiState.todayMatches.isNotEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No Matches Found",
                            message = "No matches scheduled for this competition today.",
                            actionLabel = "Show All Competitions",
                            onAction = { viewModel.setLeagueFilter("ALL") },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else if (filteredMatches.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No Matches Scheduled",
                            message = "Check back soon or explore AI picks for upcoming game weeks.",
                            actionLabel = "Refresh Data",
                            onAction = { viewModel.loadHomeData(isRefresh = true) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    itemsIndexed(filteredMatches, key = { index, match -> "${match.id}_$index" }) { _, match ->
                        MatchCard(
                            match = match,
                            onClick = { onNavigateToMatch(match) },
                            onTogglePin = { viewModel.togglePinMatch(match.id) },
                            isPinned = uiState.pinnedMatchIds.contains(match.id),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedPickHero(
    prediction: Prediction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("featured_ai_pick"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.aiViolet.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(PitchMetricsTheme.colors.aiViolet)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "AI PICK OF THE DAY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = prediction.league,
                    style = MaterialTheme.typography.labelSmall,
                    color = PitchMetricsTheme.colors.textMuted
                )
            }

            // Teams & Confidence Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${prediction.homeTeam} vs ${prediction.awayTeam}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Model Selection:",
                            style = MaterialTheme.typography.bodySmall,
                            color = PitchMetricsTheme.colors.textMuted
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = prediction.pick,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = PitchMetricsTheme.colors.pitchGreen
                            )
                        }
                    }
                }

                ConfidenceRing(
                    confidence = prediction.confidence,
                    size = 64.dp,
                    strokeWidth = 6.dp
                )
            }

            // Tactical Summary
            Text(
                text = prediction.reasoning,
                style = MaterialTheme.typography.bodyMedium,
                color = PitchMetricsTheme.colors.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LiveNowSection(
    liveMatches: List<LiveMatch>,
    onMatchClick: (LiveMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = PitchMetricsTheme.colors.liveRed,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Live Now",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "${liveMatches.size} Active",
                style = MaterialTheme.typography.labelSmall,
                color = PitchMetricsTheme.colors.liveRed
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("live_now_carousel"),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(liveMatches, key = { index, match -> "${match.eventKey}_$index" }) { _, match ->
                LiveMatchMiniCard(
                    match = match,
                    onClick = { onMatchClick(match) }
                )
            }
        }
    }
}

@Composable
fun LiveMatchMiniCard(
    match: LiveMatch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("live_match_mini_${match.eventKey}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.liveRed.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.leagueName,
                    style = MaterialTheme.typography.labelSmall,
                    color = PitchMetricsTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                LiveIndicator(minute = match.minute)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = match.homeTeamName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = match.awayTeamName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PitchMetricsTheme.colors.liveRedMuted)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${match.homeScore} - ${match.awayScore}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = PitchMetricsTheme.colors.liveRed
                    )
                }
            }
        }
    }
}

@Composable
fun LeagueFilterBar(
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "ALL" to "All Competitions",
        "Premier League" to "Premier League",
        "Champions League" to "Champions League",
        "La Liga" to "La Liga",
        "Serie A" to "Serie A",
        "Bundesliga" to "Bundesliga",
        "Ligue 1" to "Ligue 1"
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("league_filter_bar"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (key, label) ->
            val isSelected = selectedFilter == key
            val bg = if (isSelected) PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.elevatedSurface
            val textCol = if (isSelected) Color.Black else PitchMetricsTheme.colors.textMuted
            val borderCol = if (isSelected) PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.border

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bg)
                    .border(1.dp, borderCol, CircleShape)
                    .clickable { onSelectFilter(key) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("league_filter_chip_$key")
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = textCol
                )
            }
        }
    }
}
