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
import com.example.akashiconline.data.ProgramEntity
import com.example.akashiconline.data.WeekEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class WeekDraft(
    val id: String = UUID.randomUUID().toString(),
    val weekNumber: Int,
    val phase: String = "",
    val phaseDescription: String = "",
)

class BuildProgramViewModel(
    application: Application,
    val editProgramId: String?,
) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val programDao = db.programDao()
    private val weekDao = db.weekDao()

    var nameInput by mutableStateOf("")
    var descriptionInput by mutableStateOf("")
    var weeks by mutableStateOf(emptyList<WeekDraft>())

    private var originalCreatedAt = 0L

    private val _savedEvent = MutableSharedFlow<String>()
    val savedEvent: SharedFlow<String> = _savedEvent.asSharedFlow()

    val isValid: Boolean get() = nameInput.isNotBlank() && weeks.isNotEmpty()

    init {
        if (editProgramId != null) {
            viewModelScope.launch {
                val program = programDao.getById(editProgramId)
                if (program != null) {
                    nameInput = program.name
                    descriptionInput = program.description
                    originalCreatedAt = program.createdAt
                }
                weeks = weekDao.getByProgramOnce(editProgramId).map { w ->
                    WeekDraft(
                        id = w.id,
                        weekNumber = w.weekNumber,
                        phase = w.phase,
                        phaseDescription = w.phaseDescription,
                    )
                }
            }
        }
    }

    fun addWeek() {
        weeks = weeks + WeekDraft(weekNumber = weeks.size + 1)
    }

    fun removeWeek(id: String) {
        weeks = weeks.filterNot { it.id == id }
            .mapIndexed { index, w -> w.copy(weekNumber = index + 1) }
    }

    fun updateWeekPhase(id: String, phase: String) {
        weeks = weeks.map { if (it.id == id) it.copy(phase = phase) else it }
    }

    fun updateWeekPhaseDescription(id: String, desc: String) {
        weeks = weeks.map { if (it.id == id) it.copy(phaseDescription = desc) else it }
    }

    fun save() {
        if (!isValid) return
        val trimmedName = nameInput.trim()
        val trimmedDesc = descriptionInput.trim()
        viewModelScope.launch {
            val programId = editProgramId ?: UUID.randomUUID().toString()
            if (editProgramId != null) {
                programDao.update(
                    ProgramEntity(
                        id = programId,
                        name = trimmedName,
                        description = trimmedDesc,
                        createdAt = originalCreatedAt,
                    )
                )
                weekDao.deleteByProgram(programId)
            } else {
                programDao.insert(
                    ProgramEntity(
                        id = programId,
                        name = trimmedName,
                        description = trimmedDesc,
                        createdAt = System.currentTimeMillis(),
                    )
                )
            }
            weekDao.insertAll(
                weeks.map { w ->
                    WeekEntity(
                        id = w.id,
                        programId = programId,
                        weekNumber = w.weekNumber,
                        label = "Week ${w.weekNumber}",
                        phase = w.phase.trim(),
                        phaseDescription = w.phaseDescription.trim(),
                    )
                }
            )
            _savedEvent.emit(programId)
        }
    }

    class Factory(private val editProgramId: String?) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val application = checkNotNull(
                extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            )
            return BuildProgramViewModel(application, editProgramId) as T
        }
    }
}
