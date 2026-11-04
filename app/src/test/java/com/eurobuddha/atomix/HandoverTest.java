package com.eurobuddha.atomix;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.SharedPreferences;

import android.os.Handler;

import com.eurobuddha.atomix.eth.EthNet;
import com.eurobuddha.atomix.eth.EthRpc;
import com.eurobuddha.atomix.eth.EthWallet;
import com.eurobuddha.atomix.swap.MinimaHtlc;
import com.eurobuddha.atomix.swap.Order;
import com.eurobuddha.atomix.swap.OtcDb;
import com.eurobuddha.atomix.swap.SwapEngine;
import com.eurobuddha.comms.NodeApi;
import com.eurobuddha.atomix.swap.PriceOracle;
import com.eurobuddha.atomix.swap.SwapDb;

import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dexicon handover (0.1.86): Dexicon may only uninstall AtomiX once nothing in either currency is in flight and the
 *  market is withdrawn — and once handed over, AtomiX starts nothing new (Handover.active gates the engine). */
public class HandoverTest {

    private SharedPreferences prefs(final Map<String, Object> m) {
        SharedPreferences p = mock(SharedPreferences.class);
        SharedPreferences.Editor ed = mock(SharedPreferences.Editor.class);
        when(p.getString(anyString(), nullable(String.class))).thenAnswer(i -> {
            Object o = m.get(i.<String>getArgument(0)); return o instanceof String ? o : i.getArgument(1); });
        when(p.getBoolean(anyString(), anyBoolean())).thenAnswer(i -> {
            Object o = m.get(i.<String>getArgument(0)); return o instanceof Boolean ? o : i.getArgument(1); });
        when(p.edit()).thenReturn(ed);
        when(ed.putBoolean(anyString(), anyBoolean())).thenAnswer(i -> { m.put(i.getArgument(0), i.getArgument(1)); return ed; });
        when(ed.putString(anyString(), nullable(String.class))).thenAnswer(i -> { m.put(i.getArgument(0), i.getArgument(1)); return ed; });
        when(ed.putLong(anyString(), anyLong())).thenAnswer(i -> { m.put(i.getArgument(0), i.getArgument(1)); return ed; });
        when(ed.remove(anyString())).thenAnswer(i -> { m.remove(i.<String>getArgument(0)); return ed; });
        when(ed.commit()).thenReturn(true);
        return p;
    }

    private static SwapDb.Swap swap(String sell, String status, String hash) {
        SwapDb.Swap s = new SwapDb.Swap();
        s.hash = hash; s.sellToken = sell; s.buyToken = "USDT"; s.status = status;
        return s;
    }

    private static OtcDb.Deal deal(String status) {
        OtcDb.Deal d = new OtcDb.Deal();
        d.ref = "ab".repeat(16); d.currency = "minima"; d.status = status;
        return d;
    }

    @After public void reset() { Handover.takeBack(prefs(new HashMap<>())); }

    private static SwapDb db(SwapDb.Swap... swaps) {
        SwapDb db = mock(SwapDb.class);
        when(db.allSwaps()).thenReturn(Arrays.asList(swaps));
        return db;
    }

    @Test public void anyNonTerminalSwapInEitherCurrencyBlocks() {
        String h1 = "0x" + "11".repeat(32), h2 = "0x" + "22".repeat(32);
        SwapDb db = db(
                swap("mxUSDT", SwapDb.ST_LOCKED, h1),
                swap("MINIMA", SwapDb.ST_STARTED, h2),
                swap("MINIMA", SwapDb.ST_COMPLETE, "x"),
                swap("mxUSDT", SwapDb.ST_REFUNDED, "y"),
                swap("mxUSDT", SwapDb.ST_ERROR, "z"));
        when(db.haveCollectExpired("z")).thenReturn(true);
        List<String> r = Handover.inFlight(db, Collections.emptyList(), false);
        assertEquals(2, r.size());
        assertTrue(r.get(0).contains("USDT⇄MxUSD") && r.get(0).contains(h1 + " "));   // full hash, MxUSD naming
        assertTrue(r.get(1).contains("USDT⇄MINIMA") && r.get(1).contains(h2));
    }

