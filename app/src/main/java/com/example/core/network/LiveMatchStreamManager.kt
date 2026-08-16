package com.example.core.network

import android.content.Context
import com.example.data.repositories.LiveRepository
import com.example.domain.models.LiveMatch
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class LiveMatchStreamManager(context: Context) {
    private val liveRepo = LiveRepository(context)
    private val streamScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _liveStreamState = MutableStateFlow<LiveStreamState>(LiveStreamState.Idle)
    val liveStreamState: StateFlow<LiveStreamState> = _liveStreamState.asStateFlow()

    private var activeJob: Job? = null

    sealed class LiveStreamState {
        object Idle : LiveStreamState()
        object Connecting : LiveStreamState()
        data class Connected(val matches: List<LiveMatch>, val timestamp: Long) : LiveStreamState()
        data class Error(val message: String) : LiveStreamState()
    }

    fun startLiveStream(intervalMs: Long = 10000L) {
        if (activeJob?.isActive == true) return

        _liveStreamState.value = LiveStreamState.Connecting
        activeJob = streamScope.launch {
            while (isActive) {
                try {
                    val result = liveRepo.getLiveMatches()
                    if (result.isSuccess) {
                        val list = result.getOrDefault(emptyList())
                        _liveStreamState.value = LiveStreamState.Connected(
                            matches = list,
                            timestamp = System.currentTimeMillis()
                        )
                    } else {
                        val err = result.exceptionOrNull()?.localizedMessage ?: "Live feed sync error"
                        if (_liveStreamState.value is LiveStreamState.Connecting) {
                            _liveStreamState.value = LiveStreamState.Error(err)
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _liveStreamState.value = LiveStreamState.Error(e.localizedMessage ?: "Stream error")
                }
                delay(intervalMs)
            }
        }
    }

    fun stopLiveStream() {
        activeJob?.cancel()
        activeJob = null
        _liveStreamState.value = LiveStreamState.Idle
    }

    fun getMatchStream(eventKey: String, intervalMs: Long = 8000L): Flow<LiveMatch?> = flow {
        while (currentCoroutineContext().isActive) {
            val result = liveRepo.getLiveMatches()
            val match = result.getOrNull()?.find { it.eventKey == eventKey || it.asFixtureId == eventKey || it.pmMatchId == eventKey }
            emit(match)
            delay(intervalMs)
        }
    }.flowOn(Dispatchers.IO)
}
