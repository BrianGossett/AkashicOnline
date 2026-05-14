package com.example.akashiconline.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.data.TaskEntity
import com.example.akashiconline.ui.tasks.UpcomingViewModel
import com.example.akashiconline.ui.util.formatTimeMinutes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class UpcomingFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    THIS_WEEK("This week"),
    LATER("Later"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingScreen(
    onBack: () -> Unit,
    onOpenTask: (taskId: String) -> Unit,
    onNewTask: () -> Unit,
    viewModel: UpcomingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilter by rememberSaveable { mutableStateOf(UpcomingFilter.ALL) }

    val today = state.todayEpochDay
    val weekEnd = today + 6

    val visibleOverdue = if (selectedFilter == UpcomingFilter.ALL) state.overdueTasks else emptyList()

    val visibleUpcoming = when (selectedFilter) {
        UpcomingFilter.ALL -> state.upcomingTasks
        UpcomingFilter.TODAY -> state.upcomingTasks.filter { it.dueDateEpochDay == today }
        UpcomingFilter.THIS_WEEK -> state.upcomingTasks.filter {
            it.dueDateEpochDay != null && it.dueDateEpochDay <= weekEnd
        }
        UpcomingFilter.LATER -> state.upcomingTasks.filter {
            it.dueDateEpochDay != null && it.dueDateEpochDay > weekEnd
        }
    }

    // Group upcoming by date, preserving DAO sort order (already ASC by dueDateEpochDay)
    val groupedUpcoming: List<Pair<Long, List<TaskEntity>>> = visibleUpcoming
        .groupBy { it.dueDateEpochDay!! }
        .entries
        .sortedBy { it.key }
        .map { it.key to it.value }

    val isEmpty = visibleOverdue.isEmpty() && visibleUpcoming.isEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upcoming") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewTask) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = "New task")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                UpcomingFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) },
                    )
                }
            }

            HorizontalDivider()

            if (isEmpty) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = "No tasks here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Overdue section
                    if (visibleOverdue.isNotEmpty()) {
                        item(key = "header_overdue") {
                            Text(
                                text = "Overdue",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        items(visibleOverdue, key = { it.id }) { task ->
                            UpcomingTaskRow(
                                task = task,
                                onToggle = { viewModel.toggleComplete(task) },
                                onClick = { onOpenTask(task.id) },
                            )
                        }
                    }

                    // Upcoming grouped by date
                    groupedUpcoming.forEach { (epochDay, tasks) ->
                        item(key = "header_$epochDay") {
                            Text(
                                text = formatDateHeader(epochDay, today),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        items(tasks, key = { it.id }) { task ->
                            UpcomingTaskRow(
                                task = task,
                                onToggle = { viewModel.toggleComplete(task) },
                                onClick = { onOpenTask(task.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingTaskRow(
    task: TaskEntity,
    onToggle: () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
        )
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (task.dueTimeMinutes != null) {
                Text(
                    text = formatTimeMinutes(task.dueTimeMinutes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

private fun formatDateHeader(epochDay: Long, todayEpochDay: Long): String {
    val date = LocalDate.ofEpochDay(epochDay)
    val monthDay = date.format(DateTimeFormatter.ofPattern("MMM d"))
    return if (epochDay == todayEpochDay) {
        "Today · $monthDay"
    } else {
        val dow = date.format(DateTimeFormatter.ofPattern("EEE"))
        "$dow · $monthDay"
    }
}
