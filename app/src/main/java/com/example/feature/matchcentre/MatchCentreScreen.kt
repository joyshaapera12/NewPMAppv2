package com.example.feature.matchcentre

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.models.*
import com.example.feature.player.PlayerDetailBottomSheet
import com.example.shared.components.*
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun MatchCentreScreen(
    viewModel: MatchCentreViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val match = uiState.match

    Scaffold(
        topBar = {
            PitchMetricsGlassHeader(
                title = "Match Centre",
                subtitle = match?.leagueName ?: "Fixture Details",
                showBackButton = true,
                onBackClick = onBackClick,
                showSearchButton = false,
                showNotificationButton = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("match_centre_screen")
    ) { paddingValues ->
        if (uiState.isLoading && match == null) {
            LoadingView(
                message = "Loading tactical match center data...",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else if (uiState.errorMessage != null && match == null) {
            ErrorStateView(
                message = uiState.errorMessage ?: "Match details unavailable.",
                onRetry = { viewModel.loadMatchCentreData(isRefresh = true) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else if (match == null) {
            EmptyStateView(
                title = "Match Not Found",
                message = "We could not find fixture data for this match.",
                actionLabel = "Go Back",
                onAction = onBackClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Match Centre Hero
                item {
                    MatchHeroCard(
                        match = match,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Sub-Tab Navigation Bar
                item {
                    MatchCentreTabBar(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }

                // Tab Content Rendering
                when (uiState.selectedTab) {
                    MatchCentreTab.OVERVIEW -> {
                        item {
                            MatchOverviewContent(
                                events = uiState.events,
                                stats = uiState.statistics,
                                referee = uiState.refereeAnalytics,
                                stadium = uiState.stadiumAnalytics,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    MatchCentreTab.PREDICTION -> {
                        item {
                            MatchPredictionContent(
                                match = match,
                                prediction = uiState.prediction,
                                odds = uiState.odds,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    MatchCentreTab.LINEUPS -> {
                        item {
                            MatchLineupsContent(
                                lineup = uiState.lineups,
                                homeTeamName = match.homeTeam.name,
                                awayTeamName = match.awayTeam.name,
                                onPlayerClick = { player, teamName -> viewModel.onPlayerSelected(player, teamName) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    MatchCentreTab.STATS -> {
                        item {
                            MatchStatsContent(
                                stats = uiState.statistics,
                                referee = uiState.refereeAnalytics,
                                homeTeamName = match.homeTeam.name,
                                awayTeamName = match.awayTeam.name,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    MatchCentreTab.H2H -> {
                        item {
                            MatchH2HContent(
                                h2h = uiState.h2h,
                                homeTeamName = match.homeTeam.name,
                                awayTeamName = match.awayTeam.name,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    MatchCentreTab.COMMUNITY -> {
                        item {
                            MatchCommunityContent(
                                sentiment = uiState.sentiment,
                                comments = uiState.comments,
                                reactions = uiState.emojiReactions,
                                homeTeamName = match.homeTeam.name,
                                awayTeamName = match.awayTeam.name,
                                userVote = uiState.userVote,
                                isPosting = uiState.isPostingComment,
                                onVote = { viewModel.submitVote(it) },
                                onPostComment = { text, flair -> viewModel.postComment(text, flair) },
                                onToggleLike = { viewModel.toggleCommentLike(it) },
                                onReactEmoji = { viewModel.reactWithEmoji(it) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            // Player Profile Bottom Sheet
            uiState.selectedPlayer?.let { profile ->
                PlayerDetailBottomSheet(
                    player = profile,
                    onDismiss = { viewModel.dismissPlayerDetails() }
                )
            }
        }
    }
}

@Composable
fun MatchHeroCard(
    match: Match,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("match_hero_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (match.isLive) {
                    LiveIndicator(minute = match.minute)
                } else {
                    Text(
                        text = if (match.isFinished) "FULL TIME" else "${match.date} • ${match.startTime}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (match.isFinished) PitchMetricsTheme.colors.textMuted else PitchMetricsTheme.colors.pitchGreen
                    )
                }
            }

            // Teams & Main Score Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TeamLogo(url = match.homeTeam.logoUrl, name = match.homeTeam.name, size = 48)
                    Text(
                        text = match.homeTeam.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }

                // Score Box
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (match.isLive) PitchMetricsTheme.colors.liveRedMuted else PitchMetricsTheme.colors.glassBackground)
                        .border(1.dp, if (match.isLive) PitchMetricsTheme.colors.liveRed.copy(alpha = 0.4f) else PitchMetricsTheme.colors.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.homeScore != null && match.awayScore != null) {
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp
                            ),
                            color = if (match.isLive) PitchMetricsTheme.colors.liveRed else MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PitchMetricsTheme.colors.textMuted
                            )
                        )
                    }
                }

                // Away Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TeamLogo(url = match.awayTeam.logoUrl, name = match.awayTeam.name, size = 48)
                    Text(
                        text = match.awayTeam.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Stadium Footer if available
            if (!match.stadium.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stadium,
                        contentDescription = null,
                        tint = PitchMetricsTheme.colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = match.stadium,
                        style = MaterialTheme.typography.bodySmall,
                        color = PitchMetricsTheme.colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
fun MatchCentreTabBar(
    selectedTab: MatchCentreTab,
    onTabSelected: (MatchCentreTab) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("match_centre_tab_bar"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(MatchCentreTab.values()) { tab ->
            val isSelected = selectedTab == tab
            val label = when (tab) {
                MatchCentreTab.OVERVIEW -> "Overview"
                MatchCentreTab.PREDICTION -> "AI Prediction"
                MatchCentreTab.LINEUPS -> "Lineups"
                MatchCentreTab.STATS -> "Stats"
                MatchCentreTab.H2H -> "H2H"
                MatchCentreTab.COMMUNITY -> "Community"
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.elevatedSurface)
                    .border(
                        1.dp,
                        if (isSelected) PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.border,
                        CircleShape
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("match_tab_${tab.name.lowercase()}")
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isSelected) Color.Black else PitchMetricsTheme.colors.textMuted
                )
            }
        }
    }
}

@Composable
fun MatchOverviewContent(
    events: List<MatchEvent>,
    stats: MatchStatistics?,
    referee: RefereeAnalytics?,
    stadium: StadiumAnalytics?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Stats Summary
        if (stats != null && (stats.possessionHome != null || stats.xgHome != null)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Key Metrics", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

                    if (stats.possessionHome != null && stats.possessionAway != null) {
                        DualComparisonBar(
                            title = "Ball Possession",
                            homeValue = "${stats.possessionHome}%",
                            awayValue = "${stats.possessionAway}%",
                            homePercent = stats.possessionHome.toFloat(),
                            awayPercent = stats.possessionAway.toFloat()
                        )
                    }
                    if (stats.xgHome != null && stats.xgAway != null) {
                        DualComparisonBar(
                            title = "Expected Goals (xG)",
                            homeValue = "${stats.xgHome}",
                            awayValue = "${stats.xgAway}",
                            homePercent = stats.xgHome,
                            awayPercent = stats.xgAway
                        )
                    }
                }
            }
        }

        // Timeline Events
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Match Events",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (events.isEmpty()) {
                    Text(
                        text = "No key events recorded yet for this fixture.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PitchMetricsTheme.colors.textMuted
                    )
                } else {
                    events.forEach { event ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PitchMetricsTheme.colors.glassBackground)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${event.minute}'",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                    color = PitchMetricsTheme.colors.pitchGreen
                                )
                            }

                            Icon(
                                imageVector = when (event.type) {
                                    "goal" -> Icons.Default.SportsSoccer
                                    "yellow_card" -> Icons.Default.Warning
                                    "red_card" -> Icons.Default.Dangerous
                                    else -> Icons.Default.SwapHoriz
                                },
                                contentDescription = null,
                                tint = when (event.type) {
                                    "goal" -> PitchMetricsTheme.colors.pitchGreen
                                    "yellow_card" -> Color(0xFFF59E0B)
                                    "red_card" -> PitchMetricsTheme.colors.liveRed
                                    else -> PitchMetricsTheme.colors.secondaryBlue
                                },
                                modifier = Modifier.size(18.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.playerName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (!event.assistPlayerName.isNullOrBlank()) {
                                    Text(
                                        text = "Assist: ${event.assistPlayerName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PitchMetricsTheme.colors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stadium Analytics Card
        stadium?.let {
            StadiumAnalyticsCard(stadium = it)
        }

        // Referee Analytics Card
        referee?.let {
            RefereeAnalyticsCard(referee = it)
        }
    }
}

@Composable
fun MatchPredictionContent(
    match: Match,
    prediction: Prediction?,
    odds: MatchOdds?,
    modifier: Modifier = Modifier
) {
    val confidence = prediction?.confidence ?: match.aiConfidence
    val pick = prediction?.pick ?: match.aiPick
    val reasoning = prediction?.reasoning

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (pick != null || confidence != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.aiViolet.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Model Verdict",
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchMetricsTheme.colors.textMuted
                            )
                            Text(
                                text = pick ?: "Prediction Available",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = PitchMetricsTheme.colors.pitchGreen
                            )
                        }
                        if (confidence != null) {
                            ConfidenceRing(confidence = confidence, size = 60.dp, strokeWidth = 6.dp)
                        }
                    }

                    if (!reasoning.isNullOrBlank()) {
                        Divider(color = PitchMetricsTheme.colors.border.copy(alpha = 0.5f))
                        Text(
                            text = reasoning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Multi-Bookmaker Live Odds Tracker with Line Shifts
        MultiBookmakerOddsCard(
            matchOdds = odds,
            homeTeamName = match.homeTeam.name,
            awayTeamName = match.awayTeam.name
        )
    }
}

@Composable
fun MatchLineupsContent(
    lineup: Lineup?,
    homeTeamName: String,
    awayTeamName: String,
    onPlayerClick: (TacticalPlayer, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TacticalPitchView(
            lineup = lineup,
            homeTeamName = homeTeamName,
            awayTeamName = awayTeamName,
            onPlayerClick = { player ->
                val isHome = lineup?.homeStarting?.any { it.id == player.id } == true
                onPlayerClick(player, if (isHome) homeTeamName else awayTeamName)
            }
        )
    }
}

@Composable
fun MatchStatsContent(
    stats: MatchStatistics?,
    referee: RefereeAnalytics?,
    homeTeamName: String,
    awayTeamName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (stats != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "Full Match Statistics", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

                    if (stats.possessionHome != null && stats.possessionAway != null) {
                        DualComparisonBar("Possession", "${stats.possessionHome}%", "${stats.possessionAway}%", stats.possessionHome.toFloat(), stats.possessionAway.toFloat())
                    }
                    if (stats.xgHome != null && stats.xgAway != null) {
                        DualComparisonBar("Expected Goals (xG)", "${stats.xgHome}", "${stats.xgAway}", stats.xgHome, stats.xgAway)
                    }
                    if (stats.shotsHome != null && stats.shotsAway != null) {
                        DualComparisonBar("Total Shots", "${stats.shotsHome}", "${stats.shotsAway}", stats.shotsHome.toFloat(), stats.shotsAway.toFloat())
                    }
                    if (stats.shotsOnTargetHome != null && stats.shotsOnTargetAway != null) {
                        DualComparisonBar("Shots on Target", "${stats.shotsOnTargetHome}", "${stats.shotsOnTargetAway}", stats.shotsOnTargetHome.toFloat(), stats.shotsOnTargetAway.toFloat())
                    }
                    if (stats.cornersHome != null && stats.cornersAway != null) {
                        DualComparisonBar("Corners", "${stats.cornersHome}", "${stats.cornersAway}", stats.cornersHome.toFloat(), stats.cornersAway.toFloat())
                    }
                    if (stats.foulsHome != null && stats.foulsAway != null) {
                        DualComparisonBar("Fouls", "${stats.foulsHome}", "${stats.foulsAway}", stats.foulsHome.toFloat(), stats.foulsAway.toFloat())
                    }
                    if (stats.yellowCardsHome != null && stats.yellowCardsAway != null) {
                        DualComparisonBar("Yellow Cards", "${stats.yellowCardsHome}", "${stats.yellowCardsAway}", stats.yellowCardsHome.toFloat(), stats.yellowCardsAway.toFloat())
                    }
                    if (stats.passesHome != null && stats.passesAway != null) {
                        DualComparisonBar("Passes Completed", "${stats.passesHome}", "${stats.passesAway}", stats.passesHome.toFloat(), stats.passesAway.toFloat())
                    }
                }
            }
        }

        referee?.let {
            RefereeAnalyticsCard(referee = it)
        }
    }
}

@Composable
fun MatchH2HContent(
    h2h: HeadToHead?,
    homeTeamName: String,
    awayTeamName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Head to Head History", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

            if (h2h == null || h2h.matches.isEmpty()) {
                Text(
                    text = "Head-to-head historical statistics unavailable for this matchup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PitchMetricsTheme.colors.textMuted
                )
            } else {
                val hw = h2h.homeWins
                val d = h2h.draws
                val aw = h2h.awayWins

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PitchMetricsTheme.colors.glassBackground)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$homeTeamName Wins", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.pitchGreen)
                        Text(text = "$hw", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = PitchMetricsTheme.colors.pitchGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Draws", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted)
                        Text(text = "$d", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = PitchMetricsTheme.colors.textMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$awayTeamName Wins", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.secondaryBlue)
                        Text(text = "$aw", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = PitchMetricsTheme.colors.secondaryBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCommunityContent(
    sentiment: CommunitySentiment?,
    comments: List<CommunityComment>,
    reactions: List<LiveEmojiReaction>,
    homeTeamName: String,
    awayTeamName: String,
    userVote: String?,
    isPosting: Boolean,
    onVote: (String) -> Unit,
    onPostComment: (String, String) -> Unit,
    onToggleLike: (String) -> Unit,
    onReactEmoji: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    var selectedFlair by remember { mutableStateOf("Neutral") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Live Emoji Reaction Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Live Match Reactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    reactions.forEach { reaction ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (reaction.userSelected) PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.2f)
                                    else PitchMetricsTheme.colors.glassBackground
                                )
                                .border(
                                    1.dp,
                                    if (reaction.userSelected) PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.border,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onReactEmoji(reaction.emoji) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = reaction.emoji, fontSize = 16.sp)
                                Text(
                                    text = "${reaction.count}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (reaction.userSelected) PitchMetricsTheme.colors.pitchGreen else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }

        // Community Sentiment Voting Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Outcome Sentiment",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (sentiment != null && sentiment.totalVotes > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(modifier = Modifier.weight(sentiment.homePercentage.coerceAtLeast(1f)).fillMaxHeight().background(PitchMetricsTheme.colors.pitchGreen))
                        Box(modifier = Modifier.weight(sentiment.drawPercentage.coerceAtLeast(1f)).fillMaxHeight().background(PitchMetricsTheme.colors.border))
                        Box(modifier = Modifier.weight(sentiment.awayPercentage.coerceAtLeast(1f)).fillMaxHeight().background(PitchMetricsTheme.colors.secondaryBlue))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "$homeTeamName: ${(sentiment.homePercentage).toInt()}%", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.pitchGreen)
                        Text(text = "Draw: ${(sentiment.drawPercentage).toInt()}%", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.textMuted)
                        Text(text = "$awayTeamName: ${(sentiment.awayPercentage).toInt()}%", style = MaterialTheme.typography.labelSmall, color = PitchMetricsTheme.colors.secondaryBlue)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onVote("HOME") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userVote == "HOME") PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.glassBackground,
                            contentColor = if (userVote == "HOME") Color.Black else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("vote_home_button")
                    ) {
                        Text(text = "1 (Home)", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onVote("DRAW") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userVote == "DRAW") PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.glassBackground,
                            contentColor = if (userVote == "DRAW") Color.Black else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("vote_draw_button")
                    ) {
                        Text(text = "X (Draw)", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onVote("AWAY") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (userVote == "AWAY") PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.glassBackground,
                            contentColor = if (userVote == "AWAY") Color.Black else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("vote_away_button")
                    ) {
                        Text(text = "2 (Away)", fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Chat & Discussion Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.border)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Live Match Discussion",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Post Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Share match tactical insight...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PitchMetricsTheme.colors.pitchGreen,
                            unfocusedBorderColor = PitchMetricsTheme.colors.border
                        )
                    )

                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onPostComment(commentText, selectedFlair)
                                commentText = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PitchMetricsTheme.colors.pitchGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send comment",
                            tint = Color.Black
                        )
                    }
                }

                Divider(color = PitchMetricsTheme.colors.border.copy(alpha = 0.5f))

                // Comments List
                comments.forEach { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PitchMetricsTheme.colors.glassBackground)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "• ${comment.timestamp}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PitchMetricsTheme.colors.textMuted
                                )
                            }
                            Text(
                                text = comment.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onToggleLike(comment.id) }
                        ) {
                            Icon(
                                imageVector = if (comment.userLiked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                contentDescription = "Like",
                                tint = if (comment.userLiked) PitchMetricsTheme.colors.pitchGreen else PitchMetricsTheme.colors.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${comment.likesCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchMetricsTheme.colors.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
