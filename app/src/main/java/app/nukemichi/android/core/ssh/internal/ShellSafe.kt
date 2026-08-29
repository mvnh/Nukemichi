package app.nukemichi.android.core.ssh.internal

// Guards script interpolation at the type level, not just by convention at each call site.
@JvmInline
value class ShellSafe private constructor(private val raw: String) {

    override fun toString(): String = raw

    companion object {
        fun of(value: String): ShellSafe {
            require(isSafeHostname(value)) { "Refusing to interpolate an unsafe value into a script: $value" }
            return ShellSafe(value)
        }
    }
}
