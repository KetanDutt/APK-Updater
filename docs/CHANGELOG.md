# Changelog

All notable changes to APK Updater are documented here. The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [2.1.0] - 2026-09-10 (versionCode 44)

### Added
- **Self-update**: the app now checks for new releases (once a day) using the `version.json` manifest in the repository and can install the new APK directly.
- **Settings → Developer** section: About (with version), links to this repository, the original project and the GPL v3 license.
- **POST_NOTIFICATIONS** runtime permission request on Android 13+ so scheduled-check notifications actually appear.
- `version.json` release manifest at the repository root (used by self-update and documented in the README).
- **Documentation**: `docs/ARCHITECTURE.md`, `docs/CONTRIBUTING.md`, `docs/PRIVACY.md`, `docs/CHANGELOG.md` and a full README rewrite.
- Keystore files (`*.jks`, `*.keystore`) added to `.gitignore`.
- Release builds in CI are now always signed (falls back to the debug key when no upload key is configured via `local.properties`).

### Changed
- Bumped to `versionCode 44` / `versionName 2.1.0`; `targetSdk`/`compileSdk` 33, `buildTools` 33.0.1.
- Pinned the JitPack `fuel`/`fuel-gson` fork to the exact commit `0d7b26873c` (the old `-SNAPSHOT` reference is no longer buildable).
- Pinned `kotlin-stdlib` explicitly; Compose pinned via BOM `2023.01.00`.
- `git` file mode of `gradlew` restored to executable (CI `./gradlew build` was failing with *Permission denied*).
- Modernised the GitHub Actions workflow (`actions/checkout@v4`, `actions/setup-java@v4` with Zulu JDK 11, artifact upload of APKs).

### Fixed
- **Crash on Android 12+**: `PendingIntent` for the update notification and the alarm now use `FLAG_IMMUTABLE`.
- **Crash on install failure**: `MainActivity.onActivityResult` handled a `null` extras bundle; the "root not available" message is now a localised string.
- **Aptoide source skipped v2/v3-only signed apps**: the signature is now read from `signingInfo.apkContentsSigners` (API 28+) with a fallback to the legacy `signatures` field.
- **APKPure false positives**: only reports an update when the remote version code is actually higher than the installed one; malformed pages no longer fail the whole check; page fetches are capped at 8 in parallel.
- **APKMirror/F-Droid/Updates race conditions**: shared mutable state is now guarded (mutexes / volatile double-checked lock on the F-Droid index).
- **Background check killed on Android 8+**: `AlarmReceiver` now uses `goAsync()` (version-guarded for API < 26) instead of blocking `onReceive`.
- **Apps tab on the main thread**: enumerating/labeling all installed apps now runs on `Dispatchers.IO`.
- **F-Droid lookups were O(index) per app** — replaced with O(1) map lookups; a missing index now yields "no results" instead of failing the whole search.
- **Compose Apps tab**: cards no longer stretch full-screen (`fillMaxWidth`), list scrolls correctly; theme follows the activity theme.
- **Gson models hardened**: API response models have default values / nullable fields so a missing field can't crash parsing; lenient version parsing (`toIntOrNull`).
- **HTTP**: shared `OkHttpClient` with sane timeouts; downloads close their streams (`use {}`); body guards against `null` responses.
- **Network security**: cleartext traffic disabled globally except `api.auroraoss.com` (documented exception); removed the `usesCleartextTraffic` override.
- `AlarmReceiver` is no longer `exported` (it only receives app-internal broadcasts).
- `MissingTranslation`/`ExtraTranslation` lint noise handled instead of disabling all of lint.

### Removed
- **Firebase** (config + dependency) — not used.
- **ACRA** crash reporting (dependency + initialisation) — see [Privacy](PRIVACY.md).
- `google-services.json` and the unused CRC-16 utility (dead code).
- Unused `lifecycle-extensions` dependency.
- Dead code: `Int.dp`/`Int.px` helpers (wrong formula and unused), `AppUpdate.ad` field, `AppInstalled`'s index parameter.
