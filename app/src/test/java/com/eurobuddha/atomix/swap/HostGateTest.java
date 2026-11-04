package com.eurobuddha.atomix.swap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.SharedPreferences;
import android.os.Handler;

import com.eurobuddha.atomix.Handover;
import com.eurobuddha.atomix.TradingContext;
import com.eurobuddha.atomix.eth.EthNet;
import com.eurobuddha.atomix.eth.EthRpc;
import com.eurobuddha.atomix.eth.EthWallet;
import com.eurobuddha.comms.NodeApi;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Consumer;

/** 0.1.91: the embedding host's trading gate (Dexicon) stops every new liability through halted(), and the Minima
 *  refund sweep leaves the durable evidence Handover.stuck reads. */
public class HostGateTest {
    static final String HASH = "0x" + "4c".repeat(32);
    static final String MY_PK = "0x" + "bb".repeat(32);
    static final String COIN = "0x" + "77".repeat(32);

    MinimaHtlc minima; SwapDb db; SwapEngine engine; TradingContext saved;

    @Before public void setup() {
        saved = TradingContext.active();
        TradingContext.setActive(TradingContext.MXUSDT, null);
        Handover.load(mock(SharedPreferences.class));
        com.eurobuddha.atomix.IdentityWatch.resetForTest();
        SwapEngine.resetIdentityAlarmForTest();
        SwapEngine.clearRetryMarkersForTest();
        minima = mock(MinimaHtlc.class);
        when(minima.ready()).thenReturn(true);
        when(minima.activeToken()).thenReturn(TradingContext.MXUSDT.tokenId);
        EthWallet wallet = mock(EthWallet.class);
        when(wallet.ready()).thenReturn(true);
        db = mock(SwapDb.class);
        engine = new SwapEngine(mock(NodeApi.class), minima, db, wallet, mock(Handler.class), mock(SwapEngine.Notifier.class));
        engine.setMyMinimaPk(MY_PK);
        engine.setMyPubkeys(Collections.singleton(MY_PK));
        engine.setNetwork(mock(EthRpc.class), EthNet.MAINNET);
    }

    @After public void cleanup() {
        SwapEngine.setHostGate(null);
        com.eurobuddha.atomix.IdentityWatch.resetForTest();
        SwapEngine.ageStartMarkersForTest(SwapEngine.START_MARKER_TTL_MS + 1);   // release any "sell" start marker
        SwapEngine.initiatorStarting();
        engine.shutdown();
        SwapEngine.clearRetryMarkersForTest();
        TradingContext.setActive(saved, null);
    }

    private String start() {
        Order maker = new Order();
        maker.market = TradingContext.MXUSDT; maker.minimaPublicKey = "0x5678"; maker.ethAddress = "0x" + "12".repeat(20);
        final String[] err = {null};
        engine.startMinimaToErc20(maker, "2", "USDT", "6", false, new SwapEngine.StartCb() {
            public void ok(String h) {}
            public void err(String m) { err[0] = m; }
        });
        return err[0];
    }

    @Test public void hostGateSetHaltsAndClearedReleases() {
        assertFalse(SwapEngine.halted());
        SwapEngine.setHostGate(() -> true);
        assertTrue(SwapEngine.halted());
        SwapEngine.setHostGate(() -> { throw new IllegalStateException("host gone"); });
        assertTrue("a gate that cannot answer is closed", SwapEngine.halted());
        SwapEngine.setHostGate(() -> false);
        assertFalse(SwapEngine.halted());
        SwapEngine.setHostGate(null);
        assertFalse(SwapEngine.halted());
    }

