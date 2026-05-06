package com.example.akashiconline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.akashiconline.data.PresetEntity
import com.example.akashiconline.data.TimerConfig
import com.example.akashiconline.ui.screens.ActiveTimerScreen
import com.example.akashiconline.ui.screens.BookMenuScreen
import com.example.akashiconline.ui.screens.FoodScreen
import com.example.akashiconline.ui.screens.PasswordsScreen
import com.example.akashiconline.ui.screens.ScheduleScreen
import com.example.akashiconline.ui.screens.TasksScreen
import com.example.akashiconline.ui.screens.PresetScreen
import com.example.akashiconline.ui.screens.TimerScreen
import com.example.akashiconline.ui.theme.AkashicOnlineTheme
import com.example.akashiconline.ui.timer.TimerViewModel

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

@Composable
fun AkashicOnlineApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "book_menu") {
        composable("book_menu") {
            BookMenuScreen(onNavigate = { navController.navigate(it.route) })
        }
        composable(AppDestinations.TIMER.route) { backStackEntry ->
            val savedStateHandle = backStackEntry.savedStateHandle
            val presetToLoad by savedStateHandle
                .getStateFlow<String?>("preset_id", null)
                .collectAsStateWithLifecycle()
            val presetWork by savedStateHandle
                .getStateFlow("preset_work", -1)
                .collectAsStateWithLifecycle()
            val presetRest by savedStateHandle
                .getStateFlow("preset_rest", -1)
                .collectAsStateWithLifecycle()
            val presetRounds by savedStateHandle
                .getStateFlow("preset_rounds", -1)
                .collectAsStateWithLifecycle()

            val resolvedPreset = if (presetToLoad != null && presetWork > 0) {
                PresetEntity(
                    id = presetToLoad!!,
                    name = "",
                    workSeconds = presetWork,
                    restSeconds = presetRest,
                    rounds = presetRounds,
                    createdAt = 0,
                )
            } else null

            TimerScreen(
                onBack = { navController.popBackStack() },
                onStart = { config ->
                    navController.navigate(
                        "active_timer/${config.workSeconds}/${config.restSeconds}/${config.rounds}"
                    )
                },
                onLoadPreset = { navController.navigate("presets") },
                presetToLoad = resolvedPreset,
                onPresetConsumed = {
                    savedStateHandle.remove<String>("preset_id")
                    savedStateHandle.remove<Int>("preset_work")
                    savedStateHandle.remove<Int>("preset_rest")
                    savedStateHandle.remove<Int>("preset_rounds")
                },
            )
        }
        composable(
            route = "active_timer/{workSeconds}/{restSeconds}/{rounds}",
            arguments = listOf(
                navArgument("workSeconds") { type = NavType.IntType },
                navArgument("restSeconds") { type = NavType.IntType },
                navArgument("rounds") { type = NavType.IntType },
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            val config = TimerConfig(
                workSeconds = args.getInt("workSeconds"),
                restSeconds = args.getInt("restSeconds"),
                rounds = args.getInt("rounds"),
            )
            val viewModel: TimerViewModel = viewModel(factory = TimerViewModel.Factory(config))
            ActiveTimerScreen(
                viewModel = viewModel,
                onDone = { navController.popBackStack() },
            )
        }
        composable("presets") {
            PresetScreen(
                onBack = { navController.popBackStack() },
                onLoadPreset = { preset ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("preset_id", preset.id)
                        set("preset_work", preset.workSeconds)
                        set("preset_rest", preset.restSeconds)
                        set("preset_rounds", preset.rounds)
                    }
                    navController.popBackStack()
                },
            )
        }
        composable(AppDestinations.SCHEDULE.route) {
            ScheduleScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestinations.FOOD.route) {
            FoodScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestinations.PASSWORDS.route) {
            PasswordsScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestinations.TASKS.route) {
            TasksScreen(onBack = { navController.popBackStack() })
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
    val chapterNumber: String,
    val route: String,
) {
    TIMER("Timer", R.drawable.ic_timer, "I", "timer"),
    SCHEDULE("Schedule", R.drawable.ic_calendar, "II", "schedule"),
    FOOD("Food", R.drawable.ic_food, "III", "food"),
    PASSWORDS("Passwords", R.drawable.ic_lock, "IV", "passwords"),
    TASKS("Tasks", R.drawable.ic_checklist, "V", "tasks"),
}