    /** CRITICAL (0.1.88): ERROR is the trade outcome, not my funds. Initiator MxUSD→USDT, claim missed, maker refunds
     *  the ETH leg → ERROR while my MxUSD lock still waits ~2h for its own refund from the recorded coin. */
    @Test public void errorRowBlocksUntilMyRefundIsConfirmed() {
        String h = "0x" + "33".repeat(32);
        SwapDb.Swap s = swap("mxUSDT", SwapDb.ST_ERROR, h);
        s.myLegIsMinima = true;
        SwapDb db = db(s);
        List<String> r = Handover.inFlight(db, Collections.emptyList(), false);
        assertEquals(1, r.size());
        assertTrue(r.get(0).contains(h) && r.get(0).contains("not been confirmed refunded"));
        when(db.haveCollectExpired(h)).thenReturn(true);   // EV_EXPIRED: the refund confirmed on chain
        assertTrue(Handover.inFlight(db, Collections.emptyList(), false).isEmpty());
        s.myLegIsMinima = false;                            // an ETH first leg needs the same chain evidence
        assertTrue(Handover.inFlight(db, Collections.emptyList(), false).isEmpty());
        when(db.haveCollectExpired(h)).thenReturn(false);
        assertEquals(1, Handover.inFlight(db, Collections.emptyList(), false).size());
    }

    /** Finding 4 (not auto-cleared — no chain read can prove a lost lock absent): the screen says exactly why. */
    @Test public void unacknowledgedStartedRowSaysWhyItBlocks() {
        String h = "0x" + "44".repeat(32);
        SwapDb.Swap s = swap("MINIMA", SwapDb.ST_STARTED, h);
        s.myLegIsMinima = true;
        SwapDb db = db(s);
        String r = Handover.inFlight(db, Collections.emptyList(), false).get(0);
        assertTrue(r, r.contains(h) && r.contains("never confirmed as sent"));
        when(db.hasEvent(h, SwapDb.EV_STARTED)).thenReturn(true);
        assertTrue(Handover.inFlight(db, Collections.emptyList(), false).get(0).endsWith(h));
    }

    @Test public void anInitiatorStartInProgressBlocks() {
        List<String> r = Handover.inFlight(db(), Collections.emptyList(), true);
        assertEquals(1, r.size());
        assertTrue(r.get(0).contains("starting"));
    }

    @Test public void openOtcDealBlocksFinishedDoesNot() {
        assertEquals(1, Handover.inFlight(db(), Arrays.asList(
                deal(OtcDb.ST_AGREED), deal(OtcDb.ST_COMPLETE), deal(OtcDb.ST_EXPIRED), deal(OtcDb.ST_REJECTED)), false).size());
        assertTrue(Handover.inFlight(db(), new ArrayList<>(), false).isEmpty());
    }

    /** MAJOR (0.1.88): the entry check passed, then the handover quiesced AtomiX during the node round-trips — the
     *  secret must not be written (and no lock posted), and the engine must report "starting" until it answers. */
    @Test public void handoverDuringInitiatorStartWritesNoSecret() {
        TradingContext saved = TradingContext.active();
        TradingContext.setActive(TradingContext.MXUSDT, null);
        MinimaHtlc minima = mock(MinimaHtlc.class);
        when(minima.ready()).thenReturn(true);
        when(minima.activeToken()).thenReturn(TradingContext.MXUSDT.tokenId);
        EthWallet wallet = mock(EthWallet.class);
        when(wallet.ready()).thenReturn(true);
        SwapDb swaps = mock(SwapDb.class);
        SwapEngine e = new SwapEngine(mock(NodeApi.class), minima, swaps, wallet, mock(Handler.class), mock(SwapEngine.Notifier.class));
        e.setMyMinimaPk("0x1234");
        e.setNetwork(mock(EthRpc.class), EthNet.MAINNET);
        final boolean[] startingSeen = {false};
        doAnswer(i -> { ((MinimaHtlc.SecretCb) i.getArgument(0)).ok("public fixture", "hash fixture"); return null; })
                .when(minima).generateSecret(any());
        doAnswer(i -> {
            startingSeen[0] = SwapEngine.initiatorStarting();
            Handover.withdraw(prefs(new HashMap<>()));          // Dexicon's handover lands mid-start
            ((MinimaHtlc.BlockCb) i.getArgument(0)).ok(100);
            return null;
        }).when(minima).currentBlock(any());
        Order maker = new Order();
        maker.minimaPublicKey = "0x5678"; maker.ethAddress = "0x" + "12".repeat(20);
        final String[] err = {null};
        try {
            e.startMinimaToErc20(maker, "2", "USDT", "6", true, new SwapEngine.StartCb() {
                public void ok(String h) { fail("a lock was posted after the handover"); }
                public void err(String m) { err[0] = m; }
            });
            assertTrue("busy while the start is between check and write", startingSeen[0]);
            assertTrue(err[0], err[0] != null && err[0].contains("Handed over"));
            verify(swaps, never()).prepareInitiator(any(), any(), any(), any());
            verify(minima, never()).lock(any(), any(), any(), any(), any(), any(), anyInt(), any(), any());
            assertFalse("busy cleared once the start answered", SwapEngine.initiatorStarting());
        } finally { e.shutdown(); TradingContext.setActive(saved, null); }
    }

