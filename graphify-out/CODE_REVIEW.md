# AtomiX — Full Code Review & Security Audit

## Summary

AtomiX is a well-engineered Android atomic-swap app bridging Minima and Ethereum (USDT). The codebase shows deep domain expertise in HTLC mechanics, fund-safety design, and self-healing retry patterns. The test suite is unusually thorough for a mobile app, covering HTLC construction, timelock math, amount grain, identity guards, and interop vectors.

**However, there are 8 CRITICAL and 23 MAJOR issues** spanning concurrency races, command injection, queue jams, Android lifecycle violations, and missing error handling that can strand funds, crash the app, or compromise cryptographic security. Many of these are in the Android platform boundary (Services, Receivers, Workers, Scanners) rather than the core swap math — exactly the surface that keeps swaps alive when the user is not actively using the app.

---

## 🔴 CRITICAL

### 1. DB persistence race before ETH broadcast — funds can be permanently stranded
**File:** `swap/SwapEngine.java:368-398` (`startErc20ToMinima`), `820-828` (`lockEthCounterLeg`)  
**Problem:** In `startErc20ToMinima`, the secret and swap row are inserted inside `ui.post(() -> { ... })` (main-thread Handler queue), but `eth.newContract(...)` continues **synchronously on the `io` thread immediately after posting**. If the process dies or the main thread is killed before the posted Runnable executes, the DB write never happens while the ETH contract already exists on-chain. `runEthChecks` only iterates `db.allSwaps()`; there is no secondary `contractsAsSender` discovery scan. A missing DB row means the engine never monitors, claims, or refunds that leg.  
**Impact:** USDT/mxUSDT can be permanently stranded in the vault with no in-app recovery path.  
**Fix:** Perform `db.insertSecret`, `db.insertMyHtlc`, and `db.upsertSwap` **synchronously on the `io` thread** before calling `eth.newContract`. Only post UI callbacks to the main thread.

### 2. Un-checked `io.execute()` calls crash after engine shutdown
**File:** `swap/SwapEngine.java:470` (`inspect`), `561` (`poll`), `927` (`checkBuyNow`)  
**Problem:** `io.execute(Runnable)` is invoked without checking `io.isShutdown()` and without catching `RejectedExecutionException`. If `shutdown()` has been called (Activity destroyed, Service stopped), the executor rejects the task and throws on the calling thread. In `inspect` and `checkBuyNow` this propagates out of a `NodeApi` or `MinimaHtlc` callback, crashing the background command-delivery thread.  
**Impact:** Unhandled `RejectedExecutionException` crashes the process.  
**Fix:** Guard every `io.execute(...)` with `if (io.isShutdown()) return;` or wrap in `try/catch RejectedExecutionException`. `confirmMyLock` (line 425) already does this correctly — replicate that pattern everywhere.

### 3. CommsScanner exception in `process()` freezes the scanner forever
**File:** `comms/CommsScanner.java:95-102`  
**Problem:** `fetch()`’s `onResult` calls `process(coins)` directly. If `router.handle()` throws, the exception is swallowed by the Android `Looper` and never propagates back. `running` remains `true`, so no future scan ever starts.  
**Impact:** Permanent denial of service for message/payment scanning until app restart.  
**Fix:** Wrap `process(coins)` in a `try/catch` inside `onResult`, and call `finish(chainBlock, false)` if it throws.

### 4. CommsScanner `meta.setMeta()` I/O exception freezes the scanner
**File:** `comms/CommsScanner.java:116-119`  
**Problem:** `finish()` calls `meta.setMeta()` before resetting `running = false`. If `setMeta()` throws (disk full, DB locked), `running` stays `true` and the scanner is dead.  
**Impact:** Scanner locks up permanently.  
**Fix:** Wrap the `meta` calls in `try/catch`; always set `running = false` and `lastScanEnd` in a `finally` block.

### 5. SignGate unprotected `r.run()` permanently jams the signing queue
**File:** `comms/SignGate.java:81-91`  
**Problem:** `next()` invokes `r.run()` without any `try/finally`. If the `Op` throws an unchecked exception (e.g. `NullPointerException` inside a callback), `busy` stays `true` forever and the queue never advances.  
**Impact:** Complete loss of signing capability for the process lifetime; no transactions can be sent.  
**Fix:** Wrap `r.run()` in `try { ... } finally { busy = false; next(); }` (or equivalent cleanup) so the queue always advances.

