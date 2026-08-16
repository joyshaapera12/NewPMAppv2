package com.example.feature.live

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.LiveRepository
import com.example.domain.models.LiveMatch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LiveUiState(
    val isLoading: Boolean = true,
    val isPolling: Boolean = false,
    val isStale: Boolean = false,
    val liveMatches: List<LiveMatch> = emptyList(),
    val filteredMatches: List<LiveMatch> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "ALL", // "ALL", "1H", "2H", "HT"
    val errorMessage: String? = null,
    val lastUpdatedSecondsAgo: Int = 0,
    val liveGamesCount: Int = 0,
    val goalsTodayCount: Int = 0
)

class LiveViewModel(application: Application) : AndroidViewModel(application) {
    private val liveRepo = LiveRepository(application)
    private var pollingJob: Job? = null
    private var lastSuccessTimestamp: Long = 0L

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            _uiState.update { it.copy(isPolling = true) }
            while (isActive) {
                fetchLiveScores()
                // Fast poll loop every 25 seconds while counting seconds
                for (sec in 1..25) {
                    delay(1000)
                    if (!isActive) break
                    val elapsedSec = if (lastSuccessTimestamp > 0L) {
                        ((System.currentTimeMillis() - lastSuccessTimestamp) / 1000).toInt()
                    } else {
                        sec
                    }
                    _uiState.update { it.copy(lastUpdatedSecondsAgo = elapsedSec) }
                }
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        _uiState.update { it.copy(isPolling = false) }
    }

    fun refreshNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.liveMatches.isEmpty()) }
            fetchLiveScores()
        }
    }

    private suspend fun fetchLiveScores() {
        val result = liveRepo.getLiveMatches()
        if (result.isSuccess) {
            val list = result.getOrDefault(emptyList())
            lastSuccessTimestamp = System.currentTimeMillis()
            _uiState.update { state ->
                val filtered = applyFilters(list, state.searchQuery, state.selectedStatusFilter)
                val liveCount = list.count { it.isLive }
                val goalsCount = list.sumOf { (it.homeScore ?: 0) + (it.awayScore ?: 0) }
                state.copy(
                    isLoading = false,
                    isStale = false,
                    liveMatches = list,
                    filteredMatches = filtered,
                    errorMessage = null,
                    lastUpdatedSecondsAgo = 0,
                    liveGamesCount = liveCount,
                    goalsTodayCount = goalsCount
                )
            }
        } else {
            val error = result.exceptionOrNull()?.localizedMessage ?: "Live feed temporarily unreachable"
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isStale = state.liveMatches.isNotEmpty(),
                    errorMessage = if (state.liveMatches.isEmpty()) error else null
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.liveMatches, query, state.selectedStatusFilter)
            state.copy(searchQuery = query, filteredMatches = filtered)
        }
    }

    fun onStatusFilterChanged(status: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.liveMatches, state.searchQuery, status)
            state.copy(selectedStatusFilter = status, filteredMatches = filtered)
        }
    }

    private fun applyFilters(matches: List<LiveMatch>, query: String, statusFilter: String): List<LiveMatch> {
        return matches.filter { match ->
            val matchesQuery = query.isBlank() ||
                match.homeTeamName.contains(query, ignoreCase = true) ||
                match.awayTeamName.contains(query, ignoreCase = true) ||
                match.leagueName.contains(query, ignoreCase = true) ||
                match.countryName.contains(query, ignoreCase = true)

            val matchesStatus = when (statusFilter) {
                "1H" -> match.statusShort == "1H" || match.minute.contains("1") || match.status.contains("1st", ignoreCase = true)
                "HT" -> match.statusShort == "HT" || match.minute.contains("HT") || match.status.contains("Half", ignoreCase = true)
                "2H" -> match.statusShort == "2H" || match.minute.contains("2") || match.status.contains("2nd", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesStatus
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
