package com.eurobuddha.atomix;

import android.app.Activity;
import android.content.Context;

import com.eurobuddha.account.Account;
import com.eurobuddha.account.AccountVault;
import com.eurobuddha.atomix.swap.MinimaHtlc;
import com.eurobuddha.atomix.swap.OtcDb;
import com.eurobuddha.atomix.swap.SwapDb;

import java.util.Arrays;
import java.util.List;

/**
 * AtomiX's settings for the shared Trading Account (vendored from Dexicon, com.eurobuddha.account). The same 24 words
 * open the same account in Dexicon, AtomiX and PandaPools; one of them holds it at a time.
 */
public final class AtomixAccount {
    private AtomixAccount() {}

    /** HTLC outputs carry the owner port (locks stay relevant to this node); reads of the HTLC watch for locks to us. */
    public static Account.Config config() {
        return new Account.Config("AtomiX", Arrays.asList(MinimaHtlc.HTLC_ADDRESSES), null, null, AtomixAccount::oldEthNotice);
    }

    /** Swaps or OTC deals an earlier AtomiX made with the NODE's keys (Handover's own complete check, no time window):
     *  until they finish, AtomiX keeps the node-key path and does not offer the account — "finish before upgrading"
     *  (apks/atomix CLAUDE.md: drain all in-flight swaps before ANY wallet/mode migration). Never once an account exists. */
    public static boolean legacyPositions(Context c) {
        long now = System.currentTimeMillis();
        if (now - cachedAt < 5_000L) return cached;
        boolean v = openPositions(c) != null;
        cached = v; cachedAt = now;
        return v;
    }
    private static volatile boolean cached;
    private static volatile long cachedAt;

    /** What is still open with the node's keys, or null (none / an account already exists here). */
    public static String openPositions(Context c) {
        if (new AccountVault(c).everCreated()) return null;
        return openAny(c);
    }

    /** No NEW liability (start, publish, OTC execute, responder lock) until the Trading Account exists: node-key swaps
     *  from before it only drain, so "finish before upgrading" can never renew itself (review 10 Nov, M1). Settlement
     *  never consults this gate. */
    public static void gateNewSwaps(Context c) {
        Context app = c.getApplicationContext();
        com.eurobuddha.atomix.swap.SwapEngine.setHostGate(() -> !com.eurobuddha.account.Account.exists(app));
    }

    /** Only the old node-derived ETH wallet's derivation may reach the node without a loaded account, and only before an
     *  account was ever created on this phone (review 10 Nov, H1). Pure, for the routing test. */
    public static boolean oldEthToNode(boolean accountEverCreated, String command) {
        return !accountEverCreated && command != null && command.trim().matches("seedrandom\\s+modifier:ethbridge");
    }

    /** Swaps and OTC deals still in flight (incl. one starting), whichever keys made them; null when none. What a LOST
     *  account's restore must not "Start new" over: a published offer is not listed here — with the words gone nothing
     *  can withdraw it, so it must not shut the user out (its takers refund at their timelock). */
    public static String openSwaps(Context c) {
        try {
            List<String> open = Handover.inFlight(new SwapDb(c.getApplicationContext()), new OtcDb(c.getApplicationContext()).allDeals(),
                    com.eurobuddha.atomix.swap.SwapEngine.initiatorStarting());
            return open.isEmpty() ? null : open.size() + (open.size() == 1 ? " swap or deal" : " swaps or deals") + " in flight";
        } catch (RuntimeException unreadable) { return "swap history unreadable"; }
    }

