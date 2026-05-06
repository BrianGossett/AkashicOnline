package com.example.akashiconline.ui.timer

enum class Phase { WORK, REST }

enum class Status { RUNNING, PAUSED, STOPPED, COMPLETE }

data class TimerUiState(
    val phase: Phase = Phase.WORK,
    val secondsRemaining: Int,
    val totalPhaseSeconds: Int,
    val currentRound: Int = 1,
    val totalRounds: Int,
    val status: Status = Status.RUNNING,
    val totalElapsedSeconds: Int = 0,
)