    @Test public void initiatorStartAndOtcExecuteRefuseWhileTheHostGateIsClosed() {
        SwapEngine.setHostGate(() -> true);
        String err = start();
        assertNotNull(err);
        assertTrue(err, err.contains("paused"));
        verify(minima, never()).generateSecret(any());
        OtcDb.Deal d = new OtcDb.Deal();
        d.ref = "ab".repeat(16); d.currency = "mxusdt"; d.amount = "2"; d.price = "1"; d.side = OtcOffer.LP_BUYS_MINIMA;
        d.peerMinimaPk = "0x5678"; d.peerEthAddr = "0x" + "12".repeat(20);
        final String[] otcErr = {null};
        engine.executeOtcDeal(d, new SwapEngine.StartCb() {
            public void ok(String h) {}
            public void err(String m) { otcErr[0] = m; }
        });
        assertNotNull(otcErr[0]);
        verify(minima, never()).generateSecret(any());
        verify(db, never()).prepareInitiator(any(), any(), any(), any());

        SwapEngine.setHostGate(null);
        assertNull(start());
        verify(minima).generateSecret(any());
    }

    /** The gate closes between the entry check and the write: the secret is not written and nothing is locked. */
    @Test public void hostGateClosingMidStartWritesNoSecret() {
        final boolean[] closed = {false};
        SwapEngine.setHostGate(() -> closed[0]);
        doAnswer(i -> { ((MinimaHtlc.SecretCb) i.getArgument(0)).ok("public fixture", "hash fixture"); return null; })
                .when(minima).generateSecret(any());
        doAnswer(i -> { closed[0] = true; ((MinimaHtlc.BlockCb) i.getArgument(0)).ok(100); return null; })
                .when(minima).currentBlock(any());
        String err = start();
        assertTrue(err, err != null && err.contains("paused"));
        verify(db, never()).prepareInitiator(any(), any(), any(), any());
        verify(minima, never()).lock(any(), any(), any(), any(), any(), any(), anyInt(), any(), any());
    }

    // ---- refund evidence for Handover.stuck ----

    private SwapDb.Swap myMinimaLock(long timelock) throws Exception { return lockOf(timelock, MY_PK); }

    private SwapDb.Swap lockOf(long timelock, String owner) throws Exception {
        SwapDb.Swap s = new SwapDb.Swap();
        s.hash = HASH; s.status = SwapDb.ST_ERROR; s.myLegIsMinima = true; s.myTimelock = timelock;
        when(db.allSwaps()).thenReturn(Collections.singletonList(s));
        when(db.rememberedLockCoin(HASH)).thenReturn(new JSONObject().put("coinid", COIN).put("tokenid", "0x00")
                .put("amount", "1").put("tokenamount", "1")
                .put("state", new JSONArray().put(new JSONObject().put("port", 0).put("data", owner))
                        .put(new JSONObject().put("port", 3).put("data", String.valueOf(timelock)))));
        doAnswer(i -> { ((Consumer<JSONArray>) i.getArgument(3)).accept(new JSONArray()); return null; })
                .when(minima).scanHtlcByHashDeep(eq(HASH), anyInt(), anyInt(), any(), any());
        return s;
    }

    @SuppressWarnings("unchecked")
    private void coinRead(Boolean unspent) {
        doAnswer(i -> {
            if (unspent == null) ((Consumer<String>) i.getArgument(2)).accept("node busy");
            else ((Consumer<Boolean>) i.getArgument(1)).accept(unspent);
            return null;
        }).when(minima).lockCoinUnspent(eq(COIN), any(), any());
    }

    /** The db mock keeps its events, newest first, like SwapDb.getEvents. */
    private final java.util.List<SwapDb.Event> events = new java.util.ArrayList<>();
    private int tip = 0;

    private void liveEvents() {
        doAnswer(i -> {
            SwapDb.Event e = new SwapDb.Event();
            e.event = i.getArgument(1); e.token = i.getArgument(2); e.amount = i.getArgument(3); e.note = i.getArgument(4);
            events.add(0, e);
            return null;
        }).when(db).logEvent(eq(HASH), any(), any(), any(), any());
        when(db.getEvents(HASH)).thenAnswer(i -> new java.util.ArrayList<>(events));
        when(db.hasEvent(eq(HASH), any())).thenAnswer(i -> events.stream().anyMatch(e -> e.event.equals(i.getArgument(1))));
        doAnswer(i -> {
            MinimaHtlc.BlockCb cb = i.getArgument(0);
            if (tip > 0) cb.ok(tip); else cb.err("no tip");
            return null;
        }).when(minima).currentBlock(any());
    }

