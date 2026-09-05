# Hop

Offline-first hostel floor board for dead zones and power cuts (OffGrid).
No accounts. No internet required for core use.

## What this build does

- **Floor board:** Offer / Ask / Note, filter chips, claim, remove own (with confirm).
- **Persistence:** Room for posts and claim state. DataStore for name, room, floor, keep-screen-on, and a stable self id. First launch seeds the demo posts once; after that the board is real local data and survives restart.
- **History:** Claimed posts from Room, not a fake list.
- **Blackout:** Timer starts on enter. I’m OK / Need help stay for that session. Keep-screen-on from Settings is honored. Exit fades back to Floor.
- **Nearby MVP:** Real BLE advertise + scan for Hop peers on the same floor. The nearby count is live when Bluetooth and permissions allow. Otherwise the UI says Bluetooth off / permission needed / unavailable — never a fake number. Best-effort GATT exchange of a compact post snapshot when two Hop phones can connect. Full mesh gossip is not in this build.
- **Settings:** Edit name / room / floor (persisted), About Hop, one-line offline help, version.

## Open in Android Studio

1. Install [Android Studio](https://developer.android.com/studio) (current stable) with the Android SDK.
2. **File → Open** and select this repository root (the folder that contains `settings.gradle.kts`).
3. Let Gradle sync. Android Studio will write a local `local.properties` with `sdk.dir`.
4. Run the `app` configuration on a device (`com.flyme2mars.hop`). Nearby needs a real phone with Bluetooth.

JDK 17+ is required. Android Studio’s bundled JDK is fine.

## Build a debug APK locally

```bash
./gradlew assembleDebug
```

The APK is written to:

```
app/build/outputs/apk/debug/app-debug.apk
```

Unit tests:

```bash
./gradlew testDebugUnitTest
```

## Download the CI APK and sideload

GitHub Actions builds the same debug APK on every push to `main` and on pull requests. Artifact name: **`hop-debug`**.

1. Open the Actions run for the commit or pull request.
2. Download the **`hop-debug`** artifact and unzip it. You should get `app-debug.apk`.
3. Copy the APK to a phone (USB, Drive, or `adb`).
4. On the phone: enable **Install unknown apps** for the installer you use (Files, Chrome, etc.).
5. Open the APK and install **Hop**.
6. On first Floor visit, tap **Allow nearby** if you want a live peer count. Grant Bluetooth (and location on Android 11 and older). Toggle Bluetooth if the subtitle says it is off.

USB sideload from a computer:

```bash
adb install -r app-debug.apk
```

Debug builds are unsigned for Play Store use; they are meant for personal install and CI verification only.