    @Test public void withdrawQuiescesMarketAndFlagsPendingPosts() {
        Map<String, Object> m = new HashMap<>();
        m.put("auto_publish", true); m.put("otc_enable", true); m.put("otc_auto", true);
        SharedPreferences p = prefs(m);
        assertEquals(2, Handover.marketLive(p).size());
        assertFalse(Handover.active());

        Handover.withdraw(p);
        assertTrue("new swaps stop at once", Handover.active());
        assertEquals(false, m.get("auto_publish"));
        assertEquals(false, m.get("otc_enable"));
        assertEquals(false, m.get("otc_auto"));
        assertEquals(true, m.get(PriceOracle.P_WITHDRAWN));
        // Not done until SwapService has the tombstone + OTC withdrawal on the wire.
        assertEquals(2, Handover.marketLive(p).size());
        m.put(Handover.P_TOMB_PENDING, false); m.put(Handover.P_OTC_PENDING, false);
        assertTrue(Handover.marketLive(p).isEmpty());
        assertFalse(Handover.done(p));
    }

    @Test public void noLiveMarketNeedsNoPost() {
        Map<String, Object> m = new HashMap<>();
        SharedPreferences p = prefs(m);
        Handover.withdraw(p);
        assertTrue(Handover.marketLive(p).isEmpty());
    }

    @Test public void doneSurvivesReloadAndTakeBackClears() {
        Map<String, Object> m = new HashMap<>();
        SharedPreferences p = prefs(m);
        Handover.setDone(p, 123L);
        Handover.takeBack(prefs(new HashMap<>()));   // a different store — clear the static only
        Handover.load(p);
        assertTrue(Handover.active());
        assertTrue(Handover.done(p));
        Handover.takeBack(p);
        assertFalse(Handover.active());
        Handover.load(p);
        assertFalse(Handover.active());
    }

    // ---- 0.1.90 ----

    private static final long HOUR = 60 * 60 * 1000L;

    private static SwapDb.Swap minimaLeg(String status, String hash, long ageMs) {
        SwapDb.Swap s = swap("mxUSDT", status, hash);
        s.myLegIsMinima = true; s.role = "RESPONDER"; s.direction = "ERC20_TO_MINIMA";
        s.created = System.currentTimeMillis() - ageMs;
        return s;
    }

    private static final long TL = 1_000_000;

    private static SwapDb.Event refundTried(long block) {
        SwapDb.Event e = new SwapDb.Event();
        e.event = SwapDb.EV_REFUND_TRIED; e.note = "minima block " + block;
        return e;
    }

