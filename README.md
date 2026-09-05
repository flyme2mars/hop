# Hop

Offline-first hostel floor board for dead zones and power cuts (OffGrid).
No accounts. No internet required for core use.

## What this build does

- **Floor board:** Offer / Ask / Note, filter chips, claim, remove own (with confirm).
- **Persistence:** Room for posts and claim state. DataStore for name, room, floor, keep-screen-on, a stable self id, and blackout session. First launch seeds the demo posts once; after that the board is real local data and survives restart.
- **History:** Claimed posts from Room.
- **Blackout:** Timer starts on enter. I’m OK / Need help stay for that session and survive process death. Keep-screen-on from Settings is honored. Exit fades back to Floor.
- **Nearby (1-hop BLE):** Advertise + scan for Hop peers on the same floor name. Stable peer ids (not rotating MACs). Floor always shows a peer list: `name · room` once known, or **Phone nearby** plus a short id. Alone is **searching** / **Nobody nearby** with an empty list — never a naked count. One-tap Allow nearby and Turn on Bluetooth. Two-way chunked GATT exchange of the full board (create / update / claim, newest `updatedAt` wins). Periodic resync while Floor is open. Not a multi-hop mesh. Nearby works in the foreground; Android may stop BLE in the background.
- **Settings:** Edit name / room / floor (persisted), About Hop, offline help, background BLE limit, version.

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

USB sideload from a computer:

```bash
adb install -r app-debug.apk
```

## Two-phone nearby test

Need two Android phones. No internet. Same Hop APK (`hop-debug`).

1. Sideload Hop on both phones and keep both **on Floor** (app open, screen on).
2. On each phone, enter a **name**, **room**, and the **same Floor** (example: `2`). Tap Continue / Save.
3. Grant nearby Bluetooth when asked (location only on Android 11 and older). If the subtitle says **needs Bluetooth**, tap **Turn on Bluetooth**.
4. Hold the phones a few meters apart. Within about 15 seconds each Floor should list the other phone — **Name · Room** after GATT, or **Phone nearby** plus a short id before the name arrives. The subtitle may say **1 nearby** only when that row is visible. Alone must stay **searching** / **Nobody nearby** with an empty list, never a phantom **2 nearby**. A third Hop phone on that floor adds a second named row. A phone on a different floor name must not appear.
5. On phone A, create an Offer (title + body) and wait up to ~15 seconds. The same post should appear on phone B’s Floor without internet.
6. On phone B, Claim that offer. On phone A it should move to History after the next sync.
7. Force-stop Hop on phone A and reopen. The local post and claim state are still there (Room). Nearby starts again only while the app is open.

If count stays **searching**, confirm both are on Floor, same floor name, Bluetooth on, and permissions granted. Hop does not scan in the background.

Debug builds are unsigned for Play Store use; they are meant for personal install and CI verification only.
