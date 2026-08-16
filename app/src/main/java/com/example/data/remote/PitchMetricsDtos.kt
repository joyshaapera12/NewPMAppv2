package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    @Json(name = "data") val data: T? = null,
    @Json(name = "matches") val matches: T? = null,
    @Json(name = "predictions") val predictions: T? = null,
    @Json(name = "results") val results: T? = null,
    @Json(name = "result") val result: T? = null,
    @Json(name = "leagues") val leagues: T? = null,
    @Json(name = "standings") val standings: T? = null,
    @Json(name = "events") val events: T? = null,
    @Json(name = "statistics") val statistics: T? = null,
    @Json(name = "leaderboard") val leaderboard: T? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "total") val total: Int? = null,
    @Json(name = "fetched_at") val fetchedAt: String? = null,
    @Json(name = "cache") val cache: String? = null
)

@JsonClass(generateAdapter = true)
data class TeamNestedDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "logo") val logo: String? = null,
    @Json(name = "shortName") val shortName: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "league_name") val leagueName: String? = null,
    @Json(name = "league_country") val leagueCountry: String? = null
)

@JsonClass(generateAdapter = true)
data class MatchDto(
    // IDs
    @Json(name = "id") val id: String? = null,
    @Json(name = "externalId") val externalId: String? = null,
    @Json(name = "match_id") val matchId: String? = null,
    @Json(name = "pmMatchId") val pmMatchId: String? = null,
    @Json(name = "pm_match_id") val pmMatchIdSnake: String? = null,
    @Json(name = "asFixtureId") val asFixtureId: String? = null,
    @Json(name = "as_fixture_id") val asFixtureIdSnake: String? = null,
    @Json(name = "allsports_id") val allsportsId: String? = null,
    @Json(name = "event_key") val eventKey: String? = null,

    // League
    @Json(name = "leagueId") val leagueIdCamel: String? = null,
    @Json(name = "league_id") val leagueId: String? = null,
    @Json(name = "leagueKey") val leagueKey: String? = null,
    @Json(name = "league_key") val leagueKeySnake: String? = null,
    @Json(name = "leagueName") val leagueNameCamel: String? = null,
    @Json(name = "league_name") val leagueName: String? = null,
    @Json(name = "competition_name") val competitionName: String? = null,
    @Json(name = "leagueLogo") val leagueLogoCamel: String? = null,
    @Json(name = "league_logo") val leagueLogo: String? = null,
    @Json(name = "leagueFlag") val leagueFlagCamel: String? = null,
    @Json(name = "league_flag") val leagueFlag: String? = null,
    @Json(name = "leagueCountry") val leagueCountryCamel: String? = null,
    @Json(name = "league_country") val leagueCountrySnake: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "country_name") val countryName: String? = null,

    // Home Team
    @Json(name = "homeTeam") val homeTeamNested: TeamNestedDto? = null,
    @Json(name = "homeTeamId") val homeTeamIdCamel: String? = null,
    @Json(name = "home_team_id") val homeTeamId: String? = null,
    @Json(name = "homeTeamKey") val homeTeamKey: String? = null,
    @Json(name = "home_team_key") val homeTeamKeySnake: String? = null,
    @Json(name = "homeTeamName") val homeTeamNameCamel: String? = null,
    @Json(name = "home_team_name") val homeTeamName: String? = null,
    @Json(name = "home_name") val homeName: String? = null,
    @Json(name = "event_home_team") val eventHomeTeam: String? = null,
    @Json(name = "homeLogo") val homeLogoCamel: String? = null,
    @Json(name = "home_logo") val homeLogoSnake: String? = null,
    @Json(name = "homeTeamLogo") val homeTeamLogoCamel: String? = null,
    @Json(name = "home_team_logo") val homeTeamLogo: String? = null,
    @Json(name = "homeTeamShortName") val homeTeamShortName: String? = null,

    // Away Team
    @Json(name = "awayTeam") val awayTeamNested: TeamNestedDto? = null,
    @Json(name = "awayTeamId") val awayTeamIdCamel: String? = null,
    @Json(name = "away_team_id") val awayTeamId: String? = null,
    @Json(name = "awayTeamKey") val awayTeamKey: String? = null,
    @Json(name = "away_team_key") val awayTeamKeySnake: String? = null,
    @Json(name = "awayTeamName") val awayTeamNameCamel: String? = null,
    @Json(name = "away_team_name") val awayTeamName: String? = null,
    @Json(name = "away_name") val awayName: String? = null,
    @Json(name = "event_away_team") val eventAwayTeam: String? = null,
    @Json(name = "awayLogo") val awayLogoCamel: String? = null,
    @Json(name = "away_logo") val awayLogoSnake: String? = null,
    @Json(name = "awayTeamLogo") val awayTeamLogoCamel: String? = null,
    @Json(name = "away_team_logo") val awayTeamLogo: String? = null,
    @Json(name = "awayTeamShortName") val awayTeamShortName: String? = null,

    // Scores
    @Json(name = "homeScore") val homeScoreCamel: String? = null,
    @Json(name = "home_score") val homeScore: String? = null,
    @Json(name = "goals_home") val goalsHome: String? = null,
    @Json(name = "awayScore") val awayScoreCamel: String? = null,
    @Json(name = "away_score") val awayScore: String? = null,
    @Json(name = "goals_away") val goalsAway: String? = null,
    @Json(name = "event_final_result") val eventFinalResult: String? = null,
    @Json(name = "event_halftime_result") val eventHalftimeResult: String? = null,

    // Status & Timing
    @Json(name = "status") val status: String? = null,
    @Json(name = "statusShort") val statusShortCamel: String? = null,
    @Json(name = "status_short") val statusShort: String? = null,
    @Json(name = "event_status") val eventStatus: String? = null,
    @Json(name = "event_live") val eventLive: String? = null,
    @Json(name = "minute") val minute: String? = null,
    @Json(name = "elapsed") val elapsed: String? = null,
    @Json(name = "kickoff") val kickoff: String? = null,
    @Json(name = "startTime") val startTimeCamel: String? = null,
    @Json(name = "start_time") val startTime: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "event_date") val eventDate: String? = null,
    @Json(name = "time") val time: String? = null,
    @Json(name = "event_time") val eventTime: String? = null,
    @Json(name = "stadium") val stadium: String? = null,
    @Json(name = "event_stadium") val eventStadium: String? = null,
    @Json(name = "venue") val venue: String? = null,
    @Json(name = "round") val round: String? = null,
    @Json(name = "referee") val referee: String? = null,
    @Json(name = "mapped") val mapped: Boolean? = null,

    // AI & Intelligence
    @Json(name = "ai_confidence") val aiConfidence: Double? = null,
    @Json(name = "aiConfidence") val aiConfidenceCamel: Double? = null,
    @Json(name = "ai_pick") val aiPick: String? = null,
    @Json(name = "aiPick") val aiPickCamel: String? = null,
    @Json(name = "winnerPick") val winnerPick: String? = null,
    @Json(name = "winnerConfidence") val winnerConfidence: Double? = null,
    @Json(name = "drawConfidence") val drawConfidence: Double? = null,
    @Json(name = "bttsConfidence") val bttsConfidence: Double? = null,
    @Json(name = "over25Confidence") val over25Confidence: Double? = null,
    @Json(name = "safestPick") val safestPick: String? = null,
    @Json(name = "riskScore") val riskScore: String? = null,
    @Json(name = "hasPrediction") val hasPrediction: Boolean? = null,
    @Json(name = "prediction") val prediction: PredictionDto? = null,
    @Json(name = "statistics") val statistics: MatchStatisticsDto? = null,
    @Json(name = "events") val events: List<MatchEventDto>? = null
)

