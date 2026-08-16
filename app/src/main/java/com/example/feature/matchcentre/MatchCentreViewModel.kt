package com.example.feature.matchcentre

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.SafeApiLogger
import com.example.data.repositories.*
import com.example.domain.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MatchCentreTab {
    OVERVIEW,
    PREDICTION,
    LINEUPS,
    STATS,
    H2H,
    COMMUNITY
}

data class MatchRouteArgs(
    val matchId: String,
    val pmMatchId: String? = null,
    val allSportsFixtureId: String? = null,
    val source: String = ""
)

data class MatchCentreUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val match: Match? = null,
    val events: List<MatchEvent> = emptyList(),
    val statistics: MatchStatistics? = null,
    val lineups: Lineup? = null,
    val prediction: Prediction? = null,
    val odds: MatchOdds? = null,
    val h2h: HeadToHead? = null,
    val sentiment: CommunitySentiment? = null,
    val comments: List<CommunityComment> = emptyList(),
    val emojiReactions: List<LiveEmojiReaction> = emptyList(),
    val refereeAnalytics: RefereeAnalytics? = null,
    val stadiumAnalytics: StadiumAnalytics? = null,
    val selectedPlayer: PlayerProfile? = null,
    val selectedTab: MatchCentreTab = MatchCentreTab.OVERVIEW,
    val userVote: String? = null,
    val isPostingComment: Boolean = false,
    val errorMessage: String? = null
)

