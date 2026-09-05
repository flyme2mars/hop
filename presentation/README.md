# Hop OffGrid deck

Hackathon presentation for Hop. Quiet seminar-bar slides, not a pitch template.

## Files

| File | What |
| --- | --- |
| `Hop-OffGrid.pptx` | 16:9 deck (13.333 in × 7.5 in), 9 slides |
| `Hop-OffGrid.pdf` | Same layout, fonts embedded |
| `generate_deck.py` | Source of truth for both |
| `fonts/` | Inter + Libre Baskerville (OFL) |
| `screenshots/` | Night-stage Hop UI stills for the deck |

## Screenshots

Sharp 1080×2400 PNGs of the live night-stage composables from PR #6 head `27e579c` (`cursor/hop-offline-board-1207`).

| File | Screen |
| --- | --- |
| `screenshots/launch.png` | Launch / setup — Name / Room / Floor |
| `screenshots/floor.png` | Floor board with seed posts visible |
| `screenshots/sheet.png` | New post sheet open over the floor |
| `screenshots/blackout.png` | Blackout / power cut |
| `screenshots/nearby.png` | Floor with Nobody nearby + empty peer list |

**How captured:** Compose Preview Screenshot Testing of the real #6 composables (`LaunchScreen`, `FloorScreen` + home chrome, New post sheet body, `BlackoutScreen`), forced `HopTheme(darkTheme = true)`. Seed posts are `defaultSeedPosts()` from `HopBoard.kt`. Nearby is an honest Ready + empty peer list (`Nobody nearby` / `searching`).

This VM has `/dev/kvm` but nested KVM hits a kernel BUG, so hardware emulation dies. Software TCG did boot an ATD image and install the PR #6 CI APK (`adb` tree showed Hop / Name / Room / Floor), but `screencap` stayed a black framebuffer. A windowed Pixel 6 Google APIs AVD was still a grey boot after several minutes of TCG. Host-side LayoutLib is the honest fallback — not invented mockups. `ModalBottomSheet` popups do not paint in LayoutLib, so `sheet.png` uses the same New post body widgets (`HopChip`, `HopTextField`, Post) on the M3 sheet surface + scrim over the floor.

## Generate

```bash
python3 -m pip install -r presentation/requirements.txt
python3 presentation/generate_deck.py
```

Writes `Hop-OffGrid.pptx` and `Hop-OffGrid.pdf` next to the script. PNG previews land in `presentation/qa/` (gitignored).

## House style

Paper `#F5F3EF`, charcoal title/close `#121212`, panels `#EEECE8` with no stroke or shadow. Ink is charcoal, not black. Kickers and footer are mid gray.

Serif (Libre Baskerville) for slide titles and italic labels. Inter for body, kickers, and footer. Three sizes only: 32 / 16 / 11 pt. Footer is `AKSHAI KRISHNA S` left, `NN / 09` right. Kickers use `01 · PROBLEM` spacing. One left edge and even panel gaps on every paper slide.

`generate_deck.py` loads the five PNGs from `screenshots/` and places them on two dedicated slides. No invented UI.

## Slides

1. Hop · Your floor, offline. · OffGrid · solo · open source
2. Problem: dead zones, power cuts, WhatsApp needs the tower
3. Solution: local Offer / Ask / Note board, Bluetooth nearby sync, blackout mode
4. Features: setup, board, claim, history, settings, blackout, nearby sync, offline alone
5. Screens: first open, floor board, new post (`launch`, `floor`, `sheet`)
6. Screens: blackout and nearby (`blackout`, `nearby`)
7. Stack: Kotlin, Jetpack Compose, Material 3, Room, DataStore, BLE + GATT, GitHub Actions debug APK
8. Future: multi-hop mesh, peer UX, background / battery, iOS if ever needed
9. Close

Facts only. No invented metrics, competitors, or timelines.