@JsonClass(generateAdapter = true)
data class AllSportsLiveDto(
    @Json(name = "asFixtureId") val asFixtureId: String? = null,
    @Json(name = "as_fixture_id") val asFixtureIdSnake: String? = null,
    @Json(name = "pmMatchId") val pmMatchId: String? = null,
    @Json(name = "pm_match_id") val pmMatchIdSnake: String? = null,
    @Json(name = "event_key") val eventKey: String? = null,
    @Json(name = "id") val id: String? = null,
    @Json(name = "match_id") val matchId: String? = null,

    @Json(name = "homeTeamId") val homeTeamId: String? = null,
    @Json(name = "home_team_id") val homeTeamIdSnake: String? = null,
    @Json(name = "home_team_key") val homeTeamKey: String? = null,
    @Json(name = "homeTeamKey") val homeTeamKeyCamel: String? = null,
    @Json(name = "homeTeamName") val homeTeamName: String? = null,
    @Json(name = "home_team_name") val homeTeamNameSnake: String? = null,
    @Json(name = "home_name") val homeName: String? = null,
    @Json(name = "event_home_team") val eventHomeTeam: String? = null,

    @Json(name = "awayTeamId") val awayTeamId: String? = null,
    @Json(name = "away_team_id") val awayTeamIdSnake: String? = null,
    @Json(name = "away_team_key") val awayTeamKey: String? = null,
    @Json(name = "awayTeamKey") val awayTeamKeyCamel: String? = null,
    @Json(name = "awayTeamName") val awayTeamName: String? = null,
    @Json(name = "away_team_name") val awayTeamNameSnake: String? = null,
    @Json(name = "away_name") val awayName: String? = null,
    @Json(name = "event_away_team") val eventAwayTeam: String? = null,

    @Json(name = "homeLogo") val homeLogo: String? = null,
    @Json(name = "home_logo") val homeLogoSnake: String? = null,
    @Json(name = "homeTeamLogo") val homeTeamLogo: String? = null,
    @Json(name = "home_team_logo") val homeTeamLogoSnake: String? = null,

    @Json(name = "awayLogo") val awayLogo: String? = null,
    @Json(name = "away_logo") val awayLogoSnake: String? = null,
    @Json(name = "awayTeamLogo") val awayTeamLogo: String? = null,
    @Json(name = "away_team_logo") val awayTeamLogoSnake: String? = null,

    @Json(name = "homeScore") val homeScore: String? = null,
    @Json(name = "home_score") val homeScoreSnake: String? = null,
    @Json(name = "goals_home") val goalsHome: String? = null,
    @Json(name = "awayScore") val awayScore: String? = null,
    @Json(name = "away_score") val awayScoreSnake: String? = null,
    @Json(name = "goals_away") val goalsAway: String? = null,
    @Json(name = "event_final_result") val eventFinalResult: String? = null,
    @Json(name = "event_halftime_result") val eventHalftimeResult: String? = null,

    @Json(name = "elapsed") val elapsed: String? = null,
    @Json(name = "minute") val minute: String? = null,
    @Json(name = "statusShort") val statusShort: String? = null,
    @Json(name = "status_short") val statusShortSnake: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "event_status") val eventStatus: String? = null,
    @Json(name = "event_live") val eventLive: String? = null,

    @Json(name = "kickoff") val kickoff: String? = null,
    @Json(name = "event_date") val eventDate: String? = null,
    @Json(name = "event_time") val eventTime: String? = null,
    @Json(name = "leagueId") val leagueId: String? = null,
    @Json(name = "league_id") val leagueIdSnake: String? = null,
    @Json(name = "league_key") val leagueKey: String? = null,
    @Json(name = "leagueKey") val leagueKeyCamel: String? = null,
    @Json(name = "leagueName") val leagueName: String? = null,
    @Json(name = "league_name") val leagueNameSnake: String? = null,
    @Json(name = "league_round") val leagueRound: String? = null,
    @Json(name = "leagueLogo") val leagueLogo: String? = null,
    @Json(name = "league_logo") val leagueLogoSnake: String? = null,
    @Json(name = "leagueCountry") val leagueCountry: String? = null,
    @Json(name = "league_country") val leagueCountrySnake: String? = null,
    @Json(name = "country_name") val countryName: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "mapped") val mapped: Boolean? = null,
    @Json(name = "event_stadium") val eventStadium: String? = null,
    @Json(name = "ai_confidence") val aiConfidence: Double? = null,
    @Json(name = "ai_pick") val aiPick: String? = null
)

