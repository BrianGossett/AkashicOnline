package com.example.akashiconline.ui.programs

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.DayEntity
import com.example.akashiconline.data.StepEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class StepDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val minutes: Int = 0,
    val seconds: Int = 30,
    val isRestStep: Boolean = false,
) {
    val totalSeconds: Int get() = minutes * 60 + seconds
}

class DayEditorViewModel(
    application: Application,
    val editDayId: String?,
    private val initWeekId: String?,
    private val initDayNumber: Int,
) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val dayDao = db.dayDao()
    private val stepDao = db.stepDao()

    private val resolvedDayId = editDayId ?: UUID.randomUUID().toString()
    private var resolvedWeekId: String? = initWeekId
    private var resolvedDayNumber: Int = initDayNumber.coerceAtLeast(1)

    var dayType by mutableStateOf("BASE")
    var steps by mutableStateOf(emptyList<StepDraft>())

    private val _savedEvent = MutableSharedFlow<Unit>()
    val savedEvent: SharedFlow<Unit> = _savedEvent.asSharedFlow()

    init {
        if (editDayId != null) {
            viewModelScope.launch {
                val day = dayDao.getByIdOnce(editDayId)
                if (day != null) {
                    resolvedWeekId = day.weekId
                    resolvedDayNumber = day.dayNumber
                    dayType = day.type
                }
                steps = stepDao.getByDayOnce(editDayId).map { s ->
                    StepDraft(
                        id = s.id,
                        name = s.name,
                        minutes = s.durationSeconds / 60,
                        seconds = s.durationSeconds % 60,
                        isRestStep = s.isRestStep,
                    )
                }
            }
        }
    }

    fun addStep() {
        val lastIsRest = steps.lastOrNull()?.isRestStep ?: false
        steps = steps + StepDraft(isRestStep = !lastIsRest)
    }

    fun removeStep(id: String) {
        steps = steps.filterNot { it.id == id }
    }

    fun updateName(id: String, name: String) {
        steps = steps.map { if (it.id == id) it.copy(name = name) else it }
    }

    fun updateMinutes(id: String, minutes: Int) {
        steps = steps.map { if (it.id == id) it.copy(minutes = minutes.coerceIn(0, 99)) else it }
    }

    fun updateSeconds(id: String, seconds: Int) {
        steps = steps.map { if (it.id == id) it.copy(seconds = seconds.coerceIn(0, 59)) else it }
    }

    fun toggleRestStep(id: String) {
        steps = steps.map { if (it.id == id) it.copy(isRestStep = !it.isRestStep) else it }
    }

    fun moveStep(fromIndex: Int, toIndex: Int) {
        val list = steps.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex.coerceIn(0, list.size), item)
        steps = list
    }

    fun save() {
        val weekId = resolvedWeekId ?: return
        viewModelScope.launch {
            val dayEntity = DayEntity(
                id = resolvedDayId,
                weekId = weekId,
                dayNumber = resolvedDayNumber,
                type = dayType,
                label = "Day $resolvedDayNumber",
            )
            if (editDayId != null) {
                dayDao.update(dayEntity)
            } else {
                dayDao.insert(dayEntity)
            }
            stepDao.deleteByDay(resolvedDayId)
            stepDao.insertAll(
                steps.mapIndexed { index, draft ->
                    StepEntity(
                        id = draft.id,
                        dayId = resolvedDayId,
                        order = index,
                        name = draft.name.trim().ifEmpty { if (draft.isRestStep) "Rest" else "Work" },
                        durationSeconds = draft.totalSeconds.coerceAtLeast(1),
                        isRestStep = draft.isRestStep,
                    )
                }
            )
            _savedEvent.emit(Unit)
        }
    }

    class Factory(
        private val editDayId: String?,
        private val weekId: String?,
        private val dayNumber: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val application = checkNotNull(
                extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            )
            return DayEditorViewModel(application, editDayId, weekId, dayNumber) as T
        }
    }
}
