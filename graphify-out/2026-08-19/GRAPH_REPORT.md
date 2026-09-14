# Graph Report - .  (2026-08-17)

## Corpus Check
- 48 files · ~105,787 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1464 nodes · 4317 edges · 83 communities (48 shown, 35 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 517 edges (avg confidence: 0.8)
- Token cost: 40,000 input · 4,000 output

## Community Hubs (Navigation)
- Minima HTLC & Txn Construction
- Comms Identity & Hex Codec
- Price Oracle & Currency Peg
- Order Book Model
- Design System & Chart View
- OTC Controller & Deal Store
- Ethereum RPC Client
- ETH Wallet & Identity Watch
- Security Review Findings
- MainActivity Swap UI
- UI Layout Primitives
- Swap Engine Core
- Responder Gate Tests
- Swap Database
- Serial Signing Gate
- MainActivity Scaffold
- Counter-leg Lock Checks
- Comms Scanner
- Comms Transport & Messaging
- Sweep Review UI
- Order / Take Dialogs
- Swap Refund Path
- ETH Encoding Tests
- ETH Contract Settlement
- Foreground Swap Service
- Swap Initiation
- OTC Offer Board
- ETH HTLC Contract
- Launcher Icon & Branding
- AtomiX Market Concepts
- Node IPC Api
- Boot / Heartbeat Receivers
- Order Book Publishing
- Minima Claim Discovery
- Auto-Republish & Generation
- Mismatch Poison Tests
- SQLite Helper
- Identity Block Tests
- Key Derivations
- WorkManager Fallback
- Utility Helpers
- Text Input Watchers
- Service Tick & Peg
- Timelock Safety Tests
- TradingContext Tests
- Service Lifecycle
- Comms Scanner Tests
- Prefs Meta Store
- Take-Request Routing
- libsodium Binding
- Gradle Wrapper
- Community 55
- Community 56
- Community 57
- Community 58
- Community 59
- Community 60
- Community 62
- Community 63
- Community 64
- Community 65
- Community 66
- Community 67
- Community 68
- Community 69
- Community 70
- Community 71
- Community 72
- Community 73
- Community 74
- Community 75
- Community 76
- Community 77
- Community 78
- Community 79
- Community 80
- Community 81

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 208 edges
2. `SwapEngine` - 118 edges
3. `MinimaHtlc` - 77 edges
4. `Order` - 65 edges
5. `NodeApi` - 62 edges
6. `SwapService` - 50 edges
7. `SwapDb` - 49 edges
8. `Design` - 40 edges
9. `EthRpc` - 33 edges
10. `EthHtlc` - 32 edges

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

## Communities (83 total, 35 thin omitted)

### Community 0 - "Minima HTLC & Txn Construction"
Cohesion: 0.06
Nodes (12): JSONArray, JSONObject, KeysCb, MinimaHtlc, PostCb, SetupCb, AmountGrainTest, Test (+4 more)

### Community 1 - "Comms Identity & Hex Codec"
Cohesion: 0.05
Nodes (24): CommsIdentity, LazySodium, Hex, Hkdf, CommsIdentity, LazySodium, Opened, Override (+16 more)

### Community 2 - "Price Oracle & Currency Peg"
Cohesion: 0.05
Nodes (26): JSONObject, SharedPreferences, JSONArray, JSONObject, Order, SharedPreferences, PriceOracle, active() (+18 more)

### Community 3 - "Order Book Model"
Cohesion: 0.08
Nodes (12): JSONArray, JSONObject, Level, Order, Pair, Test, OrderBookTest, Test (+4 more)

### Community 4 - "Design System & Chart View"
Cohesion: 0.07
Nodes (15): Design, Context, TextView, View, Mode, DAYLIGHT, ONYX, Context (+7 more)

### Community 5 - "OTC Controller & Deal Store"
Cohesion: 0.09
Nodes (16): JSONObject, SecureRandom, OtcController, SendResult, Ui, Deal, Helper, Context (+8 more)

### Community 6 - "Ethereum RPC Client"
Cohesion: 0.09
Nodes (10): EthRpc, JSONArray, JSONObject, EthSend, Credentials, EthTx, Credentials, NonceState (+2 more)

### Community 7 - "ETH Wallet & Identity Watch"
Cohesion: 0.09
Nodes (14): EthWallet, Credentials, Handler, IdentityWatch, Handler, SharedPreferences, Notifier, IdentityWatchTest (+6 more)

### Community 8 - "Security Review Findings"
Cohesion: 0.05
Nodes (40): Reuse before you reinvent, RULE 0 — Follow explicit user instructions (blocking), RULE 0 — Follow explicit user instructions (blocking), AtomiX Android atomic-swap app (Minima↔Ethereum USDT), Concurrency control / thread-safety (volatile, TOCTOU, races), CR-1: SwapEngine DB persistence race before ETH broadcast, CR-2: SwapEngine unchecked io.execute() crashes after shutdown, CR-3: CommsScanner exception in process() freezes scanner (+32 more)

### Community 9 - "MainActivity Swap UI"
Cohesion: 0.10
Nodes (3): Opened, MainActivity, Event

### Community 10 - "UI Layout Primitives"
Cohesion: 0.18
Nodes (3): LayoutParams, LinearLayout, TextView

### Community 11 - "Swap Engine Core"
Cohesion: 0.13
Nodes (4): ConfirmCb, EthNet, Handler, SwapEngine

### Community 13 - "Responder Gate Tests"
Cohesion: 0.13
Nodes (10): EthNet, MAINNET, from(), token(), tokenByAddress(), GateTest, Before, JSONObject (+2 more)

### Community 14 - "Swap Database"
Cohesion: 0.14
Nodes (4): JSONArray, MarketCollector, MarketTrade, SwapDb

### Community 15 - "Serial Signing Gate"
Cohesion: 0.15
Nodes (9): Handler, Op, Release, SignGate, Before, Override, Test, Rec (+1 more)

### Community 16 - "MainActivity Scaffold"
Cohesion: 0.09
Nodes (15): ActivityResultLauncher, CommsIdentity, CryptoProvider, EthNet, Handler, LazySodium, OtcController, QrUtil (+7 more)

### Community 18 - "Comms Scanner"
Cohesion: 0.13
Nodes (9): CommsScanner, CryptoProvider, JSONArray, JSONObject, Opened, Listener, MetaStore, Router (+1 more)

### Community 19 - "Comms Transport & Messaging"
Cohesion: 0.14
Nodes (8): CryptoProvider, OtcMessage, SwapTake, CommsTransport, CryptoProvider, JSONObject, SendCb, TradingContext

### Community 20 - "Sweep Review UI"
Cohesion: 0.12
Nodes (3): Best, SweepLeg, SweepPlan

### Community 21 - "Order / Take Dialogs"
Cohesion: 0.26
Nodes (3): EditText, OtcOffer, SendResult

### Community 22 - "Swap Refund Path"
Cohesion: 0.21
Nodes (5): Swap, Before, JSONObject, Test, RefundRetryTest

### Community 25 - "Foreground Swap Service"
Cohesion: 0.18
Nodes (9): CommsIdentity, CryptoProvider, Handler, LazySodium, OtcController, SharedPreferences, SwapService, Notification (+1 more)

### Community 26 - "Swap Initiation"
Cohesion: 0.19
Nodes (3): BlockCb, SecretCb, StartCb

### Community 27 - "OTC Offer Board"
Cohesion: 0.19
Nodes (5): JSONObject, LazySodium, OtcBook, JSONObject, OtcOffer

### Community 28 - "ETH HTLC Contract"
Cohesion: 0.21
Nodes (5): Contract, EthHtlc, Credentials, EthNet, JSONObject

### Community 29 - "Launcher Icon & Branding"
Cohesion: 0.17
Nodes (18): AtomiX Launcher Icon Foreground (hdpi), Atom Orbit Motif (atomic swap symbolism), AtomiX Brand Identity, Minima 'M' Logo Mark (red/blue/grey bars), USDT Tether Symbol (green circular badges), Atomic Orbit Ring Motif (atomic-swap symbolism), AtomiX App Branding (Minima-to-USDT atomic swap identity), Stylized Minima 'M' Logomark (red-orange and navy halves) (+10 more)

### Community 30 - "AtomiX Market Concepts"
Cohesion: 0.14
Nodes (18): Cross-chain Atomic Swaps, AtomiX, Shared Bridge HTLC Contract (Minima leg), Bridge Vault (Ethereum ERC20 leg), Currency-Agnostic Settlement, Header Currency Pill (currency switcher), Hash-Time-Locked Contract (HTLC), MEXC MINIMA/USDT Price Feed (+10 more)

### Community 32 - "Node IPC Api"
Cohesion: 0.19
Nodes (7): Context, Handler, JSONObject, NodeApi, PairingListener, MinimaAPI, MinimaAPIListener

### Community 33 - "Boot / Heartbeat Receivers"
Cohesion: 0.23
Nodes (9): BootReceiver, Context, Intent, Override, HeartbeatReceiver, Context, Intent, Override (+1 more)

### Community 34 - "Order Book Publishing"
Cohesion: 0.25
Nodes (3): JSONObject, LazySodium, SwapOrderBook

### Community 35 - "Minima Claim Discovery"
Cohesion: 0.35
Nodes (3): ClaimDeepDiscoveryTest, Before, Test

### Community 37 - "Mismatch Poison Tests"
Cohesion: 0.28
Nodes (4): Before, JSONObject, Test, MismatchPoisonTest

### Community 38 - "SQLite Helper"
Cohesion: 0.30
Nodes (5): Helper, Context, Cursor, Override, SQLiteDatabase

### Community 39 - "Identity Block Tests"
Cohesion: 0.35
Nodes (4): IdentityBlockTest, Before, JSONArray, Test

### Community 41 - "WorkManager Fallback"
Cohesion: 0.27
Nodes (7): Context, Override, SwapWorker, NonNull, Result, Worker, WorkerParameters

### Community 43 - "Text Input Watchers"
Cohesion: 0.24
Nodes (4): Override, SimpleWatcher, Editable, TextWatcher

### Community 47 - "Service Lifecycle"
Cohesion: 0.28
Nodes (3): Intent, Override, IBinder

### Community 48 - "Comms Scanner Tests"
Cohesion: 0.53
Nodes (3): CommsScannerTest, JSONArray, Test

### Community 49 - "Prefs Meta Store"
Cohesion: 0.43
Nodes (3): Override, SharedPreferences, PrefsMeta

### Community 54 - "Gradle Wrapper"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 56 - "Community 56"
Cohesion: 0.50
Nodes (4): Android platform boundary (FGS, lifecycle, WorkManager), MA-13: SwapWorker startForegroundService prohibited on Android 12+, MA-14: AndroidManifest dataSync caps FGS at ~6h/day on Android 14+, MA-17: Images.java bitmap memory leaks (OOM)

### Community 57 - "Community 57"
Cohesion: 0.67
Nodes (4): Command injection into Minima node IPC commands, MA-10: CommsScanner command injection via targetAddress, MA-19: MinimaHtlc command-string injection via unescaped on-chain values, MA-9: CommsTransport command injection into Minima commands

## Knowledge Gaps
- **31 isolated node(s):** `ONYX`, `DAYLIGHT`, `MINIMA`, `MXUSDT`, `MAINNET` (+26 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **35 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity Swap UI` to `Minima HTLC & Txn Construction`, `Price Oracle & Currency Peg`, `Order Book Model`, `OTC Controller & Deal Store`, `Ethereum RPC Client`, `ETH Wallet & Identity Watch`, `UI Layout Primitives`, `Swap Engine Core`, `Send Dialog & Toasts`, `Swap Database`, `MainActivity Scaffold`, `Comms Scanner`, `Sweep Review UI`, `Order / Take Dialogs`, `Swap Initiation`, `Order Book Scan & Render`, `Node IPC Api`, `Auto-Republish & Generation`, `Key Derivations`, `Text Input Watchers`, `Activity Lifecycle Teardown`?**
  _High betweenness centrality (0.236) - this node is a cross-community bridge._
- **Why does `SwapEngine` connect `Swap Engine Core` to `Minima HTLC & Txn Construction`, `Order Book Model`, `OTC Controller & Deal Store`, `Ethereum RPC Client`, `ETH Wallet & Identity Watch`, `Security Review Findings`, `MainActivity Swap UI`, `Responder Gate Tests`, `Swap Database`, `MainActivity Scaffold`, `Counter-leg Lock Checks`, `Swap Refund Path`, `ETH Contract Settlement`, `Foreground Swap Service`, `Swap Initiation`, `ETH HTLC Contract`, `Node IPC Api`, `Minima Claim Discovery`, `Auto-Republish & Generation`, `Mismatch Poison Tests`, `Service Tick & Peg`, `Preimage Verification`, `Activity Lifecycle Teardown`, `Take-Request Routing`?**
  _High betweenness centrality (0.225) - this node is a cross-community bridge._
- **Why does `NodeApi` connect `Node IPC Api` to `Minima HTLC & Txn Construction`, `Order Book Publishing`, `OTC Controller & Deal Store`, `ETH Wallet & Identity Watch`, `Key Derivations`, `MainActivity Swap UI`, `Identity Block Tests`, `Swap Engine Core`, `Service Tick & Peg`, `MainActivity Scaffold`, `Comms Scanner Tests`, `Comms Scanner`, `Comms Transport & Messaging`, `Activity Lifecycle Teardown`, `Foreground Swap Service`, `OTC Offer Board`?**
  _High betweenness centrality (0.178) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `SwapEngine` (e.g. with `CR-1: SwapEngine DB persistence race before ETH broadcast` and `CR-2: SwapEngine unchecked io.execute() crashes after shutdown`) actually correct?**
  _`SwapEngine` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `ONYX`, `DAYLIGHT`, `MINIMA` to the rest of the system?**
  _31 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Minima HTLC & Txn Construction` be split into smaller, more focused modules?**
  _Cohesion score 0.06349206349206349 - nodes in this community are weakly interconnected._
- **Should `Comms Identity & Hex Codec` be split into smaller, more focused modules?**
  _Cohesion score 0.051929824561403506 - nodes in this community are weakly interconnected._