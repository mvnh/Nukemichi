package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.model.CommandResult

class DetectServerArchCommand : BashScriptCommand<String> {
    override val script: String = "uname -m"

    override fun parseOutput(result: CommandResult): String {
        return when (val arch = result.stdout.trim()) {
            "x86_64", "amd64" -> "64"
            "aarch64", "arm64" -> "arm64-v8a"
            else -> error("Unsupported server architecture: $arch")
        }
    }
}
 