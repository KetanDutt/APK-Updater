# Contributing

Thanks for helping out! This is a fork of [rumboalla/apkupdater](https://github.com/rumboalla/apkupdater), developed independently. Keep the app lightweight and privacy-respecting — if a change would add telemetry, an account requirement, or a heavy dependency, please open an issue first.

## Getting set up

1. Clone the repository.
2. Install **JDK 11** and the **Android SDK (platform 33, build-tools 33.0.1)**.
3. Build:

   ```bash
   ./gradlew build
   ```

   The first build downloads a lot; it needs network access to Google's Maven, Maven Central and JitPack.

4. (Optional) Release signing — see [README → Release signing](../README.md#release-signing).

## Code style

- Kotlin, 4-space tabs (match the surrounding files), no wildcard imports.
- One class per file; top-level utilities live in `util/Extensions.kt`.
- Naming follows the existing repositories: `FooRepository`, `FooUpdater`, `FooSearch`.
- Keep repositories on `ioScope` (shared `Dispatchers.IO` scope) and hop to the UI only via `LiveData`/`postValue`.
- New API models: give every field a default or make it nullable, and parse numbers with `toIntOrNull()` — see `model/aptoide/` for the pattern.

## The golden rule for sources

A source must **degrade, not crash**. A missing field, a 404, or a rewritten web page should reduce that source's results to zero — it must never take down the other sources or the app. When you touch a scraper, add the defensive handling, and prefer `runCatching` around per-item work.

## Pull requests

- Small, focused PRs are easier to review.
- Update `docs/CHANGELOG.md` for user-visible changes.
- If you change the network surface (new host, new payload), update `docs/PRIVACY.md` — the privacy doc is a promise.
- Don't bump `versionCode`/`versionName` in feature PRs; version bumps happen at release time.
- CI must be green: `./gradlew build` (debug + release, lint included).

## Releasing (maintainers)

1. Bump `versionCode` (and `versionName` for user-visible releases) in `app/build.gradle`.
2. Update `docs/CHANGELOG.md`.
3. Build a signed release:

   ```bash
   ./gradlew assembleRelease
   ```

4. Create a GitHub release **tagged with the version name** (e.g. `2.1.0`) and attach the APK as an asset named **`app-release.apk`** (the self-update URL depends on this exact name).
5. Update `version.json` on `main`:
   - `version` → the new **versionCode** (integer),
   - `apk` → the HTTPS URL of the release asset,
   - `changelog` → a short summary shown in the in-app dialog.
6. Push. Installed apps pick up the new manifest on their next daily self-update check.

The tag matters: `version.json` points at
`https://github.com/KetanDutt/APK-Updater/releases/download/<versionName>/app-release.apk`.

## License

Contributions are licensed under **GPL v3** (see [LICENSE](../LICENSE)). By submitting code you agree to this.