### 6. SignGate watchdog + late callback can start concurrent signing operations
**File:** `comms/SignGate.java:71-91`  
**Problem:** If the watchdog fires because a callback was lost, `next()` starts operation B. If A’s callback then arrives late and calls `gate.free()`, it removes B’s watchdog and calls `next()` again, starting C while B is still in flight.  
**Impact:** Violates the fundamental serial-signing invariant. Two Winternitz one-time signatures can run concurrently, risking **key reuse** — a known attack vector on one-time signature schemes.  
**Fix:** Add a monotonic generation / serial number to each queued operation. `Release.free()` should only call `next()` if its serial matches the current one.

### 7. MainActivity thread-unsafe cross-thread read of ETH balances
**File:** `MainActivity.java` (field declarations + lines 3514-3518, 3545, 162-163, 771-773)  
**Problem:** `ethWeiRaw` and `usdtRawBal` are plain (non-volatile) fields. They are written inside `ui.post()` (UI thread) in `fetchEthBalances()`, but read directly from the `io` thread inside `sendDialog()` and send-review validation. Without a happens-before relationship, the `io` thread may read a stale cached value (e.g., `BigInteger.ZERO`).  
**Impact:** The “Max” ETH send calculation returns 0 or an outdated balance; the send-review guard may approve an amount against stale reserves.  
**Fix:** Declare both fields `volatile`, or read them under the same monitor, or post the read back to the UI thread before passing to `io.execute()`.

### 8. IdentityWatch static coordination fields are not volatile
**File:** `IdentityWatch.java:39-40`  
**Problem:** `lastCheckMs`, `minimaInFlight`, `ethInFlight`, and `alarmRaised` are plain static fields accessed from multiple threads (`check()` runs from UI/Service threads; callbacks run from `NodeApi`/wallet background threads).  
**Impact:** Without `volatile` or `synchronized`, a thread may never see updated values, causing duplicate in-flight probes, missed mismatch detection, or duplicate notifications.  
**Fix:** Declare all four fields `volatile` (or convert to `AtomicBoolean` / `AtomicLong`).

---

## 🟠 MAJOR

### 9. EthRpc endpoints race-condition crash
**File:** `eth/EthRpc.java:40,44-51,59-72`  
**Problem:** `endpoints` is a plain `ArrayList`. `setUrl()` mutates it (`clear()` + `addAll()`) under `synchronized`, but `call()` iterates without synchronization. If `setUrl()` runs concurrently with `call()`, the iterator throws `ConcurrentModificationException`.  
**Impact:** Crashes the background thread and can stall or abort a settlement.  
**Fix:** Rebuild a new `List` inside `setUrl()` and atomically assign it to a `volatile` field (copy-on-write).

### 10. EthRpc `callOnce` crashes on non-HTTP URLs
**File:** `eth/EthRpc.java:80`  
**Problem:** `setUrl(String)` accepts any string. If the URL is not HTTP/HTTPS, `new URL(url).openConnection()` returns a non-`HttpURLConnection`. The cast `(HttpURLConnection)` throws `ClassCastException`, which is **not** an `IOException`, so it is **not** caught by the fallback loop in `call()`.  
**Impact:** Exception propagates and crashes the caller.  
**Fix:** Validate the scheme in `setUrl()` (require `http://` or `https://`) and wrap `callOnce()` in a broader catch (`Exception e`).

### 11. EthRpc accepts plain-HTTP URLs — MITM exposure
**File:** `eth/EthRpc.java:42-51`  
**Problem:** The configured primary URL is not forced to HTTPS. An attacker on the same network can MITM an HTTP RPC endpoint, censor transactions, feed fake balances, or delay responses to manipulate swap timing.  
**Impact:** Spoofed balances and timelock reads can manipulate swap decisions.  
**Fix:** Reject non-HTTPS URLs in `setUrl()` (or at minimum log a strong warning and require explicit user override).

