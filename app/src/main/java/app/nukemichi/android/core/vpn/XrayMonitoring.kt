package app.nukemichi.android.core.vpn

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface XrayMonitoring {
    val state: StateFlow<XrayEngineState>
    val stats: Flow<XrayTrafficStats>
    val logs: Flow<XrayLogMessage>
    val healthDegraded: Flow<Unit>

    /** Null while no session runs. Comes from `:vpn`, so it survives the UI process being recreated underneath a live tunnel. */
    val sessionServerId: StateFlow<String?>
}
