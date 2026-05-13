package com.example.akashiconline.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.PresetEntity
import com.example.akashiconline.data.TimerConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TimerConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.getDatabase(application).presetDao()

    val presets: StateFlow<List<PresetEntity>> = dao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _savedEvent = MutableSharedFlow<String>()
    val savedEvent: SharedFlow<String> = _savedEvent.asSharedFlow()

    fun savePreset(name: String, config: TimerConfig) {
        viewModelScope.launch {
            dao.insert(
                PresetEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    workSeconds = config.workSeconds,
                    restSeconds = config.restSeconds,
                    rounds = config.rounds,
                    createdAt = System.currentTimeMillis(),
                )
            )
            _savedEvent.emit(name.trim())
        }
    }
}
