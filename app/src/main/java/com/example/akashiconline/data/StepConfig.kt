package com.example.akashiconline.data

data class StepConfig(
    val name: String,
    val durationSeconds: Int,
    val isRestStep: Boolean,
)

fun TimerConfig.toStepList(): List<StepConfig> = (1..rounds).flatMap {
    listOf(
        StepConfig(name = "Work", durationSeconds = workSeconds, isRestStep = false),
        StepConfig(name = "Rest", durationSeconds = restSeconds, isRestStep = true),
    )
}
