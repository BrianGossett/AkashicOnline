package com.example.akashiconline.ui.timer

enum class Status { RUNNING, PAUSED, STOPPED, COMPLETE }

data class TimerUiState(
    val isLoading: Boolean = false,
    val stepName: String = "",
    val isRestStep: Boolean = false,
    val secondsRemaining: Int = 0,
    val totalPhaseSeconds: Int = 0,
    val currentStep: Int = 1,
    val totalSteps: Int = 0,
    val status: Status = Status.RUNNING,
    val totalElapsedSeconds: Int = 0,
)
