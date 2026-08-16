package com.example.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pitchmetrics_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FOLLOWED_TEAMS = stringSetPreferencesKey("followed_teams")
        val FOLLOWED_LEAGUES = stringSetPreferencesKey("followed_leagues")
        val PINNED_MATCHES = stringSetPreferencesKey("pinned_matches")
        val SAVED_USER_NAME = stringPreferencesKey("saved_user_name")
        val SAVED_USER_EMAIL = stringPreferencesKey("saved_user_email")

        // Notifications
        val NOTIFY_LIVE_GOALS = booleanPreferencesKey("notify_live_goals")
        val NOTIFY_KICKOFF = booleanPreferencesKey("notify_kickoff")
        val NOTIFY_FULL_TIME = booleanPreferencesKey("notify_full_time")
        val NOTIFY_AI_PICKS = booleanPreferencesKey("notify_ai_picks")
        val NOTIFY_HIGH_CONFIDENCE = booleanPreferencesKey("notify_high_confidence")
        val CONFIDENCE_THRESHOLD = intPreferencesKey("confidence_threshold")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    }

    val themeModeFlow: Flow<AppThemeMode> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.DARK.name
        try {
            AppThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
    }

    val followedTeamsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FOLLOWED_TEAMS] ?: emptySet()
    }

    val followedLeaguesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FOLLOWED_LEAGUES] ?: emptySet()
    }

    val pinnedMatchesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PINNED_MATCHES] ?: emptySet()
    }

    val savedUserFlow: Flow<Pair<String?, String?>> = context.dataStore.data.map { preferences ->
        Pair(
            preferences[PreferencesKeys.SAVED_USER_NAME],
            preferences[PreferencesKeys.SAVED_USER_EMAIL]
        )
    }

    val notifyLiveGoalsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFY_LIVE_GOALS] ?: true
    }

    val notifyKickoffFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFY_KICKOFF] ?: true
    }

    val notifyFullTimeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFY_FULL_TIME] ?: true
    }

    val notifyAiPicksFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFY_AI_PICKS] ?: true
    }

    val notifyHighConfidenceFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFY_HIGH_CONFIDENCE] ?: true
    }

    val confidenceThresholdFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CONFIDENCE_THRESHOLD] ?: 70
    }

    val hapticFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun toggleFollowTeam(teamId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FOLLOWED_TEAMS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(teamId)) {
                current.remove(teamId)
            } else {
                current.add(teamId)
            }
            preferences[PreferencesKeys.FOLLOWED_TEAMS] = current
        }
    }

    suspend fun toggleFollowLeague(leagueId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FOLLOWED_LEAGUES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(leagueId)) {
                current.remove(leagueId)
            } else {
                current.add(leagueId)
            }
            preferences[PreferencesKeys.FOLLOWED_LEAGUES] = current
        }
    }

    suspend fun togglePinMatch(matchId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.PINNED_MATCHES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(matchId)) {
                current.remove(matchId)
            } else {
                current.add(matchId)
            }
            preferences[PreferencesKeys.PINNED_MATCHES] = current
        }
    }

    suspend fun setNotifyLiveGoals(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_LIVE_GOALS] = enabled
        }
    }

    suspend fun setNotifyKickoff(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_KICKOFF] = enabled
        }
    }

    suspend fun setNotifyFullTime(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_FULL_TIME] = enabled
        }
    }

    suspend fun setNotifyAiPicks(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_AI_PICKS] = enabled
        }
    }

    suspend fun setNotifyHighConfidence(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_HIGH_CONFIDENCE] = enabled
        }
    }

    suspend fun setConfidenceThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONFIDENCE_THRESHOLD] = threshold
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun saveUserSession(name: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_USER_NAME] = name
            preferences[PreferencesKeys.SAVED_USER_EMAIL] = email
        }
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SAVED_USER_NAME)
            preferences.remove(PreferencesKeys.SAVED_USER_EMAIL)
        }
    }
}
