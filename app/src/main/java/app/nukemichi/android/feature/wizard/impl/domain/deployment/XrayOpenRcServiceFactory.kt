package app.nukemichi.android.feature.wizard.impl.domain.deployment


internal object XrayOpenRcServiceFactory {
    fun create(): String = """
        #!/sbin/openrc-run

        name="nukemichi-xray"
        description="Nukemichi Xray service"
        command="/usr/local/bin/xray"
        command_args="run -config /usr/local/etc/xray/config.json"
        command_background="yes"
        pidfile="/run/${'$'}{RC_SVCNAME}.pid"
        output_log="/var/log/${'$'}{RC_SVCNAME}.log"
        error_log="/var/log/${'$'}{RC_SVCNAME}.log"

        depend() {
            need net
            after firewall
        }
    """.trimIndent()
}
