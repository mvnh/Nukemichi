package app.nukemichi.android.core.ssh

/**
 * A hostname validated for interpolation into a remote script. Guards at the type level rather
 * than by convention at each call site.
 *
 * Public rather than internal on purpose: the BashScriptCommand implementations that need the
 * guard are written by feature domain layers, not by core.ssh itself.
 */
@JvmInline
value class ShellHost private constructor(private val raw: String) {

    override fun toString(): String = raw

    companion object {
        fun of(value: String): ShellHost {
            // The rejected value stays out of the message: candidates come from certificates
            // controlled by whoever shares the VPS's subnet, and this text reaches a log.
            require(isSafeHostname(value)) { "Not a hostname that is safe to put in a script." }
            return ShellHost(value)
        }
    }
}
