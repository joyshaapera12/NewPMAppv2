package com.example.feature.predictions

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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.models.Prediction
import com.example.shared.components.*
import com.example.ui.theme.PitchMetricsTheme

@Composable
fun AiPicksScreen(
    viewModel: PredictionsViewModel,
    onNavigateToMatch: (Prediction) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PitchMetricsGlassHeader(
                title = "AI Picks & Analytics",
                subtitle = "Statistical Edge & Machine Learning Forecasts",
                showSearchButton = false,
                showNotificationButton = false
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("ai_picks_screen")
    ) { paddingValues ->
        if (uiState.isLoading && uiState.predictions.isEmpty()) {
            LoadingView(
                message = "Running machine learning probability models...",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else if (uiState.errorMessage != null && uiState.predictions.isEmpty()) {
            ErrorStateView(
                message = uiState.errorMessage ?: "Prediction engine offline",
                onRetry = { viewModel.loadPredictions(isRefresh = true) },
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Model Accuracy Summary Bar
                item {
                    AiModelSummaryCard(
                        accuracy = uiState.modelAccuracy,
                        totalPicks = uiState.totalPicksAnalyzed,
                        winStreak = uiState.winStreak,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Filter Tabs
                item {
                    PredictionFilterBar(
                        selectedFilter = uiState.selectedFilter,
                        onSelectFilter = { viewModel.setFilter(it) }
                    )
                }

                // If Leaderboard selected, show PredictorLeaderboardCard
                if (uiState.selectedFilter == PredictionFilter.LEADERBOARD) {
                    item {
                        PredictorLeaderboardCard(
                            leaderboard = uiState.leaderboard,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    // Prediction Items
                    if (uiState.filteredPredictions.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Picks in Category",
                                message = "No predictions match the selected filter at this moment.",
                                actionLabel = "Show All AI Picks",
                                onAction = { viewModel.setFilter(PredictionFilter.ALL) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        itemsIndexed(uiState.filteredPredictions, key = { index, prediction -> "${prediction.id}_$index" }) { _, prediction ->
                            PredictionCard(
                                prediction = prediction,
                                onClick = { onNavigateToMatch(prediction) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiModelSummaryCard(
    accuracy: Float,
    totalPicks: Int,
    winStreak: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("ai_model_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PitchMetricsTheme.colors.elevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PitchMetricsTheme.colors.aiViolet.copy(alpha = 0.4f))
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = PitchMetricsTheme.colors.aiViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "PitchMetrics Neural Engine",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PitchMetricsTheme.colors.pitchGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "v2.4 Active",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PitchMetricsTheme.colors.pitchGreen
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PitchMetricsTheme.colors.glassBackground)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricColumn(title = "Model Accuracy", value = "$accuracy%", valueColor = PitchMetricsTheme.colors.pitchGreen)
                MetricColumn(title = "Picks Analyzed", value = "$totalPicks+", valueColor = MaterialTheme.colorScheme.onBackground)
                MetricColumn(title = "Current Streak", value = "$winStreak W", valueColor = PitchMetricsTheme.colors.aiViolet)
            }
        }
    }
}

@Composable
private fun MetricColumn(title: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = PitchMetricsTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = valueColor
        )
    }
}

@Composable
fun PredictionFilterBar(
    selectedFilter: PredictionFilter,
    onSelectFilter: (PredictionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        PredictionFilter.HIGH_CONFIDENCE to "High Confidence (70%+)",
        PredictionFilter.VALUE_PICKS to "Value Picks",
        PredictionFilter.TRENDING to "Trending Picks",
        PredictionFilter.LEADERBOARD to "Leaderboard",
        PredictionFilter.ALL to "All Forecasts"
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("predictions_filter_bar"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (filter, label) ->
            val isSelected = selectedFilter == filter
            val bg = if (isSelected) PitchMetricsTheme.colors.aiViolet else PitchMetricsTheme.colors.elevatedSurface
            val textCol = if (isSelected) Color.White else PitchMetricsTheme.colors.textMuted
            val borderCol = if (isSelected) PitchMetricsTheme.colors.aiViolet else PitchMetricsTheme.colors.border

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(bg)
                    .border(1.dp, borderCol, CircleShape)
                    .clickable { onSelectFilter(filter) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("predictions_tab_${filter.name.lowercase()}")
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = textCol
                )
            }
        }
    }
}
