# Hop

Offline-first hostel floor mesh for dead zones and power cuts (OffGrid). Scaffold only — do not invent BLE/mesh/Cut product code unless asked.

## Cloud

- JDK 17+ is required. The install script adds OpenJDK 17 for Gradle `jvmToolchain(17)` (a newer default JDK is fine).
- `.cursor/environment.json` runs `.cursor/install-android-sdk.sh`, which installs Android SDK cmdline-tools, platforms 36/37, and build-tools 36.0.0 into `$HOME/Android/Sdk`.
- After install: `export ANDROID_HOME=$HOME/Android/Sdk` (the script persists this in `~/.bashrc`) and run `./gradlew assembleDebug`.
