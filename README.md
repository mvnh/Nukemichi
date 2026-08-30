# Nukemichi

An Android VPN client that provisions your own VPS over SSH and connects to it. There is no
Nukemichi server infrastructure — every server it talks to is one you own and gave it root SSH
access to.

## What it does

1. You give the app an SSH host, port, and either a password or a private key.
2. It detects the server's distro/architecture, downloads and installs
   [Xray-core](https://github.com/XTLS/Xray-core), scans the server's own network neighborhood for
   a masking domain using [RealiTLScanner](https://github.com/XTLS/RealiTLScanner), generates
   fresh REALITY keys on the server itself, writes a config, and starts it as a system service.
3. The app then connects to that server over VLESS + REALITY + XHTTP and routes your device's
   traffic through it.

## What it installs on your server

- `/usr/local/bin/xray` — the Xray-core binary, downloaded from
  [XTLS/Xray-core releases](https://github.com/XTLS/Xray-core/releases).
- `/usr/local/etc/xray/config.json` — the generated server config (mode 0600 — contains the REALITY
  private key).
- A service unit that starts Xray on boot: `/etc/systemd/system/nukemichi-xray.service` on
  systemd-based distros, `/etc/init.d/nukemichi-xray` (OpenRC) on Alpine.
- A temporary copy of [RealiTLScanner](https://github.com/XTLS/RealiTLScanner/releases), used once
  during setup to find a masking domain and deleted afterward (`mktemp -d` + `trap ... EXIT`).

Nothing else is touched. Re-running the setup wizard against the same server overwrites the config
and service unit cleanly rather than accumulating leftovers.

## What it downloads, and from where

- **On the server**, during setup: Xray-core and RealiTLScanner release binaries from their GitHub
  Releases pages (exact versions are pinned in `InstallXrayRuntimeCommand`/`ScanSniCommand`).
- **On your phone**, at build time (not runtime): `libv2ray.aar` from
  [2dust/AndroidLibXrayLite releases](https://github.com/2dust/AndroidLibXrayLite/releases) — a
  `gomobile bind` wrapper that runs Xray-core in-process. Verified against a pinned SHA-256 before
  the build proceeds (see `downloadLibV2ray` in `app/build.gradle.kts`).

Supported server distros: Debian/Ubuntu, RHEL/Fedora/Alma/Rocky, Arch, Alpine.

## How to remove everything

On the server:

```sh
# systemd (Debian/Ubuntu, RHEL family, Arch)
systemctl disable --now nukemichi-xray.service
rm -f /etc/systemd/system/nukemichi-xray.service

# OpenRC (Alpine)
rc-service nukemichi-xray stop
rc-update del nukemichi-xray default
rm -f /etc/init.d/nukemichi-xray

# either way
rm -rf /usr/local/bin/xray /usr/local/etc/xray
```

On your phone: uninstall the app. It stores only its own encrypted connection profile locally
(Android Keystore-backed) — nothing is sent anywhere else, and there's no account or server-side
state to clean up.

## Building it

```sh
git clone --recurse-submodules github.com/mvnh/Nukemichi.git
```

`hev-socks5-tunnel` is a git submodule (native tun2socks) — the `--recurse-submodules` flag matters,
or run `git submodule update --init --recursive` after a plain clone. Open in Android Studio with
the NDK installed (SDK Manager), then run the `app` module. Requires `minSdk` 26.

## Security notes

- SSH host keys are verified trust-on-first-use: the first connection's fingerprint is shown for
  you to confirm, then pinned for future connections against that host.
- REALITY keys and the SSH host fingerprint are generated/verified live during setup, never
  hardcoded or reused across servers.
- Local storage of anything sensitive (SSH trust, saved connection profiles) is encrypted via the
  Android Keystore.

Nukemichi is pre-1.0 and has not been independently audited. Read the code before trusting it with
anything where getting it wrong has real consequences.
