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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.akashiconline.R
import com.example.akashiconline.data.TimerConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    onBack: () -> Unit,
    onStart: (TimerConfig) -> Unit = {},
    onLoadPreset: () -> Unit = {},
) {
    var workInput by rememberSaveable { mutableStateOf("30") }
    var restInput by rememberSaveable { mutableStateOf("10") }
    var roundsInput by rememberSaveable { mutableStateOf("8") }

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
                onClick = onLoadPreset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Load Preset")
            }
        }
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
