package com.example.akashiconline.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.akashiconline.data.CalendarEventEntity
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.TaskEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TasksUiState(
    val searchQuery: String = "",
    val overdueTasks: List<TaskEntity> = emptyList(),
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val undatedTasks: List<TaskEntity> = emptyList(),
    val completedTasks: List<TaskEntity> = emptyList(),
    val todayEpochDay: Long,
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val taskDao = db.taskDao()
    private val calendarDao = db.calendarEventDao()

    private val todayEpochDay = LocalDate.now().toEpochDay()

    private val _searchQuery = MutableStateFlow("")
    var searchQuery: String
        get() = _searchQuery.value
        set(value) { _searchQuery.value = value }

    private val rawState = combine(
        taskDao.getOverdue(todayEpochDay),
        taskDao.getUpcoming(todayEpochDay),
        taskDao.getUndated(),
        taskDao.getAll(),
    ) { overdue, upcoming, undated, all ->
        TasksUiState(
            overdueTasks = overdue,
            upcomingTasks = upcoming,
            undatedTasks = undated,
            completedTasks = all.filter { it.isCompleted },
            todayEpochDay = todayEpochDay,
        )
    }

    val uiState: StateFlow<TasksUiState> = combine(rawState, _searchQuery) { raw, query ->
        if (query.isBlank()) {
            raw.copy(searchQuery = query)
        } else {
            fun TaskEntity.matches() = name.contains(query, ignoreCase = true) ||
                details?.contains(query, ignoreCase = true) == true
            raw.copy(
                searchQuery = query,
                overdueTasks = raw.overdueTasks.filter { it.matches() },
                upcomingTasks = raw.upcomingTasks.filter { it.matches() },
                undatedTasks = raw.undatedTasks.filter { it.matches() },
                completedTasks = raw.completedTasks.filter { it.matches() },
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksUiState(todayEpochDay = todayEpochDay),
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
}
