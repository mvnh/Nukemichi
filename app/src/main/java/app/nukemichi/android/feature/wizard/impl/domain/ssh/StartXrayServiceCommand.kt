package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.model.PackageManager

internal class StartXrayServiceCommand(
    private val packageManager: PackageManager,
) : BashScriptCommand<Unit> {
    override val script: String = if (packageManager == PackageManager.APK) openRcScript else systemdScript

    override fun parseOutput(result: CommandResult) = Unit

    private val openRcScript: String
        get() = """
            set -eu
            rc-update add nukemichi-xray default
            rc-service nukemichi-xray restart
            rc-service nukemichi-xray status
        """.trimIndent()

    private val systemdScript: String
        get() = """
            set -eu
            systemctl daemon-reload
            systemctl enable nukemichi-xray.service
            systemctl restart nukemichi-xray.service
            systemctl is-active --quiet nukemichi-xray.service
        """.trimIndent()
}
