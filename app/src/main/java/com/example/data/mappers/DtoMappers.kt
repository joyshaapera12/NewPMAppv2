package com.example.data.mappers

import android.util.Log
import com.example.data.remote.*
import com.example.domain.models.*

object DtoMappers {
    private const val TAG = "DtoMappers"

    private fun parseScore(scoreValue: String?, fallbackResult: String?, isHome: Boolean): Int? {
        if (!scoreValue.isNullOrBlank()) {
            val trimmed = scoreValue.trim()
            trimmed.toIntOrNull()?.let { return it }
            trimmed.toDoubleOrNull()?.toInt()?.let { return it }
        }
        if (!fallbackResult.isNullOrBlank() && fallbackResult.contains("-")) {
            val parts = fallbackResult.split("-")
            val part = if (isHome) parts.getOrNull(0) else parts.getOrNull(1)
            part?.trim()?.toIntOrNull()?.let { return it }
        }
        return null
    }

    fun mapMatch(dto: MatchDto): Match? {
        val homeTeamName = (dto.homeTeamNested?.name
            ?: dto.homeTeamNameCamel
            ?: dto.homeTeamName
            ?: dto.homeName
            ?: dto.eventHomeTeam)?.trim()

        val awayTeamName = (dto.awayTeamNested?.name
            ?: dto.awayTeamNameCamel
            ?: dto.awayTeamName
            ?: dto.awayName
            ?: dto.eventAwayTeam)?.trim()

        if (homeTeamName.isNullOrBlank() || awayTeamName.isNullOrBlank()) {
            Log.w(TAG, "Dropping MatchDto with missing team name: home='$homeTeamName', away='$awayTeamName', id='${dto.id}'")
            return null
        }

        val id = (dto.id ?: dto.matchId ?: dto.pmMatchId ?: dto.pmMatchIdSnake ?: dto.asFixtureId ?: dto.asFixtureIdSnake ?: dto.eventKey ?: dto.externalId)
            ?.takeIf { it.isNotBlank() && it != "0" }
            ?: "match_${homeTeamName}_${awayTeamName}_${dto.date}_${dto.time}".hashCode().toString().replace("-", "n")

        val statusShort = (dto.statusShortCamel ?: dto.statusShort ?: dto.eventStatus)?.trim()
        val statusText = (dto.status ?: dto.eventStatus)?.trim()
        val matchStatus = MatchStatusClassifier.toMatchStatus(statusShort, statusText)
        val isLive = MatchStatusClassifier.isLive(statusShort, statusText, dto.eventLive)

        val homeScore = parseScore(dto.homeScoreCamel ?: dto.homeScore ?: dto.goalsHome, dto.eventFinalResult, isHome = true)
        val awayScore = parseScore(dto.awayScoreCamel ?: dto.awayScore ?: dto.goalsAway, dto.eventFinalResult, isHome = false)

        val leagueName = (dto.leagueNameCamel ?: dto.leagueName ?: dto.competitionName)?.trim() ?: "Football"
        val leagueCountry = (dto.leagueCountryCamel ?: dto.leagueCountrySnake ?: dto.country ?: dto.countryName ?: dto.homeTeamNested?.leagueCountry)?.trim() ?: ""
        val leagueLogo = (dto.leagueLogoCamel ?: dto.leagueLogo ?: dto.leagueFlagCamel ?: dto.leagueFlag)?.trim() ?: ""

        val homeLogo = (dto.homeTeamNested?.logo ?: dto.homeLogoCamel ?: dto.homeLogoSnake ?: dto.homeTeamLogoCamel ?: dto.homeTeamLogo)?.trim() ?: ""
        val awayLogo = (dto.awayTeamNested?.logo ?: dto.awayLogoCamel ?: dto.awayLogoSnake ?: dto.awayTeamLogoCamel ?: dto.awayTeamLogo)?.trim() ?: ""

        val rawConfidence = dto.winnerConfidence ?: dto.aiConfidence ?: dto.aiConfidenceCamel ?: dto.prediction?.confidence ?: dto.prediction?.winnerConfidence ?: 0.0
        val normalizedConfidence = (rawConfidence.coerceIn(0.0, 100.0) / 100.0).toFloat()

        return Match(
            id = id,
            pmId = dto.pmMatchId ?: dto.pmMatchIdSnake ?: dto.id ?: dto.matchId,
            allSportsId = dto.asFixtureId ?: dto.asFixtureIdSnake ?: dto.allsportsId ?: dto.eventKey ?: dto.externalId,
            leagueId = dto.leagueIdCamel ?: dto.leagueId ?: dto.leagueKey ?: "0",
            leagueName = leagueName,
            leagueLogoUrl = leagueLogo,
            leagueCountry = leagueCountry,
            homeTeam = Team(
                id = dto.homeTeamNested?.id ?: dto.homeTeamIdCamel ?: dto.homeTeamId ?: dto.homeTeamKey ?: "home_$id",
                name = homeTeamName,
                logoUrl = homeLogo,
                country = leagueCountry
            ),
            awayTeam = Team(
                id = dto.awayTeamNested?.id ?: dto.awayTeamIdCamel ?: dto.awayTeamId ?: dto.awayTeamKey ?: "away_$id",
                name = awayTeamName,
                logoUrl = awayLogo,
                country = leagueCountry
            ),
            homeScore = homeScore,
            awayScore = awayScore,
            status = matchStatus,
            statusText = statusShort ?: statusText ?: (if (isLive) "LIVE" else if (matchStatus == MatchStatus.FINISHED) "FT" else "SCH"),
            minute = (dto.elapsed ?: dto.minute)?.let { if (it.all { c -> c.isDigit() }) "$it'" else it },
            startTime = (dto.kickoff ?: dto.startTimeCamel ?: dto.startTime ?: dto.time ?: dto.eventTime ?: "").trim(),
            date = (dto.date ?: dto.eventDate ?: "").trim(),
            stadium = dto.venue ?: dto.stadium ?: dto.eventStadium,
            aiConfidence = if (rawConfidence > 0.0) normalizedConfidence else null,
            aiPick = dto.winnerPick ?: dto.aiPick ?: dto.aiPickCamel ?: dto.prediction?.winnerPick ?: dto.prediction?.pick
        )
    }

