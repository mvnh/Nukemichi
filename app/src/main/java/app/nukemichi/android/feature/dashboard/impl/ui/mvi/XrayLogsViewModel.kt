package app.nukemichi.android.feature.dashboard.impl.ui.mvi

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.nukemichi.android.core.vpn.XrayServiceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Stable
@HiltViewModel
internal class XrayLogsViewModel @Inject constructor(
    private val serviceProvider: XrayServiceProvider,
) : ViewModel() {

    private val _lines = MutableStateFlow<ImmutableList<String>>(emptyList<String>().toImmutableList())
    val lines: StateFlow<ImmutableList<String>> = _lines.asStateFlow()

    init {
        viewModelScope.launch {
            serviceProvider.monitoring.logs
                .onEach { log ->
                    val timestamp = TIME_FORMATTER.format(
                        Instant.ofEpochMilli(log.timestampMillis).atZone(ZoneId.systemDefault())
                    )
                    val line = "[$timestamp] ${log.message}"
                    _lines.value = (_lines.value + line).takeLast(MAX_LINES).toImmutableList()
                }
                .collect()
        }
    }

    private companion object {
        const val MAX_LINES = 1000
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    }
}
