package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repositories.CommunityRepository
import com.example.data.repositories.PlayerRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PitchMetrics", appName)
    }

    @Test
    fun testPlayerRepositoryProfileGeneration() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val playerRepo = PlayerRepository(context)

        val result = playerRepo.getPlayerProfile(
            playerId = "player_10",
            playerName = "Bukayo Saka",
            teamName = "Arsenal",
            position = "RW",
            number = "7"
        )

        assertTrue(result.isSuccess)
        val profile = result.getOrNull()
        assertNotNull(profile)
        assertEquals("Bukayo Saka", profile?.name)
        assertEquals("7", profile?.number)
        assertEquals("Arsenal", profile?.teamName)
        assertTrue((profile?.rating ?: 0f) > 6.0f)
    }

    @Test
    fun testRefereeAndStadiumAnalytics() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val playerRepo = PlayerRepository(context)

        val ref = playerRepo.getRefereeAnalytics("Michael Oliver")
        assertNotNull(ref)
        assertEquals("Michael Oliver", ref.name)
        assertTrue(ref.foulsPerGame > 0)

        val stadium = playerRepo.getStadiumAnalytics("Emirates Stadium", "Arsenal")
        assertNotNull(stadium)
        assertEquals("Emirates Stadium", stadium.name)
        assertTrue(stadium.capacity > 0)
    }

    @Test
    fun testCommunityCommentsAndReactions() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val communityRepo = CommunityRepository(context)

        val comments = communityRepo.getMatchComments("test_match_1").getOrDefault(emptyList())
        assertFalse(comments.isEmpty())

        val postRes = communityRepo.postComment("test_match_1", "Great pressing in midfield!", "Analyst", "Home")
        assertTrue(postRes.isSuccess)
        val posted = postRes.getOrNull()
        assertEquals("Great pressing in midfield!", posted?.text)

        val reactions = communityRepo.getLiveEmojiReactions("test_match_1").getOrDefault(emptyList())
        assertFalse(reactions.isEmpty())
        assertTrue(reactions.any { it.emoji == "🔥" })
    }
}