    fun mapAllSportsLive(dto: AllSportsLiveDto): LiveMatch? {
        val homeTeamName = (dto.homeTeamName ?: dto.homeTeamNameSnake ?: dto.homeName ?: dto.eventHomeTeam)?.trim()
        val awayTeamName = (dto.awayTeamName ?: dto.awayTeamNameSnake ?: dto.awayName ?: dto.eventAwayTeam)?.trim()

        if (homeTeamName.isNullOrBlank() || awayTeamName.isNullOrBlank()) {
            Log.w(TAG, "Dropping AllSportsLiveDto with missing team name: home='$homeTeamName', away='$awayTeamName', key='${dto.eventKey}'")
            return null
        }

        val asFixtureId = (dto.asFixtureId ?: dto.asFixtureIdSnake ?: dto.eventKey)?.takeIf { it.isNotBlank() && it != "0" }
        val pmMatchId = (dto.pmMatchId ?: dto.pmMatchIdSnake ?: dto.id ?: dto.matchId)?.takeIf { it.isNotBlank() && it != "0" }
        val eventKey = asFixtureId ?: pmMatchId ?: "live_${homeTeamName}_${awayTeamName}".hashCode().toString().replace("-", "n")

        val statusShort = (dto.statusShort ?: dto.statusShortSnake ?: dto.eventStatus)?.trim()
        val statusText = (dto.status ?: dto.eventStatus)?.trim()
        val isLive = MatchStatusClassifier.isLive(statusShort, statusText, dto.eventLive)

        val homeScore = parseScore(dto.homeScore ?: dto.homeScoreSnake ?: dto.goalsHome, dto.eventFinalResult, isHome = true)
        val awayScore = parseScore(dto.awayScore ?: dto.awayScoreSnake ?: dto.goalsAway, dto.eventFinalResult, isHome = false)

        val leagueName = (dto.leagueName ?: dto.leagueNameSnake ?: dto.countryName)?.trim() ?: "Football"
        val countryName = (dto.countryName ?: dto.leagueCountry ?: dto.leagueCountrySnake ?: dto.country)?.trim() ?: ""
        val leagueLogo = (dto.leagueLogo ?: dto.leagueLogoSnake)?.trim() ?: ""

        val homeLogo = (dto.homeLogo ?: dto.homeLogoSnake ?: dto.homeTeamLogo ?: dto.homeTeamLogoSnake)?.trim() ?: ""
        val awayLogo = (dto.awayLogo ?: dto.awayLogoSnake ?: dto.awayTeamLogo ?: dto.awayTeamLogoSnake)?.trim() ?: ""

        val elapsed = dto.elapsed ?: dto.minute
        val minuteDisplay = when {
            !elapsed.isNullOrBlank() && elapsed.all { it.isDigit() } -> "$elapsed'"
            !statusShort.isNullOrBlank() -> statusShort
            isLive -> "LIVE"
            else -> "FT"
        }

        val scoreDisplay = if (homeScore != null && awayScore != null) "$homeScore - $awayScore" else (dto.eventFinalResult ?: "0 - 0")

        return LiveMatch(
            eventKey = eventKey,
            pmMatchId = pmMatchId,
            asFixtureId = asFixtureId,
            leagueId = dto.leagueId ?: dto.leagueIdSnake ?: dto.leagueKey ?: "",
            leagueName = leagueName,
            leagueLogo = leagueLogo,
            countryName = countryName,
            homeTeamName = homeTeamName,
            homeTeamKey = dto.homeTeamKey ?: dto.homeTeamKeyCamel ?: dto.homeTeamId ?: dto.homeTeamIdSnake ?: "",
            homeTeamLogo = homeLogo,
            awayTeamName = awayTeamName,
            awayTeamKey = dto.awayTeamKey ?: dto.awayTeamKeyCamel ?: dto.awayTeamId ?: dto.awayTeamIdSnake ?: "",
            awayTeamLogo = awayLogo,
            score = scoreDisplay,
            homeScore = homeScore,
            awayScore = awayScore,
            status = statusShort ?: statusText ?: (if (isLive) "LIVE" else "FT"),
            statusShort = statusShort ?: (if (isLive) "LIVE" else "FT"),
            elapsed = elapsed,
            minute = minuteDisplay,
            kickoff = dto.kickoff ?: dto.eventTime ?: "",
            isLive = isLive,
            aiConfidence = dto.aiConfidence?.let { (it.coerceIn(0.0, 100.0) / 100.0).toFloat() },
            aiPick = dto.aiPick
        )
    }

