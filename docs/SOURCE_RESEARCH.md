# Source feasibility research

Status: catalogue feasibility is confirmed for ZONA and partly confirmed for KinoBesplatno. No source is approved for direct stream extraction or direct playback yet. Checked 2026-11-12.

## ZONA

- Current user-confirmed source: https://w140.zona.plus/
- Sample movie: https://w140.zona.plus/movies/odisseya-2026
- Sample series: https://w140.zona.plus/tvseries/ukrytie-2023
- Sample season pages:
  - https://w140.zona.plus/tvseries/ukrytie-2023/season-1
  - https://w140.zona.plus/tvseries/ukrytie-2023/season-2
- Public catalogues:
  - https://w140.zona.plus/movies
  - https://w140.zona.plus/tvseries
- Sample movie page inspection (2026-11-12): title "Одиссея", original title "The Odyssey", year 2026, age label 18+, ratings displayed as 8.2 / 8.1 / 8.4, countries Iceland, New Zealand, Canada, USA, United Kingdom; director Christopher Nolan; screenplay Christopher Nolan and Homer; cast list; duration 2h 46m; premiere 6 July 2026; synopsis and poster image URL are present in the public page.
- The movie page contains a visible "Смотреть фильм" section, but the extracted static HTML did not expose player option labels, iframe URLs, or a direct media URL. The fetch returned no iframe links. Therefore the reported "Player 1" / "Player 2 (4K)" choices are still NOT independently verified on this exact movie page. A browser-rendered/user-facing inspection is required to confirm their labels, selection behaviour, and whether quality options are distinct sources or only player variants.
- Series detail page exposes title ("Укрытие"), original title (Silo), year, ratings, genres, country, director, writers, cast, premiere date, synopsis, poster/backdrop metadata, and season navigation.
- The sample series advertises 3 seasons. Season 1 and season 2 pages each expose 10 episode names; season 3 is linked from the title page.
- Public movie catalogue exposes genre/year/country/rating filters, sort options (popularity, rating, release date), and movie detail links.
- Public series catalogue and updates pages expose lists of title links and ratings.
- Inspection returned no iframe links and no directly exposed media URL in the parsed HTML. This is not proof that the interactive browser player has no playback; it means a direct stream path has NOT been established by this inspection.
- Catalogue metadata and season/episode navigation appear technically parseable. Playback must be investigated separately using the site's permitted, user-facing flow. Do not defeat verification, DRM, authentication, paywalls, or other access controls.

## KinoBesplatno

- Sample series page: https://kinobesplatno.online/serialy/legend-of-zang-hai-2025
- Public series catalogue: https://kinobesplatno.online/serialy/
- Public movie catalogue: https://kinobesplatno.online/filmy/
- The series page exposes title, original title, year, country, ratings, cast, synopsis, poster/backdrop assets, and a 40-episode season list.
- The series listing exposes sorting choices (publication date, update date, year, rating, KP, IMDb, title), category routes, and page links such as ?page=2 and ?page=3. It reported 5,693 series at the time of inspection.
- Movie and series catalogue pages expose card titles, year, country, genres, synopsis, and poster URLs.
- The title page advertises an embed endpoint via Open Graph/Twitter metadata: https://kinobesplatno.online/serialy/legend-of-zang-hai-2025?embed=player
- Fetching that embed endpoint currently returns an instruction to complete a human tile verification ("Нажмите плитку с кинопленкой"). It does not expose a video iframe or media URL in the inspected response.
- Catalogue parsing looks technically feasible, but automated playback resolution is NOT established. Do not automate or bypass human verification. Determine whether a documented/authorized integration exists or use an external hand-off to the site's own page/player.

## Required evidence per source

Record the date checked, exact page URL, HTTP outcome, page type, visible metadata, pagination, episode structure, player choices, media type if openly exposed, subtitle/audio options, device playback result, and relevant terms/licensing restrictions.

Do not record guessed stream URLs. Do not evade bot protection, human verification, login requirements, DRM, paywalls, or access controls. If a site only supports its own player, document an external hand-off rather than extracting a protected stream.

## Test matrix

| Test | ZONA | KinoBesplatno |
|---|---|---|
| Public catalogue accessible | CONFIRMED | CONFIRMED |
| Search/category navigation | PARTLY OBSERVED | PARTLY OBSERVED |
| Movie details and artwork | CONFIRMED: sample movie page metadata and poster exposed | PARTLY OBSERVED |
| Series seasons and episodes | CONFIRMED: 3 seasons; sample seasons 1 and 2 each list 10 episodes | CONFIRMED: sample lists 40 episodes |
| Player choices (e.g. standard / 4K) | NOT CONFIRMED by static page extraction | NOT ESTABLISHED |
| Playback page / player path | NOT ESTABLISHED | EMBED PAGE RETURNS HUMAN VERIFICATION |
| Authorized direct playback path | NOT ESTABLISHED | NOT ESTABLISHED |
| Media3 playback on Google TV | NOT TESTED | NOT TESTED |
| Terms/access model reviewed | NOT TESTED | NOT TESTED |

## Decision rule

Proceed with public catalogue adapters only after reviewing the relevant site terms and access policy. Keep direct playback resolution disabled until a stable, permitted path is established. If a source only supports its own player, use an external hand-off or exclude playback integration. Do not mark a source READY until the complete flow has passed real-device tests.