### 12. EthRpc `hexToBig` propagates `NumberFormatException`
**File:** `eth/EthRpc.java:183-189`  
**Problem:** `hexToBig` strips the `0x` prefix but does not catch `NumberFormatException` from `new BigInteger(hex, 16)`. If a fallback node returns malformed hex, the unchecked exception crashes the background thread. Callers like `getBalance()` only declare `IOException`, so the runtime exception is unhandled.  
**Impact:** Background thread crash on malformed RPC responses.  
**Fix:** Wrap `new BigInteger` in try/catch and throw `IOException("invalid hex from RPC: " + hex)` so fallback logic can try the next endpoint.

### 13. EthTx per-address lock held across three blocking network calls
**File:** `eth/EthTx.java:46-81`  
**Problem:** The `synchronized (st)` block spans `getTransactionCount`, `eth_gasPrice`, `baseFeePerGasOrZero`, and `sendRawTransaction` — up to ~90s of network I/O (3 × 30s read timeout). Every other send for the same address is blocked for the entire duration.  
**Impact:** Serializes all ETH sends for the same address. In a market sweep with multiple back-to-back locks, later locks can miss their Minima expiry windows.  
**Fix:** Fetch `pending`, `gasPrice`, and `baseFee` **before** acquiring the lock; enter the synchronized block only to sign, broadcast, and bump the counter.

### 14. EthSend `checkSend` validates with stale gas price
**File:** `eth/EthSend.java:60-77` (validation) vs. `eth/EthTx.java:58-70` (actual broadcast)  
**Problem:** `checkSend()` takes a caller-provided `gasPriceWei` and checks the user has enough ETH for gas. `EthTx.send()` ignores that value, fetches a fresh `eth_gasPrice`, adds +20%, and floors at `2 × baseFee`. The two prices can diverge between UI validation and broadcast.  
**Impact:** A user can pass the check, hit “Send”, and then the transaction fails with insufficient funds or hangs because the real gas price is higher.  
**Fix:** Compute the final gas price in one place and have both `checkSend` and `EthTx.send` use the same value atomically.

### 15. EthWallet `importKey` crashes on invalid input
**File:** `eth/EthWallet.java:88-91`  
**Problem:** `importKey(String hexPriv)` does not validate the input before passing it to `Credentials.create()`. If the string is not a valid 32-byte hex key, web3j throws an unchecked exception that propagates uncaught.  
**Impact:** Crashes the app on malformed user-facing input.  
**Fix:** Validate length (66 chars with `0x` or 64 without) and hex character set before calling `Credentials.create()`.

### 16. EthWallet unchecked cast in `erc20BalanceRaw`
**File:** `eth/EthWallet.java:118`  
**Problem:** `return (BigInteger) out.get(0).getValue();` is an unchecked cast. If the contract returns malformed data, the cast throws `ClassCastException`.  
**Impact:** Crashes the balance-read background task.  
**Fix:** Guard the cast: check `out.get(0) instanceof NumericType` and return `BigInteger.ZERO` otherwise.

### 17. CommsTransport command injection into Minima node commands
**File:** `comms/CommsTransport.java:50-51, 64`  
**Problem:** `sendPayment()` and `postBlob()` build node command strings by directly concatenating externally supplied parameters (`vendorAddress`, `amount`, `tokenid`, `address`) without validation or escaping.  
**Impact:** An attacker who controls any of these values can inject extra Minima command tokens, altering transaction semantics.  
**Fix:** Validate that addresses match strict hex format and contain no whitespace; pass parameters through a structured builder that quotes/escapes them.

### 18. CommsScanner command injection via `targetAddress`
**File:** `comms/CommsScanner.java:85, 93`  
**Problem:** The `coinnotify` and `coins` commands concatenate `targetAddress` without sanitization.  
**Impact:** A malicious or malformed address can inject extra Minima command tokens.  
**Fix:** Validate that `targetAddress` is a strict hex string with no spaces before concatenation.

### 19. NodeApi `cmd()` silently drops callbacks after release
**File:** `comms/NodeApi.java:131-132`  
**Problem:** If `mReleased` is true, `cmd()` returns immediately without invoking `cb.onError(...)`. Callers that set a `running` flag before calling `cmd()` will hang forever waiting for a callback that never arrives.  
**Impact:** Scanner or engine threads can hang indefinitely.  
**Fix:** Before returning, invoke `cb.onError("released")` when `cb != null`.