    /** Everything open, whichever keys made it: swaps / deals (incl. one starting), a published market or OTC offer.
     *  A lost account's restore passes this (only the SAME words may settle it). Every blocker is named. */
    public static String openAny(Context c) {
        try {
            StringBuilder b = new StringBuilder();
            List<String> open = Handover.inFlight(new SwapDb(c.getApplicationContext()), new OtcDb(c.getApplicationContext()).allDeals(),
                    com.eurobuddha.atomix.swap.SwapEngine.initiatorStarting());
            if (!open.isEmpty()) b.append(open.size()).append(open.size() == 1 ? " swap or deal" : " swaps or deals").append(" in flight");
            // a market or OTC offer still published under the node identity: takers would lock against a key AtomiX no
            // longer answers for (they refund at the timelock, but never strand them) — withdraw it first
            android.content.SharedPreferences p = c.getSharedPreferences("atomix", Context.MODE_PRIVATE);
            // withdrawn only counts once its tombstone is CONFIRMED on the wire (P_WITHDRAWN is set before it goes out)
            boolean withdrawn = p.getBoolean(com.eurobuddha.atomix.swap.PriceOracle.P_WITHDRAWN, false)
                    && p.getBoolean(com.eurobuddha.atomix.swap.PriceOracle.P_TOMBSTONED, false);
            boolean market = !withdrawn
                    && (com.eurobuddha.atomix.swap.Order.fromConfigJson(p.getString("order_config", "")).hasLiquidity() || p.getBoolean("auto_publish", false));
            boolean otc = p.getBoolean("otc_enable", false) || p.getBoolean("otc_auto", false);
            boolean pending = p.getBoolean(Handover.P_TOMB_PENDING, false) || p.getBoolean(Handover.P_OTC_PENDING, false);
            if (market || otc || pending) b.append(b.length() > 0 ? ", " : "").append("a published market or OTC offer (withdraw it first)");
            return b.length() == 0 ? null : b.toString();
        } catch (RuntimeException unreadable) { return "swap history unreadable"; }
    }

    /** The comms (order / OTC) identity's input: with a Trading Account, {@code seedrandom modifier:dexicon-doors-v1}
     *  answered by the account — the SAME identity Dexicon's AtomiX has for these words (Dexicon DoorHost.deriveIdentities);
     *  the node's phrase never enters the app (security review 30 Oct). Without an account (node-key swaps still settling
     *  on the bears) the old vault seed, as before. */
    public static String identityCommand(Context c) {
        return new AccountVault(c).everCreated() ? "seedrandom modifier:" + DOORS_IDENTITY : "vault action:seed";
    }
    public static final String DOORS_IDENTITY = "dexicon-doors-v1";

    /** The identity input from either reply: "0x" + 64 hex (seedrandom, as Dexicon's SeedSource) or the vault seed. */
    public static String identityInput(org.json.JSONObject reply) {
        org.json.JSONObject r = reply == null ? null : reply.optJSONObject("response");
        if (r == null) return "";
        if (r.has("seedrandom")) {
            String hex = r.optString("seedrandom", "").trim();
            if (hex.startsWith("0x") || hex.startsWith("0X")) hex = hex.substring(2);
            return hex.matches("[0-9a-fA-F]{64}") ? "0x" + hex : "";
        }
        return r.optString("seed", r.optString("phrase", ""));
    }

    /** Swapped here before with the node's keys: the old Ethereum wallet came from the node's seed, not this account. */
    private static String oldEthNotice(Activity a) {
        boolean tradedBefore = !a.getSharedPreferences("atomix", Context.MODE_PRIVATE).getString("swap_pk", "").isEmpty();
        return !tradedBefore ? null : "You have swapped with AtomiX here before. The Trading Account has its OWN Ethereum "
                + "wallet: after this step AtomiX shows the new one, and any ETH / USDT left in the old AtomiX Ethereum wallet "
                + "(derived from your node's seed) is then reachable only from the AtomiX MiniDapp on your node. To move it "
                + "here first, answer \"Yes — move it first\": AtomiX stays open on its Wallet screen until you restart it.";
    }

    /** A withdraw address: Mx only, its checksum verified (a pasted token / coin id as 0x would burn the funds). */
    public static boolean validMx(String a) {
        if (a == null || !a.startsWith("Mx")) return false;
        try { return org.minima.objects.Address.convertMinimaAddress(a).getLength() == 32; } catch (RuntimeException bad) { return false; }
    }
}
