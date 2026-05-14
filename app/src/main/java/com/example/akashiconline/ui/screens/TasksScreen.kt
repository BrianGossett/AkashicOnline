package com.example.akashiconline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.data.TaskEntity
import com.example.akashiconline.ui.tasks.TaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onBack: () -> Unit,
    onNewTask: () -> Unit,
    onOpenTask: (taskId: String) -> Unit,
    onOpenUpcoming: () -> Unit,
    viewModel: TaskViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
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
            // Spine header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3C3489))
                    .padding(vertical = 24.dp, horizontal = 24.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.Start),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        tint = Color(0xFFCECBF6),
                    )
                }
                Text(
                    text = "TASKS",
                    color = Color(0xFFCECBF6),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        letterSpacing = 6.sp,
                        fontWeight = FontWeight.Light,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Chapter III",
                    color = Color(0xFFCECBF6).copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFCECBF6).copy(alpha = 0.35f))
            }

            // Search
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                label = { Text("Search tasks") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 88.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                // Overdue
                if (state.overdueTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(title = "OVERDUE")
                    }
                    items(state.overdueTasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            todayEpochDay = state.todayEpochDay,
                            onToggle = { viewModel.toggleComplete(task) },
                            onClick = { onOpenTask(task.id) },
                        )
                    }
                }

                // Upcoming
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "UPCOMING",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "See all ›",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onOpenUpcoming() },
                        )
                    }
                }
                if (state.upcomingTasks.isEmpty()) {
                    item {
                        Text(
                            text = "No upcoming tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                } else {
                    items(state.upcomingTasks.take(3), key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            todayEpochDay = state.todayEpochDay,
                            onToggle = { viewModel.toggleComplete(task) },
                            onClick = { onOpenTask(task.id) },
                        )
                    }
                }

                // No date
                item {
                    TaskSectionHeader(title = "NO DATE")
                }
                if (state.undatedTasks.isEmpty()) {
                    item {
                        Text(
                            text = "No tasks without a date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                } else {
                    items(state.undatedTasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            todayEpochDay = state.todayEpochDay,
                            onToggle = { viewModel.toggleComplete(task) },
                            onClick = { onOpenTask(task.id) },
                        )
                    }
                }

                // Completed
                if (state.completedTasks.isNotEmpty()) {
                    item {
                        TaskSectionHeader(title = "COMPLETED")
                    }
                    items(state.completedTasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            todayEpochDay = state.todayEpochDay,
                            onToggle = { viewModel.toggleComplete(task) },
                            onClick = { onOpenTask(task.id) },
                            isCompleted = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun TaskRow(
    task: TaskEntity,
    todayEpochDay: Long,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    isCompleted: Boolean = false,
) {
    val isOverdue = !isCompleted &&
        task.dueDateEpochDay != null &&
        task.dueDateEpochDay < todayEpochDay

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
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                ),
                color = if (isCompleted)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (task.dueDateEpochDay != null && !isCompleted) {
                val dateText = LocalDate.ofEpochDay(task.dueDateEpochDay)
                    .format(DateTimeFormatter.ofPattern("MMM d"))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverdue)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
