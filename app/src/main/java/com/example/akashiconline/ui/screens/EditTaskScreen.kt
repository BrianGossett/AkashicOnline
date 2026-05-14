package com.example.akashiconline.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.ui.tasks.EditTaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: String?,
    onBack: () -> Unit,
    viewModel: EditTaskViewModel = viewModel(
        factory = EditTaskViewModel.Factory(taskId),
    ),
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = viewModel.dueDateEpochDay?.let { it * 86_400_000L },
    )

    LaunchedEffect(Unit) {
        viewModel.saved.collect { onBack() }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dueDateEpochDay = datePickerState.selectedDateMillis
                        ?.let { it / 86_400_000L }
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
                title = { Text(if (taskId == null) "New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(4.dp))

            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { if (it.length <= 100) viewModel.name = it },
                label = { Text("Task name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = viewModel.details,
                onValueChange = { viewModel.details = it },
                label = { Text("Details (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )

            val dateLabel = viewModel.dueDateEpochDay?.let { epochDay ->
                LocalDate.ofEpochDay(epochDay)
                    .format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy"))
            } ?: "No due date"

            OutlinedTextField(
                value = dateLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Due date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            painterResource(R.drawable.ic_calendar),
                            contentDescription = "Pick date",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (viewModel.dueDateEpochDay != null) {
                TextButton(onClick = { viewModel.dueDateEpochDay = null }) {
                    Text("Clear date")
                }
            }

            Button(
                onClick = { viewModel.save() },
                enabled = viewModel.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
            ) {
                Text("Save")
            }
        }
    }
}
