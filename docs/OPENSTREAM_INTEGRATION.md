# OpenStream-inspired integration for PashaFlix

PashaFlix remains a clean-room native application. This branch adapts the declarative extension and repository concepts documented by Ivorisnoob/OpenStream under its MIT license. It does not copy the OpenStream application wholesale or import provider-specific scrapers.

## Included in this integration branch

- Declarative source models: SourceRepository, SourceExtension and SourceEngine.
- JSON repository loader supporting inline extensions and extension-list references.
- HTTPS-only repository and manifest URLs.
- A strict engine allowlist: catalog-only and Stremio.
- User repository URL persistence using SharedPreferences.
- Stremio add-on manifest and direct HTTPS stream client.
- Bundled PashaFlix registry describing existing ZONA and KinoBesplatno catalogue adapters.
- No remote executable code, dynamic DEX loading, torrent engine, DRM circumvention or human-verification bypass.

## Stremio protocol subset

The client reads manifest.json and supports movie and series stream resources, direct HTTPS media URLs, request headers declared in behaviorHints.proxyHeaders.request, and HTTPS subtitle URLs.

Torrent infoHash entries, YouTube, external-page links, DRM streams and non-HTTPS media URLs are not treated as playable Media3 items. Add-on compatibility depends on declared ID prefixes and supported resources; callers must inspect the manifest before requesting streams.

## Security

- Repository and manifest URLs must use HTTPS.
- Repository documents and add-on responses have size limits.
- Third-party JSON can only select engines shipped in the APK.
- Downloaded plugin binaries and executable scripts are not accepted.
- Authentication, paywalls, DRM, anti-bot controls and provider access controls are not bypassed.
- Direct playback is enabled only where the provider/add-on supplies an authorized direct media URL.

## Follow-up integration work

1. Add marketplace screens (Browse / Installed / Repositories) to existing native navigation.
2. Connect the registry to existing DetailedCatalogSource catalogue adapters.
3. Add an explicit Stremio source adapter to the playback resolver.
4. Map TMDB/IMDb identifiers and verify manifest types, resources and ID prefixes.
5. Add Media3 playback options, subtitle selection, audio-track preference and failover.
6. Add unit tests and device tests on Android phone and Fire TV / Google TV.

The bundled ZONA and KinoBesplatno entries are catalogue-only. They are not claims of direct Media3 playback support.