@JsonClass(generateAdapter = true)
data class PredictionDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "matchId") val matchIdCamel: String? = null,
    @Json(name = "match_id") val matchId: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "homeTeamName") val homeTeamName: String? = null,
    @Json(name = "home_team_name") val homeTeamNameSnake: String? = null,
    @Json(name = "home_team") val homeTeam: String? = null,
    @Json(name = "awayTeamName") val awayTeamName: String? = null,
    @Json(name = "away_team_name") val awayTeamNameSnake: String? = null,
    @Json(name = "away_team") val awayTeam: String? = null,
    @Json(name = "homeTeamLogo") val homeTeamLogo: String? = null,
    @Json(name = "home_team_logo") val homeTeamLogoSnake: String? = null,
    @Json(name = "home_logo") val homeLogo: String? = null,
    @Json(name = "awayTeamLogo") val awayTeamLogo: String? = null,
    @Json(name = "away_team_logo") val awayTeamLogoSnake: String? = null,
    @Json(name = "away_logo") val awayLogo: String? = null,
    @Json(name = "leagueName") val leagueName: String? = null,
    @Json(name = "league_name") val leagueNameSnake: String? = null,
    @Json(name = "league") val league: String? = null,
    @Json(name = "leagueLogo") val leagueLogo: String? = null,
    @Json(name = "league_logo") val leagueLogoSnake: String? = null,
    @Json(name = "leagueCountry") val leagueCountry: String? = null,
    @Json(name = "league_country") val leagueCountrySnake: String? = null,
    @Json(name = "kickoff") val kickoff: String? = null,
    @Json(name = "match_date") val matchDate: String? = null,
    @Json(name = "matchStatus") val matchStatus: String? = null,
    @Json(name = "homeScore") val homeScore: String? = null,
    @Json(name = "awayScore") val awayScore: String? = null,
    @Json(name = "match") val match: MatchDto? = null,

    // Predictions & Probabilities
    @Json(name = "winnerPick") val winnerPick: String? = null,
    @Json(name = "pick") val pick: String? = null,
    @Json(name = "outcome") val outcome: String? = null,
    @Json(name = "winnerConfidence") val winnerConfidence: Double? = null,
    @Json(name = "confidence") val confidence: Double? = null,
    @Json(name = "drawConfidence") val drawConfidence: Double? = null,
    @Json(name = "draw_prob") val drawProb: Double? = null,
    @Json(name = "homeWinProb") val homeWinProb: Double? = null,
    @Json(name = "home_win_prob") val homeWinProbSnake: Double? = null,
    @Json(name = "awayWinProb") val awayWinProb: Double? = null,
    @Json(name = "away_win_prob") val awayWinProbSnake: Double? = null,

    // BTTS & Totals
    @Json(name = "bttsPick") val bttsPick: Boolean? = null,
    @Json(name = "bttsConfidence") val bttsConfidence: Double? = null,
    @Json(name = "btts_prob") val bttsProb: Double? = null,
    @Json(name = "btts_prediction") val bttsPrediction: String? = null,
    @Json(name = "over25Pick") val over25Pick: Boolean? = null,
    @Json(name = "over25Confidence") val over25Confidence: Double? = null,
    @Json(name = "over_2_5_prob") val over25Prob: Double? = null,
    @Json(name = "under25Prob") val under25Prob: Double? = null,
    @Json(name = "under_2_5_prob") val under25ProbSnake: Double? = null,
    @Json(name = "over15Pick") val over15Pick: Boolean? = null,
    @Json(name = "over15Confidence") val over15Confidence: Double? = null,
    @Json(name = "over35Pick") val over35Pick: Boolean? = null,
    @Json(name = "over35Confidence") val over35Confidence: Double? = null,
    @Json(name = "overUnderPick") val overUnderPick: String? = null,
    @Json(name = "over_under_pick") val overUnderPickSnake: String? = null,

    // Detailed Metrics & Insights
    @Json(name = "correctScorePick") val correctScorePick: String? = null,
    @Json(name = "correctScoreConfidence") val correctScoreConfidence: Double? = null,
    @Json(name = "safestPick") val safestPick: String? = null,
    @Json(name = "safestConfidence") val safestConfidence: Double? = null,
    @Json(name = "riskScore") val riskScore: String? = null,
    @Json(name = "risk_score") val riskScoreSnake: String? = null,
    @Json(name = "expectedGoalsHome") val expectedGoalsHome: Double? = null,
    @Json(name = "expected_goals_home") val expectedGoalsHomeSnake: Double? = null,
    @Json(name = "expectedGoalsAway") val expectedGoalsAway: Double? = null,
    @Json(name = "expected_goals_away") val expectedGoalsAwaySnake: Double? = null,
    @Json(name = "aiReasoning") val aiReasoning: String? = null,
    @Json(name = "reasoning") val reasoning: String? = null,
    @Json(name = "xgAnalysis") val xgAnalysis: String? = null,
    @Json(name = "tacticalInsights") val tacticalInsights: String? = null,
    @Json(name = "tacticalSummary") val tacticalSummary: String? = null,
    @Json(name = "tactical_summary") val tacticalSummarySnake: String? = null,
    @Json(name = "h2hSummary") val h2hSummary: String? = null,
    @Json(name = "formAnalysis") val formAnalysis: String? = null,
    @Json(name = "valueBet") val valueBet: Boolean? = null,
    @Json(name = "value_bet") val valueBetSnake: Boolean? = null,
    @Json(name = "trending") val trending: Boolean? = null,
    @Json(name = "isFeatured") val isFeatured: Boolean? = null,
    @Json(name = "isPremium") val isPremium: Boolean? = null,
    @Json(name = "matchPriorityScore") val matchPriorityScore: Double? = null
)

