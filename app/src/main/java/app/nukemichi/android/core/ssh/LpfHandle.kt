package app.nukemichi.android.core.ssh

interface LpfHandle {
    val localPort: Int
    suspend fun stop()
}