package com.example.core.network

import android.util.Log

object SafeApiLogger {
    private const val TAG = "PitchMetricsDiagnostic"

    fun logRequest(baseUrl: String, endpoint: String) {
        Log.i(TAG, "API_BASE_URL=$baseUrl endpoint=$endpoint")
    }

    fun logResponse(
        endpoint: String,
        httpStatus: Int,
        contentType: String?,
        topLevelKeys: List<String>,
        matchesCount: Int,
        firstMatchId: String?,
        firstMatchStatus: String?,
        firstHomeNameNonEmpty: Boolean,
        firstAwayNameNonEmpty: Boolean
    ) {
        Log.i(
            TAG,
            "DIAGNOSTIC: endpoint=$endpoint HTTP=$httpStatus contentType=$contentType keys=$topLevelKeys count=$matchesCount firstId=$firstMatchId firstStatus=$firstMatchStatus homeNonEmpty=$firstHomeNameNonEmpty awayNonEmpty=$firstAwayNameNonEmpty"
        )
    }

    fun logError(endpoint: String, httpStatus: Int?, errorMessage: String?, exceptionType: String?) {
        Log.e(
            TAG,
            "DIAGNOSTIC_ERROR: endpoint=$endpoint HTTP=$httpStatus error=$errorMessage exception=$exceptionType"
        )
    }

    fun logMatchClick(
        source: String,
        pmMatchId: String?,
        asFixtureId: String?,
        homeTeamName: String,
        awayTeamName: String,
        competitionName: String,
        navigationRoute: String
    ) {
        Log.i(
            "MatchNav",
            "MATCH_CLICK: source=$source pmId=$pmMatchId asId=$asFixtureId home='$homeTeamName' away='$awayTeamName' comp='$competitionName' route='$navigationRoute'"
        )
    }

    fun logMatchDetailRequest(
        source: String,
        requestedEndpoint: String,
        primaryId: String,
        pmMatchId: String?,
        asFixtureId: String?
    ) {
        Log.i(
            "MatchCentre",
            "DETAIL_REQUEST: source=$source endpoint='$requestedEndpoint' primaryId=$primaryId pmId=$pmMatchId asId=$asFixtureId"
        )
    }

    fun logMatchDetailResponse(
        source: String,
        returnedMatchId: String,
        returnedHomeTeam: String,
        returnedAwayTeam: String,
        returnedStatus: String,
        returnedScore: String?
    ) {
        Log.i(
            "MatchCentre",
            "DETAIL_RESPONSE: source=$source matchId=$returnedMatchId home='$returnedHomeTeam' away='$returnedAwayTeam' status=$returnedStatus score='$returnedScore'"
        )
    }
}
