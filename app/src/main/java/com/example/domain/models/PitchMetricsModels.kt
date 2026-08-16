package com.example.domain.models

enum class MatchStatus {
    SCHEDULED,
    LIVE,
    HALF_TIME,
    FINISHED,
    POSTPONED,
    CANCELLED
}

data class MatchIdentity(
    val pmId: String?,
    val allSportsId: String?
) {
    val primaryId: String
        get() = pmId ?: allSportsId ?: ""
}

data class Team(
    val id: String,
    val name: String,
    val logoUrl: String,
    val country: String? = null
)

data class Match(
    val id: String,
    val pmId: String? = null,
    val allSportsId: String? = null,
    val leagueId: String,
    val leagueName: String,
    val leagueLogoUrl: String,
    val leagueCountry: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: MatchStatus,
    val statusText: String,
    val minute: String?,
    val startTime: String,
    val date: String,
    val stadium: String?,
    val aiConfidence: Float?, // 0.0f to 1.0f
    val aiPick: String?,
    val isLive: Boolean = status == MatchStatus.LIVE || status == MatchStatus.HALF_TIME,
    val isFinished: Boolean = status == MatchStatus.FINISHED
)

data class LiveMatch(
    val eventKey: String,
    val pmMatchId: String? = null,
    val asFixtureId: String? = null,
    val leagueId: String = "",
    val leagueName: String,
    val leagueLogo: String = "",
    val countryName: String,
    val homeTeamName: String,
    val homeTeamKey: String,
    val homeTeamLogo: String,
    val awayTeamName: String,
    val awayTeamKey: String,
    val awayTeamLogo: String,
    val score: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: String,
    val statusShort: String = status,
    val elapsed: String? = null,
    val minute: String,
    val kickoff: String = "",
    val isLive: Boolean,
    val aiConfidence: Float? = null,
    val aiPick: String? = null
)

data class Prediction(
    val id: String,
    val matchId: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeLogo: String,
    val awayLogo: String,
    val league: String,
    val pick: String,
    val confidence: Float, // 0.0f to 1.0f (strictly converted once at mapper boundary)
    val confidencePercent: Int, // 0 to 100 integer representation
    val homeWinProb: Float,
    val drawProb: Float,
    val awayWinProb: Float,
    val bttsProb: Float,
    val bttsPrediction: String?,
    val over25Prob: Float,
    val under25Prob: Float,
    val overUnderPick: String?,
    val expectedGoalsHome: Float,
    val expectedGoalsAway: Float,
    val reasoning: String,
    val tacticalSummary: String?,
    val isValueBet: Boolean,
    val isTrending: Boolean,
    val matchDate: String,
    val riskScore: String? = null,
    val safestPick: String? = null,
    val xgAnalysis: String? = null,
    val formAnalysis: String? = null
)

data class MatchEvent(
    val id: String,
    val minute: String,
    val type: String, // "goal", "card", "sub", "var"
    val isHomeTeam: Boolean,
    val playerName: String,
    val assistPlayerName: String?,
    val detail: String?,
    val score: String?
)

data class MatchStatistics(
    val possessionHome: Int? = null,
    val possessionAway: Int? = null,
    val shotsHome: Int? = null,
    val shotsAway: Int? = null,
    val shotsOnTargetHome: Int? = null,
    val shotsOnTargetAway: Int? = null,
    val xgHome: Float? = null,
    val xgAway: Float? = null,
    val cornersHome: Int? = null,
    val cornersAway: Int? = null,
    val foulsHome: Int? = null,
    val foulsAway: Int? = null,
    val yellowCardsHome: Int? = null,
    val yellowCardsAway: Int? = null,
    val redCardsHome: Int? = null,
    val redCardsAway: Int? = null,
    val passesHome: Int? = null,
    val passesAway: Int? = null,
    val passAccuracyHome: Int? = null,
    val passAccuracyAway: Int? = null
)

data class TacticalPlayer(
    val id: String,
    val name: String,
    val number: String,
    val position: String, // "GK", "DF", "MF", "FW"
    val gridX: Float, // 0.0f (left) to 1.0f (right)
    val gridY: Float, // 0.0f (goal) to 1.0f (midfield)
    val rating: String?,
    val photoUrl: String?
)

data class Lineup(
    val homeFormation: String,
    val awayFormation: String,
    val homeStarting: List<TacticalPlayer>,
    val awayStarting: List<TacticalPlayer>,
    val homeBench: List<TacticalPlayer>,
    val awayBench: List<TacticalPlayer>
)

data class League(
    val id: String,
    val name: String,
    val country: String,
    val logoUrl: String,
    val flagUrl: String,
    val season: String
)

data class Standing(
    val position: Int,
    val teamId: String,
    val teamName: String,
    val teamLogo: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDiff: Int,
    val points: Int,
    val form: String
)

