# Graph Report - .  (2026-07-26)

## Corpus Check
- 77 files · ~89,062 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1276 nodes · 4150 edges · 51 communities (41 shown, 10 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 442 edges (avg confidence: 0.8)
- Token cost: 147,368 input · 0 output

## Community Hubs (Navigation)
- Market Data & Identity Setup
- Price Oracle & Pegging
- Main Activity Swap Lifecycle
- Comms Identity & Backup Crypto
- Order Book Model
- App Startup & OTC Controller
- Ethereum RPC Client
- UI Design Tokens
- Order Publishing & Currency Switch
- Design System Core
- Swap Database
- Swap Engine Core
- Trade Gate Verification
- Market Chart View
- Order Book Scanning
- Swap Service & Hashlocks
- OTC & Comms Scanning
- Swap Tab UI & Math
- OTC Offer Board
- OTC Messaging & Take Flow
- Merchant Chat Database
- ETH HTLC Operations & Tests
- Ethereum Wallet
- Dialogs & Input Fields
- Minima HTLC Blocks & Secrets
- ETH HTLC Contract Queries
- Order Republishing & Peg Apply
- README Architecture Concepts
- Order Book Tests
- Token Mismatch Safety
- Boot & Heartbeat Receivers
- Lifecycle Teardown
- Market Memory Persistence
- Background Swap Worker
- Image Compression Utils
- Timelock Safety Tests
- Ethereum Network Config
- General Utilities
- Avatar Generation
- Preferences Metadata
- QR Code Utility
- Launcher Icon (hdpi)
- Launcher Icon (mdpi)
- Launcher Icon (xxhdpi)
- Launcher Icon (xxxhdpi)
- Gradle Wrapper Script
- Launcher Icon (xhdpi)
- Tombstone Test
- CLAUDE.md Instructions

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 204 edges
2. `SwapEngine` - 101 edges
3. `MinimaHtlc` - 62 edges
4. `Order` - 57 edges
5. `NodeApi` - 54 edges
6. `SwapService` - 48 edges
7. `SwapDb` - 47 edges
8. `Design` - 40 edges
9. `OtcController` - 35 edges
10. `EthHtlc` - 32 edges

## Surprising Connections (you probably didn't know these)
- `MainActivity` --references--> `EthNet`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/atomix/MainActivity.java → app/src/main/java/com/eurobuddha/atomix/eth/EthNet.java
- `MainActivity` --references--> `EthRpc`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/atomix/MainActivity.java → app/src/main/java/com/eurobuddha/atomix/eth/EthRpc.java
- `MainActivity` --references--> `EthWallet`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/atomix/MainActivity.java → app/src/main/java/com/eurobuddha/atomix/eth/EthWallet.java
- `MainActivity` --references--> `MinimaHtlc`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/atomix/MainActivity.java → app/src/main/java/com/eurobuddha/atomix/swap/MinimaHtlc.java
- `MainActivity` --references--> `Order`  [EXTRACTED]
  app/src/main/java/com/eurobuddha/atomix/MainActivity.java → app/src/main/java/com/eurobuddha/atomix/swap/Order.java

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Two-Leg HTLC Atomic Swap Flow** — readme_htlc, readme_bridge_htlc_contract, readme_bridge_vault, readme_minimacore_node, readme_web3j_wallet [EXTRACTED 1.00]
- **Dual-Currency Trading System (TradingContext-driven)** — readme_tradingcontext, readme_currency_pill, readme_minimaswap_market, readme_usdtswap_market, readme_sentinel_boards [EXTRACTED 1.00]

## Communities (51 total, 10 thin omitted)

### Community 0 - "Market Data & Identity Setup"
Cohesion: 0.05
Nodes (15): Cb, JSONArray, MarketCollector, JSONArray, JSONObject, KeysCb, MinimaHtlc, PostCb (+7 more)

### Community 1 - "Price Oracle & Pegging"
Cohesion: 0.05
Nodes (23): JSONArray, JSONObject, Order, SharedPreferences, PriceOracle, active(), SharedPreferences, labelFor() (+15 more)

### Community 2 - "Main Activity Swap Lifecycle"
Cohesion: 0.07
Nodes (5): Uri, View, MainActivity, ImageView, ScrollView

### Community 3 - "Comms Identity & Backup Crypto"
Cohesion: 0.05
Nodes (21): BackupCrypto, SecureRandom, LazySodium, Hex, Hkdf, LazySodium, Override, LocalEcCryptoProvider (+13 more)

### Community 4 - "Order Book Model"
Cohesion: 0.09
Nodes (12): Best, JSONArray, JSONObject, Level, Order, Pair, Order, Test (+4 more)

### Community 5 - "App Startup & OTC Controller"
Cohesion: 0.09
Nodes (15): JSONObject, SecureRandom, OtcController, SendResult, Ui, Deal, Helper, Context (+7 more)

### Community 6 - "Ethereum RPC Client"
Cohesion: 0.09
Nodes (10): EthRpc, JSONArray, JSONObject, EthSend, Credentials, EthTx, Credentials, NonceState (+2 more)

### Community 8 - "Order Publishing & Currency Switch"
Cohesion: 0.11
Nodes (6): Order, Intent, Order, Override, SwapService, Builder

### Community 9 - "Design System Core"
Cohesion: 0.11
Nodes (10): Design, Context, TextView, View, Mode, DAYLIGHT, ONYX, GradientDrawable (+2 more)

### Community 10 - "Swap Database"
Cohesion: 0.14
Nodes (4): Event, Swap, SwapDb, Notifier

### Community 12 - "Trade Gate Verification"
Cohesion: 0.16
Nodes (4): GateTest, Before, JSONObject, Test

### Community 13 - "Market Chart View"
Cohesion: 0.11
Nodes (12): Context, Override, MarketChartView, Helper, Context, Cursor, Override, SQLiteDatabase (+4 more)

### Community 14 - "Order Book Scanning"
Cohesion: 0.13
Nodes (11): JSONObject, LazySodium, SwapOrderBook, Cb, Context, Handler, JSONObject, NodeApi (+3 more)

### Community 15 - "Swap Service & Hashlocks"
Cohesion: 0.09
Nodes (14): ActivityResultLauncher, Handler, LazySodium, Handler, JSONObject, LazySodium, SharedPreferences, Opened (+6 more)

### Community 16 - "OTC & Comms Scanning"
Cohesion: 0.13
Nodes (6): CommsScanner, JSONArray, JSONObject, Listener, MetaStore, Router

### Community 18 - "OTC Offer Board"
Cohesion: 0.15
Nodes (6): JSONObject, LazySodium, OtcBook, JSONObject, OtcOffer, CommsIdentity

### Community 19 - "OTC Messaging & Take Flow"
Cohesion: 0.16
Nodes (6): OtcMessage, SwapTake, CommsTransport, JSONObject, SendCb, CryptoProvider

### Community 20 - "Merchant Chat Database"
Cohesion: 0.12
Nodes (6): Context, Override, SQLiteDatabase, MerchDb, Order, MerchMessage

### Community 22 - "Ethereum Wallet"
Cohesion: 0.12
Nodes (7): EthWallet, Credentials, Handler, ConfirmCb, Handler, JSONArray, JSONObject

### Community 24 - "Minima HTLC Blocks & Secrets"
Cohesion: 0.18
Nodes (4): BlockCb, SecretCb, Order, StartCb

### Community 25 - "ETH HTLC Contract Queries"
Cohesion: 0.20
Nodes (5): Contract, EthHtlc, Credentials, JSONObject, InspectCb

### Community 27 - "README Architecture Concepts"
Cohesion: 0.14
Nodes (18): Cross-chain Atomic Swaps, AtomiX, Shared Bridge HTLC Contract (Minima leg), Bridge Vault (Ethereum ERC20 leg), Currency-Agnostic Settlement, Header Currency Pill (currency switcher), Hash-Time-Locked Contract (HTLC), MEXC MINIMA/USDT Price Feed (+10 more)

### Community 28 - "Order Book Tests"
Cohesion: 0.27
Nodes (3): Order, Test, OrderBookTest

### Community 29 - "Token Mismatch Safety"
Cohesion: 0.24
Nodes (4): Before, JSONObject, Test, MismatchPoisonTest

### Community 30 - "Boot & Heartbeat Receivers"
Cohesion: 0.25
Nodes (9): BootReceiver, Context, Intent, Override, HeartbeatReceiver, Context, Intent, Override (+1 more)

### Community 31 - "Lifecycle Teardown"
Cohesion: 0.19
Nodes (4): Override, SimpleWatcher, Editable, TextWatcher

### Community 32 - "Market Memory Persistence"
Cohesion: 0.35
Nodes (5): JSONObject, SharedPreferences, SharedPreferences, Test, MarketMemoryTest

### Community 33 - "Background Swap Worker"
Cohesion: 0.29
Nodes (7): Context, Override, SwapWorker, NonNull, Result, Worker, WorkerParameters

### Community 34 - "Image Compression Utils"
Cohesion: 0.42
Nodes (4): Images, Bitmap, Context, Uri

### Community 36 - "Ethereum Network Config"
Cohesion: 0.28
Nodes (6): EthNet, MAINNET, from(), token(), tokenByAddress(), Token

### Community 38 - "Avatar Generation"
Cohesion: 0.39
Nodes (3): Avatars, Context, FrameLayout

### Community 39 - "Preferences Metadata"
Cohesion: 0.43
Nodes (3): Override, SharedPreferences, PrefsMeta

### Community 41 - "Launcher Icon (hdpi)"
Cohesion: 0.60
Nodes (5): AtomiX Launcher Icon Foreground (hdpi), Atom Orbit Motif (atomic swap symbolism), AtomiX Brand Identity, Minima 'M' Logo Mark (red/blue/grey bars), USDT Tether Symbol (green circular badges)

### Community 42 - "Launcher Icon (mdpi)"
Cohesion: 0.70
Nodes (5): Atomic Orbit Ring Motif (atomic-swap symbolism), AtomiX App Branding (Minima-to-USDT atomic swap identity), AtomiX Launcher Icon Foreground (mdpi), Stylized Minima 'M' Logomark (red-orange and navy halves), Green Coin Badges with Tether-style Currency Symbols

### Community 43 - "Launcher Icon (xxhdpi)"
Cohesion: 0.50
Nodes (5): Atom Orbit Motif (gray elliptical electron orbits), Atomic Swap (Minima <-> USDT cross-chain trading), AtomiX Launcher Icon Foreground (xxhdpi), Minima 'M' Logo Mark (red/blue/gray angled bars), Tether (USDT) Coin Symbols (three green coins)

### Community 44 - "Launcher Icon (xxxhdpi)"
Cohesion: 0.60
Nodes (5): Atom Orbit Motif (electron-orbit ellipses), AtomiX Atomic-Swap Branding (Minima-USDT swaps), AtomiX Launcher Icon Foreground (xxxhdpi), Minima 'M' Logo Mark (red/blue/gray angled bars), Tether (USDT) Coin Symbols (three green coins)

### Community 45 - "Gradle Wrapper Script"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 46 - "Launcher Icon (xhdpi)"
Cohesion: 0.67
Nodes (4): Atomic Swap Motif (atom orbital ring), AtomiX Launcher Icon Foreground (xhdpi), Minima 'M' Logo (red/blue/gray bars), Tether (USDT) Coin Symbols

### Community 48 - "CLAUDE.md Instructions"
Cohesion: 0.50
Nodes (4): Disagree openly; never disobey quietly, Reuse before you reinvent, RULE 0 — Follow the user's explicit instructions (blocking), User Instructions (Authoritative)

## Knowledge Gaps
- **12 isolated node(s):** `ONYX`, `DAYLIGHT`, `MINIMA`, `MXUSDT`, `MAINNET` (+7 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **10 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MainActivity` connect `Main Activity Swap Lifecycle` to `Market Data & Identity Setup`, `Price Oracle & Pegging`, `Order Book Model`, `App Startup & OTC Controller`, `Ethereum RPC Client`, `UI Design Tokens`, `Order Publishing & Currency Switch`, `Design System Core`, `Swap Database`, `Swap Engine Core`, `Order Book Scanning`, `Swap Service & Hashlocks`, `OTC & Comms Scanning`, `Swap Tab UI & Math`, `OTC Offer Board`, `OTC Messaging & Take Flow`, `Ethereum Wallet`, `Dialogs & Input Fields`, `Minima HTLC Blocks & Secrets`, `Order Republishing & Peg Apply`, `Lifecycle Teardown`, `Market Memory Persistence`, `Ethereum Network Config`?**
  _High betweenness centrality (0.314) - this node is a cross-community bridge._
- **Why does `Order` connect `Order Book Model` to `Price Oracle & Pegging`, `Main Activity Swap Lifecycle`, `Comms Identity & Backup Crypto`, `Order Publishing & Currency Switch`, `Swap Engine Core`, `Order Book Scanning`, `Swap Service & Hashlocks`, `Swap Tab UI & Math`, `Dialogs & Input Fields`, `Minima HTLC Blocks & Secrets`, `Order Republishing & Peg Apply`, `Order Book Tests`?**
  _High betweenness centrality (0.170) - this node is a cross-community bridge._
- **Why does `SwapEngine` connect `Swap Engine Core` to `Market Data & Identity Setup`, `Main Activity Swap Lifecycle`, `Ethereum Network Config`, `App Startup & OTC Controller`, `Ethereum RPC Client`, `Order Book Model`, `Order Publishing & Currency Switch`, `Swap Database`, `Trade Gate Verification`, `Order Book Scanning`, `Swap Service & Hashlocks`, `Ethereum Wallet`, `Minima HTLC Blocks & Secrets`, `ETH HTLC Contract Queries`, `Order Republishing & Peg Apply`, `Token Mismatch Safety`, `Lifecycle Teardown`?**
  _High betweenness centrality (0.159) - this node is a cross-community bridge._
- **What connects `ONYX`, `DAYLIGHT`, `MINIMA` to the rest of the system?**
  _12 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Market Data & Identity Setup` be split into smaller, more focused modules?**
  _Cohesion score 0.051138294257560314 - nodes in this community are weakly interconnected._
- **Should `Price Oracle & Pegging` be split into smaller, more focused modules?**
  _Cohesion score 0.05098934550989345 - nodes in this community are weakly interconnected._
- **Should `Main Activity Swap Lifecycle` be split into smaller, more focused modules?**
  _Cohesion score 0.0676056338028169 - nodes in this community are weakly interconnected._