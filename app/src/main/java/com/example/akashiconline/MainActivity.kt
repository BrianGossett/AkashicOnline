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
import com.example.akashiconline.ui.screens.BuildProgramScreen
import com.example.akashiconline.ui.screens.DayEditorScreen
import com.example.akashiconline.ui.screens.PresetScreen
import com.example.akashiconline.ui.screens.ProgramDetailScreen
import com.example.akashiconline.ui.screens.ProgramsScreen
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
        composable(AppDestinations.PROGRAMS.route) {
            ProgramsScreen(
                onBack = { navController.popBackStack() },
                onNewProgram = { navController.navigate("program_builder") },
                onProgramClick = { programId -> navController.navigate("program_detail/$programId") },
            )
        }
        composable(
            route = "program_detail/{programId}",
            arguments = listOf(navArgument("programId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val programId = backStackEntry.arguments!!.getString("programId")!!
            ProgramDetailScreen(
                programId = programId,
                onBack = { navController.popBackStack() },
                onEditProgram = {
                    navController.navigate("program_builder?programId=$programId")
                },
                onEditDay = { dayId ->
                    navController.navigate("day_editor?dayId=$dayId")
                },
                onAddDay = { weekId, dayNumber ->
                    navController.navigate("day_editor?weekId=$weekId&dayNumber=$dayNumber")
                },
                onRunDay = { /* WORKOUT-6 */ },
            )
        }
        composable(
            route = "day_editor?dayId={dayId}&weekId={weekId}&dayNumber={dayNumber}",
            arguments = listOf(
                navArgument("dayId") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                },
                navArgument("weekId") {
                    type = NavType.StringType; nullable = true; defaultValue = null
                },
                navArgument("dayNumber") { type = NavType.IntType; defaultValue = 1 },
            ),
        ) { backStackEntry ->
            val args = backStackEntry.arguments!!
            DayEditorScreen(
                dayId = args.getString("dayId"),
                weekId = args.getString("weekId"),
                dayNumber = args.getInt("dayNumber"),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = "program_builder?programId={programId}",
            arguments = listOf(
                navArgument("programId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
        ) { backStackEntry ->
            val programId = backStackEntry.arguments?.getString("programId")
            BuildProgramScreen(
                onBack = { navController.popBackStack() },
                onSaved = { savedId ->
                    navController.navigate("program_detail/$savedId") {
                        popUpTo("program_builder?programId=$programId") { inclusive = true }
                    }
                },
                editProgramId = programId,
            )
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
    PROGRAMS("Programs", R.drawable.ic_programs, "VI", "programs"),
}
