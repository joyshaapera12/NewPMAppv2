package com.example.feature.predictions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.CommunityRepository
import com.example.data.repositories.PredictionRepository
import com.example.domain.models.Prediction
import com.example.domain.models.PredictorLeaderboardUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PredictionFilter {
    HIGH_CONFIDENCE,
    VALUE_PICKS,
    TRENDING,
    LEADERBOARD,
    ALL
}

data class PredictionsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val predictions: List<Prediction> = emptyList(),
    val filteredPredictions: List<Prediction> = emptyList(),
    val leaderboard: List<PredictorLeaderboardUser> = emptyList(),
    val selectedFilter: PredictionFilter = PredictionFilter.HIGH_CONFIDENCE,
    val modelAccuracy: Float = 78.4f,
    val totalPicksAnalyzed: Int = 1420,
    val winStreak: Int = 8,
    val errorMessage: String? = null
)

class PredictionsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PredictionRepository(application)
    private val communityRepo = CommunityRepository(application)

    private val _uiState = MutableStateFlow(PredictionsUiState())
    val uiState: StateFlow<PredictionsUiState> = _uiState.asStateFlow()

    init {
        loadPredictions()
    }

    fun loadPredictions(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val result = repo.getAllPredictions()
            val leaderboardRes = communityRepo.getPredictorLeaderboard()

            if (result.isSuccess) {
                val all = result.getOrDefault(emptyList())
                val leaders = leaderboardRes.getOrDefault(emptyList())
                _uiState.update { state ->
                    val filtered = applyFilter(all, state.selectedFilter)
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        predictions = all,
                        filteredPredictions = filtered,
                        leaderboard = leaders,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Could not retrieve AI predictions feed."
                    )
                }
            }
        }
    }

    fun setFilter(filter: PredictionFilter) {
        _uiState.update { state ->
            val filtered = applyFilter(state.predictions, filter)
            state.copy(selectedFilter = filter, filteredPredictions = filtered)
        }
    }

    private fun applyFilter(list: List<Prediction>, filter: PredictionFilter): List<Prediction> {
        return when (filter) {
            PredictionFilter.HIGH_CONFIDENCE -> list.filter { it.confidence >= 0.70f }.sortedByDescending { it.confidence }
            PredictionFilter.VALUE_PICKS -> list.filter { it.isValueBet }
            PredictionFilter.TRENDING -> list.filter { it.isTrending }
            PredictionFilter.LEADERBOARD -> emptyList()
            PredictionFilter.ALL -> list
        }
    }
}
