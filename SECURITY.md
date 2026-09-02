# Security Policy

## Reporting a vulnerability

Use **[private vulnerability reporting](https://github.com/mvnh/Nukemichi/security/advisories/new)**
in the Security tab. Please do not open a public issue for anything you believe is exploitable.

Nukemichi is maintained by one person. Expect a first reply within about a week — if you have not
heard back in two, feel free to ping the report. Fixes are developed privately and disclosed once a
release is available. You will be credited unless you would rather not be.

Useful in a report: what an attacker controls, what they gain, and the shortest path from one to
the other. A working proof of concept is welcome but not required.

## Supported versions

Nukemichi is pre-1.0. Only the latest release is supported, and fixes are not backported.

## What this protects against

Nukemichi is a client for a VPN server **you own**. That shapes the threat model more than anything
else in it.

It is built to resist:

- a network observer between your device and your server, including active DPI probing;
- other applications on the same device — the local SOCKS inbound is credentialed for this reason,
  and stored profiles are encrypted under a non-exportable Keystore key;
- whoever else shares your VPS's subnet, since masking-domain candidates come from certificates
  those neighbours control.

It does **not** protect you from:

- **your hosting provider.** You chose them and you own the server; everything leaving it is theirs
  to see. No client-side change can alter that;
- a compromised, rooted, or malware-carrying device;
- an observer positioned to correlate traffic at both ends simultaneously;
- your own operational choices — a reused SSH password, a VPS that was already compromised, a
  server whose provider cooperates with whoever you are avoiding.

Whether REALITY itself is distinguishable on the wire is an open research question about
[Xray-core](https://github.com/XTLS/Xray-core), not about this client.

## Scope

**In scope**

- bypassing `ShellHost` or otherwise injecting into a script that runs as root on the user's server;
- defeating host key verification, or weakening the trust-on-first-use flow;
- leaking SSH credentials or profile material — through logs, Android backups, inter-process
  communication, or a generated `toString`;
- weakening the generated Xray configuration: losing a blackhole rule, degrading REALITY masking,
  or making a connection more distinguishable than intended;
- reaching the loopback SOCKS inbound from another application on the device;
- bypassing the SHA-256 verification applied to downloaded binaries;
- decrypting stored profiles off-device, or misuse of the Android Keystore.

**Out of scope**

- the hosting provider observing traffic, as above;
- detectability of REALITY, XHTTP, or Xray-core itself — report those upstream;
- weak user-chosen credentials, or an already-compromised server;
- attacks that require a rooted device, physical access to an unlocked device, or a malicious app
  already granted elevated privileges;
- denial of service against your own server;
- missing hardening that has no attack path attached to it.

## Upstream components

Vulnerabilities in [Xray-core](https://github.com/XTLS/Xray-core),
[RealiTLScanner](https://github.com/XTLS/RealiTLScanner),
[hev-socks5-tunnel](https://github.com/heiher/hev-socks5-tunnel),
[AndroidLibXrayLite](https://github.com/2dust/AndroidLibXrayLite), sshj or Bouncy Castle belong to
those projects. Report them there.

What this repository is responsible for is pinning: every binary it downloads is pinned by version
**and** SHA-256, verified before it is installed or executed, and a version bump that leaves a
stale digest fails rather than silently installing something else. If an upstream release is found
to be malicious or vulnerable, moving the pin is our job.

## Known limitations

Stated because knowing them may change how you use this, not because they are open bugs:

- **No independent audit.** The code has been reviewed only by the people who wrote it.
- **First-connection trust is often unverifiable in practice.** Trust-on-first-use pins a host key
  the first time you connect, but many hosting providers do not surface the server's fingerprint
  anywhere you could compare it against. When it is not available, the meaningful check is whether
  the connection details reached you from a source you trust, not the hex string itself.
- **IPv6 is disabled by default.** Partly to avoid the leak of an unproxied path, partly because a
  dual-stack fingerprint is itself something DPI can key on. This is a current default rather than
  a permanent property; it is intended to become configurable.

## A note for users at risk

In some jurisdictions, running circumvention software carries legal consequences independent of
what you do with it. Nukemichi cannot know your situation and makes no claim to keep you safe from
it. Understand your local law and your own exposure before relying on this — or on any tool of this
kind.
