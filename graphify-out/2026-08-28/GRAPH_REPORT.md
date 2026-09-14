# Graph Report - atomix  (2026-08-28)

## Corpus Check
- 82 files · ~107,350 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1369 nodes · 4913 edges · 60 communities (38 shown, 22 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 565 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ccf36bf6`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MinimaHtlc
- .from
- Order
- OtcController
- PriceOracle
- android.content.SharedPreferences
- MainActivity
- EthRpc
- HtlcTxnConstructionTest
- AtomiX Full Security & Code Review Report
- CommsScanner
- SwapService
- GateTest
- SendCb
- .claim
- SwapDb
- SignGate
- IdentityWatch
- EthWallet
- .stateAt
- .compareForFill
- EthHtlc
- org.junit.Before
- .pegLevels
- .checkBuyNow
- NodeApi
- .to
- .w
- .setNetwork
- OtcOffer
- MarketChartView
- .checkExpiredMinima
- AtomiX Launcher Icon Foreground (hdpi)
- AtomiX
- .checkCanSwapCoin
- org.json.JSONObject
- Override
- .startLeg
- CryptoProvider
- com.goterl.lazysodium.LazySodium
- android.database.sqlite.SQLiteDatabase
- org.junit.Test
- Util
- SwapWorker.java
- IdentityBlockTest
- OrderBookTombstoneTest
- QrUtil
- .derive
- gradlew
- SwapEngine.java
- Android platform boundary (FGS, lifecycle, WorkManager)
- Command injection into Minima node IPC commands
- Pre-commit hook blocks code change without version bump
- install.sh
- pre-commit
- SwapEngine
- .allSwaps

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 208 edges
2. `SwapEngine` - 122 edges
3. `MinimaHtlc` - 79 edges
4. `Order` - 68 edges
5. `NodeApi` - 67 edges
6. `SwapService` - 50 edges
7. `SwapDb` - 50 edges
8. `Design` - 40 edges
9. `EthRpc` - 39 edges
10. `OtcController` - 35 edges

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

## Communities (60 total, 22 thin omitted)

### Community 0 - "MinimaHtlc"
Cohesion: 0.17
Nodes (4): JSONArray, KeysCb, MinimaHtlc, SetupCb

### Community 1 - ".from"
Cohesion: 0.14
Nodes (3): Override, HexTest, InteropVectorsTest

### Community 2 - "Order"
Cohesion: 0.10
Nodes (8): Best, JSONArray, JSONObject, Level, Order, Pair, OrderBookTest, TombstoneTest

### Community 3 - "OtcController"
Cohesion: 0.09
Nodes (10): android.os.Bundle, OtcController, SendResult, Ui, Deal, Msg, OtcDb, OtcMessage (+2 more)

### Community 4 - "PriceOracle"
Cohesion: 0.18
Nodes (3): JSONArray, JSONObject, PriceOracle

### Community 5 - "android.content.SharedPreferences"
Cohesion: 0.18
Nodes (11): android.content.SharedPreferences, JSONObject, active(), labelFor(), load(), other(), setActive(), TradingContext (+3 more)

### Community 6 - "MainActivity"
Cohesion: 0.05
Nodes (26): android.content.Context, android.graphics.drawable.GradientDrawable, android.graphics.drawable.RippleDrawable, android.graphics.Typeface, android.net.Uri, android.view.View, android.widget.EditText, android.widget.LinearLayout (+18 more)

### Community 7 - "EthRpc"
Cohesion: 0.07
Nodes (11): EthRpc, JSONArray, EthSend, EthTx, NonceState, JSONArray, EthSendTest, CommsScannerTest (+3 more)

### Community 9 - "AtomiX Full Security & Code Review Report"
Cohesion: 0.05
Nodes (40): Reuse before you reinvent, RULE 0 — Follow explicit user instructions (blocking), RULE 0 — Follow explicit user instructions (blocking), AtomiX Android atomic-swap app (Minima↔Ethereum USDT), Concurrency control / thread-safety (volatile, TOCTOU, races), CR-1: SwapEngine DB persistence race before ETH broadcast, CR-2: SwapEngine unchecked io.execute() crashes after shutdown, CR-3: CommsScanner exception in process() freezes scanner (+32 more)

### Community 10 - "CommsScanner"
Cohesion: 0.13
Nodes (6): Override, PrefsMeta, CommsScanner, Listener, MetaStore, Router

### Community 11 - "SwapService"
Cohesion: 0.12
Nodes (8): android.app.Notification, android.app.Service, android.os.IBinder, Intent, JSONObject, Override, SwapService, Builder

### Community 15 - "SwapDb"
Cohesion: 0.12
Nodes (5): android.database.Cursor, MarketCollector, Event, MarketTrade, SwapDb

### Community 16 - "SignGate"
Cohesion: 0.14
Nodes (7): Handler, Op, Release, SignGate, Override, Rec, SignGateTest

### Community 17 - "IdentityWatch"
Cohesion: 0.13
Nodes (4): IdentityWatch, Notifier, IdentityWatchTest, JSONArray

### Community 18 - "EthWallet"
Cohesion: 0.23
Nodes (3): android.os.Handler, Cb, EthWallet

### Community 21 - "EthHtlc"
Cohesion: 0.09
Nodes (6): Contract, EthHtlc, JSONObject, JSONObject, InspectCb, EthEncodingTest

### Community 22 - "org.junit.Before"
Cohesion: 0.29
Nodes (7): EthNet, MAINNET, from(), token(), tokenByAddress(), org.junit.Before, Token

### Community 23 - ".pegLevels"
Cohesion: 0.18
Nodes (3): PegLevelsTest, PriceModelGuardTest, org.junit.After

### Community 25 - "NodeApi"
Cohesion: 0.13
Nodes (7): Cb, NodeApi, PairingListener, MinimaAPI, MinimaAPIListener, org.minimarex.minimaapi.MinimaAPI, org.minimarex.minimaapi.MinimaAPIListener

### Community 26 - ".to"
Cohesion: 0.34
Nodes (3): JSONObject, JSONObject, SwapOrderBookTest

### Community 27 - ".w"
Cohesion: 0.25
Nodes (8): android.content.BroadcastReceiver, android.content.Intent, BootReceiver, Intent, Override, HeartbeatReceiver, Intent, Override

### Community 29 - "OtcOffer"
Cohesion: 0.17
Nodes (4): JSONObject, OtcBook, JSONObject, OtcOffer

### Community 30 - "MarketChartView"
Cohesion: 0.27
Nodes (4): android.graphics.Canvas, android.graphics.Paint, Override, MarketChartView

### Community 32 - "AtomiX Launcher Icon Foreground (hdpi)"
Cohesion: 0.17
Nodes (18): AtomiX Launcher Icon Foreground (hdpi), Atom Orbit Motif (atomic swap symbolism), AtomiX Brand Identity, Minima 'M' Logo Mark (red/blue/grey bars), USDT Tether Symbol (green circular badges), Atomic Orbit Ring Motif (atomic-swap symbolism), AtomiX App Branding (Minima-to-USDT atomic swap identity), Stylized Minima 'M' Logomark (red-orange and navy halves) (+10 more)

### Community 33 - "AtomiX"
Cohesion: 0.14
Nodes (18): Cross-chain Atomic Swaps, AtomiX, Shared Bridge HTLC Contract (Minima leg), Bridge Vault (Ethereum ERC20 leg), Currency-Agnostic Settlement, Header Currency Pill (currency switcher), Hash-Time-Locked Contract (HTLC), MEXC MINIMA/USDT Price Feed (+10 more)

### Community 35 - "org.json.JSONObject"
Cohesion: 0.15
Nodes (8): android.widget.ImageView, androidx.activity.result.ActivityResultLauncher, androidx.appcompat.app.AppCompatActivity, SwapOrderBook, CommsTransport, Hex, Opened, org.json.JSONObject

### Community 36 - "Override"
Cohesion: 0.16
Nodes (5): android.text.TextWatcher, Override, SimpleWatcher, Editable, TextWatcher

### Community 38 - "CryptoProvider"
Cohesion: 0.21
Nodes (3): SwapTake, JSONObject, CryptoProvider

### Community 39 - "com.goterl.lazysodium.LazySodium"
Cohesion: 0.18
Nodes (5): CommsIdentity, LocalEcCryptoProvider, Sodium, CommsSwapFlowTest, com.goterl.lazysodium.LazySodium

### Community 40 - "android.database.sqlite.SQLiteDatabase"
Cohesion: 0.20
Nodes (6): android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, Helper, Override, Helper, Override

### Community 41 - "org.junit.Test"
Cohesion: 0.08
Nodes (5): AmountGrainTest, TimelockSafetyTest, TradingContextTest, UtilTest, org.junit.Test

### Community 43 - "SwapWorker.java"
Cohesion: 0.31
Nodes (6): androidx.annotation.NonNull, androidx.work.Worker, androidx.work.WorkerParameters, Override, SwapWorker, Result

### Community 48 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 50 - "Android platform boundary (FGS, lifecycle, WorkManager)"
Cohesion: 0.50
Nodes (4): Android platform boundary (FGS, lifecycle, WorkManager), MA-13: SwapWorker startForegroundService prohibited on Android 12+, MA-14: AndroidManifest dataSync caps FGS at ~6h/day on Android 14+, MA-17: Images.java bitmap memory leaks (OOM)

### Community 51 - "Command injection into Minima node IPC commands"
Cohesion: 0.67
Nodes (4): Command injection into Minima node IPC commands, MA-10: CommsScanner command injection via targetAddress, MA-19: MinimaHtlc command-string injection via unescaped on-chain values, MA-9: CommsTransport command injection into Minima commands

## Knowledge Gaps
- **31 isolated node(s):** `install.sh script`, `ONYX`, `DAYLIGHT`, `MINIMA`, `MXUSDT` (+26 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `MinimaHtlc`, `Order`, `OtcController`, `PriceOracle`, `android.content.SharedPreferences`, `EthRpc`, `CommsScanner`, `SendCb`, `SwapDb`, `IdentityWatch`, `EthWallet`, `.compareForFill`, `org.junit.Before`, `.checkBuyNow`, `NodeApi`, `OtcOffer`, `org.json.JSONObject`, `Override`, `.startLeg`, `CryptoProvider`, `com.goterl.lazysodium.LazySodium`, `SwapEngine`?**
  _High betweenness centrality (0.254) - this node is a cross-community bridge._
- **Why does `SwapEngine` connect `SwapEngine` to `MinimaHtlc`, `Order`, `OtcController`, `MainActivity`, `EthRpc`, `AtomiX Full Security & Code Review Report`, `SwapService`, `GateTest`, `SendCb`, `SwapDb`, `IdentityWatch`, `EthWallet`, `.stateAt`, `EthHtlc`, `org.junit.Before`, `.checkBuyNow`, `NodeApi`, `.setNetwork`, `.checkExpiredMinima`, `.checkCanSwapCoin`, `org.json.JSONObject`, `Override`, `.startLeg`, `SwapEngine.java`, `.allSwaps`, `.checkLadderCoins`?**
  _High betweenness centrality (0.188) - this node is a cross-community bridge._
- **Why does `MinimaHtlc` connect `MinimaHtlc` to `OtcController`, `android.content.SharedPreferences`, `MainActivity`, `HtlcTxnConstructionTest`, `SwapService`, `GateTest`, `.claim`, `SwapDb`, `IdentityWatch`, `EthWallet`, `.stateAt`, `.checkBuyNow`, `NodeApi`, `.checkExpiredMinima`, `.checkCanSwapCoin`, `org.json.JSONObject`, `.startLeg`, `org.junit.Test`, `IdentityBlockTest`, `SwapEngine.java`, `SwapEngine`, `.allSwaps`, `.checkLadderCoins`?**
  _High betweenness centrality (0.093) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `SwapEngine` (e.g. with `CR-1: SwapEngine DB persistence race before ETH broadcast` and `CR-2: SwapEngine unchecked io.execute() crashes after shutdown`) actually correct?**
  _`SwapEngine` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 4 inferred relationships involving `Order` (e.g. with `.setUp()` and `.pegLevels()`) actually correct?**
  _`Order` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `install.sh script`, `ONYX`, `DAYLIGHT` to the rest of the system?**
  _31 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.from` be split into smaller, more focused modules?**
  _Cohesion score 0.14285714285714285 - nodes in this community are weakly interconnected._