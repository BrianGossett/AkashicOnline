package com.example.akashiconline.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.ui.programs.BuildProgramViewModel
import com.example.akashiconline.ui.programs.WeekDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildProgramScreen(
    onBack: () -> Unit,
    onSaved: (programId: String) -> Unit,
    editProgramId: String? = null,
    viewModel: BuildProgramViewModel = viewModel(
        factory = BuildProgramViewModel.Factory(editProgramId)
    ),
) {
    LaunchedEffect(Unit) {
        viewModel.savedEvent.collect { programId -> onSaved(programId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.editProgramId != null) "Edit Program" else "New Program") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.nameInput,
                onValueChange = { viewModel.nameInput = it },
                label = { Text("Program name") },
                isError = viewModel.nameInput.isBlank() && viewModel.nameInput.isNotEmpty(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = viewModel.descriptionInput,
                onValueChange = { viewModel.descriptionInput = it },
                label = { Text("Description (optional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Weeks",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                if (viewModel.weeks.isEmpty()) {
                    Text(
                        "At least 1 week required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            viewModel.weeks.forEach { week ->
                WeekRow(
                    week = week,
                    onPhaseChange = { viewModel.updateWeekPhase(week.id, it) },
                    onPhaseDescriptionChange = { viewModel.updateWeekPhaseDescription(week.id, it) },
                    onDelete = { viewModel.removeWeek(week.id) },
                )
            }

            FilledTonalButton(
                onClick = { viewModel.addWeek() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add Week")
            }

            Button(
                onClick = { viewModel.save() },
                enabled = viewModel.isValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Program")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WeekRow(
    week: WeekDraft,
    onPhaseChange: (String) -> Unit,
    onPhaseDescriptionChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Week ${week.weekNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        painterResource(R.drawable.ic_delete),
                        contentDescription = "Remove Week ${week.weekNumber}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            OutlinedTextField(
                value = week.phase,
                onValueChange = onPhaseChange,
                label = { Text("Phase (e.g. walk/run foundation)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp),
            )

            OutlinedTextField(
                value = week.phaseDescription,
                onValueChange = onPhaseDescriptionChange,
                label = { Text("Phase description (optional)") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp),
            )
        }
    }
}