@JsonClass(generateAdapter = true)
data class SearchResponseDto(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "results") val results: List<SearchResultDto>? = null,
    @Json(name = "total") val total: Int? = null,
    @Json(name = "by_type") val byType: SearchByTypeDto? = null,
    @Json(name = "query") val query: String? = null
)

@JsonClass(generateAdapter = true)
data class SearchByTypeDto(
    @Json(name = "teams") val teams: List<SearchResultDto>? = null,
    @Json(name = "leagues") val leagues: List<SearchResultDto>? = null,
    @Json(name = "matches") val matches: List<SearchResultDto>? = null
)

@JsonClass(generateAdapter = true)
data class SearchResultDto(
    @Json(name = "type") val type: String? = null, // "page_link", "team", "league", "match"
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "breadcrumb") val breadcrumb: String? = null,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "logo") val logo: String? = null,
    @Json(name = "relevance") val relevance: Int? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "homeScore") val homeScore: String? = null,
    @Json(name = "awayScore") val awayScore: String? = null
)

@JsonClass(generateAdapter = true)
data class MatchEventsResponseDto(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "matchId") val matchId: String? = null,
    @Json(name = "events") val events: List<MatchEventDto>? = null
)

@JsonClass(generateAdapter = true)
data class MatchEventDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "minute") val minute: String? = null,
    @Json(name = "elapsed") val elapsed: String? = null,
    @Json(name = "type") val type: String? = null, // "goal", "card", "sub", "var"
    @Json(name = "teamId") val teamId: String? = null,
    @Json(name = "team_id") val teamIdSnake: String? = null,
    @Json(name = "teamName") val teamName: String? = null,
    @Json(name = "team_name") val teamNameSnake: String? = null,
    @Json(name = "team_side") val teamSide: String? = null,
    @Json(name = "player") val player: String? = null,
    @Json(name = "playerName") val playerName: String? = null,
    @Json(name = "player_name") val playerNameSnake: String? = null,
    @Json(name = "assistPlayer") val assistPlayer: String? = null,
    @Json(name = "assist_player") val assistPlayerSnake: String? = null,
    @Json(name = "detail") val detail: String? = null,
    @Json(name = "score") val score: String? = null
)

