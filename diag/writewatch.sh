#!/bin/bash
# Capture the MOMENT an AtomiX write goes uncertain, because logcat rotates before we get there.
#
# WHY: the interrupted-write latch (WriteSafety "pending") has fired twice in two days on the S10+.
# Three code paths can set it and two are effectively silent, so the only way to tell them apart is
# to be watching when it happens:
#
#   attempt -> FAIL in ~100ms   = the LATCH refusing a new write (begin() sees pending already set).
#                                 This is an after-effect, not the cause.
#   attempt -> FAIL in ~4s      = the reply came back but completeReply() rejected it (no Boolean
#                                 `status`, or a "too long" stub, or pending:true).
#   attempt -> FAIL after 180s  = WRITE_TIMEOUT_MS, the node never answered.
#   attempt -> (no FAIL at all) = the process died mid-write. pending is on disk, the `active`
#                                 static is gone, and it latches silently on next start.
#
# Also captures am_kill/am_proc_died so a silent death is visible next to the write it interrupted.
#
# Usage: ./writewatch.sh <serial> [outdir]
set -u
S="${1:?serial required}"
OUT="${2:-$(dirname "$0")/writewatch-out}"
mkdir -p "$OUT"
MAIN="$OUT/$S-swappub.log"
EVENTS="$OUT/$S-events.log"
echo "# started $(date -u +%Y-%m-%dT%H:%M:%SZ) serial=$S" >> "$MAIN"
echo "# started $(date -u +%Y-%m-%dT%H:%M:%SZ) serial=$S" >> "$EVENTS"
# Two long-lived tails. -v threadtime keeps the ms timestamps the latency test depends on.
adb -s "$S" logcat -v threadtime SwapPub:V SwapLog:V MinimaAPILogger:V AndroidRuntime:E '*:S' >> "$MAIN" 2>&1 &
adb -s "$S" logcat -b events -v threadtime >> "$EVENTS" 2>&1 &
wait
