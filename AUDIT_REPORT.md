# AtomiX incident and safety review — 2026-09-09

## Incident finding

The unfinished trade used hashlock `0x0E4D2079FB86E573AEF8F7DB6D751CA74F3C7BCAB2DEE12D6A1D0BAE07D04811`. The Z Fold initiated a 10.1 USDT → 10 MxUSD trade; the S23 was its responder.

The S23 retained lock submission `0x00004FB40585D5D6DAC724C22DE81CB5DDE65D3EEFA888F8B5B0ACDE8F3FBD97`, dated 12:03:45 BST. Its one input was `0x9D65D42A7F51B7649635189DE0EBD50B9018072F90DF900FD9EF7E23339C40F0`.

A different, 20-input transaction had already consumed that exact funding input: [confirmed consolidation](https://explorer.minima.global/transactions/0x000054730F250F258B089A22245CA13AF4E1D91C8725945AD1FFF99CD40ACC6C). The explorer reports block 2305490 and timestamp 12:03:14 BST. The S23 independently returned `found:true`, block `2305490`, tip `2305740`, confirmations `250` for this transaction. Its outputs are ordinary wallet outputs, not the proposed 10 MxUSD HTLC. The retained lock submission itself returned `found:false` from the S23.

This explains the apparent contradiction: each phone could see a durable trade record, but that did not establish a valid Minima lock. The attempted lock reused a spent funding input. Seeing the peer or the trade again could not repair that transaction.

The Z Fold subsequently showed its 10.1 USDT refunded. The S23 inspector independently read the Ethereum contract as refunded, with no preimage revealed and no matching unspent Minima HTLC. This attempt did not complete as a swap. No new test trade, manual transfer, node replacement, uninstall or data reset was performed during this investigation.

## Code Review

### Summary

The review followed Astra.md and the proven-code gate. It focused on the complete swap lifecycle across wallet funding, transaction construction, node transport, persistence, discovery, validation, settlement and diagnostics, with a repository-wide structural/search pass and regression suites. The confirmed incident was a stale funding-input conflict, compounded by crash-prone wallet enumeration and UI text that treated records or submissions as stronger evidence than they were.

### Findings and fixes

- **CRITICAL — stale input accepted as a lock submission.** `MinimaHtlc.myFreeCoins` now excludes mempool inputs. Explicit locks, claims and refunds build proofs once and require successful proof, amount, script and signature validation before posting. `txnpost auto:true` was removed after `txnbasics` because core appends proofs and would duplicate them. Reused PandaPools `TxPost.checkFailure` and checked the local core `txnpost`, `txncheck`, `txnbasics`, `txnutils` and miner implementations.
- **MAJOR — wallet replies could kill the Android app.** Fresh token-specific balance summaries gate wallet enumeration before issuing the dangerous IPC request. Limits are 53 coloured-token rows / 200 native rows, based on Astra's measured response budget. Oversized, malformed or failed summaries refuse the read. Diagnostics use compact state. The observed crash log included a 270,864-byte Binder parcel.
- **MAJOR — claims/refunds finalized on submission acknowledgement.** New Minima settlement events remain pending and survive restarts. A node receipt with at least two confirmations is required before writing a permanent completion/refund event. Missing receipts remain retryable. Reused the existing Ethereum submission/confirmation separation and PandaPools `ActivityLog.confirmationDepth`.
- **MAJOR — silent counter-leg refusal and funding races.** Refusals now explain fragmentation and release reservations. Funding reservations are shared across Activity/service engines. Manual consolidation shares the signing gate and excludes active funding operations, with a bounded single-run action and block cooldown. Reused PandaPools wallet consolidation and the desktop wallet's existing nudge.
- **MAJOR — Activity teardown dropped callbacks; uncertain writes could overlap retries.** Node callbacks finish through teardown. PandaPools' persisted interrupted-write protocol now pauses subsequent signing/posting when an outcome is unknown, including process restarts. A matching complete late reply can resolve its own marker. Wallet exposes the existing restart-and-reconcile acknowledgement pattern. Pending approvals and oversized-result stubs do not clear that marker.
- **MAJOR — ERROR suppressed expired-fund recovery.** Failed trades remain eligible for recovery of their own expired Minima locks. Recovery discovery rotates fairly through one due hash per poll, with a shared retry window even for empty/failed scans.
- **MAJOR — failed lookups looked like absent or refunded legs.** Diagnostics distinguish unavailable reads, absent unspent coins and recorded submissions; show full hash/transaction identifiers and actual scan bounds; use the correct participant role; and do not promise claims without a secret or refunds without a visible lock. Ethereum contract reads reject missing/truncated tuples and invalid boolean encodings.
- **MINOR — unnecessary wallet enumeration for ladder backing.** The ladder uses the existing balance summary's sendable amount, preserving the prefix clamp and failure behavior.
- **MAJOR — shared JavaScript signing hold expired by time alone.** The MDS/desktop gate now retains an uncertain hold until its callback returns. Its existing per-context scope remains documented; Android's Activity/IPC persistence mechanism is platform-specific.

### Reuse and mirrors

Primary reused sources were native `MinimaHtlc.java`, `SwapEngine.java`, `SwapDb.java`, `SignGate.java`; PandaPools `WalletTools.java`, `TxPost.java`, `ActivityLog.java`, `NodeApi.java` and their relevant tests; PandaDEX funding selection; and desktop wallet consolidation. MDS uses its existing `htlc.js`, `mdsw.js`, `settle.js`, `responder.js`, `maker.js` and `inspect.js` building blocks. Desktop engine changes are byte-identical donor copies; only its separate wrapper is adapted.

Native changes are versioned individually from 0.1.46 through 0.1.57. MDS mirrors are 0.1.25/0.1.26; desktop source mirrors are 0.16.40/0.16.41. Existing unrelated graphify changes and the user's untracked Astra/security-review documents were preserved.

### Validation

- Android: 228 tests in each of debug/release; release APK and release vital lint build checks pass.
- MDS: baseline reproduced 496 passing / 6 failing unit checks. The six failures used mismatched secret/hash fixtures without a valid hash-command response. Fixtures now exercise actual SHA-256 verification; production verification remains intact. Final checks: 548 unit, 30 Rhino, 77 UI, 8 browser-chain, all passing.
- Desktop: all 40 shared engine files match the MDS donor; six integration checks pass using the existing local Docker fixture with trade execution stubbed.
- Device investigation used read-only node commands and in-place app updates. The final live smoke check covers app startup, wallet/status reads and the existing refunded trade, not a newly funded end-to-end swap.

### Plan revisions and limits

The live stale-input incident and acknowledgement/confirmation bugs were prioritized ahead of cosmetic cleanup. Astra's windowed shared-address scan redesign remains a separate deferred change, as its own final section specifies; this release bounds expensive recovery scans and wallet reads without changing the protocol or shared-address discovery semantics. Count limits remain conservative and may include locked coins. History beyond the node's retained tree still requires available MegaMMR/archive evidence; an empty scan is not proof of a spend. Receipt tracking preserves uncertainty if a callback/receipt is unavailable.

The catalogue's current `CLAUDE.md` supersedes Astra's old binary-copy workflow: publish the APK as a release asset on the app repository, use its existing `scripts/publish-app.py`, validate the catalogue and push. No binary is committed to the manifest-only repository. Desktop here is a tested source mirror, not a newly built/signed desktop installer.

### Verdict

**Approve the reviewed fixes.** The stale-input cause is supported by independent node/explorer evidence and the affected paths have targeted regressions. This is not a claim that every possible defect in the entire application, its dependencies or the underlying blockchains has been eliminated.
