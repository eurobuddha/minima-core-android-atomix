# Graph Report - atomix  (2026-08-28)

## Corpus Check
- 82 files · ~107,559 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1391 nodes · 4929 edges · 57 communities (42 shown, 15 thin omitted)
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 573 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c06f0f21`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MinimaHtlc
- CommsIdentity
- Order
- OtcController
- PriceOracle
- android.content.SharedPreferences
- MainActivity
- EthRpc
- .dp
- AtomiX Full Security & Code Review Report
- CommsScanner
- SwapService
- GateTest
- Design
- .renderSwapTab
- SwapDb
- SignGate
- IdentityWatch
- .TEXT
- .checkEthContractFor
- .compareForFill
- EthEncodingTest
- NodeApi
- .pegLevels
- TradingContext
- .cmd
- .to
- .onReceive
- SwapEngine
- OtcOffer
- MarketChartView
- .getSwap
- AtomiX Launcher Icon Foreground (hdpi)
- AtomiX
- .checkCanSwapCoin
- MainActivity.java
- Override
- .startLeg
- SendCb
- .fmt5
- JSONArray
- org.junit.Test
- SwapWorker.java
- IdentityBlockTest
- .hasLiquidity
- .derive
- gradlew
- Android platform boundary (FGS, lifecycle, WorkManager)
- Command injection into Minima node IPC commands
- Pre-commit hook blocks code change without version bump
- install.sh
- pre-commit
- .w
- .allSwaps
- .checkCanCollectEth

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 208 edges
2. `SwapEngine` - 122 edges
3. `MinimaHtlc` - 76 edges
4. `NodeApi` - 63 edges
5. `Order` - 61 edges
6. `SwapService` - 50 edges
7. `SwapDb` - 46 edges
8. `Design` - 40 edges
9. `EthRpc` - 35 edges
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

## Communities (57 total, 15 thin omitted)

### Community 0 - "MinimaHtlc"
Cohesion: 0.06
Nodes (11): JSONArray, JSONObject, KeysCb, MinimaHtlc, PostCb, SetupCb, SwapLog, Util (+3 more)

### Community 1 - "CommsIdentity"
Cohesion: 0.10
Nodes (5): CommsIdentity, Override, CommsSwapFlowTest, HexTest, InteropVectorsTest

### Community 2 - "Order"
Cohesion: 0.12
Nodes (7): Best, JSONArray, JSONObject, Level, Order, Pair, OrderBookTest

### Community 3 - "OtcController"
Cohesion: 0.09
Nodes (13): android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, OtcController, SendResult, Ui, Deal, Helper (+5 more)

### Community 4 - "PriceOracle"
Cohesion: 0.12
Nodes (4): JSONArray, JSONObject, PriceOracle, PublishGate

### Community 5 - "android.content.SharedPreferences"
Cohesion: 0.20
Nodes (5): android.content.SharedPreferences, JSONObject, Override, PrefsMeta, MarketMemoryTest

### Community 6 - "MainActivity"
Cohesion: 0.06
Nodes (6): android.net.Uri, android.os.Bundle, MainActivity, Swap, Builder, ScrollView

### Community 7 - "EthRpc"
Cohesion: 0.07
Nodes (11): Contract, EthHtlc, JSONObject, EthRpc, JSONArray, JSONObject, EthSend, EthTx (+3 more)

### Community 8 - ".dp"
Cohesion: 0.25
Nodes (5): android.widget.LinearLayout, android.widget.TextView, TextView, LayoutParams, LinearLayout

### Community 9 - "AtomiX Full Security & Code Review Report"
Cohesion: 0.05
Nodes (40): Reuse before you reinvent, RULE 0 — Follow explicit user instructions (blocking), RULE 0 — Follow explicit user instructions (blocking), AtomiX Android atomic-swap app (Minima↔Ethereum USDT), Concurrency control / thread-safety (volatile, TOCTOU, races), CR-1: SwapEngine DB persistence race before ETH broadcast, CR-2: SwapEngine unchecked io.execute() crashes after shutdown, CR-3: CommsScanner exception in process() freezes scanner (+32 more)

### Community 10 - "CommsScanner"
Cohesion: 0.11
Nodes (7): CommsScanner, Listener, MetaStore, Router, CommsScannerTest, JSONArray, org.json.JSONArray

### Community 11 - "SwapService"
Cohesion: 0.10
Nodes (5): android.app.Notification, android.app.Service, JSONObject, SwapService, MinimaAPI

### Community 13 - "Design"
Cohesion: 0.10
Nodes (12): android.content.Context, android.graphics.drawable.GradientDrawable, android.graphics.drawable.RippleDrawable, android.graphics.Typeface, android.view.View, Design, TextView, Mode (+4 more)

### Community 14 - ".renderSwapTab"
Cohesion: 0.15
Nodes (4): android.widget.EditText, SweepLeg, SweepPlan, EditText

### Community 15 - "SwapDb"
Cohesion: 0.13
Nodes (3): MarketCollector, MarketTrade, SwapDb

### Community 16 - "SignGate"
Cohesion: 0.14
Nodes (7): Handler, Op, Release, SignGate, Override, Rec, SignGateTest

### Community 17 - "IdentityWatch"
Cohesion: 0.14
Nodes (4): IdentityWatch, Notifier, IdentityWatchTest, JSONArray

### Community 19 - ".checkEthContractFor"
Cohesion: 0.13
Nodes (4): ConfirmCb, InspectCb, Swap, EthHtlc

### Community 22 - "NodeApi"
Cohesion: 0.13
Nodes (13): android.os.Handler, EthNet, MAINNET, from(), token(), tokenByAddress(), EthWallet, NodeApi (+5 more)

### Community 23 - ".pegLevels"
Cohesion: 0.18
Nodes (3): PegLevelsTest, PriceModelGuardTest, org.junit.After

### Community 24 - "TradingContext"
Cohesion: 0.36
Nodes (8): active(), labelFor(), load(), other(), setActive(), TradingContext, MINIMA, MXUSDT

### Community 25 - ".cmd"
Cohesion: 0.13
Nodes (3): Cb, SwapOrderBook, Cb

### Community 26 - ".to"
Cohesion: 0.34
Nodes (3): JSONObject, JSONObject, SwapOrderBookTest

### Community 27 - ".onReceive"
Cohesion: 0.16
Nodes (11): android.content.BroadcastReceiver, android.content.Intent, android.os.IBinder, BootReceiver, Intent, Override, HeartbeatReceiver, Intent (+3 more)

### Community 28 - "SwapEngine"
Cohesion: 0.16
Nodes (10): MinimaHtlc, SwapDb, SwapEngine, IdentityGuardTest, com.eurobuddha.atomix.eth.EthNet, com.eurobuddha.atomix.eth.EthRpc, com.eurobuddha.atomix.eth.EthWallet, com.eurobuddha.comms.NodeApi (+2 more)

### Community 29 - "OtcOffer"
Cohesion: 0.16
Nodes (4): JSONObject, OtcBook, JSONObject, OtcOffer

### Community 30 - "MarketChartView"
Cohesion: 0.30
Nodes (4): android.graphics.Canvas, android.graphics.Paint, Override, MarketChartView

### Community 31 - ".getSwap"
Cohesion: 0.16
Nodes (5): Event, JSONObject, MinimaHtlc, SwapDb, RefundRetryTest

### Community 32 - "AtomiX Launcher Icon Foreground (hdpi)"
Cohesion: 0.17
Nodes (18): AtomiX Launcher Icon Foreground (hdpi), Atom Orbit Motif (atomic swap symbolism), AtomiX Brand Identity, Minima 'M' Logo Mark (red/blue/grey bars), USDT Tether Symbol (green circular badges), Atomic Orbit Ring Motif (atomic-swap symbolism), AtomiX App Branding (Minima-to-USDT atomic swap identity), Stylized Minima 'M' Logomark (red-orange and navy halves) (+10 more)

### Community 33 - "AtomiX"
Cohesion: 0.14
Nodes (18): Cross-chain Atomic Swaps, AtomiX, Shared Bridge HTLC Contract (Minima leg), Bridge Vault (Ethereum ERC20 leg), Currency-Agnostic Settlement, Header Currency Pill (currency switcher), Hash-Time-Locked Contract (HTLC), MEXC MINIMA/USDT Price Feed (+10 more)

### Community 35 - "MainActivity.java"
Cohesion: 0.14
Nodes (11): android.graphics.Bitmap, android.widget.ImageView, android.widget.ScrollView, androidx.activity.result.ActivityResultLauncher, androidx.appcompat.app.AppCompatActivity, SwapTake, Hex, LocalEcCryptoProvider (+3 more)

### Community 36 - "Override"
Cohesion: 0.16
Nodes (5): android.text.TextWatcher, Override, SimpleWatcher, Editable, TextWatcher

### Community 37 - ".startLeg"
Cohesion: 0.19
Nodes (4): BlockCb, SecretCb, StartCb, Order

### Community 38 - "SendCb"
Cohesion: 0.13
Nodes (7): OtcMessage, CommsTransport, JSONObject, SendCb, CryptoProvider, Opened, java.security.SecureRandom

### Community 41 - "org.junit.Test"
Cohesion: 0.10
Nodes (4): AmountGrainTest, TimelockSafetyTest, TradingContextTest, org.junit.Test

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

### Community 58 - ".checkCanCollectEth"
Cohesion: 0.14
Nodes (3): Contract, Deal, Pair

## Knowledge Gaps
- **31 isolated node(s):** `install.sh script`, `ONYX`, `DAYLIGHT`, `MINIMA`, `MXUSDT` (+26 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **15 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `MinimaHtlc`, `CommsIdentity`, `Order`, `OtcController`, `PriceOracle`, `android.content.SharedPreferences`, `EthRpc`, `.dp`, `CommsScanner`, `Design`, `.renderSwapTab`, `SwapDb`, `IdentityWatch`, `.TEXT`, `.compareForFill`, `NodeApi`, `.cmd`, `SwapEngine`, `OtcOffer`, `.getSwap`, `MainActivity.java`, `Override`, `.startLeg`, `SendCb`, `.fmt5`?**
  _High betweenness centrality (0.239) - this node is a cross-community bridge._
- **Why does `SwapEngine` connect `SwapEngine` to `OtcController`, `PriceOracle`, `MainActivity`, `AtomiX Full Security & Code Review Report`, `CommsScanner`, `SwapService`, `GateTest`, `SwapDb`, `IdentityWatch`, `.checkEthContractFor`, `NodeApi`, `.getSwap`, `.checkCanSwapCoin`, `MainActivity.java`, `Override`, `.startLeg`, `.w`, `.allSwaps`, `.checkCanCollectEth`?**
  _High betweenness centrality (0.170) - this node is a cross-community bridge._
- **Why does `MinimaHtlc` connect `MinimaHtlc` to `.checkCanSwapCoin`, `MainActivity.java`, `.startLeg`, `MainActivity`, `org.junit.Test`, `CommsScanner`, `SwapService`, `IdentityBlockTest`, `SwapDb`, `IdentityWatch`, `.checkEthContractFor`, `NodeApi`, `TradingContext`, `.allSwaps`, `.checkCanCollectEth`?**
  _High betweenness centrality (0.092) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `SwapEngine` (e.g. with `CR-1: SwapEngine DB persistence race before ETH broadcast` and `CR-2: SwapEngine unchecked io.execute() crashes after shutdown`) actually correct?**
  _`SwapEngine` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `install.sh script`, `ONYX`, `DAYLIGHT` to the rest of the system?**
  _31 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MinimaHtlc` be split into smaller, more focused modules?**
  _Cohesion score 0.06471306471306472 - nodes in this community are weakly interconnected._
- **Should `CommsIdentity` be split into smaller, more focused modules?**
  _Cohesion score 0.10084033613445378 - nodes in this community are weakly interconnected._