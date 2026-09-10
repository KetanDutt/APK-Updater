# Privacy

APK Updater has **no analytics, no crash reporting, no ad SDKs and no accounts**. Everything below is the complete list of network communication the app performs, all of it initiated by you (installing/finding updates) or by the schedule you configure.

## What leaves your device

| Request | Sent to | Payload | Why |
|---|---|---|---|
| Update check / search | `f-droid.org` | none (GET/HEAD of the public repo index) | F-Droid source |
| Update check | `ws75.aptoide.com` | list of *package names*, *installed version codes* and the **SHA-1 of each app's signing certificate** | Aptoide's `listAppsUpdates` API requires the signature to resolve which variant you have installed. The SHA-1 identifies the signing key, not you. |
| Update check | `www.apkmirror.com` | list of package names (+ User-Agent `APKUpdater-v<version>`) | APKMirror's public `app_exists` endpoint. |
| Update check / page loads | `apkpure.com` | search terms / package links only | HTML scraping. |
| Update check / install | `ws.google.com` + `api.auroraoss.com` | device build identifiers (build ID, security patch level, model) | Google Play source via the [Aurora Store](https://auroraoss.com) anonymous token flow. |
| Self-update check | `raw.githubusercontent.com` (this repo) | none (GET of `version.json`) | Once per day. |
| Install | whatever host serves the APK you chose | none | Download of the APK itself. |

**Not sent:** app usage data, location, contact data, advertising identifiers, any form of personal or usage telemetry.

## Stored data

- All app state (settings, ignore list, last update list) is stored **locally only** via SharedPreferences (`KryptoPrefs` in `nocrypt` mode — the values are not secret, so they are not encrypted).
- The F-Droid index is cached in the app's cache directory and is cleared with the app cache.
- Downloaded APKs are written to the app's external files directory (or a `FileProvider`-shared location) only long enough to hand them to the system installer.

## Network security

- `android:usesCleartextTraffic` is **not** set; the default (deny) applies from API 28+ and a `networkSecurityConfig` enforces it from minSdk: **all cleartext traffic is blocked** except the single domain `api.auroraoss.com`, which is the Aurora Store token dispenser and only supports plain HTTP.
- Everything else is HTTPS with the system certificate store only.

## Credentials in the code

The APKMirror source uses a **public, shared API account** (`api-apkupdater`) belonging to the upstream APK Updater project — it is not a personal credential and is visible in this repository by design. If you run this code against a production APK, consider that this identifier is shared with all other users of the upstream project.

## Dependencies with privacy implications

- **Firebase, ACRA/crash reporters, and analytics SDKs are not present** in this codebase (they were removed).
- [Aurora Store library](https://github.com/AuroraOSS/AuroraStore) (GPL, vendored in `app/src/main/java/com/rtctek/apkupdater/util/aurora/`) — contactless Google Play access; communicates only with Google and its own token endpoint as described above.

## Licensing

APK Updater is released under **GPL v3**. If you distribute a modified build, you must provide the corresponding source, including any changes to the vendored Aurora files. See [LICENSE](../LICENSE).