@JsonClass(generateAdapter = true)
data class MatchStatsResponseDto(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "matchId") val matchId: String? = null,
    @Json(name = "statistics") val statistics: MatchStatisticsDto? = null
)

@JsonClass(generateAdapter = true)
data class MatchStatisticsDto(
    @Json(name = "possession_home") val possessionHome: String? = null,
    @Json(name = "possession_away") val possessionAway: String? = null,
    @Json(name = "possessionHome") val possessionHomeCamel: String? = null,
    @Json(name = "possessionAway") val possessionAwayCamel: String? = null,

    @Json(name = "shots_home") val shotsHome: String? = null,
    @Json(name = "shots_away") val shotsAway: String? = null,
    @Json(name = "shotsHome") val shotsHomeCamel: String? = null,
    @Json(name = "shotsAway") val shotsAwayCamel: String? = null,

    @Json(name = "shots_on_target_home") val shotsOnTargetHome: String? = null,
    @Json(name = "shots_on_target_away") val shotsOnTargetAway: String? = null,
    @Json(name = "shotsOnTargetHome") val shotsOnTargetHomeCamel: String? = null,
    @Json(name = "shotsOnTargetAway") val shotsOnTargetAwayCamel: String? = null,

    @Json(name = "xg_home") val xgHome: String? = null,
    @Json(name = "xg_away") val xgAway: String? = null,
    @Json(name = "xgHome") val xgHomeCamel: String? = null,
    @Json(name = "xgAway") val xgAwayCamel: String? = null,

    @Json(name = "corners_home") val cornersHome: String? = null,
    @Json(name = "corners_away") val cornersAway: String? = null,
    @Json(name = "cornersHome") val cornersHomeCamel: String? = null,
    @Json(name = "cornersAway") val cornersAwayCamel: String? = null,

    @Json(name = "fouls_home") val foulsHome: String? = null,
    @Json(name = "fouls_away") val foulsAway: String? = null,
    @Json(name = "foulsHome") val foulsHomeCamel: String? = null,
    @Json(name = "foulsAway") val foulsAwayCamel: String? = null,

    @Json(name = "yellow_cards_home") val yellowCardsHome: String? = null,
    @Json(name = "yellow_cards_away") val yellowCardsAway: String? = null,
    @Json(name = "yellowCardsHome") val yellowCardsHomeCamel: String? = null,
    @Json(name = "yellowCardsAway") val yellowCardsAwayCamel: String? = null,

    @Json(name = "red_cards_home") val redCardsHome: String? = null,
    @Json(name = "red_cards_away") val redCardsAway: String? = null,
    @Json(name = "redCardsHome") val redCardsHomeCamel: String? = null,
    @Json(name = "redCardsAway") val redCardsAwayCamel: String? = null,

    @Json(name = "passes_home") val passesHome: String? = null,
    @Json(name = "passes_away") val passesAway: String? = null,
    @Json(name = "passesHome") val passesHomeCamel: String? = null,
    @Json(name = "passesAway") val passesAwayCamel: String? = null,

    @Json(name = "pass_accuracy_home") val passAccuracyHome: String? = null,
    @Json(name = "pass_accuracy_away") val passAccuracyAway: String? = null,
    @Json(name = "passAccuracyHome") val passAccuracyHomeCamel: String? = null,
    @Json(name = "passAccuracyAway") val passAccuracyAwayCamel: String? = null
)