    /** 0.1.91 MAJOR: the recovery-record escape needs chain evidence, not device-clock age: a refund pass recorded
     *  with the chain past myTimelock + margin, and — when the lock coin was recorded — that coin read as spent. */
    @Test public void stuckNeedsARecordedRefundPassPastTheTimelockNotJustAge() {
        String h = "0x" + "66".repeat(32), young = "0x" + "77".repeat(32), bare = "0x" + "88".repeat(32), early = "0x" + "89".repeat(32);
        SwapDb.Swap err = minimaLeg(SwapDb.ST_ERROR, h, 7 * HOUR);
        SwapDb.Swap yng = minimaLeg(SwapDb.ST_ERROR, young, HOUR);
        SwapDb.Swap noEvidence = minimaLeg(SwapDb.ST_LOCKED, bare, 7 * 24 * HOUR);   // a week old, never refund-tried
        SwapDb.Swap tooEarly = minimaLeg(SwapDb.ST_STARTED, early, 7 * HOUR);
        for (SwapDb.Swap x : new SwapDb.Swap[]{err, yng, noEvidence, tooEarly}) x.myTimelock = TL;
        SwapDb db = db(err, yng, noEvidence, tooEarly);
        when(db.getEvents(h)).thenReturn(Collections.singletonList(refundTried(TL + Handover.REFUND_MARGIN_BLOCKS)));
        when(db.getEvents(young)).thenReturn(Collections.singletonList(refundTried(TL + 500)));
        when(db.getEvents(early)).thenReturn(Collections.singletonList(refundTried(TL + Handover.REFUND_MARGIN_BLOCKS - 1)));
        long now = System.currentTimeMillis();
        List<String> r = Handover.inFlight(db, Collections.emptyList(), false, now);
        assertEquals(4, r.size());
        assertTrue(r.get(0), r.get(0).contains(h) && r.get(0).contains("recovery record"));
        assertEquals(1, Handover.needRecovery(db, now).size());
        assertEquals(h, Handover.needRecovery(db, now).get(0).hash);
        assertFalse("device-clock age alone never qualifies", Handover.stuck(noEvidence, db, now));
        assertFalse("the refund pass saw the chain too early", Handover.stuck(tooEarly, db, now));
        assertFalse("too young, evidence or not", Handover.stuck(yng, db, now));

        for (String x : new String[]{h, young, bare, early}) when(db.hasEvent(x, SwapDb.EV_RECOVERY_SAVED)).thenReturn(true);
        r = Handover.inFlight(db, Collections.emptyList(), false, now);
        assertEquals("rows without the evidence still block, saved record or not", 3, r.size());
        assertFalse(r.toString().contains(h));
        assertTrue(Handover.needRecovery(db, now).isEmpty());

        for (String st : new String[]{SwapDb.ST_STARTED, SwapDb.ST_LOCKED, SwapDb.ST_CLAIMING}) {
            err.status = st;
            assertTrue(st, Handover.stuck(err, db, now));
        }
        err.myLegIsMinima = false;
        assertFalse("an Ethereum leg has its own evidence (EV_NEVER_LOCKED)", Handover.stuck(err, db, now));
    }

    /** An unspent recorded lock is still refundable: it keeps blocking (and the engine keeps refunding it) until a
     *  read of the coin finds it spent. */
    @Test public void aRecordedLockCoinMustReadSpent() throws Exception {
        String h = "0x" + "6a".repeat(32);
        SwapDb.Swap s = minimaLeg(SwapDb.ST_ERROR, h, 7 * HOUR);
        s.myTimelock = TL;
        SwapDb db = db(s);
        when(db.getEvents(h)).thenReturn(Collections.singletonList(refundTried(TL + 1000)));
        when(db.rememberedLockCoin(h)).thenReturn(new org.json.JSONObject().put("coinid", "0x" + "ab".repeat(32)));
        long now = System.currentTimeMillis();
        assertFalse(Handover.stuck(s, db, now));
        assertTrue(Handover.needRecovery(db, now).isEmpty());
        when(db.hasEvent(h, SwapDb.EV_LOCK_SPENT)).thenReturn(true);
        assertTrue(Handover.stuck(s, db, now));
    }