data class TeamDetail(
    val id: String,
    val name: String,
    val logoUrl: String,
    val country: String,
    val founded: String?,
    val stadium: String?,
    val coach: String?,
    val squad: List<TacticalPlayer>,
    val recentMatches: List<Match>
)

data class HeadToHead(
    val matches: List<Match>,
    val homeWins: Int,
    val draws: Int,
    val awayWins: Int,
    val avgGoals: Double = 0.0
)

enum class OddsTrend {
    UP,
    DOWN,
    STABLE
}

data class BookmakerOdds(
    val bookmakerName: String,
    val logoUrl: String? = null,
    val homeOdds: Double,
    val drawOdds: Double,
    val awayOdds: Double,
    val openingHomeOdds: Double? = null,
    val openingDrawOdds: Double? = null,
    val openingAwayOdds: Double? = null,
    val homeTrend: OddsTrend = OddsTrend.STABLE,
    val drawTrend: OddsTrend = OddsTrend.STABLE,
    val awayTrend: OddsTrend = OddsTrend.STABLE,
    val payoutPercent: Double = 95.5
)

data class PlayerProfile(
    val id: String,
    val name: String,
    val number: String,
    val position: String,
    val teamName: String,
    val teamLogo: String? = null,
    val photoUrl: String? = null,
    val age: Int = 26,
    val nationality: String = "International",
    val height: String = "182 cm",
    val weight: String = "76 kg",
    val preferredFoot: String = "Right",
    val marketValue: String = "€35.0M",
    val appearances: Int = 22,
    val minutesPlayed: Int = 1840,
    val goals: Int = 8,
    val assists: Int = 5,
    val shotsTotal: Int = 42,
    val shotsOnTarget: Int = 24,
    val shotConversionRate: Float = 19.0f,
    val xg: Float = 7.4f,
    val xa: Float = 4.2f,
    val keyPassesPer90: Float = 1.8f,
    val passAccuracy: Float = 84.5f,
    val yellowCards: Int = 3,
    val redCards: Int = 0,
    val rating: Float = 7.35f,
    val tacticalRole: String = "Inverted Winger / Inside Forward"
)

data class RefereeAnalytics(
    val name: String,
    val matchesCount: Int = 18,
    val foulsPerGame: Float = 21.4f,
    val yellowCardsPerGame: Float = 4.2f,
    val redCardsPerGame: Float = 0.22f,
    val penaltiesPerGame: Float = 0.33f,
    val strictnessLevel: String = "Moderate"
)

data class StadiumAnalytics(
    val name: String,
    val city: String = "Metropolis",
    val capacity: Int = 54000,
    val surface: String = "Natural Grass",
    val homeWinRatePercent: Float = 58.3f,
    val avgGoalsPerGame: Float = 2.85f,
    val atmosphereRating: Float = 8.8f
)

data class CommunityComment(
    val id: String,
    val matchId: String,
    val authorName: String,
    val authorAvatar: String? = null,
    val teamFlair: String? = null,
    val text: String,
    val timestamp: String,
    val likesCount: Int = 0,
    val userLiked: Boolean = false
)

data class LiveEmojiReaction(
    val emoji: String,
    val count: Int,
    val userSelected: Boolean = false
)

data class PredictorLeaderboardUser(
    val rank: Int,
    val username: String,
    val avatarUrl: String? = null,
    val totalPredictions: Int,
    val correctPredictions: Int,
    val winRatePercent: Float,
    val points: Int,
    val streak: Int,
    val badgeTitle: String
)

data class MatchOdds(
    val homeWin: String?,
    val draw: String?,
    val awayWin: String?,
    val over25: String?,
    val under25: String?,
    val bttsYes: String?,
    val bttsNo: String?
)

data class CommunitySentiment(
    val matchId: String,
    val homeVotes: Int,
    val drawVotes: Int,
    val awayVotes: Int,
    val totalVotes: Int,
    val homePercentage: Float,
    val drawPercentage: Float,
    val awayPercentage: Float,
    val userVote: String?
)

data class User(
    val id: String,
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val accuracy: Float,
    val totalPredictions: Int,
    val correctPredictions: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val rank: Int,
    val points: Int
)

enum class SearchResultType {
    TEAM,
    LEAGUE,
    MATCH,
    PAGE_LINK
}

data class SearchResult(
    val id: String,
    val type: SearchResultType,
    val title: String,
    val subtitle: String?,
    val description: String?,
    val logoUrl: String?,
    val url: String?,
    val icon: String?,
    val relevance: Int?,
    val status: String? = null,
    val homeScore: Int? = null,
    val awayScore: Int? = null
)

data class GroupedSearchResults(
    val teams: List<SearchResult> = emptyList(),
    val competitions: List<SearchResult> = emptyList(),
    val matches: List<SearchResult> = emptyList()
) {
    val totalCount: Int
        get() = teams.size + competitions.size + matches.size

    val isEmpty: Boolean
        get() = totalCount == 0
}
