package com.example.akashiconline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.data.RoundEntity
import com.example.akashiconline.data.TimerConfig
import com.example.akashiconline.data.WorkoutEntity
import com.example.akashiconline.ui.workout.WorkoutViewModel
import com.example.akashiconline.ui.workout.WorkoutWithRounds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onBack: () -> Unit,
    onCreateWorkout: () -> Unit,
    onEditWorkout: (workoutId: String) -> Unit,
    onStartWorkout: (workoutId: String) -> Unit,
    onQuickTimerStart: (TimerConfig) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Spine-style header
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
                    text = "WORKOUT",
                    color = Color(0xFFCECBF6),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        letterSpacing = 6.sp,
                        fontWeight = FontWeight.Light,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Chapter I",
                    color = Color(0xFFCECBF6).copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFCECBF6).copy(alpha = 0.35f))
            }

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My Workouts") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Scheduled") },
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Quick Timer") },
                )
            }

            when (selectedTab) {
                0 -> MyWorkoutsTab(
                    onCreateWorkout = onCreateWorkout,
                    onEditWorkout = onEditWorkout,
                    onStartWorkout = onStartWorkout,
                )
                1 -> ScheduledTab(
                    onEditWorkout = onEditWorkout,
                    onStartWorkout = onStartWorkout,
                )
                2 -> QuickTimerTab(onStart = onQuickTimerStart)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWorkoutsTab(
    onCreateWorkout: () -> Unit,
    onEditWorkout: (workoutId: String) -> Unit,
    onStartWorkout: (workoutId: String) -> Unit,
    viewModel: WorkoutViewModel = viewModel(),
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete workout?") },
            text = { Text("This will permanently remove the workout and all its rounds.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWorkout(pendingDeleteId!!)
                    pendingDeleteId = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") }
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (workouts.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painterResource(R.drawable.ic_workout),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No workouts yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tap + Create workout to get started",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(workouts, key = { it.workout.id }) { item ->
                    WorkoutCard(
                        item = item,
                        onStart = { onStartWorkout(item.workout.id) },
                        onEdit = { onEditWorkout(item.workout.id) },
                        onDeleteRequest = { pendingDeleteId = item.workout.id },
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onCreateWorkout,
            icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
            text = { Text("Create workout") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutCard(
    item: WorkoutWithRounds,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDeleteRequest()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(end = 20.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        ElevatedCard(
            onClick = onStart,
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = item.workout.name,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        WorkoutTypeBadge(item.workout, item.rounds)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = workoutMeta(item.rounds),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutTypeBadge(workout: WorkoutEntity, rounds: List<RoundEntity>) {
    val label = when {
        workout.name.contains("Week", ignoreCase = true) -> "Program"
        rounds.any { it.durationSeconds == null && !it.isRestRound } -> "Strength"
        else -> "Interval"
    }
    val bg = when (label) {
        "Interval" -> Color(0xFFEDE7F6)
        "Strength" -> Color(0xFFE3F2FD)
        else -> Color(0xFFFFF8E1)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private fun workoutMeta(rounds: List<RoundEntity>): String {
    val workRounds = rounds.filter { !it.isRestRound }
    val totalSeconds = rounds.sumOf { it.durationSeconds ?: 0 }
    val durationText = if (totalSeconds > 0) {
        val mins = totalSeconds / 60
        val secs = totalSeconds % 60
        if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    } else {
        "No timed rounds"
    }
    return "${workRounds.size} round${if (workRounds.size != 1) "s" else ""} · $durationText"
}

@Composable
fun ScheduledTab(
    onEditWorkout: (workoutId: String) -> Unit,
    onStartWorkout: (workoutId: String) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Scheduled — coming soon", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun QuickTimerTab(onStart: (TimerConfig) -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Quick Timer — coming soon", style = MaterialTheme.typography.bodyMedium)
    }
}
