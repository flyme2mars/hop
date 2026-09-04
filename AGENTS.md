# Hop

Offline-first hostel floor mesh for dead zones and power cuts (OffGrid). Scaffold only — do not invent BLE/mesh/Cut product code unless asked.

## Cloud

- JDK 17+ is required. The install script adds OpenJDK 17 for Gradle `jvmToolchain(17)` (a newer default JDK is fine).
- `.cursor/environment.json` runs `.cursor/install-android-sdk.sh`, which installs Android SDK cmdline-tools, platforms 36/37, and build-tools 36.0.0 into `$HOME/Android/Sdk`.
- After install: `export ANDROID_HOME=$HOME/Android/Sdk` (the script persists this in `~/.bashrc`) and run `./gradlew assembleDebug`.

## Cursor Cloud / multi-agent

- Investigate with Task `explore` / bash. Do not guess the tree.
- After a UI or feature claim, spawn the verifier (`.cursor/agents/verifier.md`). It must prove `./gradlew assembleDebug` or green Android CI — not author claims.
- Max 2 cloud workers per feature unless Chief or Akshai raise the cap.
- No arena, swarm, or orch TSV.
- One writer per branch. Do not pile onto someone else’s PR.
- No `gt`, rebase, or force-push.

## Android / Compose / UI

When doing Android, Compose, or Hop UI work, follow project skills in `.agents/skills/` (official [android/skills](https://github.com/android/skills) pack plus design skills). Prefer `jetpack-compose-m3`, `styles`, `navigation-3`, `testing-setup`, `edge-to-edge`, `adaptive`, `android-cli`, and `base` when those folders exist; also use `sleek-design-mobile-apps`, `emil-design-eng`, `impeccable` (polish, critique, delight, quieter, distill, bolder), `frontend-design`, `design-taste-frontend`, and `ui-ux-pro-max`. Do not require SharedTransition morph experiments. The current android/skills snapshot has no `jetpack-compose-m3` or `base` folder; Wear Material 3 is `wear-compose-m3`.
