# Hop

Offline-first hostel floor mesh for dead zones and power cuts (OffGrid).

This repository contains a Kotlin + Jetpack Compose Android app. The current build is a **UI shell with fake floor posts** — Bluetooth, mesh protocol, and radio are not implemented.

## What you can sideload

Material You shell following the “Pixel floor, candle blackout” lock:

- Everyday: dynamic color, system light/dark, mono fallback, primary on FAB/CTAs only
- Floor board with 8 fake posts, filter chips, post sheet, claim sheet
- Cut: true black, candle clock, **I’m OK** / **Need help**
- History and Settings
- Nearby shown as a quiet “n nearby” chip — not a banner

## Open in Android Studio

1. Install [Android Studio](https://developer.android.com/studio) (current stable) with the Android SDK.
2. **File → Open** and select this repository root (the folder that contains `settings.gradle.kts`).
3. Let Gradle sync. Android Studio will write a local `local.properties` with `sdk.dir`.
4. Run the `app` configuration on a device (`com.flyme2mars.hop`).

JDK 17+ is required. Android Studio’s bundled JDK is fine.

## Build a debug APK locally

```bash
./gradlew assembleDebug
```

The APK is written to:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Download the CI APK and sideload

GitHub Actions builds the same debug APK on every push to `main` and on pull requests. Artifact name: **`hop-debug`**.

1. Open the Actions run for the commit or pull request.
2. Download the **`hop-debug`** artifact and unzip it. You should get `app-debug.apk`.
3. Copy the APK to a phone (USB, Drive, or `adb`).
4. On the phone: enable **Install unknown apps** for the installer you use (Files, Chrome, etc.).
5. Open the APK and install **Hop**.

USB sideload from a computer:

```bash
adb install -r app-debug.apk
```

Debug builds are unsigned for Play Store use; they are meant for personal install and CI verification only.

Onboarding is stored in app prefs. Clear Hop’s storage to see it again.

## License

MIT. See `LICENSE`.
