package com.example.akashiconline.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.akashiconline.data.CalendarEventEntity
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UpcomingUiState(
    val overdueTasks: List<TaskEntity> = emptyList(),
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val todayEpochDay: Long = LocalDate.now().toEpochDay(),
)

class UpcomingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val taskDao = db.taskDao()
    private val calendarDao = db.calendarEventDao()

    val todayEpochDay: Long = LocalDate.now().toEpochDay()

    val uiState: StateFlow<UpcomingUiState> = combine(
        taskDao.getOverdue(todayEpochDay),
        taskDao.getUpcoming(todayEpochDay),
    ) { overdue, upcoming ->
        UpcomingUiState(
            overdueTasks = overdue,
            upcomingTasks = upcoming,
            todayEpochDay = todayEpochDay,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpcomingUiState(todayEpochDay = todayEpochDay),
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
}
