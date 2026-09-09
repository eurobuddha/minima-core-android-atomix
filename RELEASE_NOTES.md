# AtomiX changes

## 0.1.49 — 2026-09-09

Prioritized the live stalled-swap investigation. Inspection now distinguishes scan failure from absence, prints the actual 1,024-block search depth, includes the full hashlock and recorded transaction IDs, and never promises Ethereum collection without the secret. No change to transaction retry or settlement authorization. Validation: 206 tests passed per build, release APK and lintVitalRelease passed; installed in place on S23 and Z Fold.

## 0.1.48 — 2026-09-09

Ask ladder backing uses the small balance sendable total instead of enumerating coins. Keeps cumulative prefix trimming and the existing fail-safe publish path. The default three-confirmation balance is more conservative than the old one-confirmation read. Validation: 204 tests passed in each build variant.

## 0.1.47 — 2026-09-09

Manual Wallet consolidation with Preview and Run once, fresh balance/pending checks, a persisted confirmation wait and the existing SignGate. Coin reservations now span both engines; consolidation and new Minima locks exclude each other. Node errors retain the message returned by consolidate. No automatic consolidation or spending loop.

Validation: 202 tests passed in each debug/release suite; release APK and lintVitalRelease passed.

## 0.1.46 — 2026-09-09

Preflight wallet coin enumeration using a fresh, strictly parsed token balance. Refuse more than 53 MxUSD or 200 native MINIMA coins before requesting the reply that can kill AtomiX on older MinimaCore builds. Report the refusal and release responder reservations before persisting or broadcasting a counter-leg.

Fund behaviour: above the cap, new buy counter-legs are declined. The taker's existing Ethereum leg can expire and be refunded; AtomiX does not send the maker's counter-leg. Existing settlement remains enabled. The cap is a conservative estimate from measured rows, not a transport-enforced byte limit.

Validation: debug and release JVM suites, including command-level preflight and responder refusal regressions. This guard-only version is not for device rollout; ship with the manual consolidation control.
