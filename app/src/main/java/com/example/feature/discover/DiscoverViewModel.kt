package com.example.feature.discover

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.LeagueRepository
import com.example.data.repositories.MatchRepository
import com.example.domain.models.GroupedSearchResults
import com.example.domain.models.League
import com.example.domain.models.SearchResult
import com.example.domain.models.Standing
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SearchFilterTab {
    ALL,
    CLUBS,
    COMPETITIONS,
    FIXTURES
}

data class DiscoverUiState(
    val isLoading: Boolean = true,
    val leagues: List<League> = emptyList(),
    val selectedLeagueId: String = "",
    val selectedLeagueName: String = "",
    val standings: List<Standing> = emptyList(),
    val searchQuery: String = "",
    val searchResults: GroupedSearchResults = GroupedSearchResults(),
    val selectedSearchFilter: SearchFilterTab = SearchFilterTab.ALL,
    val isSearching: Boolean = false,
    val errorMessage: String? = null
)

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {
    private val leagueRepo = LeagueRepository(application)
    private val matchRepo = MatchRepository(application)
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadDiscoverData()
    }

    fun loadDiscoverData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val leaguesRes = leagueRepo.getLeagues()
            val leagues = leaguesRes.getOrDefault(emptyList())
            val firstLeague = leagues.firstOrNull()

            val standings = if (firstLeague != null) {
                leagueRepo.getStandings(firstLeague.id).getOrDefault(emptyList())
            } else {
                emptyList()
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    leagues = leagues,
                    selectedLeagueId = firstLeague?.id.orEmpty(),
                    selectedLeagueName = firstLeague?.name.orEmpty(),
                    standings = standings,
                    errorMessage = if (leagues.isEmpty() && leaguesRes.isFailure) "Failed to load competitions" else null
                )
            }
        }
    }

    fun selectLeague(league: League) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedLeagueId = league.id, selectedLeagueName = league.name, isLoading = true) }
            val standingsRes = leagueRepo.getStandings(league.id)
            val standings = standingsRes.getOrDefault(emptyList())
            _uiState.update { it.copy(isLoading = false, standings = standings) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.trim().length >= 2) {
            searchJob = viewModelScope.launch {
                delay(300) // Debounce rapid keystrokes
                _uiState.update { it.copy(isSearching = true) }
                val results = matchRepo.searchMulti(query)
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        searchResults = results.getOrDefault(GroupedSearchResults())
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    searchResults = GroupedSearchResults(),
                    isSearching = false
                )
            }
        }
    }

    fun setSearchFilterTab(tab: SearchFilterTab) {
        _uiState.update { it.copy(selectedSearchFilter = tab) }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = GroupedSearchResults(),
                isSearching = false
            )
        }
    }
}
