# Graph Report - atomix  (2026-08-21)

## Corpus Check
- 81 files · ~106,570 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1490 nodes · 4332 edges · 97 communities (49 shown, 48 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 519 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7ca3233e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MinimaHtlc
- CommsIdentity
- Order
- OtcController
- MainActivity
- PriceOracle
- Design
- EthRpc
- .checkCanSwapCoin
- AtomiX Full Security & Code Review Report
- CommsScanner
- SwapService
- .checkCanCollectEth
- SendCb
- .dp
- SwapDb
- SignGate
- EthWallet
- .w
- EthEncodingTest
- .reviewSweep
- EthHtlc
- .cmd
- Swap
- .checkEthContractFor
- NodeApi
- .editOrderDialog
- StartCb
- SwapEngine
- .publish
- .checkExpiredMinima
- AtomiX Launcher Icon Foreground (hdpi)
- AtomiX
- MismatchPoisonTest
- SwapOrderBook
- Override
- .allSwaps
- .postBlob
- .restoreMarket
- Helper
- IdentityBlockTest
- Util
- Test
- Test
- .addIncoming
- QrUtil.java
- gradlew
- TombstoneTest.java
- Android platform boundary (FGS, lifecycle, WorkManager)
- Command injection into Minima node IPC commands
- Pre-commit hook blocks code change without version bump
- install.sh
- pre-commit
- Order
- TextView
- Uri
- View
- JSONArray
- Order
- Order
- Context
- SecureRandom
- Bitmap
- Context
- Uri
- SecureRandom
- Context
- Override
- SQLiteDatabase
- Bitmap
- Before
- Test
- Order
- Order
- .schedule
- FrameLayout
- org.junit.Before
- EthSend
- SecretKey
- TradingContext
- CommsScannerTest
- EthNet.java
- .ingest
- PrefsMeta
- Credentials
- Credentials
- Credentials
- EthNet
- Handler
- JSONArray
- JSONObject
- Test

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 208 edges
2. `SwapEngine` - 119 edges
3. `MinimaHtlc` - 75 edges
4. `Order` - 58 edges
5. `NodeApi` - 54 edges
6. `SwapService` - 50 edges
7. `SwapDb` - 46 edges
8. `Design` - 40 edges
9. `EthHtlc` - 32 edges
10. `OtcController` - 29 edges

## Surprising Connections (you probably didn't know these)
- `CR-2: SwapEngine unchecked io.execute() crashes after shutdown` --references--> `SwapEngine`  [INFERRED]
  SECURIRY_REVIEW.md → app/src/main/java/com/eurobuddha/atomix/swap/SwapEngine.java
- `MA-20: SwapEngine claim-gate race burns two one-time signing leaves` --references--> `SwapEngine`  [INFERRED]
  SECURIRY_REVIEW.md → app/src/main/java/com/eurobuddha/atomix/swap/SwapEngine.java
- `CR-1: SwapEngine DB persistence race before ETH broadcast` --references--> `SwapEngine`  [INFERRED]
  SECURIRY_REVIEW.md → app/src/main/java/com/eurobuddha/atomix/swap/SwapEngine.java
- `RULE 0 — Follow explicit user instructions (blocking)` --semantically_similar_to--> `RULE 0 — Follow explicit user instructions (blocking)`  [INFERRED] [semantically similar]
  CLAUDE.md → AGENTS.md
- `NI-1: Util.shorten()/shortAddr() truncate identifiers (violates RULE 1)` --conceptually_related_to--> `RULE 0 — Follow explicit user instructions (blocking)`  [INFERRED]
  SECURIRY_REVIEW.md → CLAUDE.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **SignGate serial one-time-signing invariant (jam + concurrent-signing + off-thread hazards)** — securiry_review_cr5_signgate_queue_jam, securiry_review_cr6_signgate_concurrent_signing, securiry_review_ma23_signgate_not_thread_safe, securiry_review_winternitz_one_time_signature [INFERRED 0.85]
- **Minima node IPC command-injection surface (unescaped concatenation)** — securiry_review_ma9_commstransport_injection, securiry_review_ma10_commsscanner_injection, securiry_review_ma19_minimahtlc_injection, securiry_review_command_injection [INFERRED 0.85]
- **Fund-stranding races (record-before-broadcast, TOCTOU, claim-gate)** — securiry_review_cr1_db_race_before_broadcast, securiry_review_ma15_merchdb_toctou, securiry_review_ma20_swapengine_claim_gate_race, securiry_review_fund_stranding [INFERRED 0.75]
- **Two-Leg HTLC Atomic Swap Flow** — readme_htlc, readme_bridge_htlc_contract, readme_bridge_vault, readme_minimacore_node, readme_web3j_wallet [EXTRACTED 1.00]
- **Dual-Currency Trading System (TradingContext-driven)** — readme_tradingcontext, readme_currency_pill, readme_minimaswap_market, readme_usdtswap_market, readme_sentinel_boards [EXTRACTED 1.00]

## Communities (97 total, 48 thin omitted)

### Community 0 - "MinimaHtlc"
Cohesion: 0.06
Nodes (14): JSONObject, NodeApi, KeysCb, MinimaHtlc, PostCb, SetupCb, AmountGrainTest, Test (+6 more)

### Community 1 - "CommsIdentity"
Cohesion: 0.05
Nodes (24): CommsIdentity, LazySodium, Hex, Hkdf, CommsIdentity, LazySodium, Opened, Override (+16 more)

### Community 2 - "Order"
Cohesion: 0.08
Nodes (12): JSONArray, JSONObject, Level, Order, Pair, Test, OrderBookTest, Test (+4 more)

### Community 3 - "OtcController"
Cohesion: 0.08
Nodes (18): JSONObject, SecureRandom, OtcController, SendResult, Ui, Deal, Helper, Context (+10 more)

### Community 4 - "MainActivity"
Cohesion: 0.08
Nodes (17): ActivityResultLauncher, Best, CommsIdentity, CryptoProvider, EthNet, Handler, LazySodium, Opened (+9 more)

### Community 5 - "PriceOracle"
Cohesion: 0.08
Nodes (12): JSONArray, JSONObject, Order, SharedPreferences, PriceOracle, After, SharedPreferences, Test (+4 more)

### Community 6 - "Design"
Cohesion: 0.07
Nodes (15): Design, Context, TextView, View, Mode, DAYLIGHT, ONYX, Context (+7 more)

### Community 7 - "EthRpc"
Cohesion: 0.18
Nodes (4): EthRpc, JSONArray, JSONObject, EthRpc

### Community 9 - "AtomiX Full Security & Code Review Report"
Cohesion: 0.05
Nodes (40): Reuse before you reinvent, RULE 0 — Follow explicit user instructions (blocking), RULE 0 — Follow explicit user instructions (blocking), AtomiX Android atomic-swap app (Minima↔Ethereum USDT), Concurrency control / thread-safety (volatile, TOCTOU, races), CR-1: SwapEngine DB persistence race before ETH broadcast, CR-2: SwapEngine unchecked io.execute() crashes after shutdown, CR-3: CommsScanner exception in process() freezes scanner (+32 more)

### Community 10 - "CommsScanner"
Cohesion: 0.12
Nodes (8): CommsScanner, CryptoProvider, JSONArray, JSONObject, Opened, Listener, MetaStore, Router

### Community 11 - "SwapService"
Cohesion: 0.10
Nodes (14): CommsIdentity, CryptoProvider, Handler, Intent, JSONObject, LazySodium, Opened, OtcController (+6 more)

### Community 12 - ".checkCanCollectEth"
Cohesion: 0.14
Nodes (5): Contract, GateTest, JSONObject, Test, Pair

### Community 13 - "SendCb"
Cohesion: 0.15
Nodes (3): PublishGate, SwapLog, SendCb

### Community 14 - ".dp"
Cohesion: 0.22
Nodes (3): LayoutParams, LinearLayout, TextView

### Community 15 - "SwapDb"
Cohesion: 0.15
Nodes (3): Event, MarketTrade, SwapDb

### Community 16 - "SignGate"
Cohesion: 0.15
Nodes (9): Handler, Op, Release, SignGate, Before, Override, Test, Rec (+1 more)

### Community 17 - "EthWallet"
Cohesion: 0.09
Nodes (11): EthWallet, Credentials, Handler, IdentityWatch, Handler, SharedPreferences, Notifier, IdentityWatchTest (+3 more)

### Community 18 - ".w"
Cohesion: 0.23
Nodes (9): BootReceiver, Context, Intent, Override, HeartbeatReceiver, Context, Intent, Override (+1 more)

### Community 21 - "EthHtlc"
Cohesion: 0.15
Nodes (8): EthHtlc, EthNet, EthRpc, JSONObject, EthTx, NonceState, org.json.JSONArray, org.web3j.crypto.Credentials

### Community 24 - ".checkEthContractFor"
Cohesion: 0.19
Nodes (3): ConfirmCb, InspectCb, Swap

### Community 25 - "NodeApi"
Cohesion: 0.23
Nodes (7): Context, Handler, JSONObject, NodeApi, PairingListener, MinimaAPI, MinimaAPIListener

### Community 27 - "StartCb"
Cohesion: 0.19
Nodes (4): BlockCb, SecretCb, StartCb, Order

### Community 28 - "SwapEngine"
Cohesion: 0.14
Nodes (13): android.os.Handler, SwapEngine, Before, IdentityGuardTest, Before, Test, com.eurobuddha.atomix.eth.EthNet, com.eurobuddha.atomix.eth.EthRpc (+5 more)

### Community 29 - ".publish"
Cohesion: 0.19
Nodes (5): JSONObject, LazySodium, OtcBook, JSONObject, OtcOffer

### Community 31 - ".checkExpiredMinima"
Cohesion: 0.21
Nodes (4): Before, JSONObject, Test, RefundRetryTest

### Community 32 - "AtomiX Launcher Icon Foreground (hdpi)"
Cohesion: 0.17
Nodes (18): AtomiX Launcher Icon Foreground (hdpi), Atom Orbit Motif (atomic swap symbolism), AtomiX Brand Identity, Minima 'M' Logo Mark (red/blue/grey bars), USDT Tether Symbol (green circular badges), Atomic Orbit Ring Motif (atomic-swap symbolism), AtomiX App Branding (Minima-to-USDT atomic swap identity), Stylized Minima 'M' Logomark (red-orange and navy halves) (+10 more)

### Community 33 - "AtomiX"
Cohesion: 0.14
Nodes (18): Cross-chain Atomic Swaps, AtomiX, Shared Bridge HTLC Contract (Minima leg), Bridge Vault (Ethereum ERC20 leg), Currency-Agnostic Settlement, Header Currency Pill (currency switcher), Hash-Time-Locked Contract (HTLC), MEXC MINIMA/USDT Price Feed (+10 more)

### Community 34 - "MismatchPoisonTest"
Cohesion: 0.30
Nodes (4): Before, JSONObject, Test, MismatchPoisonTest

### Community 35 - "SwapOrderBook"
Cohesion: 0.25
Nodes (3): JSONObject, LazySodium, SwapOrderBook

### Community 36 - "Override"
Cohesion: 0.16
Nodes (4): Override, SimpleWatcher, Editable, TextWatcher

### Community 37 - ".allSwaps"
Cohesion: 0.30
Nodes (3): ClaimDeepDiscoveryTest, Before, Test

### Community 38 - ".postBlob"
Cohesion: 0.21
Nodes (4): SwapTake, CommsTransport, CryptoProvider, JSONObject

### Community 39 - ".restoreMarket"
Cohesion: 0.22
Nodes (7): JSONObject, SharedPreferences, LazySodium, Sodium, SharedPreferences, Test, MarketMemoryTest

### Community 40 - "Helper"
Cohesion: 0.25
Nodes (6): Helper, Context, Cursor, Override, SQLiteDatabase, SQLiteOpenHelper

### Community 41 - "IdentityBlockTest"
Cohesion: 0.35
Nodes (4): IdentityBlockTest, Before, JSONArray, Test

### Community 48 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 50 - "Android platform boundary (FGS, lifecycle, WorkManager)"
Cohesion: 0.50
Nodes (4): Android platform boundary (FGS, lifecycle, WorkManager), MA-13: SwapWorker startForegroundService prohibited on Android 12+, MA-14: AndroidManifest dataSync caps FGS at ~6h/day on Android 14+, MA-17: Images.java bitmap memory leaks (OOM)

### Community 51 - "Command injection into Minima node IPC commands"
Cohesion: 0.67
Nodes (4): Command injection into Minima node IPC commands, MA-10: CommsScanner command injection via targetAddress, MA-19: MinimaHtlc command-string injection via unescaped on-chain values, MA-9: CommsTransport command injection into Minima commands

### Community 77 - ".schedule"
Cohesion: 0.22
Nodes (7): Context, Override, SwapWorker, NonNull, Result, Worker, WorkerParameters

### Community 81 - "EthSend"
Cohesion: 0.18
Nodes (4): EthSend, EthRpc, EthSendTest, org.junit.Test

### Community 84 - "TradingContext"
Cohesion: 0.36
Nodes (9): active(), SharedPreferences, labelFor(), load(), other(), setActive(), TradingContext, MINIMA (+1 more)

### Community 85 - "CommsScannerTest"
Cohesion: 0.53
Nodes (3): CommsScannerTest, JSONArray, Test

### Community 86 - "EthNet.java"
Cohesion: 0.32
Nodes (6): EthNet, MAINNET, from(), token(), tokenByAddress(), Token

### Community 88 - "PrefsMeta"
Cohesion: 0.43
Nodes (3): Override, SharedPreferences, PrefsMeta

## Knowledge Gaps
- **31 isolated node(s):** `ONYX`, `DAYLIGHT`, `MINIMA`, `MXUSDT`, `MAINNET` (+26 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **48 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SwapEngine` connect `SwapEngine` to `Order`, `OtcController`, `MainActivity`, `EthRpc`, `.checkCanSwapCoin`, `AtomiX Full Security & Code Review Report`, `SwapService`, `.checkCanCollectEth`, `SendCb`, `SwapDb`, `EthWallet`, `EthHtlc`, `.checkEthContractFor`, `StartCb`, `.checkExpiredMinima`, `MismatchPoisonTest`, `Override`, `.allSwaps`, `.addIncoming`, `.broadcastEthWithdraw`?**
  _High betweenness centrality (0.248) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity` to `MinimaHtlc`, `Order`, `OtcController`, `EthRpc`, `CommsScanner`, `SendCb`, `.dp`, `SwapDb`, `EthWallet`, `.reviewSweep`, `.cmd`, `Swap`, `NodeApi`, `.editOrderDialog`, `StartCb`, `SwapEngine`, `.toast`, `Override`, `.restoreMarket`, `.addIncoming`, `.render`, `.schedule`?**
  _High betweenness centrality (0.235) - this node is a cross-community bridge._
- **Why does `MinimaHtlc` connect `MinimaHtlc` to `MismatchPoisonTest`, `MainActivity`, `.allSwaps`, `.checkCanSwapCoin`, `IdentityBlockTest`, `SwapService`, `SwapDb`, `EthWallet`, `.ingest`, `StartCb`, `.checkExpiredMinima`?**
  _High betweenness centrality (0.142) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `SwapEngine` (e.g. with `CR-1: SwapEngine DB persistence race before ETH broadcast` and `CR-2: SwapEngine unchecked io.execute() crashes after shutdown`) actually correct?**
  _`SwapEngine` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ONYX`, `DAYLIGHT`, `MINIMA` to the rest of the system?**
  _31 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MinimaHtlc` be split into smaller, more focused modules?**
  _Cohesion score 0.05844376486578321 - nodes in this community are weakly interconnected._
- **Should `CommsIdentity` be split into smaller, more focused modules?**
  _Cohesion score 0.051929824561403506 - nodes in this community are weakly interconnected._