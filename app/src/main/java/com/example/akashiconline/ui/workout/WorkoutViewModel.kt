package com.example.akashiconline.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.akashiconline.data.CalendarEventEntity
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.RoundEntity
import com.example.akashiconline.data.WorkoutEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

data class WorkoutWithRounds(
    val workout: WorkoutEntity,
    val rounds: List<RoundEntity>,
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val workoutDao = db.workoutDao()
    private val roundDao = db.roundDao()
    private val calendarDao = db.calendarEventDao()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

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

    fun scheduleWorkout(
        workoutId: String,
        dateMillis: Long,
        timeMinutes: Int?,
        repeatRule: String?,
        reminderMinutes: Int?,
    ) {
        viewModelScope.launch {
            val existing = workoutDao.getById(workoutId) ?: return@launch
            workoutDao.insert(
                existing.copy(
                    scheduledDate = dateMillis,
                    scheduledTimeMinutes = timeMinutes,
                    repeatRule = repeatRule,
                    reminderMinutesBefore = reminderMinutes,
                )
            )
            val epochDay = Instant.ofEpochMilli(dateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toEpochDay()
            calendarDao.upsert(
                CalendarEventEntity(
                    id = "workout_evt_$workoutId",
                    dateEpochDay = epochDay,
                    featureSource = "WORKOUT",
                    sourceId = workoutId,
                    title = existing.name,
                    subtitle = null,
                    isCompleted = false,
                    isAllDay = timeMinutes == null,
                    timeMinutes = timeMinutes,
                    createdAt = System.currentTimeMillis(),
                )
            )
            _snackbarMessage.emit("Workout scheduled")
        }
    }
}
