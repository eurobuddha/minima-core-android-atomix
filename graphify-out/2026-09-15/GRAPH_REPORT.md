# Graph Report - apks/atomix  (2026-09-04)

## Corpus Check
- 82 files · ~107,559 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1448 nodes · 5004 edges · 57 communities (36 shown, 21 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 573 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `2ef4df59`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- MinimaHtlc
- com.goterl.lazysodium.LazySodium
- Order
- OtcController
- PriceOracle
- android.content.SharedPreferences
- MainActivity
- EthRpc
- 🟠 MAJOR (23 issues)
- AtomiX Full Security & Code Review Report
- CommsScanner
- SwapService
- GateTest
- .ready
- 🟡 MINOR (17 issues)
- SwapDb
- SignGate
- IdentityWatch
- .onCreate
- EthHtlc
- .compareForFill
- org.junit.Test
- NodeApi
- .pegLevels
- Util
- .cmd
- .to
- android.content.Context
- .address
- org.json.JSONObject
- AtomiX — Full Security & Code Review Report
- Swap
- AtomiX Launcher Icon Foreground (hdpi)
- AtomiX
- .checkCanSwapCoin
- MainActivity.java
- Override
- 🔴 CRITICAL (8 issues)
- SendCb
- .fmt5
- .generateVectors
- AmountGrainTest
- User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly.
- User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly.
- IdentityBlockTest
- .derive
- gradlew
- Android platform boundary (FGS, lifecycle, WorkManager)
- Command injection into Minima node IPC commands
- Pre-commit hook blocks code change without version bump
- install.sh
- pre-commit
- SwapEngine
- .allSwaps
- .checkCanCollectEth

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
  SECURIRY_REVIEW.md → apks/atomix/app/src/main/java/com/eurobuddha/atomix/swap/SwapEngine.java
- `MA-20: SwapEngine claim-gate race burns two one-time signing leaves` --references--> `SwapEngine`  [INFERRED]
  SECURIRY_REVIEW.md → apks/atomix/app/src/main/java/com/eurobuddha/atomix/swap/SwapEngine.java
- `CR-1: SwapEngine DB persistence race before ETH broadcast` --references--> `SwapEngine`  [INFERRED]
  SECURIRY_REVIEW.md → apks/atomix/app/src/main/java/com/eurobuddha/atomix/swap/SwapEngine.java
- `RULE 0 — Follow explicit user instructions (blocking)` --semantically_similar_to--> `RULE 0 — Follow explicit user instructions (blocking)`  [INFERRED] [semantically similar]
  CLAUDE.md → AGENTS.md
- `AtomiX` --implements--> `Cross-chain Atomic Swaps`  [EXTRACTED]
  apks/atomix/README.md → README.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **SignGate serial one-time-signing invariant (jam + concurrent-signing + off-thread hazards)** — securiry_review_cr5_signgate_queue_jam, securiry_review_cr6_signgate_concurrent_signing, securiry_review_ma23_signgate_not_thread_safe, securiry_review_winternitz_one_time_signature [INFERRED 0.85]
- **Minima node IPC command-injection surface (unescaped concatenation)** — securiry_review_ma9_commstransport_injection, securiry_review_ma10_commsscanner_injection, securiry_review_ma19_minimahtlc_injection, securiry_review_command_injection [INFERRED 0.85]
- **Fund-stranding races (record-before-broadcast, TOCTOU, claim-gate)** — securiry_review_cr1_db_race_before_broadcast, securiry_review_ma15_merchdb_toctou, securiry_review_ma20_swapengine_claim_gate_race, securiry_review_fund_stranding [INFERRED 0.75]
- **Two-Leg HTLC Atomic Swap Flow** — readme_htlc, readme_bridge_htlc_contract, readme_bridge_vault, readme_minimacore_node, readme_web3j_wallet [EXTRACTED 1.00]
- **Dual-Currency Trading System (TradingContext-driven)** — readme_tradingcontext, readme_currency_pill, readme_minimaswap_market, readme_usdtswap_market, readme_sentinel_boards [EXTRACTED 1.00]

## Communities (57 total, 21 thin omitted)

### Community 0 - "MinimaHtlc"
Cohesion: 0.07
Nodes (9): MarketCollector, JSONArray, JSONObject, KeysCb, MinimaHtlc, PostCb, SetupCb, HtlcTxnConstructionTest (+1 more)

### Community 1 - "com.goterl.lazysodium.LazySodium"
Cohesion: 0.12
Nodes (6): CommsIdentity, Override, LocalEcCryptoProvider, CommsSwapFlowTest, HexTest, com.goterl.lazysodium.LazySodium

### Community 2 - "Order"
Cohesion: 0.06
Nodes (12): Best, BlockCb, SecretCb, JSONArray, JSONObject, Level, Order, Pair (+4 more)

### Community 3 - "OtcController"
Cohesion: 0.13
Nodes (8): OtcController, SendResult, Ui, Deal, Msg, OtcDb, OtcMessage, java.security.SecureRandom

### Community 4 - "PriceOracle"
Cohesion: 0.15
Nodes (3): JSONArray, JSONObject, PriceOracle

### Community 5 - "android.content.SharedPreferences"
Cohesion: 0.18
Nodes (11): android.content.SharedPreferences, JSONObject, active(), labelFor(), load(), other(), setActive(), TradingContext (+3 more)

### Community 6 - "MainActivity"
Cohesion: 0.06
Nodes (21): android.graphics.drawable.GradientDrawable, android.graphics.drawable.RippleDrawable, android.graphics.Typeface, android.net.Uri, android.view.View, android.widget.EditText, android.widget.LinearLayout, android.widget.ScrollView (+13 more)

### Community 7 - "EthRpc"
Cohesion: 0.08
Nodes (9): EthRpc, JSONArray, JSONObject, EthSend, EthTx, NonceState, EthSendTest, org.json.JSONArray (+1 more)

### Community 8 - "🟠 MAJOR (23 issues)"
Cohesion: 0.08
Nodes (24): MA-10: CommsScanner — Command injection via `targetAddress`, MA-11: NodeApi — `cmd()` silently drops callbacks after release, MA-12: NodeApi — Response callbacks fire after `onDestroy()`, MA-13: SwapWorker — Calls `startForegroundService()` from Worker (prohibited on Android 12+), MA-14: AndroidManifest — `dataSync` caps FGS at ~6h/day on Android 14+, MA-15: MerchDb — `recordPayment()` read-then-write TOCTOU race, MA-16: MerchDb — Missing `onDowngrade()` crashes on APK rollback, MA-17: Images.java — Bitmap memory leaks (+16 more)

### Community 9 - "AtomiX Full Security & Code Review Report"
Cohesion: 0.05
Nodes (40): Reuse before you reinvent, RULE 0 — Follow explicit user instructions (blocking), RULE 0 — Follow explicit user instructions (blocking), AtomiX Android atomic-swap app (Minima↔Ethereum USDT), Concurrency control / thread-safety (volatile, TOCTOU, races), CR-1: SwapEngine DB persistence race before ETH broadcast, CR-2: SwapEngine unchecked io.execute() crashes after shutdown, CR-3: CommsScanner exception in process() freezes scanner (+32 more)

### Community 10 - "CommsScanner"
Cohesion: 0.09
Nodes (8): Override, PrefsMeta, CommsScanner, Listener, MetaStore, Router, CommsScannerTest, JSONArray

### Community 11 - "SwapService"
Cohesion: 0.09
Nodes (9): android.app.Notification, android.app.Service, android.os.IBinder, Intent, JSONObject, Override, SwapService, Builder (+1 more)

### Community 14 - "🟡 MINOR (17 issues)"
Cohesion: 0.11
Nodes (19): MI-10: LocalEcCryptoProvider — Returns plaintext on auth failure, MI-11: QrUtil — Unbounded bitmap size can OOM, MI-12: MainActivity — `pegTick` Runnable leaks across `onPause`, MI-13: BootReceiver — Swallows exceptions silently, MI-14: HeartbeatReceiver — Logs only class name, MI-15: MerchDb — `queryOrders()` only guards some column indices, MI-16: Images.java — Catches `Throwable`, MI-17: MarketCollector — Deprecated `BigDecimal` constant (+11 more)

### Community 16 - "SignGate"
Cohesion: 0.14
Nodes (7): Handler, Op, Release, SignGate, Override, Rec, SignGateTest

### Community 17 - "IdentityWatch"
Cohesion: 0.16
Nodes (3): IdentityWatch, IdentityWatchTest, JSONArray

### Community 19 - "EthHtlc"
Cohesion: 0.15
Nodes (4): EthHtlc, JSONObject, InspectCb, JSONArray

### Community 21 - "org.junit.Test"
Cohesion: 0.10
Nodes (4): EthEncodingTest, TimelockSafetyTest, TradingContextTest, org.junit.Test

### Community 22 - "NodeApi"
Cohesion: 0.14
Nodes (13): android.os.Handler, EthNet, MAINNET, from(), token(), tokenByAddress(), EthWallet, Notifier (+5 more)

### Community 23 - ".pegLevels"
Cohesion: 0.18
Nodes (3): PegLevelsTest, PriceModelGuardTest, org.junit.After

### Community 25 - ".cmd"
Cohesion: 0.11
Nodes (5): Cb, OtcBook, SwapOrderBook, PairingListener, MinimaAPIListener

### Community 26 - ".to"
Cohesion: 0.34
Nodes (3): JSONObject, JSONObject, SwapOrderBookTest

### Community 27 - "android.content.Context"
Cohesion: 0.05
Nodes (30): android.content.BroadcastReceiver, android.content.Context, android.content.Intent, android.database.Cursor, android.database.sqlite.SQLiteDatabase, android.database.sqlite.SQLiteOpenHelper, android.graphics.Canvas, android.graphics.Paint (+22 more)

### Community 29 - "org.json.JSONObject"
Cohesion: 0.13
Nodes (6): JSONObject, JSONObject, OtcOffer, SwapLog, Cb, org.json.JSONObject

### Community 30 - "AtomiX — Full Security & Code Review Report"
Cohesion: 0.22
Nodes (8): AtomiX — Full Security & Code Review Report, Executive Summary, NI-1: Util.shorten() and MainActivity.shortAddr() truncate identifiers, NI-2: EthRpc `readAll` uses string charset name, NI-3: Hkdf.java counter overflow for large `len`, 🔵 NIT (3 issues), Verdict, What the Code Does Well

### Community 31 - "Swap"
Cohesion: 0.19
Nodes (3): Swap, JSONObject, RefundRetryTest

### Community 32 - "AtomiX Launcher Icon Foreground (hdpi)"
Cohesion: 0.17
Nodes (18): AtomiX Launcher Icon Foreground (hdpi), Atom Orbit Motif (atomic swap symbolism), AtomiX Brand Identity, Minima 'M' Logo Mark (red/blue/grey bars), USDT Tether Symbol (green circular badges), Atomic Orbit Ring Motif (atomic-swap symbolism), AtomiX App Branding (Minima-to-USDT atomic swap identity), Stylized Minima 'M' Logomark (red-orange and navy halves) (+10 more)

### Community 33 - "AtomiX"
Cohesion: 0.10
Nodes (24): Cross-chain Atomic Swaps, AtomiX, Shared Bridge HTLC Contract (Minima leg), Bridge Vault (Ethereum ERC20 leg), Build, Currency-Agnostic Settlement, Header Currency Pill (currency switcher), How it works (+16 more)

### Community 35 - "MainActivity.java"
Cohesion: 0.11
Nodes (11): android.graphics.Bitmap, android.widget.ImageView, androidx.activity.result.ActivityResultLauncher, androidx.appcompat.app.AppCompatActivity, SwapTake, CommsTransport, CryptoProvider, Hex (+3 more)

### Community 36 - "Override"
Cohesion: 0.16
Nodes (5): android.text.TextWatcher, Override, SimpleWatcher, Editable, TextWatcher

### Community 37 - "🔴 CRITICAL (8 issues)"
Cohesion: 0.22
Nodes (9): CR-1: SwapEngine — DB persistence race before ETH broadcast (fund stranding), CR-2: SwapEngine — Un-checked `io.execute()` calls crash after shutdown, CR-3: CommsScanner — Exception in `process()` freezes scanner forever, CR-4: CommsScanner — `meta.setMeta()` I/O exception freezes scanner, CR-5: SignGate — Unprotected `r.run()` permanently jams signing queue, CR-6: SignGate — Watchdog + late callback starts concurrent signing (Winternitz key reuse), CR-7: MainActivity — Thread-unsafe cross-thread read of ETH balances, CR-8: IdentityWatch — Static coordination fields are not volatile (+1 more)

### Community 42 - "User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly."
Cohesion: 0.50
Nodes (3): RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions., User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly., Versioning guardrail — every code change ships with a version bump

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
- **95 isolated node(s):** `install.sh script`, `ONYX`, `DAYLIGHT`, `MINIMA`, `MXUSDT` (+90 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **21 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `MainActivity` to `MinimaHtlc`, `com.goterl.lazysodium.LazySodium`, `Order`, `OtcController`, `PriceOracle`, `android.content.SharedPreferences`, `EthRpc`, `CommsScanner`, `SwapService`, `.ready`, `SwapDb`, `.onCreate`, `.compareForFill`, `NodeApi`, `.cmd`, `android.content.Context`, `org.json.JSONObject`, `MainActivity.java`, `Override`, `SendCb`, `SwapEngine`?**
  _High betweenness centrality (0.240) - this node is a cross-community bridge._
- **Why does `SwapEngine` connect `SwapEngine` to `MinimaHtlc`, `Order`, `OtcController`, `PriceOracle`, `MainActivity`, `EthRpc`, `AtomiX Full Security & Code Review Report`, `SwapService`, `GateTest`, `.ready`, `SwapDb`, `.onCreate`, `EthHtlc`, `NodeApi`, `.address`, `Swap`, `.checkCanSwapCoin`, `MainActivity.java`, `Override`, `.allSwaps`, `.checkCanCollectEth`?**
  _High betweenness centrality (0.191) - this node is a cross-community bridge._
- **Why does `MinimaHtlc` connect `MinimaHtlc` to `Order`, `MainActivity.java`, `.checkCanSwapCoin`, `android.content.SharedPreferences`, `MainActivity`, `AmountGrainTest`, `SwapService`, `IdentityBlockTest`, `IdentityWatch`, `.onCreate`, `org.junit.Test`, `NodeApi`, `SwapEngine`, `.allSwaps`, `.checkCanCollectEth`, `org.json.JSONObject`, `Swap`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `SwapEngine` (e.g. with `CR-1: SwapEngine DB persistence race before ETH broadcast` and `CR-2: SwapEngine unchecked io.execute() crashes after shutdown`) actually correct?**
  _`SwapEngine` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 4 inferred relationships involving `Order` (e.g. with `.setUp()` and `.pegLevels()`) actually correct?**
  _`Order` has 4 INFERRED edges - model-reasoned connections that need verification._
- **What connects `install.sh script`, `ONYX`, `DAYLIGHT` to the rest of the system?**
  _95 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MinimaHtlc` be split into smaller, more focused modules?**
  _Cohesion score 0.07056936647955092 - nodes in this community are weakly interconnected._