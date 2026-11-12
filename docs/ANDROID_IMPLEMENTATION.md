# First Android implementation

Branch: `feature/bootstrap`
Package: `com.smart222tech.pashaflix`
Version: `0.1.0`

## Included

- Native Android application entry point with Compose.
- Leanback launcher category and touchscreen-optional manifest declarations.
- Default ZONA sample page: `https://w140.zona.plus/movies/odisseya-2026`.
- WebView playback fallback using the site's own player.
- Request observer that classifies URL path extensions for HLS, DASH, MP4, MPEG-TS, and M4S.
- UI displays only media kind, host, and a short path hint. Full URL and query parameters are not retained.
- Media3 ExoPlayer dependencies and a host-based external-playback policy, disabled by default.

## Current limitation

Android WebView's `shouldInterceptRequest` callback is a request observer, not a complete browser DevTools protocol. It cannot guarantee visibility into every media request (for example, some service-worker, encrypted, blob, MSE, or player-internal paths). A request extension is only a format hint; it does not prove the resource is a playable manifest or media file.

The ZONA page has one visible browser player in the user's screenshot. This app therefore does not invent a second player or a 4K toggle. The WebView is the playback fallback. External Media3 hand-off stays disabled until the provider's terms or documentation establish that external playback is permitted and the source can be integrated without defeating access controls.

## Build

GitHub Actions workflow: `.github/workflows/android.yml`
Command: `gradle --no-daemon :app:assembleDebug`

The first CI run must pass before treating the APK as installable.
