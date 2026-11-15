# PashaFlix architecture

## Principles

1. Build a native application from an empty Android project. Do not import or fork a media-server/client application.
2. Keep catalogue discovery separate from playback resolution.
3. Isolate each website behind its own adapter. A change to one site must not break another.
4. Normalize source-specific data into domain models before it reaches the UI.
5. Treat every source as unverified until catalogue, detail page, playback, seeking, and TV remote controls pass device tests.
6. Use only content and streams that the user is authorized to access. Do not bypass DRM, authentication, paywalls, access controls, or anti-bot protections. If direct playback is not permitted or technically exposed, provide the source page as an external hand-off instead.

## Modules / packages

- app: application entry point, dependency wiring, navigation
- core/model: Movie, Series, Season, Episode, Artwork, PlaybackOption
- core/network: HTTP client, request policy, parsing utilities
- core/database: Room entities, DAOs, migrations
- core/preferences: DataStore settings
- feature/home: home screen and continue-watching rail
- feature/catalog: movie and series grids, categories, pagination
- feature/details: metadata and playback options
- feature/search: catalogue search
- feature/favorites: saved titles
- feature/player: Media3 playback UI and session
- source/api: SourceAdapter contract and capability declarations
- source/zona: isolated ZONA adapter (only after feasibility review)
- source/kinobesplatno: isolated KinoBesplatno adapter (only after feasibility review)

## Domain contracts

A source adapter should expose:
- source identity and declared capabilities
- catalogue/category retrieval
- title search
- title details
- season and episode listing where applicable
- playback-option discovery, returning only documented/authorized options

The adapter must not leak HTML nodes or source-specific JSON into the UI. Map all results to domain models.

A PlaybackOption should describe a supported media URL or an authorized external player/page hand-off, media type when known, headers only when legitimately required, and source attribution. Never persist short-lived signed stream URLs as permanent catalogue data.

## Playback

Use AndroidX Media3 ExoPlayer for supported HLS, DASH, and progressive media. Validate MIME/container/codec compatibility and subtitle availability. Do not assume that an embedded web player exposes a reusable stream. DRM-protected content must use its official supported playback path; no DRM circumvention.

## TV UX

- Compose for TV; D-pad-first navigation.
- Visible focus state and deterministic focus restoration.
- No hover-only or touch-only actions.
- Back handling, loading/error/empty states, retry, and offline-safe favourites.
- Validate on Google TV / Android TV hardware, not only an emulator.

## Quality gates

A source is marked READY only after:
1. catalogue loads;
2. details and artwork load;
3. title/episode selection works;
4. playback starts on target hardware;
5. pause/resume/seek and audio/subtitle selection are tested where supported;
6. errors are surfaced without crashing;
7. the source terms and access model permit the integration.
