# AtomiX

Native Android cross-chain **atomic swaps** on one app, in the currency you choose:

- **MINIMA ↔ ERC20** (the minimaSwap market), or
- **mxUSDT ↔ ERC20 USDT** (the usdtSwap market — bridged native-USDT `0x7D39…2A90`).

A hash-time-locked-contract (HTLC) venue: trustless, non-custodial, peer-to-peer. AtomiX combines the two former
apps (minimaSwap + usdtSwap) into one, so a single node runs one poller/market-maker instead of two contending
for the node command thread.

## Pick a currency

A header pill switches the active currency. You **publish and market-make in ONE currency at a time**, and
switching **re-themes the whole app** to that currency's identity (Minima orange ↔ Tether green) and moves your
market to that book. It's driven by a single `TradingContext`:

| | MINIMA | mxUSDT |
|---|---|---|
| Traded Minima token | `0x00` | `0x7D39…2A90` |
| Trade grain | none (44dp) | 6dp (ERC20-USDT 1:1) |
| Order-book / OTC / take boards | `MINIMASWAP*` hex | `USDTSWAP*` hex |
| Pricing | MEXC MINIMA/USDT feed | parity 1.0 + skew/spread |
| Accent | orange `#F7931A` | Tether green `#26A17B` |

The per-currency **sentinel boards are the legacy hex verbatim**, so AtomiX's orders land on the same shared books
the old apps used — full interop with existing minimaSwap / usdtSwap peers. One AtomiX comms identity serves both.

## Settlement is currency-agnostic

Switching the active currency **never strands an in-flight swap in the other currency.** The active currency only
governs new publishing/pricing/theme; background settlement scans coins by their unique hashlock / your key
(token-independent), and claim/refund act on each coin's own token. So a swap you started in one currency keeps
settling to completion even after you switch to the other.

## How it works

- **On-chain HTLC on both legs.** The Minima leg locks the active token at the shared bridge HTLC contract; the
  Ethereum leg locks the ERC20 at the bridge vault. One SHA-256 hashlock links both — reveal to claim, or refund
  after the timelock. Contracts are the upstream bridge's, unchanged.
- **Runs against a local minimaCore node** for the Minima leg, and keyless public Ethereum RPCs for the ERC20 leg.
  An embedded web3j wallet signs the Ethereum side; keys stay on device.

## Build

Standard Android Gradle build (JDK 17+, Android SDK 36):

```
./gradlew :app:assembleRelease
```

## Package / version

- Application id / namespace: `com.eurobuddha.atomix`
- Version: `0.1.0` (versionCode 1)

## Status

Combined from the usdtSwap + minimaSwap trees. Compiles; 86 unit tests green. **On-device mainnet verification in
BOTH currency modes is the release gate.** Replaces minimaSwap + usdtSwap in the PandaApps store.
