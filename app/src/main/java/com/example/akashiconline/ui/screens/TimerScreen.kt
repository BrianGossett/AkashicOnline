package com.example.akashiconline.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.akashiconline.data.PresetEntity
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.data.TimerConfig
import com.example.akashiconline.ui.timer.TimerConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onBack: () -> Unit,
    onStart: (TimerConfig) -> Unit = {},
    onLoadPreset: () -> Unit = {},
    presetToLoad: PresetEntity? = null,
    onPresetConsumed: () -> Unit = {},
    configViewModel: TimerConfigViewModel = viewModel(),
) {
    var workInput by rememberSaveable { mutableStateOf("30") }
    var restInput by rememberSaveable { mutableStateOf("10") }
    var roundsInput by rememberSaveable { mutableStateOf("8") }

    LaunchedEffect(presetToLoad) {
        if (presetToLoad != null) {
            workInput = presetToLoad.workSeconds.toString()
            restInput = presetToLoad.restSeconds.toString()
            roundsInput = presetToLoad.rounds.toString()
            onPresetConsumed()
        }
    }

    val workSeconds = workInput.toIntOrNull()
    val restSeconds = restInput.toIntOrNull()
    val rounds = roundsInput.toIntOrNull()

    val workError = workSeconds == null || workSeconds <= 0
    val restError = restSeconds == null || restSeconds <= 0
    val roundsError = rounds == null || rounds <= 0
    val isValid = !workError && !restError && !roundsError

    val summaryText = if (isValid) {
        "Total workout: ~${formatWorkoutDuration((workSeconds!! + restSeconds!!) * rounds!!)}"
    } else {
        "Total workout: —"
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var hasInteracted by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        configViewModel.savedEvent.collect { name ->
            snackbarHostState.showSnackbar("\"$name\" saved as preset")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                value = workInput,
                onValueChange = { workInput = it },
                label = { Text("Work duration (seconds)") },
                isError = workError,
                supportingText = if (workError) {
                    { Text("Enter a number greater than 0") }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = restInput,
                onValueChange = { restInput = it },
                label = { Text("Rest duration (seconds)") },
                isError = restError,
                supportingText = if (restError) {
                    { Text("Enter a number greater than 0") }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start")
            }

            FilledTonalButton(
                onClick = { showSaveDialog = true },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save as Preset")
            }

            FilledTonalButton(
                onClick = onLoadPreset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Load Preset")
            }
        }
    }

    if (showSaveDialog) {
        val nameIsError = hasInteracted && presetNameInput.trim().isEmpty()

        AlertDialog(
            onDismissRequest = {
                showSaveDialog = false
                presetNameInput = ""
                hasInteracted = false
            },
            title = { Text("Save as Preset") },
            text = {
                OutlinedTextField(
                    value = presetNameInput,
                    onValueChange = {
                        if (it.length <= 30) {
                            presetNameInput = it
                            hasInteracted = true
                        }
                    },
                    label = { Text("Preset name") },
                    isError = nameIsError,
                    supportingText = if (nameIsError) {
                        { Text("Name is required") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = presetNameInput.trim()
                        if (trimmed.isNotEmpty() && isValid) {
                            configViewModel.savePreset(
                                trimmed,
                                TimerConfig(workSeconds!!, restSeconds!!, rounds!!)
                            )
                            showSaveDialog = false
                            presetNameInput = ""
                            hasInteracted = false
                        }
                    },
                    enabled = presetNameInput.isNotBlank(),
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveDialog = false
                        presetNameInput = ""
                        hasInteracted = false
                    }
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

private fun formatWorkoutDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes > 0 && seconds > 0 -> "$minutes min $seconds sec"
        minutes > 0 -> "$minutes min"
        else -> "$seconds sec"
    }
}