    private void pass(int block) {
        tip = block;
        SwapEngine.clearRetryMarkersForTest();
        engine.sweepExpiredMinima(block);
    }

    private long count(String ev) { return events.stream().filter(e -> e.event.equals(ev)).count(); }

    @Test public void aSpentCoinIsRecordedOnlyOnTwoReadsSixBlocksApart() throws Exception {
        myMinimaLock(1000);
        liveEvents();
        coinRead(false);
        int b = 1000 + SwapEngine.REFUND_EVIDENCE_MARGIN_BLOCKS;
        pass(b);
        verify(db).logEvent(HASH, SwapDb.EV_REFUND_TRIED, "minima", "", "minima block " + b);
        assertEquals("one read is only pending", 0, count(SwapDb.EV_LOCK_SPENT));
        assertEquals(1, count(SwapDb.EV_LOCK_GONE_SEEN));
        pass(b + SwapEngine.LOCK_GONE_CONFIRM_BLOCKS - 1);
        assertEquals("five blocks on is not enough", 0, count(SwapDb.EV_LOCK_SPENT));
        pass(b + SwapEngine.LOCK_GONE_CONFIRM_BLOCKS);
        assertEquals(1, count(SwapDb.EV_LOCK_SPENT));
        assertEquals("REFUND_TRIED is written once", 1, count(SwapDb.EV_REFUND_TRIED));
    }

    @Test public void anUnspentReadBetweenVoidsThePendingReading() throws Exception {
        myMinimaLock(1000);
        liveEvents();
        coinRead(false);
        pass(1100);
        coinRead(true);
        pass(1103);
        assertEquals(1, count(SwapDb.EV_LOCK_PRESENT));
        coinRead(false);
        pass(1110);
        assertEquals("the pending reading restarted after the unspent one", 0, count(SwapDb.EV_LOCK_SPENT));
        pass(1116);
        assertEquals(1, count(SwapDb.EV_LOCK_SPENT));
    }

    @Test public void anEmptyReadWithoutALiveTipCountsForNothing() throws Exception {
        myMinimaLock(1000);
        liveEvents();
        coinRead(false);
        tip = 0;
        SwapEngine.clearRetryMarkersForTest();
        engine.sweepExpiredMinima(1100);
        SwapEngine.clearRetryMarkersForTest();
        engine.sweepExpiredMinima(1200);
        assertEquals(0, count(SwapDb.EV_LOCK_GONE_SEEN));
        assertEquals(0, count(SwapDb.EV_LOCK_SPENT));
    }

    @Test public void anUnspentOrUnreadableCoinRecordsNoSpend() throws Exception {
        myMinimaLock(1000);
        liveEvents();
        coinRead(true);
        pass(2000);
        coinRead(null);
        pass(2010);
        pass(2020);
        assertEquals(0, count(SwapDb.EV_LOCK_SPENT));
        assertEquals(0, count(SwapDb.EV_LOCK_GONE_SEEN));
        assertEquals(1, count(SwapDb.EV_REFUND_TRIED));
    }

    @Test public void tooEarlyAPassRecordsNothing() throws Exception {
        myMinimaLock(1000);
        liveEvents();
        coinRead(false);
        pass(1000 + SwapEngine.REFUND_EVIDENCE_MARGIN_BLOCKS - 1);
        verify(db, never()).logEvent(eq(HASH), eq(SwapDb.EV_REFUND_TRIED), any(), any(), any());
        verify(minima, never()).lockCoinUnspent(any(), any(), any());
    }

    @Test public void aCoinOfAKeyTheNodeDoesNotHoldIsNeverReadAsSpent() throws Exception {
        lockOf(1000, "0x" + "cc".repeat(32));
        liveEvents();
        coinRead(false);
        pass(2000);
        verify(minima, never()).lockCoinUnspent(any(), any(), any());
    }

