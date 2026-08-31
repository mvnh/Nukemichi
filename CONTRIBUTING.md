# Contributing

Nukemichi provisions a VPN server over SSH and connects to it. Most of the code either runs
commands as root on someone else's machine or handles their credentials, so the bar for changes in
`core.ssh`, `core.vpn` and `core.security` is higher than the line count suggests.

## Getting set up

```sh
git clone --recurse-submodules https://github.com/mvnh/Nukemichi.git
```

`hev-socks5-tunnel` is a submodule — after a plain clone, run
`git submodule update --init --recursive`. You need JDK 17, the Android NDK (SDK Manager), and
`minSdk` 26. Open in Android Studio and run the `app` module.

The build downloads `libv2ray.aar` and verifies it against a pinned SHA-256; a mismatch fails the
build rather than warning.

## Verifying a change

```sh
./gradlew compileDebugKotlin          # fast loop while iterating
./gradlew testDebugUnitTest lintDebug # before pushing
```

`assembleDebug` is only needed when native code or the manifest changed. `assembleRelease` is CI's
job — it is slow locally and proves nothing that CI will not.

## Branches

| Prefix | For |
|---|---|
| `feature/<short-desc>` | new functionality |
| `fix/<short-desc>` | bug fixes |
| `refactor/<short-desc>` | restructuring without behaviour change |
| `test/<short-desc>` | test-only work |
| `docs/<short-desc>` | documentation and user-facing copy |
| `ci/<short-desc>` | workflows and build tooling |
| `release/<X.Y>` | release stabilisation |
| `hotfix/<X.Y>-<short-desc>` | fix on top of a released `release/X.Y` |

kebab-case, English, no issue numbers.

## Commits and pull request titles

Squash is the only merge method enabled, so **the pull request title becomes the commit message on
`master`**. Commits inside your branch are squashed away and can stay scruffy; the title is what
gets linted, and the check is blocking.

```
type(scope): subject
```

**Types:** `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `build`, `ci`, `chore`, `style`,
`revert`.

Two that look like types but are not:

- `ux` — user-visible copy or behaviour is `feat`, a broken layout is `fix`, a pure restructure is
  `refactor`.
- `security` — a security fix is still `fix`. Use it as a *scope* instead.

`style` means source formatting, not visual styling. Changing the Compose theme is `feat` or `fix`.

**Scopes** come from the package structure: `wizard`, `dashboard`, `hello`, `settings`, `learn`,
`vpn`, `ssh`, `storage`, `ui`, `mode`, `security`, `navigation`, `di`, `ci`, `build`, `deps`,
`release`. Omit the scope when a change has no single one — do not write `(all)`.
[`conventional-title.sh`](.github/workflows/conventional-title.sh) is the source of truth; add to
its lists when a genuinely new module appears.

**Subject:** imperative (`add`, not `added`), lowercase, no trailing period, one change per commit.
If you need "and" to describe it, it is probably two pull requests. Title length is capped at 72;
GitHub appends ` (#123)` on squash.

Breaking changes take a `!` — `feat(vpn)!: drop the legacy profile format` — plus a
`BREAKING CHANGE:` footer in the description.

The description becomes the commit body, so write it for someone reading `git log` in a year: the
reason and the consequence, not a restatement of the diff.

## Pull requests

`master` takes no direct pushes. Every change goes through a pull request that must pass `verify`,
`release-build` and `Conventional Commits`, and must be up to date with `master` before merging.

That last rule exists because of *semantic conflicts*: two branches touching different files merge
cleanly and still fail to compile. A green check from before the base moved proves nothing, so the
branch is re-checked after it catches up.

## Architecture

The layering is enforced by [`ArchitectureBoundariesTest`](app/src/test/java/app/nukemichi/android/architecture/ArchitectureBoundariesTest.kt),
not by convention — the app is a single Gradle module, so `internal` blocks nothing on its own and
Konsist stands in for the module boundaries the packages are written as if they had.

- `core.<module>` exposes only abstractions outside its own `.internal`/`.di`. Concrete behaviour
  lives in `.internal`.
- `feature.<name>` exposes only `*Key` navigation keys at its root; everything else is in `.impl`.
- Every `*Key` needs a registered `Destination<Key>`, or navigation fails at runtime instead of at
  build time.
- `core` is effectively the data layer: a feature's `domain` uses it, and UI models stay out of it.
  `core.security.Secret` is a data-layer type — UI state holds `core.ui.util.UiSecret` instead, and
  the conversion happens in the state mappers.
- `domain` never imports Compose or `core.ui`. `core` never imports `feature`.

Never hardcode a dispatcher — inject a `CoroutineDispatcher`. Flows must handle cancellation
without swallowing `CancellationException`.

## Tests

Coverage is not a target. What earns a test is code that fails *silently* — wrong output rather
than a crash — and code whose failure is expensive: wire formats, routing policy, trust decisions,
anything parsing input the app did not author.

Thin adapters over JNI, the Go runtime or the Android framework are deliberately left to
instrumented tests and reading, not wrapped in interfaces for the sake of a number.

Two habits worth keeping:

- **Check that an assertion has teeth.** Break the implementation on purpose and confirm the test
  fails. This has already caught a test that passed against a deleted routing rule, because
  `indexOfFirst` returns `-1` and `-1` sorts before everything.
- **Prefer fakes to mocks.** There is no mocking library here on purpose. A loopback `ServerSocket`
  says more about a SOCKS client than a recorded call ever will.

Golden values must come from an outside oracle — fingerprints from `ssh-keygen`, digests from the
upstream release — so a test cannot agree with a bug by restating it.

## Security-sensitive areas

Read the surrounding code before changing any of these:

- **Shell interpolation.** Anything reaching a remote script goes through `ShellSafe`, which
  validates at construction. Candidate SNI domains come from certificates controlled by whoever
  shares the VPS's subnet — treat them as attacker input.
- **Downloaded binaries.** Xray-core and RealiTLScanner are pinned by SHA-256 and verified before
  they are installed or executed. Bump the version and the digest together.
- **Host keys.** Trust-on-first-use: the fingerprint is stored only after authentication succeeds.
  Do not reorder that.
- **Credentials.** SSH passwords and keys are never persisted — only the host fingerprint and the
  connection profile are, encrypted under an Android Keystore key. Keep secrets in `Secret` /
  `UiSecret` so they cannot reach a log through a generated `toString`.

Please report vulnerabilities privately rather than in a public issue.
