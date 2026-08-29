package app.nukemichi.android.feature.wizard.impl.domain.ssh

import app.nukemichi.android.core.ssh.command.BashScriptCommand
import app.nukemichi.android.core.ssh.model.CommandResult
import app.nukemichi.android.feature.wizard.impl.domain.model.XrayServerSecrets

internal class GenerateXrayServerSecretsCommand : BashScriptCommand<XrayServerSecrets> {
    override val script: String = $$"""
        set -eu
        uuid="$(/usr/local/bin/xray uuid)"
        keys="$(/usr/local/bin/xray x25519)"
        private_key="$(printf '%s\n' "$keys" | sed -n '1s/.*: *//p')"
        public_key="$(printf '%s\n' "$keys" | sed -n '2s/.*: *//p')"
        short_id="$(openssl rand -hex 8)"
        test -n "$uuid"
        test -n "$private_key"
        test -n "$public_key"
        printf 'UUID=%s\nPRIVATE_KEY=%s\nPUBLIC_KEY=%s\nSHORT_ID=%s\n' "$uuid" "$private_key" "$public_key" "$short_id"
    """.trimIndent()

    override fun parseOutput(result: CommandResult): XrayServerSecrets {
        val values = result.stdout.lineSequence()
            .mapNotNull { line -> line.split('=', limit = 2).takeIf { it.size == 2 } }
            .associate { it[0] to it[1].trim() }
        return XrayServerSecrets(
            uuid = values.requireValue("UUID"),
            privateKey = values.requireValue("PRIVATE_KEY"),
            publicKey = values.requireValue("PUBLIC_KEY"),
            shortId = values.requireValue("SHORT_ID"),
        )
    }

    private fun Map<String, String>.requireValue(key: String): String =
        requireNotNull(this[key]?.takeIf(String::isNotBlank)) { "Server did not return $key." }
}
