package app.nukemichi.android.core.vpn.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import android.os.Process as AndroidProcess

internal class GoLogcatReader(
    private val scope: CoroutineScope,
    private val onLine: (String) -> Unit,
) {
    private var process: Process? = null
    private var job: Job? = null

    fun start() {
        job = scope.launch {
            try {
                val command = listOf(
                    "logcat", "-v", "time", "--pid=${AndroidProcess.myPid()}", "-s", "GoLog:*"
                )
                val running = ProcessBuilder(command).redirectErrorStream(true).start()
                process = running
                running.inputStream.bufferedReader().useLines { lines -> lines.forEach(onLine) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: IOException) {
                Timber.d(error, "GoLogcatReader: stream closed")
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        process?.destroy()
        process = null
    }
}
