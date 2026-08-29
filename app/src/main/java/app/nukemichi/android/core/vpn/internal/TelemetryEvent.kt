package app.nukemichi.android.core.vpn.internal

import app.nukemichi.android.core.vpn.XrayEngineState
import app.nukemichi.android.core.vpn.XrayLogMessage
import app.nukemichi.android.core.vpn.XrayTrafficStats

internal sealed interface TelemetryEvent {
    data class State(val state: XrayEngineState) : TelemetryEvent
    data class Stats(val stats: XrayTrafficStats) : TelemetryEvent
    data class Log(val log: XrayLogMessage) : TelemetryEvent
    data object HealthDegraded : TelemetryEvent
}
