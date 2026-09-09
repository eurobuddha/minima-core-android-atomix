# AtomiX changes

## 0.1.46 — 2026-09-09

Preflight wallet coin enumeration using a fresh, strictly parsed token balance. Refuse more than 53 MxUSD or 200 native MINIMA coins before requesting the reply that can kill AtomiX on older MinimaCore builds. Report the refusal and release responder reservations before persisting or broadcasting a counter-leg.

Fund behaviour: above the cap, new buy counter-legs are declined. The taker's existing Ethereum leg can expire and be refunded; AtomiX does not send the maker's counter-leg. Existing settlement remains enabled. The cap is a conservative estimate from measured rows, not a transport-enforced byte limit.

Validation: debug and release JVM suites, including command-level preflight and responder refusal regressions. This guard-only version is not for device rollout; ship with the manual consolidation control.