### 20. NodeApi response callbacks fire after `onDestroy()`
**File:** `comms/NodeApi.java:155-178`  
**Problem:** The response `Runnable` checks `done[0]` and `dead()`, but never checks `mReleased`. After `onDestroy()`, in-flight SDK responses are still posted to the main handler and delivered to the callback.  
**Impact:** Memory leaks (callback holds Activity references) and potential crashes if the callback touches dead UI.  
**Fix:** Add `if (mReleased) return;` at the top of the response `Runnable`.

### 21. SwapWorker calls `startForegroundService()` from background — prohibited on Android 12+
**File:** `SwapWorker.java:31-33`  
**Problem:** `ContextCompat.startForegroundService()` from a `Worker.doWork()` background context is **prohibited on Android 12+** (targetSdk 35). The thrown `ForegroundServiceStartNotAllowedException` is caught and **ignored**, and the worker returns `Result.success()`.  
**Impact:** The OS-killed-service fallback silently fails on modern Android. WorkManager will **not retry** because the result is success. The only remaining relaunch path is the 15-minute alarm.  
**Fix:** Convert to a `ListenableWorker` and use `setForegroundAsync()`, or schedule an explicit alarm/broadcast with an FGS-start exemption.

### 22. AndroidManifest still declares `dataSync` — caps FGS at ~6h/day on Android 14+
**File:** `AndroidManifest.xml:73`  
**Problem:** `android:foregroundServiceType="specialUse|dataSync"` — Android applies the restrictions of **every** declared type. `dataSync` is capped at ~6 hours/day on Android 14+. The manifest comment says this exact cap killed the overnight order-keeper, but the flag is still present.  
**Impact:** The foreground service can be killed by the OS after ~6 hours, leaving in-flight swaps unwatched overnight.  
**Fix:** Remove `dataSync`; use `specialUse` **only** (the property tag already justifies it).

### 23. MerchDb `recordPayment()` read-then-write TOCTOU race
**File:** `comms/MerchDb.java:105-123`  
**Problem:** `recordPayment()` reads the order row into memory, computes a status, then issues a separate `update()`. Between read and write, another thread could modify the row.  
**Impact:** The payment update could clobber a later status or apply an outdated transition. In a financial app this is a fund-safety race.  
**Fix:** Wrap the read-compute-write in `db.beginTransaction()` / `setTransactionSuccessful()` / `endTransaction()`, or perform the status guard atomically in the UPDATE SQL.

### 24. MerchDb missing `onDowngrade()` — crashes on APK rollback
**File:** `comms/MerchDb.java:51-55`  
**Problem:** `onUpgrade()` is implemented but `onDowngrade()` is not overridden. Default behavior throws `SQLiteException`.  
**Impact:** Installing an older APK over a newer one crashes the app on startup until app data is cleared.  
**Fix:** Override `onDowngrade()` to either drop/recreate tables or call `db.setVersion(o)`.

### 25. Images.java bitmap memory leaks
**File:** `comms/Images.java:21-40`  
**Problem:** `bmp` and every intermediate `scaled` bitmap are **never recycled**. Each call keeps the full native bitmap memory alive until GC.  
**Impact:** Processing several images causes monotonic native heap growth and **OOM crashes**.  
**Fix:** Call `bmp.recycle()` at method exit. In the loop, recycle the previous `scaled` before creating a new one.

### 26. SwapEngine `cpLockSince` updated outside `CP_LOCKING` monitor
**File:** `swap/SwapEngine.java:1135`  
**Problem:** `cpLockSince.put(hash, nowUnix())` is placed **outside** the `synchronized(CP_LOCKING)` block. The code’s own comment says all `cpInFlight/CP_LOCKING` mutations go through the shared monitor.  
**Impact:** `runEthChecks` reads `cpLockSince` without the lock. A concurrent `lockMinimaCounterLeg` can add a hash to `cpInFlight` but not yet to `cpLockSince`, so the watchdog misfires for one cycle.  
**Fix:** Move `cpLockSince.put(hash, nowUnix())` inside the `synchronized(CP_LOCKING)` block at lines 1127–1134.