@JsonClass(generateAdapter = true)
data class MatchLineupsResponseDto(
    @Json(name = "success") val success: Boolean? = null,
    @Json(name = "homeTeam") val homeTeam: TeamLineupDto? = null,
    @Json(name = "awayTeam") val awayTeam: TeamLineupDto? = null,
    @Json(name = "home_formation") val homeFormation: String? = null,
    @Json(name = "away_formation") val awayFormation: String? = null,
    @Json(name = "home_starting") val homeStarting: List<PlayerDto>? = null,
    @Json(name = "away_starting") val awayStarting: List<PlayerDto>? = null,
    @Json(name = "home_bench") val homeBench: List<PlayerDto>? = null,
    @Json(name = "away_bench") val awayBench: List<PlayerDto>? = null
)

@JsonClass(generateAdapter = true)
data class TeamLineupDto(
    @Json(name = "teamId") val teamId: String? = null,
    @Json(name = "teamName") val teamName: String? = null,
    @Json(name = "formation") val formation: String? = null,
    @Json(name = "startingXI") val startingXI: List<PlayerDto>? = null,
    @Json(name = "substitutes") val substitutes: List<PlayerDto>? = null,
    @Json(name = "bench") val bench: List<PlayerDto>? = null
)

@JsonClass(generateAdapter = true)
data class PlayerDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "player_key") val playerKey: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "playerName") val playerNameCamel: String? = null,
    @Json(name = "player_name") val playerName: String? = null,
    @Json(name = "number") val number: String? = null,
    @Json(name = "player_number") val playerNumber: String? = null,
    @Json(name = "pos") val pos: String? = null,
    @Json(name = "position") val position: String? = null,
    @Json(name = "player_type") val playerType: String? = null,
    @Json(name = "grid") val grid: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "player_image") val playerImage: String? = null,
    @Json(name = "rating") val rating: String? = null,
    @Json(name = "goals") val goals: String? = null,
    @Json(name = "assists") val assists: String? = null,
    @Json(name = "yellow_cards") val yellowCards: String? = null,
    @Json(name = "red_cards") val redCards: String? = null
)

