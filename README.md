# PashaFlix

A clean-room Android TV and Google TV movie/series discovery and playback app.

## Product goals

- Native Kotlin application; no Jellyfin, JellyPlay, OwnTV, or other application fork.
- TV-first interface with D-pad navigation and clear focus states.
- Separate source adapters for each supported catalogue.
- One normalized catalogue model for movies, series, seasons, and episodes.
- Playback through AndroidX Media3 when a source exposes a compatible, authorized stream.
- Local favourites and watch progress.

## Current status

Project bootstrap and source feasibility research. No source adapter or playback source is considered working until it has been tested end-to-end on a real device.

## Planned stack

- Kotlin + Jetpack Compose for TV
- AndroidX Media3 ExoPlayer
- OkHttp + Jsoup for permitted public-page parsing
- Room + DataStore
- GitHub Actions for reproducible APK builds

See [Architecture](docs/ARCHITECTURE.md) and [Source research](docs/SOURCE_RESEARCH.md).


## Android app scaffold

The `feature/bootstrap` branch now contains the first native Android application scaffold:

- Kotlin + Jetpack Compose UI.
- Android TV / Leanback launcher entry point.
- Embedded WebView fallback for the public ZONA movie page.
- A diagnostic WebView request observer that classifies visible media-shaped requests (HLS, DASH, MP4, TS, M4S) without persisting full URLs or credentials.
- Media3 dependencies and an explicit external-playback host policy. The host allowlist is empty until a source explicitly authorizes external playback.
- GitHub Actions workflow to build and upload a debug APK.

The observer is diagnostic, not a stream extractor. It does not export third-party media URLs, cookies, signed query parameters, or authorization headers into Media3. For ZONA, the first implementation keeps playback inside the site's own WebView player unless a permitted external playback interface is established.

Build status: pending first GitHub Actions run. No APK is claimed until CI succeeds.


## Native video-library interface

The app UI is being changed from a browser-first prototype to a native library:
- Home screen with named source tiles (no address bar or manual URL entry).
- Separate movie and series catalogues for active sources.
- Native title cards and detail screen.
- Dedicated playback screen without browser navigation chrome.
- Current active catalogue adapters: ZONA and KinoBesplatno.
- Other known sources (Kinogo, HDRezka, Filmix, Baskino, LordFilm, Seasonvar, Kinokrad) remain inactive until individually integrated and tested.

See [Native library UX](docs/NATIVE_LIBRARY_UX.md).
