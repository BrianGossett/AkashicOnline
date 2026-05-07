package com.example.akashiconline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.data.DayDetail
import com.example.akashiconline.data.SessionLogEntity
import com.example.akashiconline.data.StepEntity
import com.example.akashiconline.data.WeekDetail
import com.example.akashiconline.data.WeekEntity
import com.example.akashiconline.ui.programs.ProgramDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailScreen(
    programId: String,
    onBack: () -> Unit,
    onEditProgram: () -> Unit,
    onEditDay: (dayId: String) -> Unit,
    onAddDay: (weekId: String, dayNumber: Int) -> Unit,
    onRunDay: (dayId: String) -> Unit,
    viewModel: ProgramDetailViewModel = viewModel(
        factory = ProgramDetailViewModel.Factory(programId)
    ),
) {
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val sessionLogs by viewModel.sessionLogs.collectAsStateWithLifecycle()
    val completedDayIds = remember(sessionLogs) { sessionLogs.mapNotNull { it.dayId }.toSet() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.program?.name ?: "Program") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditProgram) {
                        Icon(painterResource(R.drawable.ic_edit), contentDescription = "Edit program")
                    }
                },
            )
        }
    ) { innerPadding ->
        if (detail == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val d = detail!!
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (d.program.description.isNotBlank()) {
                item {
                    Text(
                        text = d.program.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }

            items(d.weeks, key = { it.week.id }) { weekDetail ->
                WeekSection(
                    weekDetail = weekDetail,
                    completedDayIds = completedDayIds,
                    onEditDay = onEditDay,
                    onAddDay = { dayNumber -> onAddDay(weekDetail.week.id, dayNumber) },
                    onRunDay = onRunDay,
                )
            }

            if (sessionLogs.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    Text(
                        "Recent Sessions",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
                items(sessionLogs, key = { it.id }) { log ->
                    SessionLogRow(log)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun WeekSection(
    weekDetail: WeekDetail,
    completedDayIds: Set<String>,
    onEditDay: (dayId: String) -> Unit,
    onAddDay: (dayNumber: Int) -> Unit,
    onRunDay: (dayId: String) -> Unit,
) {
    Column {
        WeekPhaseHeader(weekDetail.week)
        DayGrid(
            weekDetail = weekDetail,
            completedDayIds = completedDayIds,
            onEditDay = onEditDay,
            onAddDay = onAddDay,
            onRunDay = onRunDay,
        )
    }
}

@Composable
private fun WeekPhaseHeader(week: WeekEntity) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = week.label,
                style = MaterialTheme.typography.titleSmall,
            )
            if (week.phase.isNotBlank()) {
                Text(
                    text = " · ${week.phase}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (week.phaseDescription.isNotBlank()) {
            Text(
                text = week.phaseDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 18.dp, top = 2.dp),
            )
        }
    }
}

@Composable
private fun DayGrid(
    weekDetail: WeekDetail,
    completedDayIds: Set<String>,
    onEditDay: (dayId: String) -> Unit,
    onAddDay: (dayNumber: Int) -> Unit,
    onRunDay: (dayId: String) -> Unit,
) {
    val dayMap = weekDetail.days.associateBy { it.day.dayNumber }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        for (dayNum in 1..3) {
            val dayDetail = dayMap[dayNum]
            if (dayDetail != null) {
                FilledDayCell(
                    dayDetail = dayDetail,
                    hasCompletedLog = dayDetail.day.id in completedDayIds,
                    onEdit = { onEditDay(dayDetail.day.id) },
                    onRun = { onRunDay(dayDetail.day.id) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            } else {
                EmptyDayCell(
                    dayNumber = dayNum,
                    onClick = { onAddDay(dayNum) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun FilledDayCell(
    dayDetail: DayDetail,
    hasCompletedLog: Boolean,
    onEdit: () -> Unit,
    onRun: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.clickable(onClick = onEdit),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeBadge(dayDetail.day.type, modifier = Modifier.weight(1f))
                if (hasCompletedLog) {
                    Icon(
                        painterResource(R.drawable.ic_check),
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit ${dayDetail.day.label}",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = dayDetail.day.label,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
            )

            if (dayDetail.steps.isNotEmpty()) {
                Text(
                    text = stepSummary(dayDetail.steps),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = onRun,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.End),
            ) {
                Icon(
                    painterResource(R.drawable.ic_play),
                    contentDescription = "Run ${dayDetail.day.label}",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun EmptyDayCell(
    dayNumber: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().padding(8.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Day $dayNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "+",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TypeBadge(type: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (type.uppercase()) {
        "BASE" -> MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer
        "EASY" -> MaterialTheme.colorScheme.secondaryContainer to
                MaterialTheme.colorScheme.onSecondaryContainer
        "PROGRESSION" -> MaterialTheme.colorScheme.tertiaryContainer to
                MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to
                MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
    ) {
        Text(
            text = type.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            maxLines = 1,
        )
    }
}

@Composable
private fun SessionLogRow(log: SessionLogEntity) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        if (log.wasCompleted) {
            Icon(
                painterResource(R.drawable.ic_check),
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .padding(3.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant, CircleShape),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            formatLogDate(log.completedAt),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(
            formatLogElapsed(log.totalElapsedSeconds),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatLogDate(epochMillis: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(epochMillis))

private fun formatLogElapsed(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}

private fun stepSummary(steps: List<StepEntity>): String {
    val parts = steps.take(3).map { "${formatDur(it.durationSeconds)} ${it.name}" }
    val suffix = if (steps.size > 3) " +${steps.size - 3}" else ""
    return parts.joinToString(" / ") + suffix
}

private fun formatDur(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return when {
        m > 0 && s > 0 -> "${m}m${s}s"
        m > 0 -> "${m}m"
        else -> "${s}s"
    }
}