@JsonClass(generateAdapter = true)
data class LeagueDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "league_id") val leagueId: String? = null,
    @Json(name = "league_key") val leagueKey: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "league_name") val leagueName: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "country_name") val countryName: String? = null,
    @Json(name = "logo") val logo: String? = null,
    @Json(name = "league_logo") val leagueLogo: String? = null,
    @Json(name = "flag") val flag: String? = null,
    @Json(name = "season") val season: String? = null
)

@JsonClass(generateAdapter = true)
data class StandingDto(
    @Json(name = "position") val position: String? = null,
    @Json(name = "standing_place") val standingPlace: String? = null,
    @Json(name = "teamId") val teamIdCamel: String? = null,
    @Json(name = "team_id") val teamId: String? = null,
    @Json(name = "team_key") val teamKey: String? = null,
    @Json(name = "teamName") val teamNameCamel: String? = null,
    @Json(name = "team_name") val teamName: String? = null,
    @Json(name = "standing_team") val standingTeam: String? = null,
    @Json(name = "teamLogo") val teamLogoCamel: String? = null,
    @Json(name = "team_logo") val teamLogo: String? = null,
    @Json(name = "played") val played: String? = null,
    @Json(name = "standing_P") val standingP: String? = null,
    @Json(name = "won") val won: String? = null,
    @Json(name = "standing_W") val standingW: String? = null,
    @Json(name = "drawn") val drawn: String? = null,
    @Json(name = "standing_D") val standingD: String? = null,
    @Json(name = "lost") val lost: String? = null,
    @Json(name = "standing_L") val standingL: String? = null,
    @Json(name = "goals_for") val goalsFor: String? = null,
    @Json(name = "standing_F") val standingF: String? = null,
    @Json(name = "goals_against") val goalsAgainst: String? = null,
    @Json(name = "standing_A") val standingA: String? = null,
    @Json(name = "goal_diff") val goalDiff: String? = null,
    @Json(name = "standing_GD") val standingGD: String? = null,
    @Json(name = "points") val points: String? = null,
    @Json(name = "standing_PTS") val standingPTS: String? = null,
    @Json(name = "form") val form: String? = null
)