    fun mapPrediction(dto: PredictionDto): Prediction? {
        val homeTeam = (dto.homeTeamName ?: dto.homeTeamNameSnake ?: dto.homeTeam ?: dto.match?.homeTeamNested?.name ?: dto.match?.homeTeamNameCamel ?: dto.match?.homeTeamName)?.trim()
        val awayTeam = (dto.awayTeamName ?: dto.awayTeamNameSnake ?: dto.awayTeam ?: dto.match?.awayTeamNested?.name ?: dto.match?.awayTeamNameCamel ?: dto.match?.awayTeamName)?.trim()
        if (homeTeam.isNullOrBlank() || awayTeam.isNullOrBlank()) {
            return null
        }

        val homeLogo = (dto.homeTeamLogo ?: dto.homeTeamLogoSnake ?: dto.homeLogo ?: dto.match?.homeTeamNested?.logo ?: dto.match?.homeTeamLogoCamel ?: dto.match?.homeTeamLogo ?: "").trim()
        val awayLogo = (dto.awayTeamLogo ?: dto.awayTeamLogoSnake ?: dto.awayLogo ?: dto.match?.awayTeamNested?.logo ?: dto.match?.awayTeamLogoCamel ?: dto.match?.awayTeamLogo ?: "").trim()
        val league = (dto.leagueName ?: dto.leagueNameSnake ?: dto.league ?: dto.match?.leagueNameCamel ?: dto.match?.leagueName ?: "Football").trim()

        val rawConf = dto.winnerConfidence ?: dto.confidence ?: 75.0
        val normalizedConf = (rawConf.coerceIn(0.0, 100.0) / 100.0).toFloat()
        val confPercent = (normalizedConf * 100).toInt()

        val id = (dto.id ?: dto.matchIdCamel ?: dto.matchId)?.takeIf { it.isNotBlank() && it != "0" }
            ?: "pred_${homeTeam}_${awayTeam}".hashCode().toString().replace("-", "n")

        val matchId = dto.matchIdCamel ?: dto.matchId ?: dto.id ?: id

        val pick = dto.winnerPick ?: dto.pick ?: dto.outcome ?: "$homeTeam Win"

        val homeProb = dto.homeWinProb ?: dto.homeWinProbSnake ?: (if (pick.contains(homeTeam, ignoreCase = true)) rawConf else 45.0)
        val drawProb = dto.drawConfidence ?: dto.drawProb ?: (if (pick.equals("Draw", ignoreCase = true)) rawConf else 25.0)
        val awayProb = dto.awayWinProb ?: dto.awayWinProbSnake ?: (if (pick.contains(awayTeam, ignoreCase = true)) rawConf else 30.0)

        val bttsProb = dto.bttsConfidence ?: dto.bttsProb ?: 55.0
        val over25Prob = dto.over25Confidence ?: dto.over25Prob ?: 58.0
        val under25Prob = dto.under25Prob ?: dto.under25ProbSnake ?: (100.0 - over25Prob)

        val reasoning = dto.aiReasoning ?: dto.reasoning
            ?: "PitchMetrics AI engine projects favorable expected goal momentum and key positional advantage for $pick."

        return Prediction(
            id = id,
            matchId = matchId,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeLogo = homeLogo,
            awayLogo = awayLogo,
            league = league,
            pick = pick,
            confidence = normalizedConf,
            confidencePercent = confPercent,
            homeWinProb = (homeProb.coerceIn(0.0, 100.0) / 100.0).toFloat(),
            drawProb = (drawProb.coerceIn(0.0, 100.0) / 100.0).toFloat(),
            awayWinProb = (awayProb.coerceIn(0.0, 100.0) / 100.0).toFloat(),
            bttsProb = (bttsProb.coerceIn(0.0, 100.0) / 100.0).toFloat(),
            bttsPrediction = if (dto.bttsPick == true || bttsProb >= 55.0) "Yes" else "No",
            over25Prob = (over25Prob.coerceIn(0.0, 100.0) / 100.0).toFloat(),
            under25Prob = (under25Prob.coerceIn(0.0, 100.0) / 100.0).toFloat(),
            overUnderPick = dto.overUnderPick ?: (if (over25Prob >= 50.0) "Over 2.5" else "Under 2.5"),
            expectedGoalsHome = (dto.expectedGoalsHome ?: dto.expectedGoalsHomeSnake ?: 1.6).toFloat(),
            expectedGoalsAway = (dto.expectedGoalsAway ?: dto.expectedGoalsAwaySnake ?: 1.1).toFloat(),
            reasoning = reasoning,
            tacticalSummary = dto.tacticalInsights ?: dto.tacticalSummary ?: dto.tacticalSummarySnake,
            isValueBet = dto.valueBet == true || dto.valueBetSnake == true,
            isTrending = dto.trending == true || dto.isFeatured == true,
            matchDate = dto.kickoff ?: dto.matchDate ?: "",
            riskScore = dto.riskScore ?: dto.riskScoreSnake ?: (if (confPercent >= 75) "Low" else if (confPercent >= 60) "Medium" else "High"),
            safestPick = dto.safestPick,
            xgAnalysis = dto.xgAnalysis,
            formAnalysis = dto.formAnalysis
        )
    }

