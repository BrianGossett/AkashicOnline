package com.example.akashiconline.ui.workout

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.akashiconline.data.CalendarEventEntity
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.RoundEntity
import com.example.akashiconline.data.WorkoutEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

data class RoundDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val durationInput: String = "",
    val isRestRound: Boolean = false,
    val weightInput: String = "",
    val repsInput: String = "",
)

class CreateWorkoutViewModel(
    application: Application,
    private val workoutId: String?,
) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val workoutDao = db.workoutDao()
    private val roundDao = db.roundDao()
    private val calendarDao = db.calendarEventDao()

    var name by mutableStateOf("")
    val rounds = mutableStateListOf<RoundDraft>()
    var scheduleEnabled by mutableStateOf(false)
    var scheduledDateMillis by mutableStateOf<Long?>(null)
    var repeatRule by mutableStateOf<String?>(null)
    var reminderMinutesBefore by mutableStateOf<Int?>(null)
    var isSaving by mutableStateOf(false)

    private val _saved = MutableSharedFlow<Unit>()
    val saved: SharedFlow<Unit> = _saved

    val isNewWorkout: Boolean get() = workoutId == null

    val nameError: Boolean get() = name.isBlank()
    val canSave: Boolean get() = !nameError && rounds.isNotEmpty() && !isSaving

    init {
        if (workoutId != null) {
            viewModelScope.launch {
                val workout = workoutDao.getById(workoutId) ?: return@launch
                name = workout.name
                scheduledDateMillis = workout.scheduledDate
                repeatRule = workout.repeatRule
                reminderMinutesBefore = workout.reminderMinutesBefore
                scheduleEnabled = workout.scheduledDate != null

                roundDao.getRoundsForWorkout(workoutId).collect { entities ->
                    rounds.clear()
                    rounds.addAll(entities.map { r ->
                        RoundDraft(
                            id = r.id,
                            name = r.name,
                            durationInput = r.durationSeconds?.toString() ?: "",
                            isRestRound = r.isRestRound,
                            weightInput = r.weightKg?.let {
                                if (it == it.toLong().toFloat()) it.toLong().toString()
                                else it.toString()
                            } ?: "",
                            repsInput = r.reps?.toString() ?: "",
                        )
                    })
                }
            }
        } else {
            rounds.add(RoundDraft(name = "Round 1"))
        }
    }

    fun addRound() {
        rounds.add(RoundDraft(name = "Round ${rounds.size + 1}"))
    }

    fun removeRound(index: Int) {
        if (index in rounds.indices) rounds.removeAt(index)
    }

    fun updateRound(index: Int, draft: RoundDraft) {
        if (index in rounds.indices) rounds[index] = draft
    }

    fun moveRoundUp(index: Int) {
        if (index > 0) {
            val tmp = rounds[index - 1]
            rounds[index - 1] = rounds[index]
            rounds[index] = tmp
        }
    }

    fun moveRoundDown(index: Int) {
        if (index < rounds.size - 1) {
            val tmp = rounds[index + 1]
            rounds[index + 1] = rounds[index]
            rounds[index] = tmp
        }
    }

    fun save() {
        if (!canSave) return
        isSaving = true
        viewModelScope.launch {
            val id = workoutId ?: UUID.randomUUID().toString()
            val schedDate = if (scheduleEnabled) scheduledDateMillis else null

            val workout = WorkoutEntity(
                id = id,
                name = name.trim(),
                scheduledDate = schedDate,
                repeatRule = if (scheduleEnabled) repeatRule else null,
                reminderMinutesBefore = if (scheduleEnabled) reminderMinutesBefore else null,
                createdAt = System.currentTimeMillis(),
                lastUsedAt = null,
            )
            workoutDao.insert(workout)

            roundDao.deleteByWorkoutId(id)
            roundDao.insertAll(
                rounds.mapIndexed { index, draft ->
                    RoundEntity(
                        id = draft.id,
                        workoutId = id,
                        order = index,
                        name = draft.name.trim().ifEmpty { "Round ${index + 1}" },
                        durationSeconds = draft.durationInput.toIntOrNull()?.takeIf { it > 0 },
                        isRestRound = draft.isRestRound,
                        weightKg = draft.weightInput.toFloatOrNull()?.takeIf { it > 0 },
                        reps = draft.repsInput.toIntOrNull()?.takeIf { it > 0 },
                    )
                }
            )

            val calEventId = "workout_evt_$id"
            if (schedDate != null) {
                val epochDay = Instant.ofEpochMilli(schedDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toEpochDay()
                calendarDao.upsert(
                    CalendarEventEntity(
                        id = calEventId,
                        dateEpochDay = epochDay,
                        featureSource = "WORKOUT",
                        sourceId = id,
                        title = name.trim(),
                        subtitle = null,
                        isCompleted = false,
                        isAllDay = true,
                        createdAt = System.currentTimeMillis(),
                    )
                )
            } else {
                calendarDao.delete(calEventId)
            }

            _saved.emit(Unit)
        }
    }

    class Factory(private val workoutId: String?) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
            return CreateWorkoutViewModel(application, workoutId) as T
        }
    }
}
