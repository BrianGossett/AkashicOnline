package com.example.akashiconline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.akashiconline.data.PresetEntity
import com.example.akashiconline.data.TimerConfig
import com.example.akashiconline.data.lastUsedDataStore
import com.example.akashiconline.ui.screens.ActiveTimerScreen
import com.example.akashiconline.ui.screens.BookMenuScreen
import com.example.akashiconline.ui.screens.BuildProgramScreen
import com.example.akashiconline.ui.screens.CalendarScreen
import com.example.akashiconline.ui.screens.DayEditorScreen
import com.example.akashiconline.ui.screens.DiaryScreen
import com.example.akashiconline.ui.screens.FoodScreen
import com.example.akashiconline.ui.screens.HomeScreen
import com.example.akashiconline.ui.screens.NotesScreen
import com.example.akashiconline.ui.screens.PasswordsScreen
import com.example.akashiconline.ui.screens.PresetScreen
import com.example.akashiconline.ui.screens.ProgramDetailScreen
import com.example.akashiconline.ui.screens.ProgramsScreen
import com.example.akashiconline.ui.screens.EditTaskScreen
import com.example.akashiconline.ui.screens.TaskDetailScreen
import com.example.akashiconline.ui.screens.TasksScreen
import com.example.akashiconline.ui.screens.TimerScreen
import com.example.akashiconline.ui.screens.UpcomingScreen
import com.example.akashiconline.ui.screens.ActiveWorkoutScreen
import com.example.akashiconline.ui.screens.CreateWorkoutScreen
import com.example.akashiconline.ui.screens.ScheduledWorkoutsScreen
import com.example.akashiconline.ui.screens.WorkoutScreen
import com.example.akashiconline.ui.theme.AkashicOnlineTheme
import com.example.akashiconline.ui.timer.TimerViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        context.lastUsedDataStore.data.first().let { prefs ->
            AppDestinations.entries.forEach { dest ->
                dest.lastUsedAt = prefs[longPreferencesKey("last_used_${dest.route}")] ?: 0L
            }
        }
    }

    fun navigateTo(dest: AppDestinations) {
        val now = System.currentTimeMillis()
        dest.lastUsedAt = now
        scope.launch {
            context.lastUsedDataStore.edit { prefs ->
                prefs[longPreferencesKey("last_used_${dest.route}")] = now
            }
        }
        navController.navigate(dest.route)
    }

    NavHost(navController = navController, startDestination = "home") {

        // ── Home ──────────────────────────────────────────────────────────────
        composable("home") {
            HomeScreen(
                onNavigate = { dest -> navigateTo(dest) },
                onOpenBookMenu = { navController.navigate("book_menu") },
            )
        }
        composable("book_menu") {
            BookMenuScreen(onNavigate = { dest -> navigateTo(dest) })
        }

        // ── Chapter I: Workout (hub) ──────────────────────────────────────────
        composable(AppDestinations.WORKOUT.route) {
            WorkoutScreen(
                onBack = { navController.popBackStack() },
                onCreateWorkout = { navController.navigate("workout/create") },
                onEditWorkout = { workoutId -> navController.navigate("workout/edit/$workoutId") },
                onStartWorkout = { workoutId -> navController.navigate("active_workout/$workoutId") },
                onScheduleWorkout = { /* WORKOUT-CHANGE-3 */ },
                onQuickTimerStart = { config ->
                    navController.navigate(
                        "active_timer/${config.workSeconds}/${config.restSeconds}/${config.rounds}"
                    )
                },
            )
        }
        composable(
            route = "active_workout/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments!!.getString("workoutId")!!
            ActiveWorkoutScreen(
                workoutId = workoutId,
                onDone = { navController.popBackStack() },
            )
        }
        composable("workout/create") {
            CreateWorkoutScreen(
                workoutId = null,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "workout/edit/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments!!.getString("workoutId")!!
            CreateWorkoutScreen(
                workoutId = workoutId,
                onBack = { navController.popBackStack() },
            )
        }
        composable("workout/scheduled") {
            ScheduledWorkoutsScreen(onBack = { navController.popBackStack() })
        }

        // Timer sub-routes
        composable("timer") { backStackEntry ->
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
            val timerVm: TimerViewModel = viewModel(factory = TimerViewModel.Factory(config))
            ActiveTimerScreen(
                viewModel = timerVm,
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

        // Programs sub-routes
        composable("programs") {
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
                onRunDay = { dayId -> navController.navigate("day_timer/$dayId") },
            )
        }
        composable(
            route = "day_timer/{dayId}",
            arguments = listOf(navArgument("dayId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val dayId = backStackEntry.arguments!!.getString("dayId")!!
            val dayVm: TimerViewModel = viewModel(factory = TimerViewModel.FromDay(dayId))
            ActiveTimerScreen(
                viewModel = dayVm,
                onDone = { navController.popBackStack() },
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

        // ── Chapter II–VII: standalone chapters ──────────────────────────────
        composable(AppDestinations.FOOD.route) {
            FoodScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestinations.TASKS.route) {
            TasksScreen(
                onBack = { navController.popBackStack() },
                onNewTask = { navController.navigate("tasks/create") },
                onOpenTask = { taskId -> navController.navigate("tasks/detail/$taskId") },
                onOpenUpcoming = { navController.navigate("tasks/upcoming") },
            )
        }
        composable("tasks/create") {
            EditTaskScreen(taskId = null, onBack = { navController.popBackStack() })
        }
        composable(
            route = "tasks/edit/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments!!.getString("taskId")!!
            EditTaskScreen(taskId = taskId, onBack = { navController.popBackStack() })
        }
        composable(
            route = "tasks/detail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments!!.getString("taskId")!!
            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate("tasks/edit/$id") },
            )
        }
        composable("tasks/upcoming") {
            UpcomingScreen(
                onBack = { navController.popBackStack() },
                onOpenTask = { taskId -> navController.navigate("tasks/detail/$taskId") },
                onNewTask = { navController.navigate("tasks/create") },
            )
        }
        composable(AppDestinations.DIARY.route) {
            DiaryScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestinations.NOTES.route) {
            NotesScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestinations.CALENDAR.route) {
            CalendarScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestinations.PASSWORDS.route) {
            PasswordsScreen(onBack = { navController.popBackStack() })
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
    val chapterNumber: String,
    val route: String,
) {
    WORKOUT("Workout", R.drawable.ic_workout, "I", "workout"),
    FOOD("Food Tracker", R.drawable.ic_food, "II", "food"),
    TASKS("Tasks", R.drawable.ic_checklist, "III", "tasks"),
    DIARY("Diary", R.drawable.ic_diary, "IV", "diary"),
    NOTES("Notes", R.drawable.ic_notes, "V", "notes"),
    CALENDAR("Calendar", R.drawable.ic_calendar, "VI", "calendar"),
    PASSWORDS("Passwords", R.drawable.ic_lock, "VII", "passwords");

    @Volatile var lastUsedAt: Long = 0L
}