    @Test public void noLockReadWhileHaltedOrWithNoNodeKeys() throws Exception {
        myMinimaLock(1000);
        liveEvents();
        coinRead(false);
        com.eurobuddha.atomix.IdentityWatch.noteMinimaOrphaned(MY_PK, null);
        pass(2000);
        com.eurobuddha.atomix.IdentityWatch.resetForTest();
        engine.setMyPubkeys(Collections.emptySet());
        pass(2010);
        verify(minima, never()).lockCoinUnspent(any(), any(), any());
        verify(minima, never()).findMyLockByHash(any(), any(), any());
    }

    @Test public void aResponderRecordsItsRefundPassOnlyAFullTimelockOn() throws Exception {
        myMinimaLock(1000).role = "RESPONDER";
        liveEvents();
        coinRead(true);
        pass(1000 + SwapEngine.REFUND_EVIDENCE_MARGIN_BLOCKS);
        assertEquals(0, count(SwapDb.EV_REFUND_TRIED));
        pass(1000 + SwapEngine.TIMELOCK_BLOCKS);
        verify(db).logEvent(HASH, SwapDb.EV_REFUND_TRIED, "minima", "", "minima block " + (1000 + SwapEngine.TIMELOCK_BLOCKS));
    }

    // ---- no recorded coin: one depth-free read, never "no record = gone" ----

    private SwapDb.Swap unrecordedLock(long timelock) {
        SwapDb.Swap s = new SwapDb.Swap();
        s.hash = HASH; s.status = SwapDb.ST_STARTED; s.myLegIsMinima = true; s.myTimelock = timelock; s.role = "INITIATOR";
        when(db.allSwaps()).thenReturn(Collections.singletonList(s));
        doAnswer(i -> { ((Consumer<JSONArray>) i.getArgument(3)).accept(new JSONArray()); return null; })
                .when(minima).scanHtlcByHashDeep(eq(HASH), anyInt(), anyInt(), any(), any());
        return s;
    }

    @SuppressWarnings("unchecked")
    private void relevantRead(JSONArray coins) {
        doAnswer(i -> {
            if (coins == null) ((Consumer<String>) i.getArgument(2)).accept("node busy");
            else ((Consumer<JSONArray>) i.getArgument(1)).accept(coins);
            return null;
        }).when(minima).findMyLockByHash(eq(HASH), any(), any());
    }

    @Test public void noRecordedCoinIsAbsentOnlyOnTwoEmptyDepthFreeReads() {
        unrecordedLock(1000);
        liveEvents();
        relevantRead(null);
        pass(1100);
        assertEquals("a failed read is not absence", 0, count(SwapDb.EV_LOCK_GONE_SEEN));
        relevantRead(new JSONArray());
        pass(1110);
        assertEquals(0, count(SwapDb.EV_LOCK_ABSENT));
        pass(1116);
        assertEquals(1, count(SwapDb.EV_LOCK_ABSENT));
        assertTrue(events.get(0).note, events.get(0).note.contains(MinimaHtlc.HTLC_ADDRESS));
        verify(minima, never()).lockCoinUnspent(any(), any(), any());
    }

    @Test public void aDepthFreeReadThatFindsMyLockRecordsAndRefundsIt() throws Exception {
        unrecordedLock(1000);
        liveEvents();
        JSONObject coin = new JSONObject().put("coinid", COIN).put("tokenid", "0x00").put("amount", "1").put("tokenamount", "1")
                .put("state", new JSONObject().put("0", MY_PK).put("3", "1000").put("4", "0x" + "dd".repeat(32)).put("5", HASH));
        relevantRead(new JSONArray().put(coin));
        pass(1100);
        verify(db).rememberLockCoin(eq(HASH), eq(COIN), any(), eq("0x00"), eq(MY_PK), any());
        verify(minima).refund(any(), any());
        assertEquals(0, count(SwapDb.EV_LOCK_GONE_SEEN));
        assertEquals(0, count(SwapDb.EV_LOCK_ABSENT));
    }
}
