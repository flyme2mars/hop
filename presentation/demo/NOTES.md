# Hop emulator demo recording

Live Android emulator session of Hop (`com.flyme2mars.hop`) on this Cursor Cloud VM. Not Compose Preview, not a PNG slideshow.

## File

- `presentation/demo/hop-emulator-demo.mp4`
- **153.75 s** (2:33), **540×960**, H.264, 24 fps
- Source capture was **922.5 s** (15:22) of real time; sped **6×** so the walk fits ~2–3 minutes. Pixels are the same live frames.

## How it was recorded

Host-window capture of the running AVD on XFCE `DISPLAY=:1` (TigerVNC 1920×1200):

```text
ffmpeg -f x11grab -framerate 24 -video_size 540x960 -i :1+41,85 \
  -c:v libx264 -preset veryfast -pix_fmt yuv420p -crf 18
```

UI driven with `adb` / `uiautomator` (type Name/Room/Floor, Continue, Claim, New post FAB sheet, Settings, Blackout).

APK: GitHub Actions artifact `hop-debug` from
https://github.com/flyme2mars/hop/actions/runs/33952493072
(`app-debug.apk`, commit `27e579c` on `cursor/hop-offline-board-1207`).

## Emulator

- AVD `hop_demo`, **API 30 Google APIs x86_64**, medium_phone, 1080×1920 @ 420 dpi
- Flags: `-gpu swiftshader_indirect -accel off -no-snapshot -no-audio -no-boot-anim`
- KVM exists at `/dev/kvm` but the `ubuntu` user has no kvm group. Nested KVM previously kernel-BUG’d on this repo’s cloud VM, so TCG was used on purpose.
- Windowed (no `-no-window`) so the guest is painted into an X11 window.

## What we tried (black-frame hunt)

| Path | Result |
| --- | --- |
| Nested KVM | Not used. No kvm group; prior attempt on this VM class hit a kernel BUG. |
| `adb exec-out screencap -p` after boot | **All black**: 1080×1920, unique colors = 1, RGB (0,0,0). |
| `adb shell screenrecord` to `/sdcard` | Permission denied. |
| `adb shell screenrecord` to `/data/local/tmp` | 3232-byte empty-ish file. |
| Host `import`/`scrot` of emulator window while guest still booting | Flat grey (~#808080), no guest frame yet. |
| Same host capture after `sys.boot_completed=1` | **Real pixels**: Android wallpaper, then Hop UI. |
| `ffmpeg x11grab` of the emulator window | **Works.** This is the delivered MP4. |
| scrcpy | Not installed; x11grab already produced non-black frames. |

`adb screencap` staying black while the X11 window shows Hop is the known TCG/software-GPU framebuffer gap. The demo is host-window pixels of the live app, not a reconstructed slideshow.

## UI walked (real session)

1. **Launch** — Hop / “Your floor, offline.” Name `Maya`, Room `214`, Floor `2`, Continue.
2. **Floor 2** — seed posts (Spare rice cooker, Phone charger, Water tank), filters, FAB.
3. **Nearby status** — `214 · Maya · needs Bluetooth` + **Turn on Bluetooth** (API 30 emulator has no usable BLE).
4. **Claim** — Claim sheet on Priya’s rice-cooker offer, confirm Claim.
5. **New post** — ModalBottomSheet Offer / Title / Body; posted “Extra power bank”.
6. **Settings** — Maya / 214 / 2, Keep screen on, About Hop.
7. **Blackout** — timer, “power cut”, **I'm OK**, “Marked OK for this blackout”.

The AVD follows the system light theme, so Floor/Settings are the light Hop stage. **Blackout is the night-stage screen** (black + gold).

## Proof stills (extracted from the MP4)

Under `presentation/demo/frames/`:

- `still_01_launch.png` — ~8 s — launch form
- `still_02_floor.png` — ~43 s — Floor 2 + posts + nearby line
- `still_03_newpost.png` — ~80 s — New post sheet + keyboard
- `still_04_settings.png` — ~126 s — Settings
- `still_05_blackout.png` — ~144 s — Blackout (mostly black **by design**; unique colors >12k, gold `#E6B66C`)

Frame hashes are not uniform-black except the intentional blackout background.

## ffprobe (delivered file)

```text
Duration: 00:02:33.75
540x960, h264, 24 fps, ~62 kb/s
```
