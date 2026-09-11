package app.nukemichi.android.feature.dashboard.impl.ui.mvi

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.nukemichi.android.core.vpn.XrayLogMessage
import app.nukemichi.android.core.vpn.XrayServiceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Stable
@HiltViewModel
internal class XrayLogsViewModel @Inject constructor(
    private val serviceProvider: XrayServiceProvider,
) : ViewModel() {

    private val _lines = MutableStateFlow<ImmutableList<String>>(persistentListOf())
    val lines: StateFlow<ImmutableList<String>> = _lines.asStateFlow()

    // Appending to an ArrayDeque and evicting from its head is O(1) at both ends; the O(n) cost
    // is the immutable snapshot Compose reads, and that is what the conflate below rations.
    private val buffer = ArrayDeque<String>(MAX_LINES)

    init {
        viewModelScope.launch {
            serviceProvider.monitoring.logs
                .map { it.toDisplayLine() }
                .onEach { line ->
                    if (buffer.size == MAX_LINES) buffer.removeFirst()
                    buffer.addLast(line)
                }
                // Upstream of conflate, so every line still reaches the buffer; what gets dropped
                // is only the redundant snapshots. xray-core at debug level out-produces anything
                // that can render it, and rebuilding a 1000-element list per line - on the main
                // dispatcher, for frames nobody sees - was most of this screen's cost.
                .conflate()
                .collect {
                    _lines.value = buffer.toImmutableList()
                    delay(SNAPSHOT_INTERVAL_MS)
                }
        }
    }

    private fun XrayLogMessage.toDisplayLine(): String {
        val timestamp = TIME_FORMATTER.format(
            Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault())
        )
        return "[$timestamp] $message"
    }

    internal companion object {
        const val MAX_LINES = 1000
        private const val SNAPSHOT_INTERVAL_MS = 100L
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    }
}