@JsonClass(generateAdapter = true)
data class TeamDetailDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "team_key") val teamKey: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "team_name") val teamName: String? = null,
    @Json(name = "logo") val logo: String? = null,
    @Json(name = "team_badge") val teamBadge: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "country_name") val countryName: String? = null,
    @Json(name = "league_name") val leagueName: String? = null,
    @Json(name = "league_country") val leagueCountry: String? = null,
    @Json(name = "founded") val founded: String? = null,
    @Json(name = "stadium") val stadium: String? = null,
    @Json(name = "coach") val coach: String? = null,
    @Json(name = "players") val players: List<PlayerDto>? = null,
    @Json(name = "recent_matches") val recentMatches: List<MatchDto>? = null
)

@JsonClass(generateAdapter = true)
data class SentimentDto(
    @Json(name = "matchId") val matchIdCamel: String? = null,
    @Json(name = "match_id") val matchId: String? = null,
    @Json(name = "homeConfidence") val homeConfidence: Double? = null,
    @Json(name = "drawConfidence") val drawConfidence: Double? = null,
    @Json(name = "awayConfidence") val awayConfidence: Double? = null,
    @Json(name = "home_votes") val homeVotes: Int? = null,
    @Json(name = "draw_votes") val drawVotes: Int? = null,
    @Json(name = "away_votes") val awayVotes: Int? = null,
    @Json(name = "total") val total: Int? = null,
    @Json(name = "total_votes") val totalVotes: Int? = null,
    @Json(name = "home_percentage") val homePercentage: Double? = null,
    @Json(name = "draw_percentage") val drawPercentage: Double? = null,
    @Json(name = "away_percentage") val awayPercentage: Double? = null,
    @Json(name = "userVote") val userVote: String? = null,
    @Json(name = "user_vote") val userVoteSnake: String? = null
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "accuracy") val accuracy: Double? = null,
    @Json(name = "total_predictions") val totalPredictions: Int? = null,
    @Json(name = "correct_predictions") val correctPredictions: Int? = null,
    @Json(name = "current_streak") val currentStreak: Int? = null,
    @Json(name = "best_streak") val bestStreak: Int? = null,
    @Json(name = "rank") val rank: Int? = null,
    @Json(name = "points") val points: Int? = null
)

@JsonClass(generateAdapter = true)
data class CommentaryDto(
    @Json(name = "minute") val minute: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "is_important") val isImportant: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class H2HDto(
    @Json(name = "matches") val matches: List<MatchDto>? = null,
    @Json(name = "h2h") val h2h: List<MatchDto>? = null,
    @Json(name = "summary") val summary: H2HSummaryDto? = null,
    @Json(name = "teamA") val teamA: TeamNestedDto? = null,
    @Json(name = "teamB") val teamB: TeamNestedDto? = null,
    @Json(name = "home_team_last_matches") val homeTeamLastMatches: List<MatchDto>? = null,
    @Json(name = "away_team_last_matches") val awayTeamLastMatches: List<MatchDto>? = null
)

@JsonClass(generateAdapter = true)
data class H2HSummaryDto(
    @Json(name = "total") val total: Int? = null,
    @Json(name = "homeWins") val homeWins: Int? = null,
    @Json(name = "draws") val draws: Int? = null,
    @Json(name = "awayWins") val awayWins: Int? = null,
    @Json(name = "avgGoals") val avgGoals: Double? = null
)

@JsonClass(generateAdapter = true)
data class OddsDto(
    @Json(name = "home_win") val homeWin: String? = null,
    @Json(name = "draw") val draw: String? = null,
    @Json(name = "away_win") val awayWin: String? = null,
    @Json(name = "over_2_5") val over25: String? = null,
    @Json(name = "under_2_5") val under25: String? = null,
    @Json(name = "btts_yes") val bttsYes: String? = null,
    @Json(name = "btts_no") val bttsNo: String? = null
)
