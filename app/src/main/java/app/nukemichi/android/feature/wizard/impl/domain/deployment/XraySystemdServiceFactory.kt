package app.nukemichi.android.feature.wizard.impl.domain.deployment

internal object XraySystemdServiceFactory {
    fun create(): String = """
        [Unit]
        Description=Nukemichi Xray service
        After=network-online.target
        Wants=network-online.target

        [Service]
        Type=simple
        ExecStart=/usr/local/bin/xray run -config /usr/local/etc/xray/config.json
        Restart=on-failure
        RestartSec=3
        NoNewPrivileges=true

        [Install]
        WantedBy=multi-user.target
    """.trimIndent()
}
