package com.example.akashiconline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.data.CalendarEventEntity
import com.example.akashiconline.ui.calendar.CalendarViewModel
import com.example.akashiconline.ui.util.formatTimeMinutes
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    viewModel: CalendarViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today = remember { LocalDate.now() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* placeholder — wired in future ticket */ }) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = "Add event")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item {
                MonthHeader(
                    month = state.displayedMonth,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth,
                )
            }
            item { DayOfWeekRow() }
            item {
                CalendarGrid(
                    month = state.displayedMonth,
                    eventsThisMonth = state.eventsThisMonth,
                    selectedDate = state.selectedDate,
                    today = today,
                    onSelectDate = viewModel::selectDate,
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            item {
                Text(
                    text = state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            val allDayEvents = state.selectedDayEvents.filter { it.isAllDay }
            val scheduledEvents = state.selectedDayEvents.filter { !it.isAllDay }

            if (state.selectedDayEvents.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                    ) {
                        Text(
                            "Nothing scheduled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                if (allDayEvents.isNotEmpty()) {
                    item(key = "header_all_day") {
                        Text(
                            text = "All Day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    items(allDayEvents, key = { it.id }) { event ->
                        EventRow(
                            event = event,
                            onToggle = { viewModel.toggleCompleted(event) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
                if (scheduledEvents.isNotEmpty()) {
                    item(key = "header_scheduled") {
                        Text(
                            text = "Scheduled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                    items(scheduledEvents, key = { it.id }) { event ->
                        EventRow(
                            event = event,
                            onToggle = { viewModel.toggleCompleted(event) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                painterResource(R.drawable.ic_chevron_left),
                contentDescription = "Previous month",
            )
        }
        Text(
            text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNext) {
            Icon(
                painterResource(R.drawable.ic_chevron_right),
                contentDescription = "Next month",
            )
        }
    }
}

private val DAY_LABELS = listOf("S", "M", "T", "W", "T", "F", "S")

@Composable
private fun DayOfWeekRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        DAY_LABELS.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    eventsThisMonth: Map<LocalDate, List<CalendarEventEntity>>,
    selectedDate: LocalDate,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
) {
    val firstDay = month.atDay(1)
    // DayOfWeek: MON=1..SUN=7; map to Sun=0..Sat=6
    val offset = firstDay.dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    val rows = (offset + daysInMonth + 6) / 7

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        repeat(rows) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { colIndex ->
                    val cellIndex = rowIndex * 7 + colIndex
                    val dayNumber = cellIndex - offset + 1
                    if (dayNumber < 1 || dayNumber > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = month.atDay(dayNumber)
                        DayCell(
                            dayNumber = dayNumber,
                            events = eventsThisMonth[date] ?: emptyList(),
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            onSelect = { onSelectDate(date) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    dayNumber: Int,
    events: List<CalendarEventEntity>,
    isSelected: Boolean,
    isToday: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isSelected) Color(0xFF3C3489) else Color.Transparent
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val dotSources = events.map { it.featureSource }.distinct().take(3)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (isToday && !isSelected)
                    Modifier.border(1.5.dp, Color(0xFF3C3489), CircleShape)
                else
                    Modifier
            )
            .clickable(onClick = onSelect),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = dayNumber.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodySmall,
            )
            if (dotSources.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 1.dp),
                ) {
                    dotSources.forEach { source ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(sourceColor(source), CircleShape),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(
    event: CalendarEventEntity,
    onToggle: () -> Unit,
) {
    val showCheckbox = event.featureSource == "TASK" || event.featureSource == "WORKOUT"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        SourceBadge(event.featureSource)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (event.isCompleted) TextDecoration.LineThrough
                                 else TextDecoration.None,
            )
            if (!event.isAllDay && event.timeMinutes != null) {
                Text(
                    text = formatTimeMinutes(event.timeMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (event.subtitle != null) {
                Text(
                    text = event.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (showCheckbox) {
            Checkbox(
                checked = event.isCompleted,
                onCheckedChange = { onToggle() },
            )
        }
    }
}

@Composable
private fun SourceBadge(source: String) {
    val (bg, label) = when (source) {
        "WORKOUT" -> Color(0xFFEDE7F6) to "WRKT"
        "TASK"    -> Color(0xFFE3F2FD) to "TASK"
        "FOOD"    -> Color(0xFFE8F5E9) to "FOOD"
        "DIARY"   -> Color(0xFFFFF8E1) to "DIARY"
        "NOTE"    -> Color(0xFFF5F5F5) to "NOTE"
        else      -> Color(0xFFF5F5F5) to source.take(4)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun sourceColor(source: String): Color = when (source) {
    "WORKOUT" -> Color(0xFF7C3DFF)
    "TASK"    -> Color(0xFF1E88E5)
    "FOOD"    -> Color(0xFF43A047)
    "DIARY"   -> Color(0xFFFFB300)
    "NOTE"    -> Color(0xFF757575)
    else      -> Color(0xFF9E9E9E)
}
