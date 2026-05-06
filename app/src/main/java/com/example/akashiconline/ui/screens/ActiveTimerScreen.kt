package com.example.akashiconline.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.akashiconline.ui.timer.Status
import com.example.akashiconline.ui.timer.TimerUiState
import com.example.akashiconline.ui.timer.TimerViewModel

@Composable
fun ActiveTimerScreen(
    viewModel: TimerViewModel,
    onDone: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        when (state.status) {
            Status.COMPLETE -> CompletionContent(
                totalElapsedSeconds = state.totalElapsedSeconds,
                onDone = onDone,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            else -> RunningContent(
                state = state,
                onPauseResume = viewModel::togglePause,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun RunningContent(
    state: TimerUiState,
    onPauseResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = state.phase.name,
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
            text = "Round ${state.currentRound} of ${state.totalRounds}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onPauseResume,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.status == Status.PAUSED) "Resume" else "Pause")
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
