package com.example.akashiconline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.akashiconline.ui.screens.FoodScreen
import com.example.akashiconline.ui.screens.PasswordsScreen
import com.example.akashiconline.ui.screens.ScheduleScreen
import com.example.akashiconline.ui.screens.TasksScreen
import com.example.akashiconline.ui.screens.TimerScreen
import com.example.akashiconline.ui.theme.AkashicOnlineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AkashicOnlineTheme {
                AkashicOnlineApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun AkashicOnlineApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.TIMER) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.TIMER -> TimerScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.SCHEDULE -> ScheduleScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.FOOD -> FoodScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.PASSWORDS -> PasswordsScreen(modifier = Modifier.padding(innerPadding))
                AppDestinations.TASKS -> TasksScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
    val chapterNumber: String,
) {
    TIMER("Timer", R.drawable.ic_timer, "I"),
    SCHEDULE("Schedule", R.drawable.ic_calendar, "II"),
    FOOD("Food", R.drawable.ic_food, "III"),
    PASSWORDS("Passwords", R.drawable.ic_lock, "IV"),
    TASKS("Tasks", R.drawable.ic_checklist, "V"),
}

