package com.example.akashiconline.ui.tasks

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.akashiconline.data.CalendarEventEntity
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.TaskEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class EditTaskViewModel(
    application: Application,
    private val taskId: String?,
) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val taskDao = db.taskDao()
    private val calendarDao = db.calendarEventDao()

    var name by mutableStateOf("")
    var details by mutableStateOf("")
    var dueDateEpochDay by mutableStateOf<Long?>(null)
    var dueTimeMinutes by mutableStateOf<Int?>(null)

    private var originalTask: TaskEntity? = null

    private val _saved = MutableSharedFlow<Unit>()
    val saved: SharedFlow<Unit> = _saved.asSharedFlow()

    val canSave: Boolean get() = name.isNotBlank()

    init {
        if (taskId != null) {
            viewModelScope.launch {
                taskDao.getById(taskId).first()?.let { task ->
                    originalTask = task
                    name = task.name
                    details = task.details ?: ""
                    dueDateEpochDay = task.dueDateEpochDay
                    dueTimeMinutes = task.dueTimeMinutes
                }
            }
        }
    }

    fun save() {
        if (!canSave) return
        viewModelScope.launch {
            val id = taskId ?: UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val existing = originalTask
            val task = TaskEntity(
                id = id,
                name = name.trim(),
                details = details.trim().ifEmpty { null },
                dueDateEpochDay = dueDateEpochDay,
                dueTimeMinutes = if (dueDateEpochDay != null) dueTimeMinutes else null,
                isCompleted = existing?.isCompleted ?: false,
                completedAt = existing?.completedAt,
                createdAt = existing?.createdAt ?: now,
            )
            taskDao.upsert(task)
            if (dueDateEpochDay != null) {
                calendarDao.upsert(
                    CalendarEventEntity(
                        id = "task_evt_$id",
                        dateEpochDay = dueDateEpochDay!!,
                        featureSource = "TASK",
                        sourceId = id,
                        title = task.name,
                        subtitle = null,
                        isCompleted = task.isCompleted,
                        isAllDay = task.dueTimeMinutes == null,
                        timeMinutes = task.dueTimeMinutes,
                        createdAt = now,
                    )
                )
            } else {
                calendarDao.delete("task_evt_$id")
            }
            _saved.emit(Unit)
        }
    }

    class Factory(private val taskId: String?) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
            return EditTaskViewModel(application, taskId) as T
        }
    }
}
