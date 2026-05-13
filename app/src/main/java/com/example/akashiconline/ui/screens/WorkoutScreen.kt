package com.example.akashiconline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.akashiconline.R
import com.example.akashiconline.data.TimerConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onBack: () -> Unit,
    onCreateWorkout: () -> Unit,
    onEditWorkout: (workoutId: String) -> Unit,
    onStartWorkout: (workoutId: String) -> Unit,
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

@Composable
fun MyWorkoutsTab(
    onCreateWorkout: () -> Unit,
    onEditWorkout: (workoutId: String) -> Unit,
    onStartWorkout: (workoutId: String) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("My Workouts — coming soon", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ScheduledTab(
    onEditWorkout: (workoutId: String) -> Unit,
    onStartWorkout: (workoutId: String) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Scheduled — coming soon", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun QuickTimerTab(onStart: (TimerConfig) -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Quick Timer — coming soon", style = MaterialTheme.typography.bodyMedium)
    }
}