    @Test public void recoveryRecordCarriesEveryIdentifierInFull() throws Exception {
        String h = "0x" + "99".repeat(32), coin = "0x" + "ab".repeat(32), secret = "0x" + "cd".repeat(32);
        SwapDb.Swap s = minimaLeg(SwapDb.ST_ERROR, h, 7 * HOUR);
        s.myTimelock = 1_234_567; s.sellAmount = "10"; s.buyAmount = "9.9";
        SwapDb db = db(s);
        when(db.getSecret(h)).thenReturn(secret);
        String owner = "0x" + "0f".repeat(32), claimer = "0x" + "e1".repeat(32);
        when(db.rememberedLockCoin(h)).thenReturn(new org.json.JSONObject().put("coinid", coin).put("amount", "10").put("tokenid", "0x00")
                .put("state", new org.json.JSONArray()
                        .put(new org.json.JSONObject().put("port", 0).put("data", owner))
                        .put(new org.json.JSONObject().put("port", 4).put("data", claimer))));
        String rec = Handover.recoveryRecord(s, db);
        org.json.JSONObject j = new org.json.JSONObject(rec);
        assertEquals(h, j.getString("hash"));
        assertEquals(secret, j.getString("secret"));
        assertEquals(com.eurobuddha.atomix.eth.EthHtlc.contractId(h), j.getString("contractId"));
        assertEquals(coin, j.getString("lockCoinId"));
        assertEquals(1_234_567, j.getLong("myTimelock"));
        assertEquals("USDT⇄MxUSD", j.getString("market"));
        assertEquals("RESPONDER", j.getString("role"));
        assertEquals("10", j.getString("sellAmount"));
        assertEquals("9.9", j.getString("buyAmount"));
        assertEquals("legacy-labelled rows read MxUSD", "MxUSD", j.getString("sellToken"));
        assertFalse(rec, rec.contains("mxUSDT"));
        assertEquals(owner, j.getString("lockOwnerKey"));
        assertEquals(claimer, j.getString("counterpartyKey"));
        assertEquals(Handover.SECRET_WARNING, j.getString("warning"));
        assertFalse(rec, rec.contains("…"));

        byte[] b = rec.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String sha = Handover.sha256(b);
        assertNull(Handover.verifyRecoveryFile(b, b.length, sha, h));
        assertNotNull("truncated", Handover.verifyRecoveryFile(java.util.Arrays.copyOf(b, b.length - 1), b.length, sha, h));
        byte[] flipped = b.clone(); flipped[5] ^= 1;
        assertNotNull("altered", Handover.verifyRecoveryFile(flipped, b.length, sha, h));
        assertNotNull("another swap's record", Handover.verifyRecoveryFile(b, b.length, sha, "0x" + "11".repeat(32)));
    }

    /** 1(a): an ETH-first row finalized on two endpoints' evidence is finished; the evidence counts for no other row. */
    @Test public void neverLockedEvidenceFinishesOnlyAnEthFirstRow() {
        String h = "0x" + "aa".repeat(32);
        SwapDb.Swap s = swap("USDT", SwapDb.ST_STARTED, h);
        s.role = "INITIATOR"; s.direction = "ERC20_TO_MINIMA";
        SwapDb db = db(s);
        String why = Handover.inFlight(db, Collections.emptyList(), false).get(0);
        assertTrue(why, why.contains("two independent Ethereum endpoints"));
        s.status = SwapDb.ST_ERROR;
        when(db.hasEvent(h, SwapDb.EV_NEVER_LOCKED)).thenReturn(true);
        assertTrue(Handover.inFlight(db, Collections.emptyList(), false).isEmpty());
        s.myLegIsMinima = true;
        assertEquals(1, Handover.inFlight(db, Collections.emptyList(), false).size());
    }

    /** 2: while handed over a currency switch must not revive the other market's parked flags, and Take back leaves
     *  the market exactly as the handover's withdraw left it (no auto-republish). */
    @Test public void handedOverMarketStaysWithdrawnThroughSwitchAndTakeBack() {
        Map<String, Object> m = new HashMap<>();
        m.put("auto_publish", true);
        m.put("market_minima", "{\"auto_publish\":true,\"otc_enable\":true,\"otc_auto\":true}");
        SharedPreferences p = prefs(m);
        Handover.withdraw(p);
        MainActivity.restoreMarket(p, "minima");
        assertEquals(false, m.get("auto_publish"));
        assertEquals(false, m.get("otc_enable"));
        assertEquals(true, m.get(PriceOracle.P_WITHDRAWN));
        Handover.takeBack(p);
        assertEquals(false, m.get("auto_publish"));
        assertEquals(true, m.get(PriceOracle.P_WITHDRAWN));
        MainActivity.restoreMarket(p, "minima");   // a deliberate switch after Take back restores as before
        assertEquals(true, m.get("auto_publish"));
    }
}
