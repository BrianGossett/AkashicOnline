package com.example.akashiconline.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.akashiconline.data.TimerConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimerViewModel(
    application: Application,
    private val config: TimerConfig,
) : AndroidViewModel(application) {

    private val feedback = FeedbackManager(application)

    private val _state = MutableStateFlow(
        TimerUiState(
            secondsRemaining = config.workSeconds,
            totalPhaseSeconds = config.workSeconds,
            totalRounds = config.rounds,
        )
    )
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        feedback.playWork()
        startCountdown()
    }

    fun togglePause() {
        when (_state.value.status) {
            Status.RUNNING -> {
                timerJob?.cancel()
                _state.update { it.copy(status = Status.PAUSED) }
            }
            Status.PAUSED -> {
                _state.update { it.copy(status = Status.RUNNING) }
                startCountdown()
            }
            else -> {}
        }
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                if (_state.value.status != Status.RUNNING) break
                tick()
            }
        }
    }

    private fun tick() {
        val current = _state.value
        if (current.secondsRemaining > 1) {
            _state.update {
                it.copy(
                    secondsRemaining = it.secondsRemaining - 1,
                    totalElapsedSeconds = it.totalElapsedSeconds + 1,
                )
            }
        } else {
            _state.update { it.copy(totalElapsedSeconds = it.totalElapsedSeconds + 1) }
            advance()
        }
    }

    private fun advance() {
        val current = _state.value
        when (current.phase) {
            Phase.WORK -> {
                _state.update {
                    it.copy(
                        phase = Phase.REST,
                        secondsRemaining = config.restSeconds,
                        totalPhaseSeconds = config.restSeconds,
                    )
                }
                feedback.playRest()
            }
            Phase.REST -> {
                if (current.currentRound < current.totalRounds) {
                    _state.update {
                        it.copy(
                            phase = Phase.WORK,
                            secondsRemaining = config.workSeconds,
                            totalPhaseSeconds = config.workSeconds,
                            currentRound = it.currentRound + 1,
                        )
                    }
                    feedback.playWork()
                } else {
                    _state.update { it.copy(status = Status.COMPLETE, secondsRemaining = 0) }
                    feedback.playComplete()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        feedback.release()
    }

    class Factory(private val config: TimerConfig) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val application = checkNotNull(
                extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            )
            return TimerViewModel(application, config) as T
        }
    }
}