class MatchCentreViewModel(
    application: Application,
    private val args: MatchRouteArgs
) : AndroidViewModel(application) {
    private val matchRepo = MatchRepository(application)
    private val liveRepo = LiveRepository(application)
    private val predictionRepo = PredictionRepository(application)
    private val communityRepo = CommunityRepository(application)
    private val playerRepo = PlayerRepository(application)

    private val _uiState = MutableStateFlow(MatchCentreUiState())
    val uiState: StateFlow<MatchCentreUiState> = _uiState.asStateFlow()

    init {
        loadMatchCentreData()
    }

    fun loadMatchCentreData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val pmId = args.pmMatchId?.takeIf { it.isNotBlank() } ?: args.matchId.takeIf { it.isNotBlank() && args.source != "allsports" }
            val asId = args.allSportsFixtureId?.takeIf { it.isNotBlank() } ?: if (args.source == "allsports") args.matchId else null
            val isAllSportsOnly = args.source == "allsports" || (pmId == null && asId != null)
            val fixtureKey = pmId ?: asId ?: args.matchId

            // Load community data
            loadCommunityData(fixtureKey)

            if (isAllSportsOnly && asId != null) {
                SafeApiLogger.logMatchDetailRequest(
                    source = "allsports",
                    requestedEndpoint = "live_polling_overlay",
                    primaryId = asId,
                    pmMatchId = null,
                    asFixtureId = asId
                )

                val liveMatchesRes = liveRepo.getLiveMatches()
                val liveList = liveMatchesRes.getOrDefault(emptyList())
                val matchedLive = liveList.find { it.eventKey == asId || it.asFixtureId == asId }

                if (matchedLive != null) {
                    val resolvedMatch = Match(
                        id = matchedLive.eventKey,
                        pmId = matchedLive.pmMatchId,
                        allSportsId = matchedLive.asFixtureId ?: matchedLive.eventKey,
                        leagueId = matchedLive.leagueId,
                        leagueName = matchedLive.leagueName,
                        leagueLogoUrl = matchedLive.leagueLogo,
                        leagueCountry = matchedLive.countryName,
                        homeTeam = Team(
                            id = matchedLive.homeTeamKey.ifBlank { "h_${matchedLive.eventKey}" },
                            name = matchedLive.homeTeamName,
                            logoUrl = matchedLive.homeTeamLogo
                        ),
                        awayTeam = Team(
                            id = matchedLive.awayTeamKey.ifBlank { "a_${matchedLive.eventKey}" },
                            name = matchedLive.awayTeamName,
                            logoUrl = matchedLive.awayTeamLogo
                        ),
                        homeScore = matchedLive.homeScore,
                        awayScore = matchedLive.awayScore,
                        status = if (matchedLive.isLive) MatchStatus.LIVE else if (matchedLive.statusShort in listOf("FT", "AET", "PEN")) MatchStatus.FINISHED else MatchStatus.SCHEDULED,
                        statusText = matchedLive.statusShort.ifBlank { if (matchedLive.isLive) "LIVE" else "SCHEDULED" },
                        minute = matchedLive.minute,
                        startTime = matchedLive.kickoff,
                        date = "Today",
                        stadium = null,
                        aiConfidence = matchedLive.aiConfidence,
                        aiPick = matchedLive.aiPick
                    )

                    val sentimentRes = communityRepo.getSentiment(asId)

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            match = resolvedMatch,
                            events = emptyList(),
                            statistics = null,
                            lineups = null,
                            prediction = null,
                            odds = null,
                            h2h = null,
                            sentiment = sentimentRes.getOrNull(),
                            refereeAnalytics = playerRepo.getRefereeAnalytics(null),
                            stadiumAnalytics = playerRepo.getStadiumAnalytics(null, resolvedMatch.homeTeam.name),
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            match = null,
                            errorMessage = "Live fixture #$asId is no longer active in the current feed."
                        )
                    }
                }
            } else if (pmId != null) {
                val requestSource = if (asId != null) "merged" else "pitchmetrics"
                SafeApiLogger.logMatchDetailRequest(
                    source = requestSource,
                    requestedEndpoint = "matches/$pmId",
                    primaryId = pmId,
                    pmMatchId = pmId,
                    asFixtureId = asId
                )

                val matchRes = matchRepo.getMatchDetail(pmId)
                val eventsRes = matchRepo.getMatchEvents(pmId)
                val statsRes = matchRepo.getMatchStatistics(pmId)
                val lineupsRes = matchRepo.getMatchLineups(pmId)
                val oddsRes = matchRepo.getMatchOdds(pmId)
                val h2hRes = matchRepo.getMatchH2H(pmId)
                val predRes = predictionRepo.getPredictionForMatch(pmId)
                val sentimentRes = communityRepo.getSentiment(pmId)

                var loadedMatch = matchRes.getOrNull()

                if (loadedMatch != null && asId != null) {
                    val liveMatchesRes = liveRepo.getLiveMatches()
                    val liveMatch = liveMatchesRes.getOrDefault(emptyList()).find { it.eventKey == asId || it.asFixtureId == asId }
                    if (liveMatch != null) {
                        loadedMatch = loadedMatch.copy(
                            homeScore = liveMatch.homeScore ?: loadedMatch.homeScore,
                            awayScore = liveMatch.awayScore ?: loadedMatch.awayScore,
                            minute = liveMatch.minute ?: loadedMatch.minute,
                            status = if (liveMatch.isLive) MatchStatus.LIVE else loadedMatch.status,
                            statusText = liveMatch.statusShort.ifBlank { loadedMatch.statusText }
                        )
                    }
                }

                if (loadedMatch != null) {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            match = loadedMatch,
                            events = eventsRes.getOrDefault(emptyList()),
                            statistics = statsRes.getOrNull(),
                            lineups = lineupsRes.getOrNull(),
                            prediction = predRes.getOrNull(),
                            odds = oddsRes.getOrNull(),
                            h2h = h2hRes.getOrNull(),
                            sentiment = sentimentRes.getOrNull(),
                            refereeAnalytics = playerRepo.getRefereeAnalytics(null),
                            stadiumAnalytics = playerRepo.getStadiumAnalytics(loadedMatch.stadium, loadedMatch.homeTeam.name),
                            errorMessage = null
                        )
                    }
                } else {
                    val err = matchRes.exceptionOrNull()?.localizedMessage ?: "Match details unavailable for fixture #$pmId"
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            match = null,
                            errorMessage = err
                        )
                    }
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        match = null,
                        errorMessage = "Invalid fixture identifier."
                    )
                }
            }
        }
    }

    private suspend fun loadCommunityData(fixtureKey: String) {
        val commentsRes = communityRepo.getMatchComments(fixtureKey)
        val reactionsRes = communityRepo.getLiveEmojiReactions(fixtureKey)
        _uiState.update {
            it.copy(
                comments = commentsRes.getOrDefault(emptyList()),
                emojiReactions = reactionsRes.getOrDefault(emptyList())
            )
        }
    }

    fun selectTab(tab: MatchCentreTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onPlayerSelected(player: TacticalPlayer, teamName: String) {
        viewModelScope.launch {
            val profileRes = playerRepo.getPlayerProfile(
                playerId = player.id,
                playerName = player.name,
                teamName = teamName,
                position = player.position,
                number = player.number
            )
            _uiState.update { it.copy(selectedPlayer = profileRes.getOrNull()) }
        }
    }

    fun dismissPlayerDetails() {
        _uiState.update { it.copy(selectedPlayer = null) }
    }

    fun submitVote(choice: String) {
        val targetId = args.pmMatchId ?: args.matchId
        if (targetId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(userVote = choice) }
            val res = communityRepo.submitVote(targetId, choice)
            if (res.isSuccess) {
                _uiState.update { it.copy(sentiment = res.getOrNull()) }
            }
        }
    }

    fun postComment(text: String, flair: String) {
        if (text.isBlank()) return
        val targetId = args.pmMatchId ?: args.matchId
        viewModelScope.launch {
            _uiState.update { it.copy(isPostingComment = true) }
            communityRepo.postComment(targetId, text, authorName = "You", teamFlair = flair)
            val updatedComments = communityRepo.getMatchComments(targetId).getOrDefault(emptyList())
            _uiState.update { it.copy(comments = updatedComments, isPostingComment = false) }
        }
    }

    fun toggleCommentLike(commentId: String) {
        val targetId = args.pmMatchId ?: args.matchId
        viewModelScope.launch {
            communityRepo.toggleCommentLike(targetId, commentId)
            val updatedComments = communityRepo.getMatchComments(targetId).getOrDefault(emptyList())
            _uiState.update { it.copy(comments = updatedComments) }
        }
    }

    fun reactWithEmoji(emoji: String) {
        val targetId = args.pmMatchId ?: args.matchId
        viewModelScope.launch {
            val updatedReactions = communityRepo.reactWithEmoji(targetId, emoji).getOrDefault(emptyList())
            _uiState.update { it.copy(emojiReactions = updatedReactions) }
        }
    }
}
