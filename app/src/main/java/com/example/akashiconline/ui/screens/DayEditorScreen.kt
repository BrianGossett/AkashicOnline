package com.example.akashiconline.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState
import com.example.akashiconline.R
import com.example.akashiconline.ui.programs.DayEditorViewModel
import com.example.akashiconline.ui.programs.StepDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEditorScreen(
    dayId: String?,
    weekId: String?,
    dayNumber: Int,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: DayEditorViewModel = viewModel(
        factory = DayEditorViewModel.Factory(dayId, weekId, dayNumber)
    ),
) {
    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { onSaved() }
    }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyColumnState(lazyListState) { from, to ->
        val fromIdx = from.index - 1 // 1 header item (type selector)
        val toIdx = to.index - 1
        if (fromIdx >= 0 && toIdx in 0 until viewModel.steps.size) {
            viewModel.moveStep(fromIdx, toIdx)
        }
    }

    val title = if (viewModel.editDayId != null) "Edit Day $dayNumber" else "Day $dayNumber"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.save() },
                        modifier = Modifier.padding(end = 8.dp),
                    ) { Text("Save") }
                },
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            item {
                DayTypeSelector(
                    selected = viewModel.dayType,
                    onSelect = { viewModel.dayType = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                )
            }

            items(viewModel.steps, key = { it.id }) { step ->
                ReorderableItem(reorderState, key = step.id) { isDragging ->
                    StepRow(
                        step = step,
                        isDragging = isDragging,
                        dragHandleModifier = Modifier.draggableHandle(),
                        onNameChange = { viewModel.updateName(step.id, it) },
                        onMinutesChange = { viewModel.updateMinutes(step.id, it) },
                        onSecondsChange = { viewModel.updateSeconds(step.id, it) },
                        onToggleRest = { viewModel.toggleRestStep(step.id) },
                        onDelete = { viewModel.removeStep(step.id) },
                    )
                }
            }

            item {
                FilledTonalButton(
                    onClick = { viewModel.addStep() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                ) { Text("Add Step") }
            }

            if (viewModel.steps.isNotEmpty()) {
                item {
                    SessionSummary(
                        steps = viewModel.steps,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            item { Spacer(Modifier.padding(bottom = 16.dp)) }
        }
    }
}

@Composable
private fun DayTypeSelector(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val types = listOf("BASE", "EASY", "PROGRESSION")
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selected == type,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index, types.size),
                label = { Text(type.take(4), style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}

@Composable
private fun StepRow(
    step: StepDraft,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
    onNameChange: (String) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onToggleRest: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        ) {
            // Drag handle
            Icon(
                painterResource(R.drawable.ic_drag_handle),
                contentDescription = "Reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = dragHandleModifier,
            )

            Spacer(Modifier.width(4.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                // Work/Rest chip + delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = !step.isRestStep,
                        onClick = onToggleRest,
                        label = { Text(if (step.isRestStep) "Rest" else "Work") },
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDelete) {
                        Icon(
                            painterResource(R.drawable.ic_delete),
                            contentDescription = "Delete step",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Name field
                OutlinedTextField(
                    value = step.name,
                    onValueChange = onNameChange,
                    label = { Text("Step name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Duration: minutes + seconds
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OutlinedTextField(
                        value = if (step.minutes == 0) "" else step.minutes.toString(),
                        onValueChange = { onMinutesChange(it.toIntOrNull() ?: 0) },
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(72.dp),
                    )
                    Text("m", style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(
                        value = step.seconds.toString(),
                        onValueChange = { onSecondsChange(it.toIntOrNull() ?: 0) },
                        label = { Text("Sec") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(72.dp),
                    )
                    Text("s", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun SessionSummary(steps: List<StepDraft>, modifier: Modifier = Modifier) {
    val totalSecs = steps.sumOf { it.totalSeconds }
    val workSecs = steps.filter { !it.isRestStep }.sumOf { it.totalSeconds }
    val pattern = detectPattern(steps)

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Summary", style = MaterialTheme.typography.titleSmall)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            if (pattern != null) {
                Text(
                    pattern,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LabeledStat("Total", formatDuration(totalSecs))
                LabeledStat("Work", formatDuration(workSecs))
                LabeledStat("Rest", formatDuration(totalSecs - workSecs))
            }
        }
    }
}

@Composable
private fun LabeledStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun detectPattern(steps: List<StepDraft>): String? {
    if (steps.size < 2 || steps.size % 2 != 0) return null
    val work = steps[0]
    val rest = steps[1]
    if (work.isRestStep || !rest.isRestStep) return null
    val isPattern = steps.indices.all { i ->
        val expected = if (i % 2 == 0) work else rest
        steps[i].name == expected.name &&
            steps[i].totalSeconds == expected.totalSeconds &&
            steps[i].isRestStep == expected.isRestStep
    }
    if (!isPattern) return null
    val count = steps.size / 2
    val totalSecs = steps.sumOf { it.totalSeconds }
    return "${durShort(work.totalSeconds)} ${work.name.ifEmpty { "Work" }} / " +
        "${durShort(rest.totalSeconds)} ${rest.name.ifEmpty { "Rest" }} × $count = ~${formatDuration(totalSecs)}"
}

private fun durShort(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return when {
        m > 0 && s > 0 -> "${m}m${s}s"
        m > 0 -> "${m}m"
        else -> "${s}s"
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return when {
        m > 0 && s > 0 -> "${m}m ${s}s"
        m > 0 -> "${m}m"
        else -> "${s}s"
    }
}
