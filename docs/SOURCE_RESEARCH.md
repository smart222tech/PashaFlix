# Source feasibility research

Status: IN PROGRESS. No source is approved for direct playback yet.

## Candidate sources

### ZONA
- User-provided historical reference: https://telegra.ph/ZONA-ZONA-NETFLIX-DISNEY-AMAZON-PRIME-KINO-I-TD-12-12
- User previously described a movie page with multiple player choices, including a 4K option.
- Need exact current public catalogue URL and one working title page from the user.
- Need to establish whether catalogue metadata and playback options are accessible through documented/public interfaces and whether embedding or direct playback is permitted.

### KinoBesplatno
- Historical sample supplied in the conversation: https://kinobesplatno.online/serialy/legend-of-zang-hai-2025#watch
- Need a current working movie page and series page from the user.
- Need to establish page structure, episode navigation, player hand-off, and permitted playback integration.

## Required evidence per source

Record the date checked, exact page URL, HTTP outcome, page type, visible metadata, pagination, episode structure, player choices, media type if openly exposed, subtitle/audio options, device playback result, and any relevant terms/licensing restrictions.

Do not record guessed stream URLs. Do not attempt to evade bot protection, login requirements, DRM, paywalls, or access controls. If a site only supports its own browser/player experience, document an external hand-off rather than extracting a protected stream.

## Test matrix

| Test | Result |
|---|---|
| Public catalogue accessible | NOT TESTED |
| Search/category navigation | NOT TESTED |
| Movie details and artwork | NOT TESTED |
| Series seasons and episodes | NOT TESTED |
| Playback options discoverable | NOT TESTED |
| Authorized playback path identified | NOT TESTED |
| Media3 playback on Google TV | NOT TESTED |
| Terms/access model reviewed | NOT TESTED |

## Decision rule

Implement the first adapter only after the evidence above establishes a stable, permitted integration path. Otherwise, keep the source as a catalogue link / external hand-off or exclude it.
