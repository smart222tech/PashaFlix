# Native video-library UX

PashaFlix is a video-library interface, not a general-purpose browser.

## User flow

1. Home screen shows named source tiles. No address field and no manual URL entry.
2. Selecting an active source opens its native catalogue.
3. Catalogue has separate Films and Series tabs.
4. Selecting a title opens a native detail screen.
5. Watch opens a dedicated playback screen without browser chrome or an address bar.
6. Android Back returns playback -> title details -> catalogue -> source home.

## Active catalogue adapters

- ZONA: public movie and series catalogue pages.
- KinoBesplatno: public movie and series catalogue pages.

The adapters currently parse publicly accessible listing HTML into normalized title, detail URL, and poster fields. Site markup may change; empty/error states are shown instead of fabricated titles.

## Planned sources (not yet connected)

Kinogo, HDRezka, Filmix, Baskino, LordFilm, Seasonvar, and Kinokrad are displayed as inactive placeholders until their public catalogue structure and permitted integration path are checked. Do not claim these sources are working before adapters and tests exist.

## Playback

The native detail screen opens a dedicated playback surface. The site's page is used only as the current fallback player surface; it is not exposed as the app's navigation/catalogue UI. A future provider-authorized playback adapter may pass a media item to Media3. No direct stream extraction, access-control circumvention, or fabricated playback links are part of this implementation.

## Device validation still required

- Android phone portrait/landscape.
- Google TV / Android TV remote focus and D-pad navigation.
- Poster loading and catalogue pagination/infinite scrolling.
- Fullscreen custom-view handling for HTML5 video.
- Actual playback and return navigation on device.
