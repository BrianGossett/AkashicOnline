package com.example.akashiconline.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.ui.workout.CreateWorkoutViewModel
import com.example.akashiconline.ui.workout.RoundDraft
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    workoutId: String?,
    onBack: () -> Unit,
    viewModel: CreateWorkoutViewModel = viewModel(
        factory = CreateWorkoutViewModel.Factory(workoutId)
    ),
) {
    LaunchedEffect(Unit) {
        viewModel.saved.collect { onBack() }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = viewModel.scheduledDateMillis,
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.scheduledDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isNewWorkout) "New Workout" else "Edit Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { if (it.length <= 50) viewModel.name = it },
                    label = { Text("Workout name") },
                    isError = viewModel.nameError && viewModel.name.isNotEmpty(),
                    supportingText = if (viewModel.nameError && viewModel.name.isNotEmpty()) {
                        { Text("Name is required") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = "Rounds",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            itemsIndexed(viewModel.rounds, key = { _, draft -> draft.id }) { index, draft ->
                RoundRowItem(
                    draft = draft,
                    index = index,
                    total = viewModel.rounds.size,
                    onUpdate = { viewModel.updateRound(index, it) },
                    onDelete = { viewModel.removeRound(index) },
                    onMoveUp = { viewModel.moveRoundUp(index) },
                    onMoveDown = { viewModel.moveRoundDown(index) },
                )
            }

            item {
                TextButton(
                    onClick = { viewModel.addRound() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add round")
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Schedule this workout",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = viewModel.scheduleEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.scheduleEnabled = enabled
                            if (!enabled) {
                                viewModel.scheduledDateMillis = null
                                viewModel.repeatRule = null
                                viewModel.reminderMinutesBefore = null
                            }
                        },
                    )
                }
            }

            if (viewModel.scheduleEnabled) {
                item {
                    val dateLabel = viewModel.scheduledDateMillis?.let { millis ->
                        Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy"))
                    } ?: "Select date"

                    OutlinedTextField(
                        value = dateLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    painterResource(R.drawable.ic_calendar),
                                    contentDescription = "Pick date",
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                }

                item {
                    SimpleDropdown(
                        label = "Repeat",
                        options = listOf("None", "Daily", "Weekly"),
                        selected = when (viewModel.repeatRule) {
                            "DAILY" -> "Daily"
                            "WEEKLY" -> "Weekly"
                            else -> "None"
                        },
                        onSelect = { choice ->
                            viewModel.repeatRule = when (choice) {
                                "Daily" -> "DAILY"
                                "Weekly" -> "WEEKLY"
                                else -> null
                            }
                        },
                    )
                }

                item {
                    SimpleDropdown(
                        label = "Reminder",
                        options = listOf("None", "15 min", "30 min", "1 hour"),
                        selected = when (viewModel.reminderMinutesBefore) {
                            15 -> "15 min"
                            30 -> "30 min"
                            60 -> "1 hour"
                            else -> "None"
                        },
                        onSelect = { choice ->
                            viewModel.reminderMinutesBefore = when (choice) {
                                "15 min" -> 15
                                "30 min" -> 30
                                "1 hour" -> 60
                                else -> null
                            }
                        },
                    )
                }
            }

            item {
                Button(
                    onClick = { viewModel.save() },
                    enabled = viewModel.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                ) {
                    Text("Save workout")
                }
            }
        }
    }
}

@Composable
private fun RoundRowItem(
    draft: RoundDraft,
    index: Int,
    total: Int,
    onUpdate: (RoundDraft) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(end = 4.dp)) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_chevron_left),
                        contentDescription = "Move up",
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(90f),
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = index < total - 1,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_chevron_right),
                        contentDescription = "Move down",
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(90f),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { onUpdate(draft.copy(name = it)) },
                    label = { Text("Round name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = draft.durationInput,
                    onValueChange = { onUpdate(draft.copy(durationInput = it)) },
                    label = { Text("Duration (sec, leave blank for no timer)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draft.weightInput,
                        onValueChange = { onUpdate(draft.copy(weightInput = it)) },
                        label = { Text("Weight (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = draft.repsInput,
                        onValueChange = { onUpdate(draft.copy(repsInput = it)) },
                        label = { Text("Reps (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = "Delete round",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