### 27. MinimaHtlc command-string injection via unescaped on-chain values
**File:** `swap/MinimaHtlc.java:219` (`lock` send command), `241-249` (`lockFromCoins` txnstate/txninput commands)  
**Problem:** Node commands are built with raw string concatenation. `receiverPubkey`, `hashlock`, `ownerEthKey`, and `coinid` are read directly from on-chain coins or peer orders without validation/escaping of spaces, quotes, or other delimiters.  
**Impact:** If a malicious peer crafts an HTLC coin with a state value containing spaces or control characters, the resulting `cmd` string is malformed — a latent command-injection vector into the Minima node IPC.  
**Fix:** Validate that all interpolated values are strict hex (only `[0-9A-Fa-f]`) before building commands, and/or escape/quote values for the node command grammar.

### 28. SwapEngine claim-gate race between `ethRetryDue` and `markEthAttempt`
**File:** `swap/SwapEngine.java:713-714`  
**Problem:** The check and the mark are two separate, non-atomic operations on a `ConcurrentHashMap`. With two engines (foreground Activity + background Service) running concurrently, both can pass `ethRetryDue` before either calls `markEthAttempt`.  
**Impact:** Both engines call `minima.claim` on the same coin. `SignGate` serializes signing, but the node still attempts to build two different claim transactions, permanently **burning two one-time signing leaves** instead of one. Over many stuck claims this exhausts the 64-key pool faster than intended.  
**Fix:** Use `compute` or `putIfAbsent` on `ethAttempt` so the check-and-set is atomic.

### 29. MainActivity potential background-thread engine mutation via CommsScanner
**File:** `MainActivity.java:826-830, 844-860, 869-875`  
**Problem:** `otcScanner` and `takeScanner` are `CommsScanner` instances. If their `scan()` delivers `route()` / `routeTakeRequest()` on a background thread, then `addIncoming()` mutates `prefs`, calls `engine.addIncomingHashlock()`, `engine.checkBuyNow()`, and updates UI state off the main thread.  
**Impact:** Race between the background discovery scanner and the UI polling loop can corrupt the swap engine’s internal state or crash with `CalledFromWrongThreadException`.  
**Fix:** Wrap the bodies of `routeTakeRequest()` and the `OtcController.Ui` callbacks in `ui.post(() -> { ... })`.

### 30. OtcController `lastProposeNote` is not volatile
**File:** `swap/OtcController.java:44`  
**Problem:** `lastProposeNote` is a plain `long` read/written inside `noteProposeRateLimited()`. If `CommsScanner` calls `route()` on a background thread, this is a data race.  
**Impact:** Rate-limiting may fail under concurrent delivery, spamming duplicate notifications.  
**Fix:** Make it `volatile`, or guard both methods with `synchronized`.

### 31. SignGate not thread-safe if called off the main thread
**File:** `comms/SignGate.java:37-38, 63-66`  
**Problem:** `ArrayDeque` and the `busy` flag have no synchronization. The comment says “everything runs on the main thread,” but `submit()` is a public static API accessible from any thread.  
**Impact:** Calling `submit()` from a background thread corrupts the deque or causes missed `next()` calls.  
**Fix:** Add `synchronized` blocks around `QUEUE` / `busy` accesses, or document the thread requirement with an `assert` / `Looper` check.

---

## 🟡 MINOR

### 32. SwapEngine `parseInt` silently returns 0 on overflow
**File:** `swap/SwapEngine.java:1496`  
**Problem:** `parseInt` swallows `NumberFormatException` (including integer overflow) and returns `0`. A maliciously crafted coin with `state[3]` > `Integer.MAX_VALUE` would be parsed as timelock `0`.  
**Fix:** Use `Long.parseLong` for block/timelock fields, or detect overflow and treat it as an error rather than 0.

### 33. SwapEngine static `inflight` entries can persist across engine instances
**File:** `swap/SwapEngine.java:110`  
**Problem:** `inflight` is `static final` but entries are only removed in success/finally paths. If the hosting Service is destroyed without those callbacks firing, the static set retains stale markers.  
**Fix:** In `shutdown()`, iterate and clear any entries belonging to this engine instance.

### 34. SwapTake potential NPE on null callback
**File:** `swap/SwapTake.java:33`  
**Problem:** `cb.onFailed(...)` is called without a null-check on `cb`.  
**Fix:** Add `if (cb == null) return;` at the top of `send`.

