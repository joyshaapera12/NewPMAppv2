package com.example.data.repositories

import android.content.Context
import com.example.core.network.NetworkClient
import com.example.domain.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class CommunityRepository(context: Context) {
    private val api = NetworkClient.getApiService(context)
    private val sharedPrefs = context.getSharedPreferences("pitchmetrics_community", Context.MODE_PRIVATE)

    // In-memory / cached comments store for live match discussion
    private val localComments = mutableMapOf<String, MutableList<CommunityComment>>()
    private val localReactions = mutableMapOf<String, MutableMap<String, Int>>()

    suspend fun getSentiment(matchId: String): Result<CommunitySentiment> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCommunitySentiment(matchId)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    val homeConf = dto.homeConfidence ?: dto.homePercentage ?: 33.3
                    val drawConf = dto.drawConfidence ?: dto.drawPercentage ?: 33.3
                    val awayConf = dto.awayConfidence ?: dto.awayPercentage ?: 33.4

                    val homeVotes = dto.homeVotes ?: ((homeConf / 100.0) * (dto.total ?: dto.totalVotes ?: 100)).toInt()
                    val drawVotes = dto.drawVotes ?: ((drawConf / 100.0) * (dto.total ?: dto.totalVotes ?: 100)).toInt()
                    val awayVotes = dto.awayVotes ?: ((awayConf / 100.0) * (dto.total ?: dto.totalVotes ?: 100)).toInt()
                    val total = dto.total ?: dto.totalVotes ?: (homeVotes + drawVotes + awayVotes).coerceAtLeast(1)

                    val sentiment = CommunitySentiment(
                        matchId = matchId,
                        homeVotes = homeVotes,
                        drawVotes = drawVotes,
                        awayVotes = awayVotes,
                        totalVotes = total,
                        homePercentage = homeConf.toFloat(),
                        drawPercentage = drawConf.toFloat(),
                        awayPercentage = awayConf.toFloat(),
                        userVote = dto.userVote ?: dto.userVoteSnake
                    )
                    Result.success(sentiment)
                } else {
                    Result.success(CommunitySentiment(matchId, 0, 0, 0, 0, 33.3f, 33.3f, 33.4f, null))
                }
            } else {
                Result.success(CommunitySentiment(matchId, 0, 0, 0, 0, 33.3f, 33.3f, 33.4f, null))
            }
        } catch (e: Exception) {
            Result.success(CommunitySentiment(matchId, 0, 0, 0, 0, 33.3f, 33.3f, 33.4f, null))
        }
    }

    suspend fun submitVote(matchId: String, vote: String): Result<CommunitySentiment> = withContext(Dispatchers.IO) {
        try {
            val response = api.submitCommunityVote(mapOf("match_id" to matchId, "vote" to vote))
            if (response.isSuccessful) {
                getSentiment(matchId)
            } else {
                Result.failure(Exception("Vote failed: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatchComments(matchId: String): Result<List<CommunityComment>> = withContext(Dispatchers.IO) {
        val existing = localComments.getOrPut(matchId) {
            mutableListOf(
                CommunityComment(
                    id = "c1",
                    matchId = matchId,
                    authorName = "Alex_Tactics",
                    authorAvatar = null,
                    teamFlair = "Home",
                    text = "High pressing from the home side in the first 20 mins. xG looking favorable for early goal.",
                    timestamp = "12m ago",
                    likesCount = 14,
                    userLiked = false
                ),
                CommunityComment(
                    id = "c2",
                    matchId = matchId,
                    authorName = "MarcoFootball",
                    authorAvatar = null,
                    teamFlair = "Away",
                    text = "Away counter-attacks look dangerous on the wings. Over 2.5 still looks very solid here.",
                    timestamp = "5m ago",
                    likesCount = 9,
                    userLiked = true
                ),
                CommunityComment(
                    id = "c3",
                    matchId = matchId,
                    authorName = "PunterPro99",
                    authorAvatar = null,
                    teamFlair = "Neutral",
                    text = "Referee is letting physical play go, lower chance of early red card.",
                    timestamp = "Just now",
                    likesCount = 4,
                    userLiked = false
                )
            )
        }
        Result.success(existing.toList())
    }

    suspend fun postComment(
        matchId: String,
        text: String,
        authorName: String = "Punter",
        teamFlair: String? = null
    ): Result<CommunityComment> = withContext(Dispatchers.IO) {
        val newComment = CommunityComment(
            id = UUID.randomUUID().toString(),
            matchId = matchId,
            authorName = authorName,
            authorAvatar = null,
            teamFlair = teamFlair ?: "Fan",
            text = text.trim(),
            timestamp = "Just now",
            likesCount = 0,
            userLiked = false
        )
        val list = localComments.getOrPut(matchId) { mutableListOf() }
        list.add(0, newComment)
        Result.success(newComment)
    }

    suspend fun toggleCommentLike(matchId: String, commentId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val list = localComments[matchId] ?: return@withContext Result.success(false)
        val idx = list.indexOfFirst { it.id == commentId }
        if (idx != -1) {
            val item = list[idx]
            val newLiked = !item.userLiked
            val newLikes = if (newLiked) item.likesCount + 1 else (item.likesCount - 1).coerceAtLeast(0)
            list[idx] = item.copy(likesCount = newLikes, userLiked = newLiked)
            Result.success(newLiked)
        } else {
            Result.success(false)
        }
    }

    suspend fun getLiveEmojiReactions(matchId: String): Result<List<LiveEmojiReaction>> = withContext(Dispatchers.IO) {
        val reactionsMap = localReactions.getOrPut(matchId) {
            mutableMapOf(
                "🔥" to 84,
                "⚽" to 142,
                "👏" to 57,
                "🟥" to 12,
                "😱" to 31,
                "🤯" to 49
            )
        }
        val userReactionKey = "user_reaction_$matchId"
        val selectedEmoji = sharedPrefs.getString(userReactionKey, null)

        val result = reactionsMap.map { (emoji, count) ->
            LiveEmojiReaction(
                emoji = emoji,
                count = count,
                userSelected = emoji == selectedEmoji
            )
        }
        Result.success(result)
    }

    suspend fun reactWithEmoji(matchId: String, emoji: String): Result<List<LiveEmojiReaction>> = withContext(Dispatchers.IO) {
        val reactionsMap = localReactions.getOrPut(matchId) {
            mutableMapOf("🔥" to 84, "⚽" to 142, "👏" to 57, "🟥" to 12, "😱" to 31, "🤯" to 49)
        }
        val userReactionKey = "user_reaction_$matchId"
        val currentSelected = sharedPrefs.getString(userReactionKey, null)

        if (currentSelected == emoji) {
            // Deselect
            reactionsMap[emoji] = (reactionsMap[emoji] ?: 1) - 1
            sharedPrefs.edit().remove(userReactionKey).apply()
        } else {
            // Select new
            if (currentSelected != null && reactionsMap.containsKey(currentSelected)) {
                reactionsMap[currentSelected] = (reactionsMap[currentSelected] ?: 1) - 1
            }
            reactionsMap[emoji] = (reactionsMap[emoji] ?: 0) + 1
            sharedPrefs.edit().putString(userReactionKey, emoji).apply()
        }
        getLiveEmojiReactions(matchId)
    }

    suspend fun getLeaderboard(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCommunityLeaderboard()
            if (response.isSuccessful) {
                val envelope = response.body()
                val list = envelope?.leaderboard ?: envelope?.data ?: emptyList()
                val users = list.mapIndexed { idx, dto ->
                    User(
                        id = dto.id ?: "$idx",
                        username = dto.username ?: "Punter_${idx + 1}",
                        email = dto.email ?: "",
                        avatarUrl = dto.avatar,
                        accuracy = (dto.accuracy ?: 72.0).toFloat(),
                        totalPredictions = dto.totalPredictions ?: 150,
                        correctPredictions = dto.correctPredictions ?: 108,
                        currentStreak = dto.currentStreak ?: 5,
                        bestStreak = dto.bestStreak ?: 12,
                        rank = dto.rank ?: (idx + 1),
                        points = dto.points ?: (1000 - idx * 25)
                    )
                }
                Result.success(users)
            } else {
                Result.success(getFallbackLeaderboard())
            }
        } catch (e: Exception) {
            Result.success(getFallbackLeaderboard())
        }
    }

    suspend fun getPredictorLeaderboard(): Result<List<PredictorLeaderboardUser>> = withContext(Dispatchers.IO) {
        val users = listOf(
            PredictorLeaderboardUser(1, "OracleTactics", null, 312, 248, 79.5f, 4820, 11, "Grandmaster AI Analyst"),
            PredictorLeaderboardUser(2, "StrikerMetrics", null, 280, 218, 77.8f, 4210, 8, "Master Forecaster"),
            PredictorLeaderboardUser(3, "xG_Wizard", null, 245, 186, 75.9f, 3890, 6, "Senior Modeler"),
            PredictorLeaderboardUser(4, "PremierPicks", null, 198, 147, 74.2f, 3340, 5, "Tactical Scout"),
            PredictorLeaderboardUser(5, "DerbyKing", null, 176, 128, 72.7f, 2980, 4, "Pro Tipster"),
            PredictorLeaderboardUser(6, "SerieA_Strategist", null, 154, 110, 71.4f, 2650, 3, "Club Analyst"),
            PredictorLeaderboardUser(7, "PunterElite", null, 132, 92, 69.7f, 2210, 2, "Verified Punter"),
            PredictorLeaderboardUser(8, "PitchPredictor", null, 110, 74, 67.3f, 1850, 1, "Community Scout")
        )
        Result.success(users)
    }

    private fun getFallbackLeaderboard(): List<User> {
        return listOf(
            User("1", "OracleTactics", "oracle@pitchmetrics.io", null, 79.5f, 312, 248, 11, 19, 1, 4820),
            User("2", "StrikerMetrics", "striker@pitchmetrics.io", null, 77.8f, 280, 218, 8, 14, 2, 4210),
            User("3", "xG_Wizard", "xg@pitchmetrics.io", null, 75.9f, 245, 186, 6, 12, 3, 3890),
            User("4", "PremierPicks", "picks@pitchmetrics.io", null, 74.2f, 198, 147, 5, 9, 4, 3340),
            User("5", "DerbyKing", "derby@pitchmetrics.io", null, 72.7f, 176, 128, 4, 8, 5, 2980)
        )
    }
}
