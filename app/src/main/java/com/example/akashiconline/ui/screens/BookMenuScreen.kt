package com.example.akashiconline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.akashiconline.AppDestinations

@Composable
fun BookMenuScreen(onNavigate: (AppDestinations) -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3C3489))
                    .padding(vertical = 32.dp, horizontal = 24.dp)
            ) {
                Text(
                    text = "AKASHIC ONLINE",
                    color = Color(0xFFCECBF6),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        letterSpacing = 6.sp,
                        fontWeight = FontWeight.Light
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFCECBF6).copy(alpha = 0.35f))
            }

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(AppDestinations.entries) { destination ->
                    ChapterRow(
                        destination = destination,
                        onClick = { onNavigate(destination) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ChapterRow(destination: AppDestinations, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = destination.chapterNumber,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            painter = painterResource(destination.icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = destination.label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
