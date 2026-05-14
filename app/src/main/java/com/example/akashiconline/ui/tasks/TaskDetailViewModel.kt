package com.example.akashiconline.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.akashiconline.data.CalendarEventEntity
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    application: Application,
    private val taskId: String,
) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val taskDao = db.taskDao()
    private val calendarDao = db.calendarEventDao()

    val task: StateFlow<TaskEntity?> = taskDao.getById(taskId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun toggleComplete(task: TaskEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) now else null,
            )
            taskDao.upsert(updated)
            if (updated.dueDateEpochDay != null) {
                calendarDao.upsert(
                    CalendarEventEntity(
                        id = "task_evt_${task.id}",
                        dateEpochDay = updated.dueDateEpochDay,
                        featureSource = "TASK",
                        sourceId = task.id,
                        title = task.name,
                        subtitle = null,
                        isCompleted = updated.isCompleted,
                        isAllDay = task.dueTimeMinutes == null,
                        timeMinutes = task.dueTimeMinutes,
                        createdAt = System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            taskDao.deleteById(id)
            calendarDao.delete("task_evt_$id")
        }
    }

    class Factory(private val taskId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
            return TaskDetailViewModel(application, taskId) as T
        }
    }
}
