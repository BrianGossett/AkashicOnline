package com.example.akashiconline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.data.RoundEntity
import com.example.akashiconline.data.TimerConfig
import com.example.akashiconline.data.WorkoutEntity
import com.example.akashiconline.ui.timer.TimerConfigViewModel
import com.example.akashiconline.ui.workout.WorkoutViewModel
import com.example.akashiconline.ui.workout.WorkoutWithRounds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onBack: () -> Unit,
    onCreateWorkout: () -> Unit,
    onEditWorkout: (workoutId: String) -> Unit,
    onStartWorkout: (workoutId: String) -> Unit,
    onScheduleWorkout: (workoutId: String) -> Unit,
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
                    onScheduleWorkout = onScheduleWorkout,
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
    onScheduleWorkout: (workoutId: String) -> Unit,
    viewModel: WorkoutViewModel = viewModel(),
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var selectedWorkout by remember { mutableStateOf<WorkoutWithRounds?>(null) }

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

    selectedWorkout?.let { item ->
        WorkoutActionSheet(
            item = item,
            onDismiss = { selectedWorkout = null },
            onStart = {
                selectedWorkout = null
                onStartWorkout(item.workout.id)
            },
            onSchedule = {
                selectedWorkout = null
                onScheduleWorkout(item.workout.id)
            },
            onEdit = {
                selectedWorkout = null
                onEditWorkout(item.workout.id)
            },
            onDelete = {
                selectedWorkout = null
                pendingDeleteId = item.workout.id
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
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(workouts, key = { it.workout.id }) { item ->
                    WorkoutCard(
                        item = item,
                        onSelect = { selectedWorkout = item },
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
private fun WorkoutActionSheet(
    item: WorkoutWithRounds,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onSchedule: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(item.workout.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = workoutMeta(item.rounds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider()
            SheetActionRow(label = "▶  Start now", onClick = onStart)
            SheetActionRow(label = "📅  Schedule", onClick = onSchedule)
            SheetActionRow(label = "✏️  Edit workout", onClick = onEdit)
            SheetActionRow(
                label = "🗑  Delete",
                color = MaterialTheme.colorScheme.error,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun SheetActionRow(
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutCard(
    item: WorkoutWithRounds,
    onSelect: () -> Unit,
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
            onClick = onSelect,
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
                Icon(
                    painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
    viewModel: WorkoutViewModel = viewModel(),
) {
    val scheduled by viewModel.scheduled.collectAsStateWithLifecycle()
    val today = remember { java.time.LocalDate.now() }

    if (scheduled.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
        ) {
            Text(
                text = "No scheduled workouts — schedule one when creating or editing a workout",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        return
    }

    // Group by calendar date (epoch millis → LocalDate)
    val grouped = scheduled.groupBy { item ->
        java.time.Instant.ofEpochMilli(item.workout.scheduledDate!!)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }.entries.sortedBy { it.key }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        grouped.forEach { (date, items) ->
            item(key = date.toString()) {
                Text(
                    text = date.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d")),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
            }
            items(items, key = { it.workout.id }) { item ->
                val isMissed = date.isBefore(today)
                ScheduledWorkoutCard(
                    item = item,
                    isMissed = isMissed,
                    onStart = { onStartWorkout(item.workout.id) },
                )
            }
        }
    }
}

@Composable
private fun ScheduledWorkoutCard(
    item: WorkoutWithRounds,
    isMissed: Boolean,
    onStart: () -> Unit,
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.workout.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isMissed) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = workoutMeta(item.rounds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isMissed) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "missed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun QuickTimerTab(
    onStart: (TimerConfig) -> Unit,
    configViewModel: TimerConfigViewModel = viewModel(),
) {
    var workInput by rememberSaveable { mutableStateOf("20") }
    var restInput by rememberSaveable { mutableStateOf("10") }
    var roundsInput by rememberSaveable { mutableStateOf("8") }

    val presets by configViewModel.presets.collectAsStateWithLifecycle()

    val workSeconds = workInput.toIntOrNull()
    val restSeconds = restInput.toIntOrNull()
    val rounds = roundsInput.toIntOrNull()

    val workError = workSeconds == null || workSeconds <= 0
    val restError = restSeconds == null || restSeconds <= 0
    val roundsError = rounds == null || rounds <= 0
    val isValid = !workError && !restError && !roundsError

    val summaryText = if (isValid) {
        "Total: ~${quickFormatDuration((workSeconds!! + restSeconds!!) * rounds!!)}"
    } else {
        "Total: —"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        OutlinedTextField(
            value = workInput,
            onValueChange = { workInput = it },
            label = { Text("Work (seconds)") },
            isError = workError,
            supportingText = if (workError) {
                { Text("Enter a number greater than 0") }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = restInput,
            onValueChange = { restInput = it },
            label = { Text("Rest (seconds)") },
            isError = restError,
            supportingText = if (restError) {
                { Text("Enter a number greater than 0") }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = roundsInput,
            onValueChange = { roundsInput = it },
            label = { Text("Rounds") },
            isError = roundsError,
            supportingText = if (roundsError) {
                { Text("Enter a number greater than 0") }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = {
                if (isValid) onStart(TimerConfig(workSeconds!!, restSeconds!!, rounds!!))
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("▶ Start")
        }

        if (presets.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Saved presets",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(presets, key = { it.id }) { preset ->
                    SuggestionChip(
                        onClick = {
                            workInput = preset.workSeconds.toString()
                            restInput = preset.restSeconds.toString()
                            roundsInput = preset.rounds.toString()
                        },
                        label = { Text(preset.name) },
                    )
                }
            }
        }
    }
}

private fun quickFormatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes > 0 && seconds > 0 -> "$minutes min $seconds sec"
        minutes > 0 -> "$minutes min"
        else -> "$seconds sec"
    }
}
