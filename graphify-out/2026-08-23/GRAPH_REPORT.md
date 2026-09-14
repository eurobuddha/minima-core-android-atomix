# Graph Report - atomix  (2026-08-23)

## Corpus Check
- 82 files · ~107,232 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1405 nodes · 4889 edges · 63 communities (41 shown, 22 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 558 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `56f6b4cc`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MinimaHtlc
- .generateVectors
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
- .ready
- .claim
- SwapDb
- SignGate
- IdentityWatch
- .renderSwapTab
- EthHtlc
- .reviewSweep
- EthSend
- EthWallet
- .pegLevels
- .checkBuyNow
- NodeApi
- .to
- .dp
- SwapEngine
- OtcOffer
- .swapCard
- .checkExpiredMinima
- AtomiX Launcher Icon Foreground (hdpi)
- AtomiX
- .checkCanSwapCoin
- SwapService.java
- Override
- .allSwaps
- SendCb
- com.goterl.lazysodium.LazySodium
- android.content.Context
- org.junit.Test
- Util
- .normKey
- IdentityBlockTest
- .onCreate
- QrUtil
- .derive
- gradlew
- org.json.JSONObject
- Android platform boundary (FGS, lifecycle, WorkManager)
- Command injection into Minima node IPC commands
- Pre-commit hook blocks code change without version bump
- install.sh
- pre-commit
- org.junit.Before
- TradingContext
- CommsScannerTest
- Override
- TextView

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 208 edges
2. `SwapEngine` - 119 edges
3. `MinimaHtlc` - 76 edges
4. `NodeApi` - 64 edges
5. `Order` - 52 edges
6. `SwapService` - 50 edges
7. `SwapDb` - 47 edges
8. `Design` - 40 edges
9. `EthRpc` - 36 edges
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

## Communities (63 total, 22 thin omitted)

### Community 0 - "MinimaHtlc"
Cohesion: 0.17
Nodes (4): JSONArray, KeysCb, MinimaHtlc, SetupCb

### Community 2 - "Order"
Cohesion: 0.07
Nodes (9): JSONArray, JSONObject, Level, Order, Pair, OrderBookTest, OrderBookTombstoneTest, RoutingOrderTest (+1 more)

### Community 3 - "OtcController"
Cohesion: 0.11
Nodes (10): SecretCb, OtcController, SendResult, Ui, Deal, Msg, OtcDb, OtcMessage (+2 more)

### Community 4 - "PriceOracle"
Cohesion: 0.15
Nodes (4): JSONArray, JSONObject, PriceOracle, Order

### Community 5 - "android.content.SharedPreferences"
Cohesion: 0.16
Nodes (6): android.content.SharedPreferences, JSONObject, Override, PrefsMeta, MarketMemoryTest, TradingContext

### Community 6 - "MainActivity"
Cohesion: 0.07
Nodes (8): android.net.Uri, android.widget.ScrollView, MainActivity, com.eurobuddha.atomix.eth.EthNet, com.eurobuddha.atomix.swap.OtcOffer, Deal, Event, Notifier

### Community 7 - "EthRpc"
Cohesion: 0.17
Nodes (4): EthRpc, JSONArray, JSONObject, JSONArray

### Community 9 - "AtomiX Full Security & Code Review Report"
Cohesion: 0.05
Nodes (40): Reuse before you reinvent, RULE 0 — Follow explicit user instructions (blocking), RULE 0 — Follow explicit user instructions (blocking), AtomiX Android atomic-swap app (Minima↔Ethereum USDT), Concurrency control / thread-safety (volatile, TOCTOU, races), CR-1: SwapEngine DB persistence race before ETH broadcast, CR-2: SwapEngine unchecked io.execute() crashes after shutdown, CR-3: CommsScanner exception in process() freezes scanner (+32 more)

### Community 10 - "CommsScanner"
Cohesion: 0.11
Nodes (5): CommsScanner, Listener, MetaStore, Router, CommsScanner

### Community 11 - "SwapService"
Cohesion: 0.14
Nodes (5): android.app.Notification, android.app.Service, Override, SwapService, Builder

### Community 13 - ".ready"
Cohesion: 0.13
Nodes (3): PublishGate, JSONObject, OtcOffer

### Community 15 - "SwapDb"
Cohesion: 0.19
Nodes (3): Event, MarketTrade, SwapDb

### Community 16 - "SignGate"
Cohesion: 0.14
Nodes (7): Handler, Op, Release, SignGate, Override, Rec, SignGateTest

### Community 17 - "IdentityWatch"
Cohesion: 0.14
Nodes (4): IdentityWatch, Notifier, IdentityWatchTest, JSONArray

### Community 18 - ".renderSwapTab"
Cohesion: 0.11
Nodes (10): android.graphics.drawable.GradientDrawable, android.graphics.drawable.RippleDrawable, android.graphics.Typeface, android.view.View, android.widget.TextView, Design, TextView, GradientDrawable (+2 more)

### Community 19 - "EthHtlc"
Cohesion: 0.11
Nodes (4): Contract, EthHtlc, JSONObject, EthEncodingTest

### Community 20 - ".reviewSweep"
Cohesion: 0.11
Nodes (7): Best, SweepLeg, SweepPlan, com.eurobuddha.atomix.swap.Order, Level, SendResult, StartCb

### Community 22 - "EthWallet"
Cohesion: 0.19
Nodes (9): EthNet, MAINNET, from(), token(), tokenByAddress(), EthWallet, org.minimarex.minimaapi.MinimaAPI, org.minimarex.minimaapi.MinimaAPIListener (+1 more)

### Community 23 - ".pegLevels"
Cohesion: 0.19
Nodes (3): PegLevelsTest, PriceModelGuardTest, org.junit.After

### Community 25 - "NodeApi"
Cohesion: 0.11
Nodes (8): android.os.Handler, Cb, SwapOrderBook, Cb, NodeApi, PairingListener, MinimaAPI, MinimaAPIListener

### Community 26 - ".to"
Cohesion: 0.34
Nodes (3): JSONObject, JSONObject, SwapOrderBookTest

### Community 27 - ".dp"
Cohesion: 0.26
Nodes (7): android.widget.EditText, android.widget.LinearLayout, EditText, LayoutParams, LinearLayout, MarketTrade, TextView

### Community 29 - "OtcOffer"
Cohesion: 0.16
Nodes (4): JSONObject, OtcBook, JSONObject, OtcOffer

### Community 32 - "AtomiX Launcher Icon Foreground (hdpi)"
Cohesion: 0.17
Nodes (18): AtomiX Launcher Icon Foreground (hdpi), Atom Orbit Motif (atomic swap symbolism), AtomiX Brand Identity, Minima 'M' Logo Mark (red/blue/grey bars), USDT Tether Symbol (green circular badges), Atomic Orbit Ring Motif (atomic-swap symbolism), AtomiX App Branding (Minima-to-USDT atomic swap identity), Stylized Minima 'M' Logomark (red-orange and navy halves) (+10 more)

### Community 33 - "AtomiX"
Cohesion: 0.14
Nodes (18): Cross-chain Atomic Swaps, AtomiX, Shared Bridge HTLC Contract (Minima leg), Bridge Vault (Ethereum ERC20 leg), Currency-Agnostic Settlement, Header Currency Pill (currency switcher), Hash-Time-Locked Contract (HTLC), MEXC MINIMA/USDT Price Feed (+10 more)

### Community 34 - ".checkCanSwapCoin"
Cohesion: 0.21
Nodes (3): Swap, JSONObject, MismatchPoisonTest

### Community 35 - "SwapService.java"
Cohesion: 0.09
Nodes (19): android.widget.ImageView, androidx.activity.result.ActivityResultLauncher, androidx.appcompat.app.AppCompatActivity, SwapTake, CommsTransport, CryptoProvider, Hex, Opened (+11 more)

### Community 36 - "Override"
Cohesion: 0.16
Nodes (5): android.text.TextWatcher, SimpleWatcher, Editable, Override, TextWatcher

### Community 38 - "SendCb"
Cohesion: 0.27
Nodes (3): JSONObject, SendCb, com.eurobuddha.comms.Opened

### Community 39 - "com.goterl.lazysodium.LazySodium"
Cohesion: 0.10
Nodes (7): CommsIdentity, Override, LocalEcCryptoProvider, Sodium, CommsSwapFlowTest, HexTest, com.goterl.lazysodium.LazySodium

### Community 40 - "android.content.Context"
Cohesion: 0.05
Nodes (31): android.content.BroadcastReceiver, android.content.Context, android.content.Intent, android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, android.graphics.Canvas, android.graphics.Paint (+23 more)

### Community 41 - "org.junit.Test"
Cohesion: 0.08
Nodes (5): AmountGrainTest, TimelockSafetyTest, TradingContextTest, UtilTest, org.junit.Test

### Community 45 - ".onCreate"
Cohesion: 0.12
Nodes (9): android.os.Bundle, EthRpc, MinimaHtlc, NodeApi, OtcController, OtcDb, ScrollView, SwapDb (+1 more)

### Community 48 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 49 - "org.json.JSONObject"
Cohesion: 0.16
Nodes (6): EthTx, NonceState, SwapLog, org.json.JSONArray, org.json.JSONObject, org.web3j.crypto.Credentials

### Community 50 - "Android platform boundary (FGS, lifecycle, WorkManager)"
Cohesion: 0.50
Nodes (4): Android platform boundary (FGS, lifecycle, WorkManager), MA-13: SwapWorker startForegroundService prohibited on Android 12+, MA-14: AndroidManifest dataSync caps FGS at ~6h/day on Android 14+, MA-17: Images.java bitmap memory leaks (OOM)

### Community 51 - "Command injection into Minima node IPC commands"
Cohesion: 0.67
Nodes (4): Command injection into Minima node IPC commands, MA-10: CommsScanner command injection via targetAddress, MA-19: MinimaHtlc command-string injection via unescaped on-chain values, MA-9: CommsTransport command injection into Minima commands

### Community 58 - "TradingContext"
Cohesion: 0.36
Nodes (8): active(), labelFor(), load(), other(), setActive(), TradingContext, MINIMA, MXUSDT

## Knowledge Gaps
- **31 isolated node(s):** `install.sh script`, `ONYX`, `DAYLIGHT`, `MINIMA`, `MXUSDT` (+26 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **22 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `OtcController`, `PriceOracle`, `android.content.SharedPreferences`, `EthRpc`, `CommsScanner`, `.ready`, `.renderSwapTab`, `.reviewSweep`, `EthSend`, `.checkBuyNow`, `NodeApi`, `.dp`, `.swapCard`, `SwapService.java`, `Override`, `SendCb`, `com.goterl.lazysodium.LazySodium`, `android.content.Context`, `.onCreate`?**
  _High betweenness centrality (0.202) - this node is a cross-community bridge._
- **Why does `SwapEngine` connect `SwapEngine` to `MinimaHtlc`, `Order`, `OtcController`, `android.content.SharedPreferences`, `EthRpc`, `AtomiX Full Security & Code Review Report`, `SwapService`, `GateTest`, `.ready`, `SwapDb`, `IdentityWatch`, `EthHtlc`, `EthWallet`, `.checkBuyNow`, `NodeApi`, `.checkExpiredMinima`, `.checkCanSwapCoin`, `SwapService.java`, `Override`, `.allSwaps`, `.normKey`, `.onCreate`, `org.json.JSONObject`, `.checkEthContractFor`, `org.junit.Before`?**
  _High betweenness centrality (0.187) - this node is a cross-community bridge._
- **Why does `SwapDb` connect `SwapDb` to `.checkCanSwapCoin`, `SwapService.java`, `.allSwaps`, `android.content.Context`, `.normKey`, `SwapService`, `.onCreate`, `.checkEthContractFor`, `NodeApi`, `SwapEngine`, `.checkExpiredMinima`?**
  _High betweenness centrality (0.076) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `SwapEngine` (e.g. with `CR-1: SwapEngine DB persistence race before ETH broadcast` and `CR-2: SwapEngine unchecked io.execute() crashes after shutdown`) actually correct?**
  _`SwapEngine` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `install.sh script`, `ONYX`, `DAYLIGHT` to the rest of the system?**
  _31 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Order` be split into smaller, more focused modules?**
  _Cohesion score 0.07256571640133284 - nodes in this community are weakly interconnected._
- **Should `OtcController` be split into smaller, more focused modules?**
  _Cohesion score 0.11103047895500726 - nodes in this community are weakly interconnected._