### 35. PublishGate clock skew can spuriously lock the publish slot
**File:** `swap/PublishGate.java:31`  
**Problem:** `if (since[slot] != 0 && now - since[slot] < TIMEOUT_MS)` uses `System.currentTimeMillis()`. If the device clock jumps backwards (NTP sync), `now - since[slot]` becomes negative, satisfying `< TIMEOUT_MS`, so `tryAcquire` returns `false` for up to 5 minutes.  
**Fix:** Use `Math.max(0, now - since[slot])` or switch to `SystemClock.elapsedRealtime()` for interval math.

### 36. CommsTransport mutates caller’s JSONObject
**File:** `comms/CommsTransport.java:62-63`  
**Problem:** `postBlob` mutates `extraState` in place (`state.put("99", ...)`). If the caller reuses the same `JSONObject`, it unexpectedly carries `state.99`.  
**Fix:** Always clone `extraState` before mutating: `JSONObject state = extraState != null ? new JSONObject(extraState) : new JSONObject();`.

### 37. CommsIdentity secret key fields are publicly accessible
**File:** `comms/CommsIdentity.java:22-23`  
**Problem:** `boxSk` and `signSk` are declared `public final`. Any code holding a `CommsIdentity` reference can read the raw secret key bytes.  
**Fix:** Make the fields `private` and expose controlled accessors (e.g., `sign(byte[])` / `sealOpen(...)`) so the keys never leave the object.

### 38. BackupCrypto version field is unauthenticated and unchecked
**File:** `comms/BackupCrypto.java:31, 36`  
**Problem:** The `"v"` metadata sits outside the AES-GCM ciphertext and is neither authenticated nor validated during decryption.  
**Fix:** Include the version in the GCM Additional Authenticated Data (AAD), or verify `o.getInt("v") == 1` before decrypting.

### 39. NodeApi `onDestroy()` does not guard `mApi.onDestroy()`
**File:** `comms/NodeApi.java:187`  
**Problem:** If `mApi.onDestroy()` throws, the exception propagates to the caller and aborts any remaining cleanup.  
**Fix:** Wrap in `try/catch`.

### 40. Hex.java odd-length hex strings silently truncated
**File:** `comms/Hex.java:20-27`  
**Problem:** `int n = h.length() / 2;` drops the final nibble if the input has odd length after stripping the `0x` prefix.  
**Fix:** After stripping the prefix, check `h.length() % 2 == 0` and throw `IllegalArgumentException` if not.

### 41. LocalEcCryptoProvider returns plaintext on authentication failure
**File:** `comms/LocalEcCryptoProvider.java:69`  
**Problem:** When the sender public ID is malformed or the signature length is wrong, `open()` returns `new Opened(false, from, body)` instead of `null`. The decrypted `body` is still exposed.  
**Fix:** Return `null` for any malformed or unverified blob.

### 42. QrUtil unbounded bitmap size can OOM
**File:** `comms/QrUtil.java:16`  
**Problem:** `qr()` accepts any `sizePx` without a ceiling. A caller passing a very large value will allocate a giant `int[]` and bitmap, causing `OutOfMemoryError`.  
**Fix:** Enforce a reasonable maximum (e.g., `4096`) and throw `IllegalArgumentException`.

### 43. MainActivity `pegTick` Runnable leaks across `onPause`
**File:** `MainActivity.java:1253-1262`  
**Problem:** `pegTick[0]` reposts itself every 2s while `dlgOpen[0]` is true. `onPause` / `onDestroy` do not remove this specific callback.  
**Fix:** Store the `pegTick` Runnable in a field and remove it explicitly in `onPause()` / `onDestroy()`.

### 44. BootReceiver and HeartbeatReceiver swallow exceptions silently
**File:** `BootReceiver.java:24`, `HeartbeatReceiver.java:54`  
**Problem:** `catch (Exception ignored) {}` and logging only `e.getClass().getSimpleName()` — no message.  
**Fix:** Log `e.toString()` or `e.getMessage()` to `SwapLog`.

### 45. MerchDb queryOrders only guards some column indices
**File:** `comms/MerchDb.java:125-156`  
**Problem:** `queryOrders()` only guards `shopid`/`shopname` with `iSid >= 0` / `iSnm >= 0`. All other columns assume they exist.  
**Fix:** Apply the `>= 0` guard to **all** column indices, or use `c.getColumnIndexOrThrow()`.