    fun mapSearchResult(dto: SearchResultDto): SearchResult? {
        val title = dto.title?.trim() ?: return null
        val type = when (dto.type?.lowercase()?.trim()) {
            "team" -> SearchResultType.TEAM
            "league", "competition" -> SearchResultType.LEAGUE
            "match" -> SearchResultType.MATCH
            else -> SearchResultType.PAGE_LINK
        }

        val id = dto.id?.takeIf { it.isNotBlank() } ?: "search_${title}".hashCode().toString().replace("-", "n")

        val homeScore = dto.homeScore?.toIntOrNull()
        val awayScore = dto.awayScore?.toIntOrNull()

        return SearchResult(
            id = id,
            type = type,
            title = title,
            subtitle = dto.subtitle,
            description = dto.description,
            logoUrl = dto.logo,
            url = dto.url,
            icon = dto.icon,
            relevance = dto.relevance,
            status = dto.status,
            homeScore = homeScore,
            awayScore = awayScore
        )
    }

    fun mapMatchEvent(dto: MatchEventDto, homeTeamId: String? = null): MatchEvent {
        val minute = (dto.elapsed ?: dto.minute ?: "0").let { if (it.all { c -> c.isDigit() }) "$it'" else it }
        val playerName = dto.playerName ?: dto.player ?: dto.playerNameSnake ?: "Player"
        val teamId = dto.teamId ?: dto.teamIdSnake
        val isHome = if (dto.teamSide != null) {
            dto.teamSide.equals("home", ignoreCase = true)
        } else if (homeTeamId != null && teamId != null) {
            teamId == homeTeamId
        } else {
            true
        }

        return MatchEvent(
            id = dto.id ?: "evt_${minute}_${playerName}".hashCode().toString().replace("-", "n"),
            minute = minute,
            type = dto.type ?: "goal",
            isHomeTeam = isHome,
            playerName = playerName,
            assistPlayerName = dto.assistPlayer ?: dto.assistPlayerSnake,
            detail = dto.detail,
            score = dto.score
        )
    }

