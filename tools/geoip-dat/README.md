# Regenerating `geoip.dat`

`app/src/main/assets/geoip.dat` is a **vendored, hand-curated** GeoIP database: it is not
downloaded during a normal build. It routes traffic to a handful of countries with heavy
domestic internet filtering (currently `ru`, `by`, `ir`, `cn`, `tm`; see `config.json` in this
directory) directly instead of through the proxy, because those countries' own domestic services
often geo-fence themselves to local IPs or block known VPN exit ranges outright.

`./gradlew preBuild` (and therefore every normal build) only verifies the committed file against
the pinned `GEOIP_DAT_SHA256` in `app/build.gradle.kts`. It does not touch the network or need Go.
Regeneration is a separate, manual, maintainer-only step.

## Why this can't be pinned-forever like `libv2ray.aar`

The generator's raw input, DB-IP's free `country-lite` database, is published at a URL keyed by
year and month (`dbip-country-lite-<year>-<month>.mmdb.gz`), and only the current and previous
month stay live; older ones 404. There is no immutable, permanently-archived source to pin a
SHA-256 against the way `libv2ray.aar`'s GitHub release asset allows. So the committed `geoip.dat`
is only ever as fresh as the last time someone ran the regenerate step. There is no way to make
that automatic without either breaking on schedule (once DB-IP rotates the URL) or giving up
reproducibility.

## When to regenerate

Not on a schedule. Country-level IP allocation moves slowly, so this isn't a security patch.
Refresh it every year or so, or sooner if you notice a country in the list no longer routing
directly (a sign its IP ranges drifted out of the vendored snapshot).

## How

Requires Go on `PATH`. Run:

```sh
./gradlew regenerateGeoipDat
```

This clones `v2fly/geoip` at the commit pinned in `app/build.gradle.kts`
(`geoipGeneratorCommit`), downloads the current month's DB-IP `country-lite` MMDB, runs the
generator against `config.json` in this directory, and overwrites
`app/src/main/assets/geoip.dat` in place. The task prints the new file's SHA-256 when it finishes.

Then:

1. Update `GEOIP_DAT_SHA256` in `app/build.gradle.kts` to the printed value.
2. Sanity-check the new file isn't empty or wildly smaller than before (`ls -la
   app/src/main/assets/geoip.dat`; expect several hundred KB, not bytes).
3. Commit `app/src/main/assets/geoip.dat` and the updated checksum **together**, in one commit, or
   `preBuild`'s verify step fails the build for everyone else once they drift apart.

To add or drop a country, edit `wantedList` in `config.json` first, then regenerate as above.

## Attribution

The DB-IP `country-lite` data is licensed CC BY 4.0; see `/NOTICE` at the repo root. Do not
remove that attribution while this file ships DB-IP-derived data.
