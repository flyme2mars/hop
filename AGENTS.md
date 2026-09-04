# Hop

Offline-first hostel floor mesh for dead zones and power cuts (OffGrid).

The Compose UI shell (Floor, Cut, History, Settings, fake posts) is in the tree. Do not invent BLE/mesh protocol work unless asked. Local fake posts only unless a later task says otherwise.

## Cloud

- JDK 17+ is required. The install script adds OpenJDK 17 for Gradle `jvmToolchain(17)` (a newer default JDK is fine).
- `.cursor/environment.json` runs `.cursor/install-android-sdk.sh`, which installs Android SDK cmdline-tools, platforms 36/37, and build-tools 36.0.0 into `$HOME/Android/Sdk`.
- After install: `export ANDROID_HOME=$HOME/Android/Sdk` (the script persists this in `~/.bashrc`) and run `./gradlew assembleDebug`.
- No emulator is required. `assembleDebug` plus green CI (`hop-debug` APK artifact) is the proof bar.

## Cursor Cloud / multi-agent

- Investigate with Task `explore` / bash. Do not guess the tree.
- After a UI or feature claim, spawn the verifier (`.cursor/agents/verifier.md`). It must prove `./gradlew assembleDebug` or green Android CI — not author claims.
- Max 2 cloud workers per feature unless Chief or Akshai raise the cap.
- No arena, swarm, or orch TSV.
- One writer per branch. Do not pile onto someone else’s PR.
- No `gt`, rebase, or force-push.
