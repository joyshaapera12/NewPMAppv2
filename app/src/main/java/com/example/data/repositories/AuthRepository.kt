package com.example.data.repositories

import android.content.Context
import com.example.core.network.NetworkClient
import com.example.core.storage.UserPreferencesRepository
import com.example.domain.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    private val api = NetworkClient.getApiService(context)
    private val cookieJar = NetworkClient.getCookieJar(context)
    private val prefs = UserPreferencesRepository(context)

    suspend fun login(usernameOrEmail: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(mapOf("username" to usernameOrEmail, "password" to password))
            if (response.isSuccessful) {
                val dto = response.body()?.data
                val user = User(
                    id = dto?.id ?: "1",
                    username = dto?.username ?: usernameOrEmail,
                    email = dto?.email ?: "",
                    avatarUrl = dto?.avatar,
                    accuracy = (dto?.accuracy ?: 68.0).toFloat(),
                    totalPredictions = dto?.totalPredictions ?: 0,
                    correctPredictions = dto?.correctPredictions ?: 0,
                    currentStreak = dto?.currentStreak ?: 0,
                    bestStreak = dto?.bestStreak ?: 0,
                    rank = dto?.rank ?: 1,
                    points = dto?.points ?: 0
                )
                prefs.saveUserSession(user.username, user.email)
                Result.success(user)
            } else {
                val errMsg = response.body()?.error ?: "Invalid credentials (HTTP ${response.code()})"
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(mapOf("username" to username, "email" to email, "password" to password))
            if (response.isSuccessful) {
                val dto = response.body()?.data
                val user = User(
                    id = dto?.id ?: "1",
                    username = dto?.username ?: username,
                    email = dto?.email ?: email,
                    avatarUrl = null,
                    accuracy = 0f,
                    totalPredictions = 0,
                    correctPredictions = 0,
                    currentStreak = 0,
                    bestStreak = 0,
                    rank = 100,
                    points = 0
                )
                prefs.saveUserSession(user.username, user.email)
                Result.success(user)
            } else {
                val errMsg = response.body()?.error ?: "Registration failed"
                Result.failure(Exception(errMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMe(): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMe()
            if (response.isSuccessful) {
                val dto = response.body()?.data
                if (dto != null) {
                    val user = User(
                        id = dto.id ?: "1",
                        username = dto.username ?: "Tactician",
                        email = dto.email ?: "",
                        avatarUrl = dto.avatar,
                        accuracy = (dto.accuracy ?: 74.5).toFloat(),
                        totalPredictions = dto.totalPredictions ?: 24,
                        correctPredictions = dto.correctPredictions ?: 18,
                        currentStreak = dto.currentStreak ?: 3,
                        bestStreak = dto.bestStreak ?: 7,
                        rank = dto.rank ?: 42,
                        points = dto.points ?: 560
                    )
                    Result.success(user)
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.success(null)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.logout()
        } catch (ignored: Exception) {}
        cookieJar.clearAllCookies()
        prefs.clearUserSession()
        Result.success(Unit)
    }
}
