package com.example.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.storage.UserPreferencesRepository
import com.example.data.repositories.LiveRepository
import com.example.data.repositories.MatchRepository
import com.example.data.repositories.PredictionRepository
import com.example.domain.models.LiveMatch
import com.example.domain.models.Match
import com.example.domain.models.Prediction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val liveMatches: List<LiveMatch> = emptyList(),
    val todayMatches: List<Match> = emptyList(),
    val featuredPick: Prediction? = null,
    val highConfidencePicks: List<Prediction> = emptyList(),
    val selectedLeagueFilter: String = "ALL",
    val pinnedMatchIds: Set<String> = emptySet(),
    val lastUpdatedText: String = "Just now"
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val matchRepo = MatchRepository(application)
    private val liveRepo = LiveRepository(application)
    private val predictionRepo = PredictionRepository(application)
    private val prefs = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.pinnedMatchesFlow.collect { pinned ->
                _uiState.update { it.copy(pinnedMatchIds = pinned) }
            }
        }
        loadHomeData()
    }

    fun loadHomeData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val matchesResult = matchRepo.getTodayMatches(forceRefresh = isRefresh)
            val liveResult = liveRepo.getLiveMatches()
            val predictionsResult = predictionRepo.getHighConfidencePredictions()

            val matches = matchesResult.getOrDefault(emptyList())
            val live = liveResult.getOrDefault(emptyList())
            val activeLive = live.filter { it.isLive }
            val predictions = predictionsResult.getOrDefault(emptyList())

            val isOffline = matchesResult.isFailure && liveResult.isFailure && matches.isEmpty()
            val error = if (isOffline) "Could not connect to PitchMetrics live feed." else null

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = isOffline,
                    errorMessage = error,
                    todayMatches = matches,
                    liveMatches = activeLive,
                    highConfidencePicks = predictions,
                    featuredPick = predictions.firstOrNull(),
                    lastUpdatedText = "Just now"
                )
            }
        }
    }

    fun setLeagueFilter(leagueKey: String) {
        _uiState.update { it.copy(selectedLeagueFilter = leagueKey) }
    }

    fun togglePinMatch(matchId: String) {
        viewModelScope.launch {
            prefs.togglePinMatch(matchId)
        }
    }
}
