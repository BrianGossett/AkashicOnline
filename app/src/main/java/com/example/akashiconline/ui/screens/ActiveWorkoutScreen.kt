package com.example.akashiconline.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.ui.timer.Status
import com.example.akashiconline.ui.workout.ActiveWorkoutUiState
import com.example.akashiconline.ui.workout.ActiveWorkoutViewModel
import kotlin.math.min

@Composable
fun ActiveWorkoutScreen(
    workoutId: String,
    onDone: () -> Unit,
    viewModel: ActiveWorkoutViewModel = viewModel(
        factory = ActiveWorkoutViewModel.Factory(workoutId)
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.status) {
        if (state.status == Status.STOPPED) onDone()
    }

    KeepAwake(isRunning = state.status == Status.RUNNING)

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Stop workout?") },
            text = { Text("Progress will be saved as an incomplete session.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.stopEarly() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Stop") }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) { Text("Keep going") }
            },
        )
    }

    Scaffold { innerPadding ->
        when {
            state.isLoading -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) { CircularProgressIndicator() }

            state.status == Status.COMPLETE -> WorkoutCompleteContent(
                state = state,
                onDone = onDone,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )

            else -> ActiveWorkoutContent(
                state = state,
                onCompleteRound = viewModel::completeRound,
                onSkip = viewModel::skipRound,
                onBack = viewModel::goBack,
                onPause = viewModel::pause,
                onResume = viewModel::resume,
                onStop = { showStopDialog = true },
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
    }
}

@Composable
private fun KeepAwake(isRunning: Boolean) {
    val context = LocalContext.current
    DisposableEffect(isRunning) {
        val window = (context as? Activity)?.window
        if (isRunning) window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

@Composable
private fun ActiveWorkoutContent(
    state: ActiveWorkoutUiState,
    onCompleteRound: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentRound = state.rounds.getOrNull(state.currentRoundIndex)
    val nextRound = state.rounds.getOrNull(state.currentRoundIndex + 1)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        // Workout name header
        Text(
            text = state.workoutName,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(4.dp))

        // Current round name
        Text(
            text = currentRound?.name ?: "",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        // Dual-ring timer
        DualRingTimer(
            outerProgress = if (state.rounds.isNotEmpty())
                state.totalRoundsCompleted.toFloat() / state.rounds.size
            else 0f,
            innerProgress = if (currentRound?.durationSeconds != null && currentRound.durationSeconds > 0)
                min(
                    state.currentRoundElapsedSeconds.toFloat() / currentRound.durationSeconds,
                    1f,
                )
            else 0f,
            elapsedSeconds = state.currentRoundElapsedSeconds,
            modifier = Modifier.size(200.dp),
        )

        Spacer(Modifier.height(24.dp))

        // Complete round button
        Button(
            onClick = onCompleteRound,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("✓ Complete round")
        }

        Spacer(Modifier.height(16.dp))

        // Round info section
        RoundInfoSection(
            currentRound = currentRound,
            nextRound = nextRound,
            currentIndex = state.currentRoundIndex,
            totalRounds = state.rounds.size,
        )

        Spacer(Modifier.weight(1f))

        // Control row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            OutlinedButton(
                onClick = onBack,
                enabled = state.currentRoundIndex > 0,
                modifier = Modifier.weight(1f),
            ) {
                Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = "Back")
            }

            FilledTonalButton(
                onClick = if (state.status == Status.PAUSED) onResume else onPause,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (state.status == Status.PAUSED) "Resume" else "Pause")
            }

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f),
            ) {
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "Skip")
            }

            OutlinedButton(
                onClick = onStop,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                modifier = Modifier.weight(1f),
            ) {
                Text("Stop")
            }
        }
    }
}

@Composable
private fun DualRingTimer(
    outerProgress: Float,
    innerProgress: Float,
    elapsedSeconds: Int,
    modifier: Modifier = Modifier,
) {
    val outerColor = Color(0xFFAFA9EC)
    val innerColor = Color(0xFF3C3489)
    val trackColor = outerColor.copy(alpha = 0.2f)
    val innerTrackColor = innerColor.copy(alpha = 0.15f)

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.width * 0.08f
            val innerStrokeWidth = size.width * 0.07f
            val padding = strokeWidth / 2f
            val innerPadding = padding + strokeWidth + innerStrokeWidth / 2f + 6.dp.toPx()

            // Outer track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = Size(size.width - padding * 2, size.height - padding * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            // Outer progress arc (total workout)
            if (outerProgress > 0f) {
                drawArc(
                    color = outerColor,
                    startAngle = -90f,
                    sweepAngle = 360f * outerProgress,
                    useCenter = false,
                    topLeft = Offset(padding, padding),
                    size = Size(size.width - padding * 2, size.height - padding * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }

            // Inner track
            drawArc(
                color = innerTrackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(innerPadding, innerPadding),
                size = Size(
                    size.width - innerPadding * 2,
                    size.height - innerPadding * 2,
                ),
                style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round),
            )
            // Inner progress arc (current round)
            if (innerProgress > 0f) {
                drawArc(
                    color = innerColor,
                    startAngle = -90f,
                    sweepAngle = 360f * innerProgress,
                    useCenter = false,
                    topLeft = Offset(innerPadding, innerPadding),
                    size = Size(
                        size.width - innerPadding * 2,
                        size.height - innerPadding * 2,
                    ),
                    style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatMmSs(elapsedSeconds),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
            )
            Text(
                text = "elapsed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RoundInfoSection(
    currentRound: com.example.akashiconline.data.RoundEntity?,
    nextRound: com.example.akashiconline.data.RoundEntity?,
    currentIndex: Int,
    totalRounds: Int,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Round ${currentIndex + 1} of $totalRounds",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (currentRound != null) {
            val meta = buildString {
                currentRound.durationSeconds?.let { append("${it}s") }
                currentRound.weightKg?.let { if (isNotEmpty()) append(" · "); append("${it}kg") }
                currentRound.reps?.let { if (isNotEmpty()) append(" · "); append("${it} reps") }
            }
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (nextRound != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Next: ${nextRound.name}${nextRound.durationSeconds?.let { " (${it}s)" } ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WorkoutCompleteContent(
    state: ActiveWorkoutUiState,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 32.dp),
    ) {
        Text(
            text = "Workout Complete",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Total time: ${formatElapsed(state.totalElapsedSeconds)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${state.totalRoundsCompleted} of ${state.rounds.size} rounds completed",
            style = MaterialTheme.typography.bodyMedium,
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

private fun formatMmSs(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}

private fun formatElapsed(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m}m ${s}s"
}
