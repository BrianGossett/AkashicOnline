package com.example.akashiconline.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.akashiconline.R
import com.example.akashiconline.data.ProgramSummary
import com.example.akashiconline.ui.programs.ProgramsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramsScreen(
    onBack: () -> Unit,
    onNewProgram: () -> Unit,
    onProgramClick: (programId: String) -> Unit,
    viewModel: ProgramsViewModel = viewModel(),
) {
    val programs by viewModel.programs.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Programs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewProgram) {
                Icon(painterResource(R.drawable.ic_programs), contentDescription = "New Program")
            }
        },
    ) { innerPadding ->
        if (programs.isEmpty()) {
            EmptyPrograms(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
            ) {
                items(programs, key = { it.id }) { program ->
                    ProgramCard(
                        program = program,
                        onClick = { onProgramClick(program.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgramCard(
    program: ProgramSummary,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(program.name, style = MaterialTheme.typography.titleMedium)
            if (program.description.isNotBlank()) {
                Text(
                    text = program.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text(
                text = "${program.weekCount} ${if (program.weekCount == 1) "week" else "weeks"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun EmptyPrograms(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No programs yet", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tap + to create your first workout program.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailPlaceholder(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Program") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Coming in WORKOUT-4", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Back")
                }
            }
        }
    }
}