    fun mapMatchStatistics(dto: MatchStatisticsDto): MatchStatistics {
        return MatchStatistics(
            possessionHome = (dto.possessionHome ?: dto.possessionHomeCamel)?.replace("%", "")?.trim()?.toIntOrNull(),
            possessionAway = (dto.possessionAway ?: dto.possessionAwayCamel)?.replace("%", "")?.trim()?.toIntOrNull(),
            shotsHome = (dto.shotsHome ?: dto.shotsHomeCamel)?.toIntOrNull(),
            shotsAway = (dto.shotsAway ?: dto.shotsAwayCamel)?.toIntOrNull(),
            shotsOnTargetHome = (dto.shotsOnTargetHome ?: dto.shotsOnTargetHomeCamel)?.toIntOrNull(),
            shotsOnTargetAway = (dto.shotsOnTargetAway ?: dto.shotsOnTargetAwayCamel)?.toIntOrNull(),
            xgHome = (dto.xgHome ?: dto.xgHomeCamel)?.toFloatOrNull(),
            xgAway = (dto.xgAway ?: dto.xgAwayCamel)?.toFloatOrNull(),
            cornersHome = (dto.cornersHome ?: dto.cornersHomeCamel)?.toIntOrNull(),
            cornersAway = (dto.cornersAway ?: dto.cornersAwayCamel)?.toIntOrNull(),
            foulsHome = (dto.foulsHome ?: dto.foulsHomeCamel)?.toIntOrNull(),
            foulsAway = (dto.foulsAway ?: dto.foulsAwayCamel)?.toIntOrNull(),
            yellowCardsHome = (dto.yellowCardsHome ?: dto.yellowCardsHomeCamel)?.toIntOrNull(),
            yellowCardsAway = (dto.yellowCardsAway ?: dto.yellowCardsAwayCamel)?.toIntOrNull(),
            redCardsHome = (dto.redCardsHome ?: dto.redCardsHomeCamel)?.toIntOrNull(),
            redCardsAway = (dto.redCardsAway ?: dto.redCardsAwayCamel)?.toIntOrNull(),
            passesHome = (dto.passesHome ?: dto.passesHomeCamel)?.toIntOrNull(),
            passesAway = (dto.passesAway ?: dto.passesAwayCamel)?.toIntOrNull(),
            passAccuracyHome = (dto.passAccuracyHome ?: dto.passAccuracyHomeCamel)?.replace("%", "")?.trim()?.toIntOrNull(),
            passAccuracyAway = (dto.passAccuracyAway ?: dto.passAccuracyAwayCamel)?.replace("%", "")?.trim()?.toIntOrNull()
        )
    }

    fun mapLineup(dto: MatchLineupsResponseDto): Lineup {
        val homeStarting = (dto.homeTeam?.startingXI ?: dto.homeStarting ?: emptyList()).map { mapPlayer(it, isHome = true) }
        val awayStarting = (dto.awayTeam?.startingXI ?: dto.awayStarting ?: emptyList()).map { mapPlayer(it, isHome = false) }
        val homeBench = (dto.homeTeam?.substitutes ?: dto.homeTeam?.bench ?: dto.homeBench ?: emptyList()).map { mapPlayer(it, isHome = true) }
        val awayBench = (dto.awayTeam?.substitutes ?: dto.awayTeam?.bench ?: dto.awayBench ?: emptyList()).map { mapPlayer(it, isHome = false) }

        return Lineup(
            homeFormation = dto.homeTeam?.formation ?: dto.homeFormation ?: "4-3-3",
            awayFormation = dto.awayTeam?.formation ?: dto.awayFormation ?: "4-2-3-1",
            homeStarting = homeStarting,
            awayStarting = awayStarting,
            homeBench = homeBench,
            awayBench = awayBench
        )
    }

