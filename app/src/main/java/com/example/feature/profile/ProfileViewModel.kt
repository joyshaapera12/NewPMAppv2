package com.example.feature.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.storage.UserPreferencesRepository
import com.example.data.repositories.AuthRepository
import com.example.data.repositories.CommunityRepository
import com.example.domain.models.User
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val liveNotificationsEnabled: Boolean = true,
    val oddsFormat: String = "DECIMAL",
    val leaderboard: List<User> = emptyList(),
    val isAuthLoading: Boolean = false,
    val authError: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = AuthRepository(application)
    private val communityRepo = CommunityRepository(application)
    private val prefs = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.themeModeFlow,
                prefs.savedUserFlow
            ) { theme, userPair ->
                Pair(theme, userPair)
            }.collect { (theme, userPair) ->
                val (username, _) = userPair
                _uiState.update { state ->
                    state.copy(
                        themeMode = theme,
                        isLoggedIn = !username.isNullOrBlank()
                    )
                }
            }
        }
        loadLeaderboardAndUser()
    }

    private fun loadLeaderboardAndUser() {
        viewModelScope.launch {
            val userRes = authRepo.getMe()
            val lbRes = communityRepo.getLeaderboard()
            _uiState.update { state ->
                state.copy(
                    user = userRes.getOrNull(),
                    leaderboard = lbRes.getOrDefault(getFallbackLeaderboard())
                )
            }
        }
    }

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authError = null) }
            val res = authRepo.login(username, pass)
            if (res.isSuccess) {
                _uiState.update { it.copy(isAuthLoading = false, isLoggedIn = true, user = res.getOrNull(), authError = null) }
            } else {
                _uiState.update { it.copy(isAuthLoading = false, authError = res.exceptionOrNull()?.message ?: "Login failed") }
            }
        }
    }

    fun register(username: String, email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authError = null) }
            val res = authRepo.register(username, email, pass)
            if (res.isSuccess) {
                _uiState.update { it.copy(isAuthLoading = false, isLoggedIn = true, user = res.getOrNull(), authError = null) }
            } else {
                _uiState.update { it.copy(isAuthLoading = false, authError = res.exceptionOrNull()?.message ?: "Registration failed") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _uiState.update { it.copy(isLoggedIn = false, user = null) }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
        }
    }

    private fun getFallbackLeaderboard(): List<User> {
        return listOf(
            User("1", "TacticalMaster", "", null, 84.2f, 210, 177, 9, 14, 1, 1420),
            User("2", "KloppGenius", "", null, 81.5f, 195, 159, 7, 12, 2, 1310),
            User("3", "AnalyticsKing", "", null, 79.0f, 180, 142, 5, 11, 3, 1240),
            User("4", "DataPunter", "", null, 76.8f, 160, 123, 4, 9, 4, 1150),
            User("5", "PitchOracle", "", null, 74.2f, 140, 104, 3, 8, 5, 1080)
        )
    }
}
