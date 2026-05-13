package com.example.akashiconline.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.RoundEntity
import com.example.akashiconline.data.WorkoutEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkoutWithRounds(
    val workout: WorkoutEntity,
    val rounds: List<RoundEntity>,
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val workoutDao = db.workoutDao()
    private val roundDao = db.roundDao()

    private val allRounds = roundDao.getAllRounds()

    val workouts: StateFlow<List<WorkoutWithRounds>> = combine(
        workoutDao.getAll(),
        allRounds,
    ) { workouts, rounds ->
        val byWorkout = rounds.groupBy { it.workoutId }
        workouts.map { WorkoutWithRounds(it, byWorkout[it.id] ?: emptyList()) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val scheduled: StateFlow<List<WorkoutWithRounds>> = combine(
        workoutDao.getScheduled(),
        allRounds,
    ) { workouts, rounds ->
        val byWorkout = rounds.groupBy { it.workoutId }
        workouts.map { WorkoutWithRounds(it, byWorkout[it.id] ?: emptyList()) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch {
            workoutDao.deleteById(workoutId)
        }
    }
}