### 46. Images.java catches `Throwable`
**File:** `comms/Images.java:37`  
**Problem:** `catch (Throwable t)` — catches `Error` subclasses including `OutOfMemoryError`. Swallowing `OutOfMemoryError` can leave the JVM in an undefined state.  
**Fix:** Change to `catch (Exception e)`.

### 47. MarketCollector uses deprecated `BigDecimal` constant
**File:** `swap/MarketCollector.java:101`  
**Problem:** `req.divide(size, 12, BigDecimal.ROUND_HALF_UP)` uses the deprecated int constant.  
**Fix:** Replace with `RoundingMode.HALF_UP`.

### 48. build.gradle `minifyEnabled false`
**File:** `app/build.gradle:41`  
**Problem:** Release builds are not obfuscated. A financial app handling HTLC secrets ships fully readable bytecode.  
**Fix:** Enable `minifyEnabled true` and add `-keep` rules for serialization/reflection classes.

---

## 🔵 NIT

### 49. Util.shorten() and MainActivity.shortAddr() truncate identifiers
**File:** `Util.java:25-29`, `MainActivity.java` (multiple call sites)  
**Problem:** Truncating cryptographic identifiers (addresses, keys, hashes) into `0x1234…ABCD` form makes them un-copyable and unverifiable in a block explorer or wallet.  
**Fix:** Display identifiers in full, or provide a tap-to-copy action that places the complete string on the clipboard. Remove all truncation helpers.

### 50. EthRpc `readAll` uses string charset name
**File:** `eth/EthRpc.java:198`  
**Problem:** `bos.toString("UTF-8")` works but is less type-safe than `StandardCharsets.UTF_8`.  
**Fix:** Replace with `bos.toString(StandardCharsets.UTF_8)`.

### 51. Hkdf.java counter overflow for large `len`
**File:** `comms/Hkdf.java:27-30`  
**Problem:** The counter `ctr` is cast to `(byte) ctr`. For `len > 255 × 32 = 8160` bytes the counter wraps to zero and produces repeated blocks.  
**Fix:** Validate that `len <= 8160` and throw if exceeded.

---

## What the Code Does Well

- **Fund-safety design:** The engine is built around trustless HTLC mechanics with careful timelock math, deterministic contract IDs, and idempotent retry patterns. The README and inline comments demonstrate deep understanding of the attack surface.
- **Identity Watch:** The `IdentityWatch` system detects node reseeds and halts new liabilities — a real-world safeguard against the “orphaned key” fund leak.
- **Self-healing retries:** `ETH_RETRY_SECS` gating, record-before-broadcast patterns, and the `broadcast → confirm` split (F1) show mature fault-tolerance design.
- **Test coverage:** 24 test files cover HTLC construction, timelock safety, amount grain, identity guards, order book logic, price model guards, interop vectors, and encoding — unusually thorough for an Android app.
- **Parameterized SQL:** `SwapDb.java` and `OtcDb.java` use `?` bind-args throughout — no SQL injection vulnerabilities in the database layer.
- **Secret verification:** `MinimaHtlc.verifyPreimage()` validates SHA-256 before storing secrets, preventing poisoned notify-coin attacks.

---

## Verdict

🔁 **Request changes**

The core swap logic (HTLC construction, timelock math, amount grain, identity guards) is **solid and well-tested**. The primary risks are in the **Android platform boundary** and **concurrency control**:

1. **CRITICAL:** Fix the `SignGate` queue jam and concurrent-signing bugs (#5, #6) immediately — Winternitz key reuse is a catastrophic security failure.
2. **CRITICAL:** Fix the `CommsScanner` freeze bugs (#3, #4) — permanent DoS of the messaging layer.
3. **CRITICAL:** Fix the DB-write-before-broadcast race in `SwapEngine` (#1) and the thread-unsafe balance reads in `MainActivity` (#7) — both can strand or lose funds.
4. **MAJOR:** Fix the `EthRpc` race, MITM, and crash paths (#9-#12), the `SwapWorker` Android 12+ FGS prohibition (#21), and the manifest `dataSync` cap (#22) before any release targeting modern Android.
5. **MAJOR:** Fix the `MerchDb` TOCTOU race (#23) and bitmap memory leaks (#25) to prevent OOM and data corruption.

Most fixes are surgical (adding `volatile`, `try/finally`, atomic operations, or thread guards) and should not perturb the core swap logic. The test suite gives confidence that regressions can be caught.
