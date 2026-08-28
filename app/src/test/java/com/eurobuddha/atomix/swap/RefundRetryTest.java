package com.eurobuddha.atomix.swap;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Handler;

import com.eurobuddha.comms.NodeApi;
import com.eurobuddha.atomix.eth.EthNet;
import com.eurobuddha.atomix.eth.EthRpc;
import com.eurobuddha.atomix.eth.EthWallet;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * STEP: the two faults that stranded a real 35.014005 MINIMA lock, refundable-but-never-refunded.
 *
 * The coin was locked at block 2241159, became refundable at 2241192, and was still unspent 947 blocks later
 * with the app polling every 30 s the whole time.
 *
 * 1. DISCOVERY. Refund eligibility was decided from a chain scan bounded to HTLC_SCAN_DEPTH (256) blocks. The
 *    node's `coins depth:` is a walk-back over blocks from the tip, so the coin left the scan ~3 h 33 m after
 *    creation — while the responder leg opens for refund just 36 blocks in. Anything that consumed that window
 *    stranded the coin permanently: the engine never saw it again, and there is no manual refund in the UI.
 *
 * 2. RETRY. The refund's in-flight guard was `inflight.add("refundM:"+hash)`, cleared ONLY inside the post
 *    callback. A lost callback (NodeApi drops pending callbacks once the hosting Activity is finishing) left it
 *    set forever — and it is static, so it wedged the foreground and background engines together. The claim path
 *    had already been moved off this exact pattern onto a timestamp window; the refund path had not.
 */
public class RefundRetryTest {

    private static final String HASH  = "0x" + "44".repeat(32);
    private static final String MY_PK = "0xBBBB";

    private SwapEngine engine;
    private SwapDb db;
    private MinimaHtlc minima;

    @Before public void setUp() {
        SwapEngine.clearRetryMarkersForTest();          // static — must not leak between tests
        EthWallet wallet = mock(EthWallet.class);
        when(wallet.address()).thenReturn("0x2222222222222222222222222222222222222222");
        minima = mock(MinimaHtlc.class);
        db = mock(SwapDb.class);
        engine = new SwapEngine(mock(NodeApi.class), minima, db,
                wallet, mock(Handler.class), mock(SwapEngine.Notifier.class));
        engine.setNetwork(new EthRpc(EthNet.MAINNET.defaultRpc), EthNet.MAINNET);
        engine.setMyMinimaPk(MY_PK);
        engine.setMyPubkeys(Collections.singleton(MY_PK.toLowerCase()));
    }

    /** A coin I locked (owner = my key), past its timelock. */
    private static JSONObject myExpiredCoin() throws Exception {
        return new JSONObject()
                .put("coinid", "0x" + "77".repeat(32))
                .put("tokenid", "0x00")
                .put("amount", "35.014005")
                .put("state", new JSONObject()
                        .put("0", MY_PK)          // owner — me
                        .put("3", "2241192")      // timelock
                        .put("4", "0xCOUNTERPARTY")
                        .put("5", HASH));
    }

    // ================= 2. the refund must survive a callback that never arrives =================

    @Test public void refundRetriesAfterALostCallback() throws Exception {
        JSONObject coin = myExpiredCoin();
        int block = 2242106;                                  // 914 blocks past the timelock

        // The node accepts the command and then never calls back — neither ok() nor err().
        engine.checkExpiredMinima(coin, block);
        verify(minima, times(1)).refund(any(JSONObject.class), any(MinimaHtlc.PostCb.class));

        // Same cycle / same window: must NOT re-sign. Every retry burns a one-time key leaf permanently.
        engine.checkExpiredMinima(coin, block);
        verify(minima, times(1)).refund(any(JSONObject.class), any(MinimaHtlc.PostCb.class));

        // Past the window it MUST fire again. Under the old `inflight` guard this never happened — the marker
        // was still held by the operation whose callback was lost, for the life of the process.
        SwapEngine.ageRetryMarkerForTest("refundM:" + HASH, 10_000);
        engine.checkExpiredMinima(coin, block);
        verify(minima, times(2)).refund(any(JSONObject.class), any(MinimaHtlc.PostCb.class));
    }

    @Test public void refundIsNotAttemptedBeforeTheTimelock() throws Exception {
        engine.checkExpiredMinima(myExpiredCoin(), 2241000);   // still short of 2241192
        verify(minima, never()).refund(any(JSONObject.class), any(MinimaHtlc.PostCb.class));
    }

    @Test public void alreadyRefundedIsNeverRefundedAgain() throws Exception {
        when(db.haveCollectExpired(HASH)).thenReturn(true);
        engine.checkExpiredMinima(myExpiredCoin(), 2242106);
        verify(minima, never()).refund(any(JSONObject.class), any(MinimaHtlc.PostCb.class));
    }

