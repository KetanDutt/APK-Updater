# Architecture

APK Updater is a classic MVVM Android app with three screens behind a `BottomNavigationView`, one Koin graph for dependency injection, and one coroutine scope per concern. This document describes how the pieces fit together, the threading model, and the deliberate fault-isolation strategy used for the update check.

## High-level flow

```
                ┌────────────────────────────────────────────────┐
                │                MainActivity                    │
                │  badges · swipe-to-refresh · self-update check │
                └──────────────┬─────────────────────────────────┘
                               │ observe (LiveData)
        ┌──────────────┬───────┴────────┬──────────────┬──────────────┐
   AppsFragment   UpdatesFragment  SearchFragment  SettingsFragment   (Compose)
   (viewModel)    (viewModel)      (viewModel)      (preferences)
        │                │               │
        ▼                ▼               ▼
  AppsRepository  UpdatesRepository  SearchRepository
                        │
        ┌───────┬───────┼────────┬─────────────┐
        ▼       ▼       ▼        ▼             ▼
     F-Droid  Aptoide APKMirror APKPure   Google Play
     (jar)    (JSON)  (JSON)    (HTML)    (Aurora API)
```

- **Activities/Fragments** hold UI state through `ViewModel`s (`MainViewModel`, `AppsViewModel`, `UpdatesViewModel`, `SearchViewModel`) exposed as `LiveData`.
- **Repositories** do all I/O. Each source has an *updater* (installed → newer version) and, where applicable, a *searcher* (query → apps).
- **`UpdatesRepository` / `SearchRepository`** fan out to the enabled sources in parallel and merge results.
- **Dependency injection** is Koin (`di/MainModule.kt`): repositories, `InstallUtil`, `NotificationUtil`, `AlarmUtil`, `AppPrefs` and the view models are singletons.

## Update check (the core path)

`UpdatesRepository.getUpdatesAsync()`:

1. Enumerates installed packages once (`AppsRepository.getPackageInfosFiltered(GET_SIGNATURES)`) and the displayable app list (`getAppsFiltered`).
2. Launches one `async` per enabled source. Each source returns `Deferred<Result<List<AppUpdate>>>` — a failure of one source never cancels the others.
3. Merges results under a `Mutex` and, if **Compare version names** is on, drops entries whose *version name* isn't actually higher than the installed one (guards against repackaged builds with a lower version name but higher version code).
4. Returns `Result.success` if every enabled source succeeded, `Result.failure(first error)` otherwise. The UI shows the partial list via the snackbar path only when nothing succeeded; otherwise it renders whatever came in.

### Per-source notes

| Source | Mechanism | Specificities |
|---|---|---|
| F-Droid | `index-v1.jar` from `f-droid.org/repo` | Cached in `cacheDir/fdroid`, refreshed at most once an hour, gated by a `HEAD` request comparing `Last-Modified` against a stored ETag-like value. Package lookups go through `Map`s built from the index (O(1) per app, not O(index) per app). |
| Aptoide | JSON API `listAppsUpdates` (POST, chunks of 100) | The request must carry the SHA-1 of each app's signing certificate. Apps signed only with v2/v3 schemes report an empty legacy `signatures` array on modern Android, so `signingInfo.apkContentsSigners` is preferred (API 28+) with a fallback. |
| APKMirror | Authenticated JSON API `app_exists` | Credentials are the public `api-apkupdater` account (see [Privacy](PRIVACY.md)). Results are filtered by device architecture, min SDK and experimental flags. |
| APKPure | HTML scraping (Jsoup) | Most fragile source, so every page parse is wrapped: a single malformed row or page yields *no results from APKPure* rather than a failure of the whole check. Concurrency is capped at 8 in-flight page fetches. Only reports a result when the remote version code is actually greater than the installed one. |
| Google Play | Aurora Store's anonymous token API (`ws.google.com` RPCs) | Uses a device profile (build ID, security patch level, …) obtained locally; a one-time token is fetched from the HTTP-only dispenser `api.auroraoss.com` (the single cleartext exception in the network security config). |

### Thread model

- One shared `ioScope = CoroutineScope(Dispatchers.IO)` (`util/Extensions.kt`) is the default host for repository work; everything blocking (OkHttp via fuel, Jsoup, file IO) runs on it.
- `UpdatesRepository`-level state (the merged list, the error list) is touched from several coroutines → guarded by a `Mutex`.
- The F-Droid index load uses *double-checked locking* (`@Volatile data` + `Mutex`) so concurrent update and search requests download the index at most once.
- UI updates hop back to the main thread through `LiveData`/`postValue` only; nothing repository-side touches views.
- `AlarmReceiver` uses `goAsync()` (API 26+) to get the 60-second extended broadcast window for the scheduled check, then `finish()`es in a `finally`.

## Self-update

`SelfUpdateRepository` fetches [version.json](../version.json) (repo root, served over HTTPS by raw.githubusercontent.com) at most once per day (timestamp in `AppPrefs`). If the manifest's `version` (a versionCode) is greater than `BuildConfig.VERSION_CODE` and the URL is non-blank, an `AlertDialog` shows the changelog; on **Install** the APK is downloaded and pushed to the system installer like any other update.

The manifest is part of the repository and is updated as part of the release process (see [README → Releasing](../README.md#releasing)).

## Settings & preferences

`AppPrefs` (KryptoPrefs, *nocrypt* mode) stores:

- source toggles, check schedule + time, theme (0–5), compare-version-names flag,
- architecture/min-API/experimental filters, root install toggle,
- the ignore list, the last F-Droid index `Last-Modified`, the last self-update check timestamp,
- the last update list (so the notification deep-link can restore it).

Theme selection is a full XML theme (`Theme.MaterialComponents[.Light]` + colour set); the activity is recreated on change. The Compose tab needs no extra dark-mode flag because `accompanist-themeadapter-material`'s `MdcTheme` reads the palette from the activity's XML theme.

## Error philosophy

The guiding rule: **a broken source degrades, it doesn't break the app.**

- Per-row parsing (scrapers, Gson models) is defensive: missing fields get defaults, unparseable version codes become `0`, unknown entries are skipped.
- Per-page / per-app work is wrapped in `runCatching` so one bad entry can't sink the source.
- Per-source results are `Result`s merged by the orchestrators; the first failure is surfaced in a snackbar while partial results are still shown when available.
- The only things that are allowed to crash the app are genuine programming errors, because there is no crash reporter — the cost of an unhandled crash is a lost check, not a data leak.

## Notable dependencies

| Dependency | Why |
|---|---|
| Kotlin 1.8.10 / AGP 7.4.2 / compileSdk 33 | Toolchain baseline (JDK 11). |
| [fuel (fork)](https://github.com/rumboalla/fuel) @ `0d7b26873c` (JitPack) | Kotlin HTTP client. The upstream `-SNAPSHOT` is no longer buildable on JitPack, so the exact commit the fork published from is pinned. |
| Koin 2.0.1 | DI (kept at the version the code was written against). |
| KryptoPrefs 0.4.3 (nocrypt) | Typed SharedPreferences wrapper; encryption is intentionally *off* (nothing stored is secret). |
| playstore-api-v2 (JitPack) | Aurora's Google Play RPC client (vendored wrapper in `util/aurora/`). |
| libsuperuser | Root install option. |
| versioncompare 1.5.0 | Semver-ish comparison for the "compare version names" filter. |
| Jetpack Compose (BOM 2023.01.00) + accompanist-themeadapter-material | The Apps tab. |
| Jsoup 1.13.1 | APKPure scraping. |
