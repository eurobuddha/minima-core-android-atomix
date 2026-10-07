# User instructions — AUTHORITATIVE. These override default behavior and must be followed exactly.

## RULE 0 (highest priority) — Follow the user's explicit instructions. They are BLOCKING, not suggestions.

When the user gives an explicit instruction, do exactly that, first, before anything else. The user's instruction
takes priority over your own plan, your preferred approach, and your judgment about a "better" way. You must never
substitute your own idea for what the user told you to do.

1. **Reuse before you reinvent.** If the user points you at existing code, a file, a sibling app, or an existing
   solution, read and use it FIRST — before you propose, design, diagnose, or build anything new. Reinventing is
   permitted only after you have read the named source and can state specifically why it does not fit.
2. **"Look at X", "use Y", "do Z first", "don't do W" are hard, blocking instructions.** Act on them immediately, in
   the same turn. They are never something to get to later.
3. **Never silently substitute your own approach.** The moment you notice you are about to build, diagnose, or design
   something new when the user has named an existing source or given a direct instruction, stop and follow the
   instruction.
4. **Disagree openly; never disobey quietly.** If you genuinely believe an instruction is wrong or will not work, say
   so plainly and ask — in the same turn, before acting. Quietly doing something else instead is not acceptable.

When your instinct conflicts with the user's instruction, follow the instruction. Ignoring it wastes the user's time,
tokens, and money, and is the most serious mistake you can make.

## Versioning guardrail — every code change ships with a version bump

Real funds, real chain. NEVER change code without bumping the version
(versionCode + versionName in app/build.gradle), so every committed state is distinct, reversible and trackable. One
logical change = one version = one commit = one push, in order. Enforced by a
pre-commit hook (.githooks/pre-commit, install once: sh .githooks/install.sh)
that blocks a code change with no version bump. Do NOT bypass with --no-verify.
Docs/config-only commits need no bump.

## Block-as-key-uses (minima-core 1.1.2.31+) — operational rules, recorded 2026-10-05

Upstream nodes can now run `-blockaskeyuses` (upstream Android 1.7 forces it; our MinimaBlock app
`com.eurobuddha.minimablock` does too). Under it, new keys are 128×4 Winternitz trees and key uses
track the chain-tip block number. AtomiX **code needs no change** — it signs via
`txnsign publickey:auto|<state key>` and never reads `uses` — but two operational rules are hard:

1. **Drain all in-flight swaps before ANY wallet/mode migration.** The HTLC script takes its keys
   from coin state (`PREVSTATE(0)` owner / `PREVSTATE(4)` counterparty). A party that loses the
   ability to sign with the key already baked into a live lock's state loses that leg: a claimer
   who can't sign before the timelock forfeits what they already paid; an owner who can't sign
   can't refund. A seed restored under the other key mode produces DIFFERENT keys, so the old
   state keys are simply gone.
2. **The persisted swap identity does not survive a mode switch.** `swap_pk`/`swap_addr`
   (MinimaHtlc, MDS KV `swap_identity`) is a legacy-shape wallet key published to counterparties.
   After moving to a block-mode wallet, IdentityWatch halts trading until a new identity is
   picked, and counterparties must be given the new maker key.

The HTLC address itself embeds no keys and is identical under both modes:
`MxG080CRJB1D4NHGRYGNF7Q52FK7023UM3FUUPVD1W1WCQZSA8MDQ25982N842G`
(hex `0x0CDCD61692F186EB0BBCFA289F438043F586FF7B3F6864193358E29166E8454A`).
