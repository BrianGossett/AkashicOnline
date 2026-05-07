package com.example.akashiconline.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.StepConfig
import com.example.akashiconline.data.TimerConfig
import com.example.akashiconline.data.toStepList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimerViewModel internal constructor(
    application: Application,
    presetSteps: List<StepConfig>,
    private val dayId: String?,
) : AndroidViewModel(application) {

    private val feedback = FeedbackManager(application)
    private val db = DatabaseProvider.getDatabase(application)

    private var sessionSteps: List<StepConfig> = presetSteps
    private var currentStepIndex = 0

    private val _state = MutableStateFlow(
        if (dayId != null) {
            TimerUiState(isLoading = true)
        } else {
            presetSteps.firstOrNull().toState(totalSteps = presetSteps.size)
        }
    )
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        if (dayId != null) {
            viewModelScope.launch {
                val steps = loadDaySteps(dayId)
                sessionSteps = steps
                currentStepIndex = 0
                val first = steps.firstOrNull()
                if (first != null) {
                    _state.value = first.toState(totalSteps = steps.size)
                    first.playFeedback()
                    startCountdown()
                } else {
                    _state.update { it.copy(isLoading = false, status = Status.COMPLETE) }
                }
            }
        } else if (presetSteps.isNotEmpty()) {
            presetSteps[0].playFeedback()
            startCountdown()
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
        startCountdown()
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
        if (_state.value.secondsRemaining > 1) {
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

    fun stopEarly() {
        timerJob?.cancel()
        _state.update { it.copy(status = Status.STOPPED) }
        viewModelScope.launch { logSession(wasCompleted = false) }
    }

    private suspend fun logSession(wasCompleted: Boolean) {
        db.sessionLogDao().insert(
            com.example.akashiconline.data.SessionLogEntity(
                id = java.util.UUID.randomUUID().toString(),
                dayId = dayId,
                completedAt = System.currentTimeMillis(),
                totalElapsedSeconds = _state.value.totalElapsedSeconds,
                wasCompleted = wasCompleted,
            )
        )
    }

    private fun advance() {
        val nextIndex = currentStepIndex + 1
        if (nextIndex >= sessionSteps.size) {
            _state.update { it.copy(status = Status.COMPLETE, secondsRemaining = 0) }
            feedback.playComplete()
            viewModelScope.launch { logSession(wasCompleted = true) }
            return
        }
        currentStepIndex = nextIndex
        val next = sessionSteps[nextIndex]
        _state.update {
            it.copy(
                stepName = next.name,
                isRestStep = next.isRestStep,
                secondsRemaining = next.durationSeconds,
                totalPhaseSeconds = next.durationSeconds,
                currentStep = nextIndex + 1,
            )
        }
        next.playFeedback()
    }

    private fun StepConfig.playFeedback() {
        if (isRestStep) feedback.playRest() else feedback.playWork()
    }

    private suspend fun loadDaySteps(dayId: String): List<StepConfig> =
        db.stepDao().getByDayOnce(dayId).map { entity ->
            StepConfig(
                name = entity.name,
                durationSeconds = entity.durationSeconds,
                isRestStep = entity.isRestStep,
            )
        }

    override fun onCleared() {
        super.onCleared()
        feedback.release()
    }

    /** Factory for the preset work/rest timer. */
    class Factory(private val config: TimerConfig) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val application = checkNotNull(
                extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            )
            return TimerViewModel(application, config.toStepList(), null) as T
        }
    }

    /** Factory for a program day session; steps are loaded from Room on start. */
    class FromDay(private val dayId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val application = checkNotNull(
                extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            )
            return TimerViewModel(application, emptyList(), dayId) as T
        }
    }
}

private fun StepConfig?.toState(totalSteps: Int): TimerUiState = TimerUiState(
    isLoading = false,
    stepName = this?.name ?: "",
    isRestStep = this?.isRestStep ?: false,
    secondsRemaining = this?.durationSeconds ?: 0,
    totalPhaseSeconds = this?.durationSeconds ?: 0,
    currentStep = 1,
    totalSteps = totalSteps,
)
