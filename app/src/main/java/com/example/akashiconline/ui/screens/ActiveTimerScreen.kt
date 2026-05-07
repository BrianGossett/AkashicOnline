package com.example.akashiconline.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.akashiconline.ui.timer.Status
import com.example.akashiconline.ui.timer.TimerUiState
import com.example.akashiconline.ui.timer.TimerViewModel

@Composable
private fun KeepScreenOn(isRunning: Boolean) {
    val context = LocalContext.current
    DisposableEffect(isRunning) {
        val window = (context as? Activity)?.window
        if (isRunning) window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

@Composable
fun ActiveTimerScreen(
    viewModel: TimerViewModel,
    onDone: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }

    KeepScreenOn(isRunning = state.status == Status.RUNNING)

    Scaffold { innerPadding ->
        when {
            state.isLoading -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) { CircularProgressIndicator() }

            state.status == Status.COMPLETE -> CompletionContent(
                totalElapsedSeconds = state.totalElapsedSeconds,
                onDone = onDone,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> RunningContent(
                state = state,
                onPause = viewModel::pause,
                onResume = viewModel::resume,
                onStop = { showStopDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("End workout early?") },
            text = { Text("Your progress will not be saved.") },
            confirmButton = {
                TextButton(onClick = onDone) { Text("End") }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) { Text("Keep going") }
            },
        )
    }
}

@Composable
private fun RunningContent(
    state: TimerUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = state.stepName,
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = formatCountdown(state.secondsRemaining),
            style = MaterialTheme.typography.displayLarge,
        )

        Spacer(Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = {
                if (state.totalPhaseSeconds > 0)
                    1f - state.secondsRemaining.toFloat() / state.totalPhaseSeconds
                else 0f
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Step ${state.currentStep} of ${state.totalSteps}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(40.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.status == Status.PAUSED) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f),
                ) { Text("Resume") }
            } else {
                Button(
                    onClick = onPause,
                    modifier = Modifier.weight(1f),
                ) { Text("Pause") }
            }

            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.weight(1f),
            ) { Text("Stop") }
        }
    }
}

@Composable
private fun CompletionContent(
    totalElapsedSeconds: Int,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = "Workout Complete",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Total time: ${formatElapsed(totalElapsedSeconds)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Done")
        }
    }
}

private fun formatCountdown(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}

private fun formatElapsed(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m}m ${s}s"
}
