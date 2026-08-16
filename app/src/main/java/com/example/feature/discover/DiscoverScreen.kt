package com.example.feature.discover

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.models.League
import com.example.domain.models.SearchResult
import com.example.domain.models.SearchResultType
import com.example.domain.models.Standing
import com.example.shared.components.*
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onNavigateToMatchId: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PitchMetricsGlassHeader(
                title = "Discover",
                subtitle = "Leagues, Standings & Club Analytics",
                showSearchButton = false,
                showNotificationButton = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("discover_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Input
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Search clubs, fixtures, or competitions...",
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
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = PitchMetricsTheme.colors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
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
                        .padding(horizontal = 16.dp)
                        .testTag("discover_search_input")
                )
            }

            // Search Active Mode
            if (uiState.searchQuery.isNotBlank()) {
                if (uiState.isSearching) {
                    item {
                        LoadingView(message = "Searching PitchMetrics database...")
                    }
                } else if (uiState.searchResults.isEmpty) {
                    item {
                        EmptyStateView(
                            title = "No Matches Found",
                            message = "No clubs, fixtures, or leagues found for '${uiState.searchQuery}'",
                            actionLabel = "Clear Search",
                            onAction = { viewModel.clearSearch() },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    // Search Category Filter Tabs
                    item {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                SearchCategoryChip(
                                    title = "All (${uiState.searchResults.totalCount})",
                                    isSelected = uiState.selectedSearchFilter == SearchFilterTab.ALL,
                                    onClick = { viewModel.setSearchFilterTab(SearchFilterTab.ALL) }
                                )
                            }
                            if (uiState.searchResults.teams.isNotEmpty()) {
                                item {
                                    SearchCategoryChip(
                                        title = "Clubs (${uiState.searchResults.teams.size})",
                                        isSelected = uiState.selectedSearchFilter == SearchFilterTab.CLUBS,
                                        onClick = { viewModel.setSearchFilterTab(SearchFilterTab.CLUBS) }
                                    )
                                }
                            }
                            if (uiState.searchResults.competitions.isNotEmpty()) {
                                item {
                                    SearchCategoryChip(
                                        title = "Competitions (${uiState.searchResults.competitions.size})",
                                        isSelected = uiState.selectedSearchFilter == SearchFilterTab.COMPETITIONS,
                                        onClick = { viewModel.setSearchFilterTab(SearchFilterTab.COMPETITIONS) }
                                    )
                                }
                            }
                            if (uiState.searchResults.matches.isNotEmpty()) {
                                item {
                                    SearchCategoryChip(
                                        title = "Fixtures (${uiState.searchResults.matches.size})",
                                        isSelected = uiState.selectedSearchFilter == SearchFilterTab.FIXTURES,
                                        onClick = { viewModel.setSearchFilterTab(SearchFilterTab.FIXTURES) }
                                    )
                                }
                            }
                        }
                    }

                    // Display Clubs
                    if ((uiState.selectedSearchFilter == SearchFilterTab.ALL || uiState.selectedSearchFilter == SearchFilterTab.CLUBS) &&
                        uiState.searchResults.teams.isNotEmpty()
                    ) {
                        item {
                            Text(
                                text = "Clubs & Teams",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(uiState.searchResults.teams, key = { "team_${it.id}" }) { item ->
                            SearchResultRow(
                                result = item,
                                onClick = {
                                    if (!item.id.isNullOrBlank()) {
                                        onNavigateToMatchId(item.id)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Display Competitions
                    if ((uiState.selectedSearchFilter == SearchFilterTab.ALL || uiState.selectedSearchFilter == SearchFilterTab.COMPETITIONS) &&
                        uiState.searchResults.competitions.isNotEmpty()
                    ) {
                        item {
                            Text(
                                text = "Competitions & Leagues",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(uiState.searchResults.competitions, key = { "comp_${it.id}" }) { item ->
                            SearchResultRow(
                                result = item,
                                onClick = {},
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Display Matches / Fixtures
                    if ((uiState.selectedSearchFilter == SearchFilterTab.ALL || uiState.selectedSearchFilter == SearchFilterTab.FIXTURES) &&
                        uiState.searchResults.matches.isNotEmpty()
                    ) {
                        item {
                            Text(
                                text = "Matches & Fixtures",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(uiState.searchResults.matches, key = { "match_${it.id}" }) { item ->
                            SearchResultRow(
                                result = item,
                                onClick = {
                                    if (!item.id.isNullOrBlank()) {
                                        onNavigateToMatchId(item.id)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                // Competitions Selector
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Top Competitions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(uiState.leagues, key = { index, league -> "${league.id}_$index" }) { _, league ->
                                LeagueChip(
                                    league = league,
                                    isSelected = league.id == uiState.selectedLeagueId,
                                    onSelect = { viewModel.selectLeague(league) }
                                )
                            }
                        }
                    }
                }

                // Selected League Standings Table
                item {
                    StandingsTableCard(
                        leagueName = uiState.selectedLeagueName,
                        standings = uiState.standings,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchCategoryChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.elevatedSurface
    val contentCol = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onBackground
    val borderCol = if (isSelected) Color.Transparent else PitchMetricsTheme.colors.border

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = contentCol
        )
    }
}

@Composable
fun SearchResultRow(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("search_result_${result.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon / Logo
            if (!result.logoUrl.isNullOrBlank()) {
                TeamLogo(url = result.logoUrl, name = result.title, size = 32)
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (result.type) {
                                SearchResultType.TEAM -> PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.2f)
                                SearchResultType.LEAGUE -> PitchMetricsTheme.colors.aiViolet.copy(alpha = 0.2f)
                                SearchResultType.MATCH -> PitchMetricsTheme.colors.warningAmber.copy(alpha = 0.2f)
                                SearchResultType.PAGE_LINK -> PitchMetricsTheme.colors.textMuted.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (result.type) {
                            SearchResultType.TEAM -> Icons.Default.Shield
                            SearchResultType.LEAGUE -> Icons.Default.EmojiEvents
                            SearchResultType.MATCH -> Icons.Default.SportsSoccer
                            SearchResultType.PAGE_LINK -> Icons.Default.Link
                        },
                        contentDescription = null,
                        tint = when (result.type) {
                            SearchResultType.TEAM -> PitchMetricsTheme.colors.pitchGreen
                            SearchResultType.LEAGUE -> PitchMetricsTheme.colors.aiViolet
                            SearchResultType.MATCH -> PitchMetricsTheme.colors.warningAmber
                            SearchResultType.PAGE_LINK -> PitchMetricsTheme.colors.textMuted
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!result.subtitle.isNullOrBlank()) {
                    Text(
                        text = result.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = PitchMetricsTheme.colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Status or score if match
            if (result.homeScore != null && result.awayScore != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PitchMetricsTheme.colors.glassBackground)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${result.homeScore} - ${result.awayScore}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PitchMetricsTheme.colors.pitchGreen
                    )
                }
            } else if (!result.status.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PitchMetricsTheme.colors.glassBackground)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = result.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchMetricsTheme.colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
fun LeagueChip(
    league: League,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val bg = if (isSelected) PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.elevatedSurface
    val textCol = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onBackground
    val borderCol = if (isSelected) Color.Transparent else PitchMetricsTheme.colors.border

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (league.logoUrl.isNotBlank()) {
            TeamLogo(url = league.logoUrl, name = league.name, size = 18)
        }
        Text(
            text = league.name,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = textCol
        )
    }
}

@Composable
fun StandingsTableCard(
    leagueName: String,
    standings: List<Standing>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("standings_table_card"),
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
                Text(
                    text = if (leagueName.isNotBlank()) "$leagueName Table" else "Standings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "2024/25 Season",
                    style = MaterialTheme.typography.labelSmall,
                    color = PitchMetricsTheme.colors.textMuted
                )
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(PitchMetricsTheme.colors.glassBackground)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.width(24.dp))
                Text(text = "Club", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.weight(1f))
                Text(text = "P", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text(text = "W", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text(text = "D", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text(text = "L", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text(text = "GD", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                Text(text = "PTS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
            }

            if (standings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Standings data currently unavailable for this league",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PitchMetricsTheme.colors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    standings.take(20).forEach { standing ->
                        StandingRow(standing = standing)
                    }
                }
            }
        }
    }
}

@Composable
fun StandingRow(standing: Standing) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = standing.position.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (standing.position <= 4) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (standing.position <= 4) PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.textMuted,
            modifier = Modifier.width(24.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TeamLogo(url = standing.teamLogo, name = standing.teamName, size = 18)
            Text(
                text = standing.teamName,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(text = standing.played.toString(), style = MaterialTheme.typography.bodySmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        Text(text = standing.won.toString(), style = MaterialTheme.typography.bodySmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        Text(text = standing.drawn.toString(), style = MaterialTheme.typography.bodySmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        Text(text = standing.lost.toString(), style = MaterialTheme.typography.bodySmall, color = PitchMetricsTheme.colors.textMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
        Text(
            text = (if (standing.goalDiff > 0) "+${standing.goalDiff}" else "${standing.goalDiff}"),
            style = MaterialTheme.typography.bodySmall,
            color = if (standing.goalDiff > 0) PitchMetricsTheme.colors.pitchGreen else if (standing.goalDiff < 0) PitchMetricsTheme.colors.liveRed else PitchMetricsTheme.colors.textMuted,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = standing.points.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}
