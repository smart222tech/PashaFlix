# Source feasibility research

Status: IN PROGRESS. Catalogue feasibility is partly confirmed for KinoBesplatno. No source is approved for direct playback yet. Checked 2026-11-12.

## ZONA

- Historical user-provided reference: https://telegra.ph/ZONA-ZONA-NETFLIX-DISNEY-AMAZON-PRIME-KINO-I-TD-12-12
- The Telegraph page currently returns a DELETED page, so it cannot establish the present-day source URL, page structure, or playback flow.
- User previously described a movie page with multiple player choices, including a 4K option. This is historical user observation, not a current verification.
- NEED FROM USER: current direct ZONA catalogue/title URL(s), ideally one movie and one series page, and the exact domain/app name if "ZONA" refers to a separate app or website.

## KinoBesplatno

- Sample series page: https://kinobesplatno.online/serialy/legend-of-zang-hai-2025
- Public series catalogue: https://kinobesplatno.online/serialy/
- Public movie catalogue: https://kinobesplatno.online/filmy/
- The series page exposes title, original title, year, country, ratings, cast, synopsis, poster/backdrop assets, and a 40-episode season list.
- The series listing exposes sorting choices (publication date, update date, year, rating, KP, IMDb, title), category routes, and page links such as ?page=2 and ?page=3. It reported 5,693 series at the time of inspection.
- Movie and series catalogue pages expose card titles, year, country, genres, synopsis, and poster URLs.
- The title page advertises an embed endpoint via Open Graph/Twitter metadata: https://kinobesplatno.online/serialy/legend-of-zang-hai-2025?embed=player
- Fetching that embed endpoint currently returns an instruction to complete a human tile verification ("Нажмите плитку с кинопленкой"). It does not expose a video iframe or media URL in the inspected response.
- This means catalogue parsing looks technically feasible, but automated playback resolution is NOT established. We must not automate or bypass the human verification. We need to determine whether the site offers a documented/authorized integration or whether playback must be handed off to the site's own page/player.

## Required evidence per source

Record the date checked, exact page URL, HTTP outcome, page type, visible metadata, pagination, episode structure, player choices, media type if openly exposed, subtitle/audio options, device playback result, and any relevant terms/licensing restrictions.

Do not record guessed stream URLs. Do not evade bot protection, human verification, login requirements, DRM, paywalls, or access controls. If a site only supports its own browser/player experience, document an external hand-off rather than extracting a protected stream.

## Test matrix

| Test | ZONA | KinoBesplatno |
|---|---|---|
| Public catalogue accessible | NEED URL | CONFIRMED |
| Search/category navigation | NOT TESTED | PARTLY OBSERVED |
| Movie details and artwork | NEED URL | PARTLY OBSERVED |
| Series seasons and episodes | NEED URL | CONFIRMED: 1 season / 40 episodes sample |
| Playback page accessible | NEED URL | EMBED PAGE RETURNS HUMAN VERIFICATION |
| Authorized direct playback path | NOT ESTABLISHED | NOT ESTABLISHED |
| Media3 playback on Google TV | NOT TESTED | NOT TESTED |
| Terms/access model reviewed | NOT TESTED | NOT TESTED |

## Decision rule

Implement the KinoBesplatno catalogue adapter only for public catalogue metadata after checking the site's terms and access policy. Keep playback resolution disabled until a stable, permitted path is established. If only the site's own verified player is supported, use an external hand-off or exclude playback integration. Implement ZONA only after the current source URLs and access model are supplied and verified.
