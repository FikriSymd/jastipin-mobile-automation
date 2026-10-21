#!/usr/bin/env bash
#
# Dump the live UI hierarchy of the connected Android device or emulator.
#
# Usage:  bash tools/dump-hierarchy.sh [output-file]
# Default output: ui-dump.xml
#
set -euo pipefail

OUT="${1:-ui-dump.xml}"
REMOTE="/sdcard/window_dump.xml"

if ! command -v adb >/dev/null 2>&1; then
    echo "ERROR: adb tidak ditemukan di PATH." >&2
    echo "Tambahkan folder platform-tools dari Android SDK ke PATH." >&2
    exit 1
fi

DEVICES="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
if [ -z "$DEVICES" ]; then
    echo "ERROR: tidak ada device dengan status 'device'." >&2
    echo "Cek dengan: adb devices" >&2
    exit 1
fi

COUNT="$(echo "$DEVICES" | wc -l | tr -d ' ')"
if [ "$COUNT" -gt 1 ]; then
    echo "PERINGATAN: ada $COUNT device terhubung, memakai yang pertama: $(echo "$DEVICES" | head -1)" >&2
fi

adb shell uiautomator dump "$REMOTE" >/dev/null
adb pull "$REMOTE" "$OUT" >/dev/null
adb shell rm -f "$REMOTE" >/dev/null

echo "UI hierarchy tersimpan di: $OUT"
echo "Langkah berikutnya:"
echo "  python3 tools/check_locators.py $OUT"
