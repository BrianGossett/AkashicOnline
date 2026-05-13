package com.example.akashiconline.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.RoundEntity
import com.example.akashiconline.data.WorkoutSessionLogEntity
import com.example.akashiconline.ui.timer.Status
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ActiveWorkoutUiState(
    val workoutName: String = "",
    val rounds: List<RoundEntity> = emptyList(),
    val currentRoundIndex: Int = 0,
    val totalRoundsCompleted: Int = 0,
    val currentRoundElapsedSeconds: Int = 0,
    val totalElapsedSeconds: Int = 0,
    val status: Status = Status.RUNNING,
    val isLoading: Boolean = true,
)

class ActiveWorkoutViewModel(
    application: Application,
    private val workoutId: String,
) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val roundDao = db.roundDao()
    private val workoutDao = db.workoutDao()
    private val sessionLogDao = db.workoutSessionLogDao()

    private val _state = MutableStateFlow(ActiveWorkoutUiState())
    val state: StateFlow<ActiveWorkoutUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val workout = workoutDao.getById(workoutId)
            roundDao.getRoundsForWorkout(workoutId).collect { rounds ->
                _state.update {
                    it.copy(
                        workoutName = workout?.name ?: "",
                        rounds = rounds,
                        isLoading = false,
                    )
                }
                if (rounds.isNotEmpty() && _state.value.status == Status.RUNNING) {
                    startTicking()
                }
            }
        }
    }

    private fun startTicking() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val s = _state.value
                if (s.status != Status.RUNNING) break
                _state.update {
                    it.copy(
                        currentRoundElapsedSeconds = it.currentRoundElapsedSeconds + 1,
                        totalElapsedSeconds = it.totalElapsedSeconds + 1,
                    )
                }
            }
        }
    }

    fun pause() {
        if (_state.value.status != Status.RUNNING) return
        timerJob?.cancel()
        _state.update { it.copy(status = Status.PAUSED) }
    }

    fun resume() {
        if (_state.value.status != Status.PAUSED) return
        _state.update { it.copy(status = Status.RUNNING) }
        startTicking()
    }

    fun completeRound() {
        val s = _state.value
        val nextIndex = s.currentRoundIndex + 1
        if (nextIndex >= s.rounds.size) {
            finishWorkout(wasCompleted = true)
        } else {
            _state.update {
                it.copy(
                    currentRoundIndex = nextIndex,
                    totalRoundsCompleted = it.totalRoundsCompleted + 1,
                    currentRoundElapsedSeconds = 0,
                )
            }
        }
    }

    fun skipRound() {
        val s = _state.value
        val nextIndex = s.currentRoundIndex + 1
        if (nextIndex >= s.rounds.size) {
            finishWorkout(wasCompleted = true)
        } else {
            _state.update {
                it.copy(
                    currentRoundIndex = nextIndex,
                    currentRoundElapsedSeconds = 0,
                )
            }
        }
    }

    fun goBack() {
        val s = _state.value
        if (s.currentRoundIndex > 0) {
            _state.update {
                it.copy(
                    currentRoundIndex = it.currentRoundIndex - 1,
                    currentRoundElapsedSeconds = 0,
                )
            }
        }
    }

    fun stopEarly() {
        timerJob?.cancel()
        _state.update { it.copy(status = Status.STOPPED) }
        viewModelScope.launch { logSession(wasCompleted = false) }
    }

    private fun finishWorkout(wasCompleted: Boolean) {
        timerJob?.cancel()
        _state.update {
            it.copy(
                totalRoundsCompleted = it.totalRoundsCompleted + 1,
                status = Status.COMPLETE,
            )
        }
        viewModelScope.launch { logSession(wasCompleted = wasCompleted) }
    }

    private suspend fun logSession(wasCompleted: Boolean) {
        val s = _state.value
        sessionLogDao.insert(
            WorkoutSessionLogEntity(
                id = UUID.randomUUID().toString(),
                workoutId = workoutId,
                workoutName = s.workoutName,
                completedAt = System.currentTimeMillis(),
                totalElapsedSeconds = s.totalElapsedSeconds,
                roundsCompleted = s.totalRoundsCompleted,
                totalRounds = s.rounds.size,
                wasCompleted = wasCompleted,
            )
        )
    }

    class Factory(private val workoutId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val application = checkNotNull(
                extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            )
            return ActiveWorkoutViewModel(application, workoutId) as T
        }
    }
}