    @Test public void refundIsRefusedWhenTheTimelockIsNotParseable() throws Exception {
        // MI-1: a coin whose timelock state field isn't an integer must NEVER be treated as already-expired.
        // The old parseInt returned 0, so `block <= 0` was false and a doomed refund fired every retry — burning
        // a one-time key leaf each time. parseBlock returns -1 and the guard fails closed.
        JSONObject coin = new JSONObject()
                .put("coinid", "0x" + "77".repeat(32)).put("tokenid", "0x00").put("amount", "35.014005")
                .put("state", new JSONObject().put("0", MY_PK).put("3", "not-a-number").put("4", "0xCC").put("5", HASH));
        engine.checkExpiredMinima(coin, 2242106);
        verify(minima, never()).refund(any(JSONObject.class), any(MinimaHtlc.PostCb.class));
    }

    // ================= 1. discovery must not depend on the shallow scan window =================

    @Test public void expiredLockIsSweptFromTheDbNotTheShallowScan() {
        SwapDb.Swap s = new SwapDb.Swap();
        s.hash = HASH; s.status = SwapDb.ST_LOCKED; s.myLegIsMinima = true; s.myTimelock = 2241192;
        when(db.allSwaps()).thenReturn(Collections.singletonList(s));

        // 947 blocks old — far outside HTLC_SCAN_DEPTH, so the shallow scan cannot see it. The DB can.
        engine.sweepExpiredMinima(2242106);

        verify(minima, times(1)).scanHtlcByHashDeep(eq(HASH), anyInt(),
                eq(SwapEngine.REFUND_SCAN_DEPTH), any(), any());
    }

    @Test public void sweepIgnoresSwapsThatAreNotYetRefundable() {
        SwapDb.Swap s = new SwapDb.Swap();
        s.hash = HASH; s.status = SwapDb.ST_LOCKED; s.myLegIsMinima = true; s.myTimelock = 2241192;
        when(db.allSwaps()).thenReturn(Collections.singletonList(s));

        engine.sweepExpiredMinima(2241000);

        verify(minima, never()).scanHtlcByHashDeep(any(), anyInt(), anyInt(), any(), any());
    }

    @Test public void sweepIgnoresTerminalSwaps() {
        SwapDb.Swap s = new SwapDb.Swap();
        s.hash = HASH; s.status = SwapDb.ST_REFUNDED; s.myLegIsMinima = true; s.myTimelock = 2241192;
        when(db.allSwaps()).thenReturn(Collections.singletonList(s));

        engine.sweepExpiredMinima(2242106);

        verify(minima, never()).scanHtlcByHashDeep(any(), anyInt(), anyInt(), any(), any());
    }

    /** The window a refund is discoverable in must outlast the window it becomes refundable in — with room for
     *  a phone that was simply off. 256 did not, which is the whole bug. */
    @Test public void refundScanOutlastsTheTimelockByAWideMargin() {
        assertTrue("refund scan depth (" + SwapEngine.REFUND_SCAN_DEPTH + ") must exceed the first-leg timelock ("
                        + SwapEngine.TIMELOCK_BLOCKS + ") several times over",
                SwapEngine.REFUND_SCAN_DEPTH >= SwapEngine.TIMELOCK_BLOCKS * 4);
        // ~1024 blocks ≈ 14 h at 50 s/block. That is the TxPoW tree ceiling
        // (MINIMA_CASCADE_START_DEPTH); past it `tip.getParent()` is null and only MegaMMR can help, which is
        // why the sweep asks for it via scanHtlcByHashDeep.
        assertTrue("a deeper scan than the tree holds would be a false promise",
                SwapEngine.REFUND_SCAN_DEPTH <= 1024);
    }

    // ================= the refund reason must name the actual failure (0.1.44: role-aware) =================
    // Proven live 2026-08-27: a first-time buyer locked 438 USDT, never claimed the matching 433.663366 mxUSDT
    // counter-leg, and the bare "Timelock passed" notification left the operator reconstructing WHY on-chain.

    private SwapDb.Swap roleSwap(String role) {
        SwapDb.Swap s = new SwapDb.Swap();
        s.hash = HASH; s.role = role;
        return s;
    }

    @Test public void refundReasonResponderSaysCounterpartyNeverClaimed() {
        when(db.getEvents(HASH)).thenReturn(Collections.emptyList());
        when(db.getSwap(HASH)).thenReturn(roleSwap("RESPONDER"));
        assertTrue(engine.refundReason(HASH).contains("never claimed yours"));
    }

    @Test public void refundReasonInitiatorSaysCounterpartyNeverLocked() {
        when(db.getEvents(HASH)).thenReturn(Collections.emptyList());
        when(db.getSwap(HASH)).thenReturn(roleSwap("INITIATOR"));
        assertTrue(engine.refundReason(HASH).contains("never locked their side"));
    }

    @Test public void refundReasonPrefersStoredMismatchNote() {
        SwapDb.Event e = new SwapDb.Event();
        e.event = SwapDb.EV_MISMATCH; e.note = "counter-leg amount 1 short of 2";
        when(db.getEvents(HASH)).thenReturn(Collections.singletonList(e));
        when(db.getSwap(HASH)).thenReturn(roleSwap("RESPONDER"));
        assertTrue(engine.refundReason(HASH).contains("amount 1 short of 2"));
    }
}
