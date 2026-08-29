package app.nukemichi.android.core.security

@JvmInline
value class Secret(val value: String) {
    override fun toString(): String = "Secret(***)"
}
