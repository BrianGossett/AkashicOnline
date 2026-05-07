package com.example.akashiconline.ui.programs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.ProgramSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProgramsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.getDatabase(application).programDao()

    val programs: StateFlow<List<ProgramSummary>> = dao.getAllWithWeekCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
