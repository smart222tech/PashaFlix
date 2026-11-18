# Atodo APK

A minimal Android application that opens the original Atodo TV portal directly:

- URL: https://msx.atodo.fun
- No browser address bar or extra in-app navigation UI.
- Fullscreen WebView.
- Android TV / Fire TV launcher support.
- D-pad, OK/Enter and Back key handling.
- Touchscreen launch support on Android phones and tablets.

## Build

The GitHub Actions workflow **Build Atodo APK** builds a universal debug APK for Android devices.

Download the artifact named **Atodo-Android-APK** from the completed workflow run.

## Important

This application is a WebView wrapper. The catalog, interface, video sources and playback are provided by the Atodo website itself. The phone build displays the portal's existing TV-oriented interface; it does not add a separate mobile layout.
