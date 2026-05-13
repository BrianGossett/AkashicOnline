package com.example.akashiconline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.AppDestinations
import com.example.akashiconline.R
import com.example.akashiconline.data.CalendarEventEntity
import com.example.akashiconline.ui.home.HomeUiState
import com.example.akashiconline.ui.home.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onNavigate: (AppDestinations) -> Unit,
    onOpenBookMenu: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SpineHeader(date = state.todayDate, onOpenBookMenu = onOpenBookMenu)
            RecentsStrip(destinations = state.recentDestinations, onNavigate = onNavigate)
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    TwoPanelDashboard(
                        state = state,
                        onNavigate = onNavigate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                    )
                }
                item { HorizontalDivider() }
                items(AppDestinations.entries.toList()) { dest ->
                    ChapterRow(destination = dest, onClick = { onNavigate(dest) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SpineHeader(date: LocalDate, onOpenBookMenu: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF3C3489))
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Column {
            Text(
                text = "AKASHIC ONLINE",
                color = Color(0xFFCECBF6),
                style = MaterialTheme.typography.headlineMedium.copy(
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.Light,
                ),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                color = Color(0xFFCECBF6).copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        IconButton(
            onClick = onOpenBookMenu,
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Icon(
                painterResource(R.drawable.ic_book_menu),
                contentDescription = "Chapter list",
                tint = Color(0xFFCECBF6),
            )
        }
    }
}

@Composable
private fun RecentsStrip(
    destinations: List<AppDestinations>,
    onNavigate: (AppDestinations) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(destinations) { dest ->
            AssistChip(
                onClick = { onNavigate(dest) },
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                },
                leadingIcon = {
                    Icon(
                        painterResource(dest.icon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun TwoPanelDashboard(
    state: HomeUiState,
    onNavigate: (AppDestinations) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LeftPanel(
            pastDue = state.pastDueEvents,
            undated = state.undatedTasks,
            onNavigate = onNavigate,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        RightPanel(
            todayEvents = state.todayEvents,
            onNavigate = onNavigate,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun LeftPanel(
    pastDue: List<CalendarEventEntity>,
    undated: List<CalendarEventEntity>,
    onNavigate: (AppDestinations) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
        ) {
            Text(
                "Past Due",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
            if (pastDue.isEmpty() && undated.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "All clear",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Spacer(Modifier.height(4.dp))
                pastDue.take(3).forEach { event ->
                    PastDueRow(event = event, onNavigate = onNavigate)
                }
                if (undated.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "No date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                    Spacer(Modifier.height(2.dp))
                    undated.take(3).forEach { event ->
                        UndatedRow(event = event, onNavigate = onNavigate)
                    }
                }
            }
        }
    }
}

@Composable
private fun PastDueRow(event: CalendarEventEntity, onNavigate: (AppDestinations) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { featureDest(event.featureSource)?.let(onNavigate) }
            .padding(vertical = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(28.dp)
                .background(MaterialTheme.colorScheme.error),
        )
        Text(
            text = event.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun UndatedRow(event: CalendarEventEntity, onNavigate: (AppDestinations) -> Unit) {
    Text(
        text = event.title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { featureDest(event.featureSource)?.let(onNavigate) }
            .padding(horizontal = 4.dp, vertical = 3.dp),
    )
}

@Composable
private fun RightPanel(
    todayEvents: List<CalendarEventEntity>,
    onNavigate: (AppDestinations) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
        ) {
            Text(
                "Today",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (todayEvents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Nothing scheduled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Spacer(Modifier.height(4.dp))
                todayEvents.take(5).forEach { event ->
                    TodayEventCard(event = event, onNavigate = onNavigate)
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun TodayEventCard(event: CalendarEventEntity, onNavigate: (AppDestinations) -> Unit) {
    val bg = if (event.featureSource == "WORKOUT") Color(0xFFEEEDFE)
    else MaterialTheme.colorScheme.surfaceVariant
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { featureDest(event.featureSource)?.let(onNavigate) },
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (event.subtitle != null) {
                Text(
                    text = event.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun featureDest(source: String): AppDestinations? = when (source) {
    "WORKOUT" -> AppDestinations.WORKOUT
    "TASK" -> AppDestinations.TASKS
    "FOOD" -> AppDestinations.FOOD
    "DIARY" -> AppDestinations.DIARY
    "NOTE" -> AppDestinations.NOTES
    else -> null
}