    fun mapStanding(dto: StandingDto): Standing? {
        val teamName = (dto.teamName ?: dto.teamNameCamel ?: dto.standingTeam)?.trim() ?: return null
        return Standing(
            position = dto.position?.toIntOrNull() ?: dto.standingPlace?.toIntOrNull() ?: 1,
            teamId = (dto.teamId ?: dto.teamIdCamel ?: dto.teamKey)?.takeIf { it.isNotBlank() && it != "0" }
                ?: "standing_${teamName}_${dto.position}".hashCode().toString().replace("-", "n"),
            teamName = teamName,
            teamLogo = dto.teamLogo ?: dto.teamLogoCamel ?: "",
            played = dto.played?.toIntOrNull() ?: dto.standingP?.toIntOrNull() ?: 0,
            won = dto.won?.toIntOrNull() ?: dto.standingW?.toIntOrNull() ?: 0,
            drawn = dto.drawn?.toIntOrNull() ?: dto.standingD?.toIntOrNull() ?: 0,
            lost = dto.lost?.toIntOrNull() ?: dto.standingL?.toIntOrNull() ?: 0,
            goalsFor = dto.goalsFor?.toIntOrNull() ?: dto.standingF?.toIntOrNull() ?: 0,
            goalsAgainst = dto.goalsAgainst?.toIntOrNull() ?: dto.standingA?.toIntOrNull() ?: 0,
            goalDiff = dto.goalDiff?.toIntOrNull() ?: dto.standingGD?.toIntOrNull() ?: 0,
            points = dto.points?.toIntOrNull() ?: dto.standingPTS?.toIntOrNull() ?: 0,
            form = dto.form ?: "W-D-W-L-W"
        )
    }

    fun mapLeague(dto: LeagueDto): League? {
        val name = (dto.name ?: dto.leagueName)?.trim() ?: return null
        return League(
            id = (dto.id ?: dto.leagueId ?: dto.leagueKey ?: dto.slug)?.takeIf { it.isNotBlank() && it != "0" }
                ?: "league_${name}".hashCode().toString().replace("-", "n"),
            name = name,
            country = dto.country ?: dto.countryName ?: "International",
            logoUrl = dto.logo ?: dto.leagueLogo ?: "",
            flagUrl = dto.flag ?: "",
            season = dto.season ?: "2024/2025"
        )
    }

    fun mapPlayer(dto: PlayerDto, isHome: Boolean = true): TacticalPlayer {
        val rawPos = dto.pos ?: dto.position ?: dto.playerType ?: ""
        val position = when {
            rawPos.contains("G", ignoreCase = true) -> "GK"
            rawPos.contains("D", ignoreCase = true) -> "DF"
            rawPos.contains("M", ignoreCase = true) -> "MF"
            else -> "FW"
        }

        // Grid parsing e.g. "1:1" or "2:3"
        var gridX = 0.5f
        var gridY = 0.5f
        if (!dto.grid.isNullOrBlank() && dto.grid.contains(":")) {
            val parts = dto.grid.split(":")
            val row = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val col = parts.getOrNull(1)?.toIntOrNull() ?: 1
            gridX = (col.toFloat() / 5f).coerceIn(0.1f, 0.9f)
            gridY = (row.toFloat() / 5f).coerceIn(0.1f, 0.9f)
        }

        return TacticalPlayer(
            id = (dto.id ?: dto.playerKey)?.takeIf { it.isNotBlank() && it != "0" }
                ?: "player_${dto.name ?: dto.playerNameCamel}_${dto.number}".hashCode().toString().replace("-", "n"),
            name = (dto.playerNameCamel ?: dto.playerName ?: dto.name ?: "Player").trim(),
            number = dto.playerNumber ?: dto.number ?: "10",
            position = position,
            gridX = gridX,
            gridY = gridY,
            rating = dto.rating,
            photoUrl = dto.playerImage ?: dto.image
        )
    }

    fun mapH2H(dto: H2HDto): HeadToHead {
        val rawMatches = dto.matches ?: dto.h2h ?: emptyList()
        val mappedMatches = rawMatches.mapNotNull { mapMatch(it) }

        val summary = dto.summary
        val homeWins = summary?.homeWins ?: mappedMatches.count { (it.homeScore ?: 0) > (it.awayScore ?: 0) }
        val draws = summary?.draws ?: mappedMatches.count { it.homeScore != null && it.homeScore == it.awayScore }
        val awayWins = summary?.awayWins ?: mappedMatches.count { (it.awayScore ?: 0) > (it.homeScore ?: 0) }
        val avgGoals = summary?.avgGoals ?: if (mappedMatches.isNotEmpty()) {
            mappedMatches.map { (it.homeScore ?: 0) + (it.awayScore ?: 0) }.average()
        } else {
            2.5
        }

        return HeadToHead(
            matches = mappedMatches,
            homeWins = homeWins,
            draws = draws,
            awayWins = awayWins,
            avgGoals = avgGoals
        )
    }
}
