package com.example.akashiconline.ui.programs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.DayDetail
import com.example.akashiconline.data.ProgramDetail
import com.example.akashiconline.data.WeekDetail
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProgramDetailViewModel(
    application: Application,
    val programId: String,
) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.getDatabase(application).programDao()

    val detail: StateFlow<ProgramDetail?> = dao.getProgramDetail(programId)
        .map { it?.sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    class Factory(private val programId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val application = checkNotNull(
                extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            )
            return ProgramDetailViewModel(application, programId) as T
        }
    }
}

private fun ProgramDetail.sorted(): ProgramDetail = copy(
    weeks = weeks
        .sortedBy { it.week.weekNumber }
        .map { it.sorted() }
)

private fun WeekDetail.sorted(): WeekDetail = copy(
    days = days
        .sortedBy { it.day.dayNumber }
        .map { it.sorted() }
)

private fun DayDetail.sorted(): DayDetail = copy(
    steps = steps.sortedBy { it.order }
)
