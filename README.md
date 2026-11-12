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
