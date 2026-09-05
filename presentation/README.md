# Hop OffGrid deck

Hackathon presentation for Hop. Quiet seminar-bar slides, not a pitch template.

## Files

| File | What |
| --- | --- |
| `Hop-OffGrid.pptx` | 16:9 deck (13.333 in × 7.5 in), 7 slides |
| `Hop-OffGrid.pdf` | Same layout, fonts embedded |
| `generate_deck.py` | Source of truth for both |
| `fonts/` | Inter + Libre Baskerville (OFL) |

## Generate

```bash
python3 -m pip install -r presentation/requirements.txt
python3 presentation/generate_deck.py
```

Writes `Hop-OffGrid.pptx` and `Hop-OffGrid.pdf` next to the script. PNG previews land in `presentation/qa/` (gitignored).

## House style

Paper `#F5F3EF`, charcoal title/close `#121212`, panels `#EEECE8` with no stroke or shadow. Ink is charcoal, not black. Kickers and footer are mid gray.

Serif (Libre Baskerville) for slide titles and italic labels. Inter for body, kickers, and footer. Three sizes only: 32 / 16 / 11 pt. Footer is `AKSHAI KRISHNA S` left, `NN / 07` right.

## Slides

1. Hop · Your floor, offline. · OffGrid · solo · open source
2. Problem: dead zones, power cuts, WhatsApp needs the tower
3. Solution: local Offer / Ask / Note board, Bluetooth nearby sync, blackout mode
4. Features: setup, board, claim, history, settings, blackout, nearby sync, offline alone
5. Stack: Kotlin, Jetpack Compose, Material 3, Room, DataStore, BLE + GATT, GitHub Actions debug APK
6. Future: multi-hop mesh, peer UX, background / battery, iOS if ever needed
7. Close

Facts only. No invented metrics, competitors, or timelines.
