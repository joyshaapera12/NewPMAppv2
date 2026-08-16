package com.example.core.network

import com.example.data.remote.*
import retrofit2.Response
import retrofit2.http.*

interface PitchMetricsApiService {

    // Auth (Session Cookie based)
    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<ApiEnvelope<UserDto>>

    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): Response<ApiEnvelope<UserDto>>

    @GET("auth/me")
    suspend fun getMe(): Response<ApiEnvelope<UserDto>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiEnvelope<Unit>>

    // Matches
    @GET("matches")
    suspend fun getMatches(@Query("date") date: String? = null): Response<ApiEnvelope<List<MatchDto>>>

    @GET("matches/{id}")
    suspend fun getMatchDetail(@Path("id") id: String): Response<MatchDto>

    @GET("matches/{id}/events")
    suspend fun getMatchEvents(@Path("id") id: String): Response<MatchEventsResponseDto>

    @GET("matches/{id}/statistics")
    suspend fun getMatchStatistics(@Path("id") id: String): Response<MatchStatsResponseDto>

    @GET("matches/{id}/lineups")
    suspend fun getMatchLineups(@Path("id") id: String): Response<MatchLineupsResponseDto>

    @GET("matches/{id}/odds")
    suspend fun getMatchOdds(@Path("id") id: String): Response<ApiEnvelope<OddsDto>>

    @GET("matches/{id}/h2h")
    suspend fun getMatchH2H(@Path("id") id: String): Response<H2HDto>

    @GET("matches/{id}/commentary")
    suspend fun getMatchCommentary(@Path("id") id: String): Response<ApiEnvelope<List<CommentaryDto>>>

    // Live Feed (Fast polling ~25s)
    @GET("allsports-live-all")
    suspend fun getAllSportsLive(): Response<ApiEnvelope<List<AllSportsLiveDto>>>

    // Predictions
    @GET("predictions/high-confidence")
    suspend fun getHighConfidencePredictions(): Response<ApiEnvelope<List<PredictionDto>>>

    @GET("predictions")
    suspend fun getPredictions(): Response<ApiEnvelope<List<PredictionDto>>>

    @GET("predictions/trending")
    suspend fun getTrendingPredictions(): Response<ApiEnvelope<List<PredictionDto>>>

    @GET("predictions/match/{matchId}")
    suspend fun getPredictionForMatch(@Path("matchId") matchId: String): Response<PredictionDto>

    // Leagues
    @GET("leagues")
    suspend fun getLeagues(): Response<ApiEnvelope<List<LeagueDto>>>

    @GET("leagues/{id}/standings")
    suspend fun getLeagueStandings(@Path("id") id: String): Response<ApiEnvelope<List<StandingDto>>>

    @GET("leagues/{id}/matches")
    suspend fun getLeagueMatches(@Path("id") id: String): Response<ApiEnvelope<List<MatchDto>>>

    // Teams
    @GET("teams/{id}")
    suspend fun getTeamDetail(@Path("id") id: String): Response<TeamDetailDto>

    @GET("teams/{id}/matches")
    suspend fun getTeamMatches(@Path("id") id: String): Response<ApiEnvelope<List<MatchDto>>>

    // Multi-entity Search
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int? = 25
    ): Response<SearchResponseDto>

    // Community
    @GET("community/sentiment/{matchId}")
    suspend fun getCommunitySentiment(@Path("matchId") matchId: String): Response<SentimentDto>

    @POST("community/vote")
    suspend fun submitCommunityVote(@Body body: Map<String, String>): Response<SentimentDto>

    @GET("community/leaderboard")
    suspend fun getCommunityLeaderboard(): Response<ApiEnvelope<List<UserDto>>>
}
